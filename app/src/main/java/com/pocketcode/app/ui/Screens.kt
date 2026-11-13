package com.pocketcode.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import java.io.File
import com.pocketcode.app.data.SettingsRepository
import com.pocketcode.app.data.WorkspaceFileService
import com.pocketcode.app.terminal.RealTerminalBackend
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val Mono = FontFamily.Monospace
private val PureBlack = Color(0xFF000000)
private val GroupedSurface = Color(0xFF1C1C1E)
private val ElevatedSurface = Color(0xFF2C2C2E)
private val Hairline = Color(0xFF38383A)
private val AppleBlue = Color(0xFF0A84FF)
private val AppleGreen = Color(0xFF30D158)
private val AppleRed = Color(0xFFFF453A)

private fun submitAgent(
    prompt: String,
    engine: com.pocketcode.app.data.AgentEngine,
    scope: CoroutineScope,
    clear: () -> Unit,
    setStatus: (String) -> Unit,
    addOutput: (String) -> Unit,
    addLog: (String) -> Unit,
    setApproval: (com.pocketcode.app.data.AgentEngine.Event.Approval) -> Unit,
) {
    val request = prompt.trim()
    if (request.isEmpty()) return
    clear()
    setStatus("Streaming")
    scope.launch {
        engine.prompt(request) { event -> scope.launch { when (event) {
            is com.pocketcode.app.data.AgentEngine.Event.Text -> addOutput(event.value)
            is com.pocketcode.app.data.AgentEngine.Event.Log -> addLog(event.value)
            is com.pocketcode.app.data.AgentEngine.Event.Approval -> setApproval(event)
            is com.pocketcode.app.data.AgentEngine.Event.Error -> { setStatus("Error"); addOutput("\\n${event.value}") }
        } } }
        setStatus("Ready")
    }
}

@Composable
fun MainScaffold() {
    PocketCodeTheme {
        var tab by remember { mutableStateOf(0) }
        var terminalCommand by remember { mutableStateOf<String?>(null) }
        val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { WorkspaceDrawer { scope.launch { drawerState.close() } } }
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier.clickable { },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("hopehype-next", style = MaterialTheme.typography.titleMedium)
                                    Text("⌄", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            navigationIcon = {
                                IconButton({ scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Workspace")
                                }
                            },
                            actions = {
                                Row(
                                    modifier = Modifier
                                        .background(GroupedSurface, RoundedCornerShape(9.dp))
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Segment("Agent", tab == 0) { tab = 0 }
                                    Segment("Terminal", tab == 1) { tab = 1 }
                                }
                                IconButton({ tab = 2 }) { Icon(Icons.Default.Settings, "Settings", tint = AppleBlue) }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack.copy(alpha = .94f))
                        )
                        HorizontalDivider(color = Hairline)
                    }
                },
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    when (tab) {
                        0 -> AgentScreen { terminalCommand = it; tab = 1 }
                        1 -> TerminalScreen(terminalCommand) { terminalCommand = null }
                        else -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) ElevatedSurface else Color.Transparent,
        shape = RoundedCornerShape(7.dp)
    ) { Text(label, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium) }
}

