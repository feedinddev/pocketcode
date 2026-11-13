package com.pocketcode.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

import com.pocketcode.app.data.SettingsRepository
import com.pocketcode.app.data.WorkspaceFileService

private val TextExtensions = setOf("kt", "kts", "java", "js", "ts", "tsx", "py", "json", "css", "md", "txt")
private val KeywordRegex = Regex("\\b(fun|val|var|class|interface|object|if|else|when|for|while|return|import|package|public|private|const|let|function|def|true|false|null)\\b")
private val StringRegex = Regex("\\\"(?:\\\\.|[^\\\"\\n])*\\\"|'(?:\\\\.|[^'\\n])*'")
private val CommentRegex = Regex("//[^\\n]*|#[^\\n]*")
private val FunctionRegex = Regex("\\b[A-Za-z_][A-Za-z0-9_]*(?=\\s*\\()")
private val BraceRegex = Regex("[{}\\[\\]()]" )

private fun highlight(source: String): AnnotatedString {
    val builder = AnnotatedString.Builder(source)
    fun style(regex: Regex, color: Color) = regex.findAll(source).forEach { builder.addStyle(SpanStyle(color = color), it.range.first, it.range.last + 1) }
    style(CommentRegex, Color(0xFF6A9955))
    style(StringRegex, Color(0xFFCE9178))
    style(KeywordRegex, Color(0xFF569CD6))
    style(FunctionRegex, Color(0xFFDCDCAA))
    style(BraceRegex, Color(0xFFFFD700))
    return builder.toAnnotatedString()
}

private fun normalizePath(root: File, input: String?): String? = input?.trim()?.takeIf { it.isNotEmpty() }?.let {
    runCatching {
        val candidate = File(it)
        val absolute = if (candidate.isAbsolute) candidate else File(root, it)
        val canonical = absolute.canonicalFile
        val base = root.canonicalFile
        require(canonical.path == base.path || canonical.path.startsWith(base.path + File.separator))
        canonical.relativeTo(base).path
    }.getOrNull()
}

@Composable
fun EditorScreen(filePath: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { SettingsRepository(context) }
    val root = remember(settings.activeWorkspacePath) { File(settings.activeWorkspacePath).canonicalFile }
    val service = remember(root) { WorkspaceFileService(root) }
    val buffers = remember { mutableStateMapOf<String, String>() }
    val tabs = remember { mutableStateListOf<String>() }
    var selected by remember { mutableStateOf<String?>(null) }

    fun open(path: String) {
        if (path !in tabs) tabs += path
        selected = path
        if (path !in buffers) buffers[path] = runCatching { service.read(path) }.getOrDefault("")
    }

    LaunchedEffect(root, filePath) {
        val requested = normalizePath(root, filePath)
        val fallback = root.walkTopDown().firstOrNull { it.isFile && it.length() < 2_000_000 && it.extension.lowercase() in TextExtensions }
            ?.relativeTo(root)?.path
        (requested ?: fallback)?.let(::open)
    }

    val path = selected
    val content = path?.let { buffers[it].orEmpty() }.orEmpty()
    Column(Modifier.fillMaxSize().background(Color(0xFF101114)).padding(10.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            tabs.forEach { tab ->
                val active = tab == path
                Row(
                    Modifier.background(if (active) Color(0xFF252526) else Color(0xFF18191C))
                        .border(1.dp, if (active) Color(0xFF569CD6) else Color.Transparent)
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                        .clickable { open(tab) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(File(tab).name, color = if (active) Color.White else Color(0xFF9DA1A6), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    TextButton({ tabs.remove(tab); buffers.remove(tab); if (tab == selected) selected = tabs.lastOrNull() }, contentPadding = PaddingValues(2.dp)) {
                        Text("×", color = Color(0xFFB8B8B8), fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(2.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(path?.replace(File.separatorChar, '/') ?: "No file", color = Color(0xFF9DA1A6), fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Button({ path?.let { runCatching { service.write(it, buffers[it].orEmpty()) } } }, enabled = path != null) { Text("Save") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxSize()) {
            val lineCount = maxOf(1, content.count { it == '\n' } + 1)
            Column(Modifier.width(48.dp), horizontalAlignment = Alignment.End) {
                repeat(lineCount) { Text("${it + 1}", color = Color(0xFF60636A), fontFamily = FontFamily.Monospace, fontSize = 13.sp, modifier = Modifier.padding(end = 10.dp)) }
            }
            BasicTextField(
                value = content,
                onValueChange = { value -> path?.let { buffers[it] = value } },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(color = Color(0xFFE5E5EA), fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                visualTransformation = VisualTransformation { value -> TransformedText(highlight(value.text), OffsetMapping.Identity) }
            )
        }
    }
}
