package com.pocketcode.app.data

import com.pocketcode.app.terminal.RealTerminalBackend
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class AgentEngine(
    private val settings: SettingsRepository,
    private val files: WorkspaceFileService,
    private val terminal: RealTerminalBackend? = null,
) {
    sealed class Event {
        data class Text(val value: String) : Event()
        data class Log(val value: String) : Event()
        data class Approval(val action: String, val approve: (Boolean) -> Unit) : Event()
        data class Error(val value: String) : Event()
    }

    private val client = OkHttpClient.Builder().readTimeout(120, TimeUnit.SECONDS).build()
    private val toolDefinitions = JSONArray().apply {
        put(tool("list_files", "List workspace files", obj("path" to stringSchema())))
        put(tool("read_file", "Read workspace file", obj("path" to stringSchema())))
        put(tool("write_file", "Stage workspace file write for approval", obj("path" to stringSchema(), "content" to stringSchema(), "diffSummary" to stringSchema())))
        put(tool("run_terminal_command", "Run shell command in workspace", obj("command" to stringSchema())))
    }

    suspend fun prompt(input: String, emit: (Event) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", systemPrompt()))
                .put(JSONObject().put("role", "user").put("content", input))
            repeat(8) {
                val result = stream(messages, emit)
                if (result.toolCalls.isEmpty()) return@withContext
                messages.put(result.assistant)
                result.toolCalls.forEach { call ->
                    val args = runCatching { JSONObject(call.arguments) }.getOrDefault(JSONObject())
                    val output = executeTool(call.name, args, emit)
                    messages.put(JSONObject().put("role", "tool").put("tool_call_id", call.id).put("content", output))
                }
            }
            emit(Event.Error("Tool loop limit reached"))
        } catch (e: Exception) {
            emit(Event.Error(e.message ?: "Agent request failed"))
        }
    }

    private fun systemPrompt(): String = buildString {
        append("You are a coding agent. Workspace: ").append(settings.activeWorkspacePath).append(". ")
        append("Use tools for workspace inspection and changes. Never invent file contents. Top-level files: ")
        append(files.listFiles().joinToString())
    }

    private data class ToolCall(val id: String, val name: String, val arguments: String)
    private data class StreamResult(val assistant: JSONObject, val toolCalls: List<ToolCall>)

    private fun stream(messages: JSONArray, emit: (Event) -> Unit): StreamResult {
        val request = Request.Builder()
            .url(settings.baseUrl.trimEnd('/') + "/chat/completions")
            .addHeader("Authorization", "Bearer ${settings.apiKey}")
            .post(JSONObject().put("model", settings.activeModel).put("stream", true).put("messages", messages).put("tools", toolDefinitions).toString().toRequestBody("application/json".toMediaType()))
            .build()
        val content = StringBuilder()
        val calls = LinkedHashMap<Int, ToolCallBuilder>()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            val source = response.body?.source() ?: error("Empty response")
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (!line.startsWith("data: ") || line == "data: [DONE]") continue
                val choice = runCatching { JSONObject(line.removePrefix("data: ")).optJSONArray("choices")?.optJSONObject(0) }.getOrNull() ?: continue
                val delta = choice.optJSONObject("delta") ?: continue
                delta.optString("content").takeIf { it.isNotEmpty() }?.let { content.append(it); emit(Event.Text(it)) }
                delta.optJSONArray("tool_calls")?.let { parts ->
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i) ?: continue
                        val index = part.optInt("index", i)
                        val builder = calls.getOrPut(index) { ToolCallBuilder() }
                        builder.id = part.optString("id", builder.id)
                        val fn = part.optJSONObject("function") ?: continue
                        builder.name = fn.optString("name", builder.name)
                        builder.arguments.append(fn.optString("arguments"))
                    }
                }
            }
        }
        val toolCalls = calls.values.mapIndexed { index, call -> ToolCall(call.id.ifEmpty { "call_$index" }, call.name, call.arguments.toString()) }
        val assistant = JSONObject().put("role", "assistant").put("content", content.toString())
        if (toolCalls.isNotEmpty()) assistant.put("tool_calls", JSONArray().apply { toolCalls.forEach { put(JSONObject().put("id", it.id).put("type", "function").put("function", JSONObject().put("name", it.name).put("arguments", it.arguments))) } })
        return StreamResult(assistant, toolCalls)
    }

    private class ToolCallBuilder {
        var id = ""
        var name = ""
        val arguments = StringBuilder()
    }

    private suspend fun executeTool(name: String, args: JSONObject, emit: (Event) -> Unit): String {
        return when (name) {
            "list_files" -> if (settings.autoApproveRead) files.listFiles(args.optString("path")).joinToString("\n") else if (approval(emit, "List files: ${args.optString("path")}")) files.listFiles(args.optString("path")).joinToString("\n") else "List rejected"
            "read_file" -> if (settings.autoApproveRead) files.readFile(args.getString("path")).also { emit(Event.Log("Read ${args.getString("path")}")) } else if (approval(emit, "Read file: ${args.getString("path")}")) files.readFile(args.getString("path")).also { emit(Event.Log("Read ${args.getString("path")}")) } else "Read rejected"
            "write_file" -> {
                val path = args.getString("path")
                val content = args.optString("content")
                val approved = approval(emit, "Apply diff to $path: ${args.optString("diffSummary")}")
                if (approved) runCatching { files.writeFile(path, content); emit(Event.Log("Applied $path")); "Applied $path" }.getOrElse { emit(Event.Log("Write failed: ${it.message}")); "Write failed: ${it.message}" } else "Discarded $path"
            }
            "run_terminal_command" -> {
                val command = args.getString("command")
                val approved = !settings.requireTerminalApproval || approval(emit, "Run in terminal: $command")
                if (!approved) "Terminal command rejected" else terminal?.let { backend -> buildString { backend.execute(command) { appendLine(it); emit(Event.Log(it)) } } } ?: "Terminal backend unavailable"
            }
            else -> "Unknown tool: $name"
        }
    }

    private suspend fun approval(emit: (Event) -> Unit, action: String): Boolean {
        val result = CompletableDeferred<Boolean>()
        emit(Event.Approval(action) { accepted -> result.complete(accepted) })
        return result.await()
    }

    private fun tool(name: String, description: String, parameters: JSONObject) = JSONObject().put("type", "function").put("function", JSONObject().put("name", name).put("description", description).put("parameters", parameters))
    private fun obj(vararg fields: Pair<String, JSONObject>) = JSONObject().put("type", "object").apply { val properties = JSONObject(); fields.forEach { properties.put(it.first, it.second) }; put("properties", properties) }
    private fun stringSchema() = JSONObject().put("type", "string")
}