@Composable
fun AgentScreen(onRunTerminal: (String) -> Unit) {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    val files = remember(settings.activeWorkspacePath) { WorkspaceFileService(File(settings.activeWorkspacePath)) }
    val workspacePath = settings.activeWorkspacePath
    val engine = remember(workspacePath) { com.pocketcode.app.data.AgentEngine(settings, files, RealTerminalBackend(File(workspacePath), context.filesDir)) }
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    var approval by remember { mutableStateOf<com.pocketcode.app.data.AgentEngine.Event.Approval?>(null) }
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PureBlack).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("●  AGENT", fontFamily = Mono, style = MaterialTheme.typography.labelSmall, color = AppleGreen)
                Text(status, fontFamily = Mono, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Text(output.ifBlank { "Ask agent to inspect, edit, or test workspace." }, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (logs.isNotEmpty()) item {
            Card(colors = CardDefaults.cardColors(containerColor = GroupedSurface), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Inspect & Index", style = MaterialTheme.typography.titleMedium)
                    logs.forEach { Text(it, fontFamily = Mono, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 3.dp)) }
                }
            }
        }
        approval?.let { pending ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = GroupedSurface), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(if (pending.action.startsWith("Apply diff")) "Inline Diff Card" else "Command Suggestion Card", style = MaterialTheme.typography.titleMedium)
                        Text(pending.action, fontFamily = Mono, modifier = Modifier.padding(vertical = 10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({ pending.approve(false); approval = null }) { Text("Discard") }
                            Button({ pending.approve(true); approval = null }) { Text(if (pending.action.startsWith("Apply diff")) "Apply Diff" else "Run in Terminal") }
                        }
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = GroupedSurface), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(prompt, { prompt = it }, Modifier.weight(1f), placeholder = { Text("Ask agent…") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitAgent(prompt, engine, scope, { prompt = "" }, { status = it }, { output += it }, { logs = logs + it }, { approval = it }) }))
                    IconButton({ submitAgent(prompt, engine, scope, { prompt = "" }, { status = it }, { output += it }, { logs = logs + it }, { approval = it }) }) { Icon(Icons.Default.Send, "Send", tint = AppleBlue) }
                }
            }
        }
    }
}

@Composable
private fun DiffLine(text: String, color: Color) {
    Text(text, Modifier.fillMaxWidth().background(color).padding(horizontal = 12.dp, vertical = 7.dp), fontFamily = Mono)
}

@Composable
fun TerminalScreen(prefill: String? = null, onConsumed: () -> Unit = {}) {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    val backend = remember { RealTerminalBackend(File(settings.activeWorkspacePath), context.filesDir) }
    var command by remember { mutableStateOf(prefill.orEmpty()) }
    var output by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val history = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }
    LaunchedEffect(prefill) { if (prefill != null) { command = prefill; onConsumed() } }
    fun execute(raw: String) {
        val value = raw.trim()
        if (value.isEmpty()) return
        history.add(value); historyIndex = -1; command = ""
        if (value == "clear") { output = ""; return }
        scope.launch {
            output += "\$ $value\n"
            runCatching { withContext(Dispatchers.IO) { backend.execute(value) { line -> scope.launch { output += "$line\n" } } } }
                .onFailure { output += "${it.message}\n" }
        }
    }
    Column(Modifier.fillMaxSize().background(PureBlack).imePadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("workspace shell", fontFamily = Mono, style = MaterialTheme.typography.titleMedium); Text("● connected", color = AppleGreen, style = MaterialTheme.typography.labelSmall) }
            IconButton({}) { Icon(Icons.Default.Add, "New session", tint = AppleBlue) }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), reverseLayout = true) { item { Text(output, fontFamily = Mono, color = Color(0xFFE5E5EA), modifier = Modifier.padding(bottom = 12.dp)) } }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\$", fontFamily = Mono, color = AppleGreen); Spacer(Modifier.width(6.dp))
            OutlinedTextField(command, { command = it }, Modifier.weight(1f), singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Mono, color = AppleBlue), placeholder = { Text("command", color = AppleBlue) }, trailingIcon = { IconButton({ execute(command) }) { Icon(Icons.Default.ArrowUpward, "Run", tint = AppleBlue) } })
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("esc", "tab", "ctrl", "^C", "/", "-", "|", "↑", "↓", "←", "→").forEach { key ->
                InputChip(selected = false, onClick = { when (key) { "^C" -> { backend.cancel(); output += "^C\n"; command = "" }; "↑" -> if (history.isNotEmpty()) { historyIndex = (historyIndex + 1).coerceAtMost(history.lastIndex); command = history[history.lastIndex - historyIndex] }; "↓" -> command = ""; else -> command += if (key == "tab") "\t" else key } }, label = { Text(key, fontFamily = Mono) })
            }
        }
    }
}

@Composable
fun WorkspaceDrawer(onClose: () -> Unit) {
    val settings = remember { SettingsRepository(LocalContext.current) }
    var path by remember { mutableStateOf(settings.activeWorkspacePath) }
    var pathDraft by remember { mutableStateOf(path) }
    var showDialog by remember { mutableStateOf(false) }
    var openFolders by remember { mutableStateOf(setOf("src")) }
    val root = remember(path) { File(path) }
    val files = remember(path) { root.listFiles()?.filterNot { it.name == ".git" || it.name == "node_modules" }?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name }) ?: emptyList() }
    ModalDrawerSheet(Modifier.fillMaxWidth(.85f), drawerContainerColor = PureBlack) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("HH", color = AppleBlue, style = MaterialTheme.typography.headlineMedium); Text("hopehype workspace", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClose) { Icon(Icons.Default.Close, "Close") }
        }
        Card(Modifier.padding(horizontal = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(GroupedSurface), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp)) { Text("Root Mount", style = MaterialTheme.typography.titleMedium); Text(if (root.isDirectory) "● Mounted" else "● Unavailable", color = if (root.isDirectory) AppleGreen else AppleRed, style = MaterialTheme.typography.labelSmall); Text(path, fontFamily = Mono, modifier = Modifier.padding(vertical = 8.dp)); TextButton({ pathDraft = path; showDialog = true }) { Text("Change Folder") } }
        }
        Text("Project Files", Modifier.padding(16.dp, 14.dp, 16.dp, 6.dp), style = MaterialTheme.typography.labelLarge)
        Card(Modifier.padding(horizontal = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(GroupedSurface), shape = RoundedCornerShape(14.dp)) {
            files.forEach { file ->
                TreeRow(file.name, openFolders.contains(file.absolutePath), file = file.isFile) {
                    if (file.isDirectory) openFolders = openFolders.toggle(file.absolutePath)
                }
                if (file.isDirectory && openFolders.contains(file.absolutePath)) {
                    file.listFiles()?.filterNot { it.name == ".git" || it.name == "node_modules" }?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })?.forEach { child ->
                        TreeRow(child.name, false, indent = 20, file = child.isFile) {}
                    }
                }
            }
        }
        Text("Recent Sessions", Modifier.padding(16.dp, 14.dp, 16.dp, 6.dp), style = MaterialTheme.typography.labelLarge)
        Card(Modifier.padding(horizontal = 12.dp).fillMaxWidth(), colors = CardDefaults.cardColors(GroupedSurface), shape = RoundedCornerShape(14.dp)) { Column {
            ListItem({ Text("Current workspace") }, leadingContent = { Icon(Icons.Default.History, null) }, supportingContent = { Text(path, fontFamily = Mono) })
        } }
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(12.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("New Coding Session") }
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Change Folder") },
        text = { OutlinedTextField(pathDraft, { pathDraft = it }, label = { Text("Folder path") }, singleLine = true) },
        confirmButton = { TextButton({ path = pathDraft.ifBlank { path }; settings.activeWorkspacePath = path; showDialog = false }) { Text("Change") } },
        dismissButton = { TextButton({ showDialog = false }) { Text("Cancel") } }
    )
}

private fun Set<String>.toggle(value: String): Set<String> = if (contains(value)) this - value else this + value

@Composable
private fun TreeRow(name: String, open: Boolean, indent: Int = 0, file: Boolean = false, onClick: () -> Unit) {
    ListItem(
        { Text(name, fontFamily = if (file) Mono else FontFamily.SansSerif) },
        leadingContent = { Icon(if (file) Icons.Default.Description else Icons.Default.Folder, null) },
        trailingContent = if (!file) { { Icon(if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Toggle") } } else null,
        modifier = Modifier.padding(start = indent.dp).clickable(onClick = onClick)
    )
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.activeModel) }
    var workspace by remember { mutableStateOf(settings.activeWorkspacePath) }
    var autoApproveRead by remember { mutableStateOf(settings.autoApproveRead) }
    var requireTerminalApproval by remember { mutableStateOf(settings.requireTerminalApproval) }
    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val storageGranted = Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()
    LazyColumn(Modifier.fillMaxSize().background(PureBlack), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Settings", style = MaterialTheme.typography.headlineMedium, Modifier.weight(1f)); TextButton({}) { Text("Done", color = AppleBlue) } } }
        item { Text("AI Provider Endpoint", color = MaterialTheme.colorScheme.onSurfaceVariant); Surface(color = GroupedSurface, shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(baseUrl, { baseUrl = it }, Modifier.fillMaxWidth(), label = { Text("Base URL") }, supportingText = { Text("OpenAI-compatible endpoint") }, singleLine = true)
            OutlinedTextField(apiKey, { apiKey = it }, Modifier.fillMaxWidth(), label = { Text("API Key") }, supportingText = { Text("••••••••${if (apiKey.isEmpty()) "" else apiKey.takeLast(4)}") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Active Model") }, singleLine = true)
            OutlinedTextField(workspace, { workspace = it }, Modifier.fillMaxWidth(), label = { Text("Workspace Path") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ settings.baseUrl = baseUrl; settings.apiKey = apiKey; settings.activeModel = model; settings.activeWorkspacePath = workspace; settings.autoApproveRead = autoApproveRead; settings.requireTerminalApproval = requireTerminalApproval; status = "Saved" }) { Text("Save") }
                OutlinedButton({ scope.launch(Dispatchers.IO) { try { val c = URL(baseUrl.trimEnd('/') + "/models").openConnection() as HttpURLConnection; c.setRequestProperty("Authorization", "Bearer $apiKey"); val body = c.inputStream.bufferedReader().use { it.readText() }; c.disconnect(); withContext(Dispatchers.Main) { models = Regex("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)").findAll(body).map { it.groupValues[1] }.toList(); status = "Connected" } } catch (e: Exception) { withContext(Dispatchers.Main) { status = e.message ?: "Connection failed" } } } }) { Text("Test Connection / Fetch Models") }
            }
            if (status.isNotEmpty()) Text(status, color = if (status == "Connected") AppleGreen else AppleRed)
            models.forEach { Text(it, fontFamily = Mono, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } } }
        item { Text("Execution & Safety Guardrails", color = MaterialTheme.colorScheme.onSurfaceVariant); Surface(color = GroupedSurface, shape = RoundedCornerShape(16.dp)) { Column { SettingRow("Auto-approve read tools", autoApproveRead) { autoApproveRead = it; settings.autoApproveRead = it }; HorizontalDivider(color = Hairline); SettingRow("Require terminal approval", requireTerminalApproval) { requireTerminalApproval = it; settings.requireTerminalApproval = it } } } }
        item { Text("Virtual Environment", color = MaterialTheme.colorScheme.onSurfaceVariant); Surface(color = GroupedSurface, shape = RoundedCornerShape(16.dp)) { Column { ListItem({ Text("Storage Access") }, supportingContent = { Text(if (storageGranted) "Granted" else "Denied", color = if (storageGranted) AppleGreen else AppleRed) }, leadingContent = { Icon(Icons.Default.Storage, null) }, modifier = Modifier.clickable { if (!storageGranted && Build.VERSION.SDK_INT >= 30) context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))) }); ListItem({ Text("Sandbox Container") }, supportingContent = { Text("Isolated workspace") }); ListItem({ Text("Memory Allocation") }, supportingContent = { Text("512 MB") }); ListItem({ Text("Reset PTY Sessions & Cache", color = AppleRed) }) } } }
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    ListItem({ Text(label) }, trailingContent = { Switch(value, onChange) })
}
