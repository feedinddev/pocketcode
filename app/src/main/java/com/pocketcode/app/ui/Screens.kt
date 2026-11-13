package com.pocketcode.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults
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
import kotlinx.coroutines.launch

private val Mono = FontFamily.Monospace
private val PureBlack = Color(0xFF000000)
private val GroupedSurface = Color(0xFF1C1C1E)
private val ElevatedSurface = Color(0xFF2C2C2E)
private val Hairline = Color(0xFF38383A)
private val AppleBlue = Color(0xFF0A84FF)
private val AppleGreen = Color(0xFF30D158)
private val AppleRed = Color(0xFFFF453A)

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
                                IconButton({ tab = 2 }) { Icon(Icons.Default.Settings, "Settings") }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack.copy(alpha = .94f))
                        )
                        HorizontalDivider(color = Hairline)
                    }
                },
                bottomBar = {
                    NavigationBar(containerColor = PureBlack, windowInsets = WindowInsets.navigationBars) {
                        NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Default.AutoAwesome, "Agent") }, label = { Text("Agent") })
                        NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Default.Terminal, "Terminal") }, label = { Text("Terminal") })
                        NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Default.Settings, "Settings") }, label = { Text("Settings") })
                    }
                }
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
    var logsOpen by remember { mutableStateOf(false) }
    var diffState by remember { mutableStateOf("pending") }
    var prompt by remember { mutableStateOf("") }
    var showFiles by remember { mutableStateOf(false) }
    val files = listOf("src/auth/passkeys.ts", "src/auth/session.ts", "tests/auth.test.ts")
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PureBlack).imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Fix auth edge cases", style = MaterialTheme.typography.headlineMedium)
            Text("I reviewed your workspace and found one expiry boundary.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = GroupedSurface), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Read-only passkey review", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(7.dp))
                    Text("The token check rejects an expired token only after the clock crosses its exact expiry.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { logsOpen = !logsOpen },
                colors = CardDefaults.cardColors(containerColor = GroupedSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tool logs", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Icon(if (logsOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Expand logs")
                    }
                    if (logsOpen) {
                        Spacer(Modifier.height(10.dp))
                        files.forEach { Text("Read $it", fontFamily = Mono, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 3.dp)) }
                    } else Text("3 files read · 1.2s", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Inline diff", style = MaterialTheme.typography.labelLarge)
            DiffLine("- if (expiry < now)", AppleRed.copy(alpha = .15f))
            DiffLine("+ if (expiry <= now)", AppleGreen.copy(alpha = .15f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ diffState = "discarded" }, enabled = diffState == "pending") { Text("Discard") }
                Button({ diffState = "applied" }, enabled = diffState == "pending") { Text(if (diffState == "applied") "Applied" else "Apply Diff") }
            }
            if (diffState != "pending") Text(diffState.replaceFirstChar { it.uppercase() }, color = if (diffState == "applied") AppleGreen else AppleRed)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = GroupedSurface), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Suggested command", style = MaterialTheme.typography.labelLarge)
                    Text("$ npm run test:auth -- --watch=false", fontFamily = Mono, modifier = Modifier.padding(vertical = 10.dp))
                    Button({ onRunTerminal("npm run test:auth -- --watch=false") }) { Text("Run in Terminal") }
                }
            }
        }
        item {
            var showMenu by remember { mutableStateOf(false) }
            Box {
                Row(
                    Modifier.fillMaxWidth().background(GroupedSurface, RoundedCornerShape(24.dp)).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        TextButton({ showMenu = true }) { Text("@", color = AppleBlue, style = MaterialTheme.typography.titleMedium) }
                        androidx.compose.material3.DropdownMenu(showMenu, { showMenu = false }) {
                            listOf("src/auth/passkeys.ts", "src/auth/session.ts", "tests/auth.test.ts").forEach { file ->
                                DropdownMenuItem(text = { Text(file, fontFamily = Mono) }, onClick = { prompt = "@$file "; showMenu = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        prompt,
                        { prompt = it },
                        Modifier.weight(1f),
                        placeholder = { Text("Ask agent…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { prompt = "" })
                    )
                    IconButton({ prompt = "" }) { Icon(Icons.Default.Send, "Send", tint = AppleBlue) }
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
    var command by remember { mutableStateOf(prefill.orEmpty()) }
    var output by remember { mutableStateOf("alpine:~/projects/hopehype-next$ help\nCommands: ls  pwd  clear  help\n") }
    val history = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }
    LaunchedEffect(prefill) { if (prefill != null) { command = prefill; onConsumed() } }
    fun execute(raw: String) {
        val value = raw.trim()
        if (value.isEmpty()) return
        history.add(value)
        historyIndex = -1
        command = ""
        output = if (value == "clear") "" else output + "alpine:~/projects/hopehype-next$ $value\n" + when (value) {
            "ls" -> "app  src  tests  package.json\n"
            "pwd" -> "/sdcard/Projects/hopehype-next\n"
            "help" -> "Commands: ls  pwd  clear  help\n"
            else -> "sh: $value: command not found\n"
        }
    }
    Column(Modifier.fillMaxSize().background(PureBlack).imePadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), reverseLayout = true) {
            item { Text(output, fontFamily = Mono, color = Color(0xFFE5E5EA), modifier = Modifier.padding(bottom = 12.dp)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("alpine:~/projects/hopehype-next$", fontFamily = Mono, color = AppleGreen)
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(command, { command = it }, Modifier.weight(1f), singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Mono), placeholder = { Text("command") }, trailingIcon = { IconButton({ execute(command) }) { Icon(Icons.Default.ArrowUpward, "Run") } })
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("esc", "tab", "ctrl", "^C", "/", "-", "|", "↑", "↓", "←", "→").forEach { key ->
                InputChip(selected = false, onClick = {
                    when (key) {
                        "^C" -> command = ""
                        "↑" -> if (history.isNotEmpty()) { historyIndex = (historyIndex + 1).coerceAtMost(history.lastIndex); command = history[history.lastIndex - historyIndex] }
                        "↓" -> command = ""
                        else -> command += if (key == "tab") "\t" else key
                    }
                }, label = { Text(key, fontFamily = Mono) })
            }
        }
    }
}

@Composable
fun WorkspaceDrawer(onClose: () -> Unit) {
    var path by remember { mutableStateOf("/sdcard/Projects/hopehype-next") }
    var pathDraft by remember { mutableStateOf(path) }
    var showDialog by remember { mutableStateOf(false) }
    var openFolders by remember { mutableStateOf(setOf("src")) }
    ModalDrawerSheet(Modifier.fillMaxWidth(.85f), drawerContainerColor = GroupedSurface) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Workspace", style = MaterialTheme.typography.titleLarge); Text(path, fontFamily = Mono, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClose) { Icon(Icons.Default.Close, "Close") }
        }
        HorizontalDivider(color = Hairline)
        TextButton({ pathDraft = path; showDialog = true }, Modifier.padding(horizontal = 12.dp)) { Text("Change Folder") }
        Text("Files", Modifier.padding(16.dp, 10.dp), style = MaterialTheme.typography.labelLarge)
        TreeRow("src", openFolders.contains("src")) { openFolders = openFolders.toggle("src") }
        if (openFolders.contains("src")) {
            TreeRow("app", openFolders.contains("app"), indent = 24) { openFolders = openFolders.toggle("app") }
            TreeRow("auth", openFolders.contains("auth"), indent = 24) { openFolders = openFolders.toggle("auth") }
            if (openFolders.contains("auth")) TreeRow("passkeys.ts", false, indent = 48, file = true) {}
        }
        Text("Recent Sessions", Modifier.padding(16.dp, 10.dp), style = MaterialTheme.typography.labelLarge)
        ListItem({ Text("Auth boundary review") }, leadingContent = { Icon(Icons.Default.History, null) })
        ListItem({ Text("New Coding Session", color = AppleBlue) }, leadingContent = { Icon(Icons.Default.Add, null, tint = AppleBlue) }, modifier = Modifier.clickable(onClick = onClose))
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Change Folder") },
        text = { OutlinedTextField(pathDraft, { pathDraft = it }, label = { Text("Folder path") }, singleLine = true) },
        confirmButton = { TextButton({ path = pathDraft.ifBlank { path }; showDialog = false }) { Text("Change") } },
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
    var baseUrl by remember { mutableStateOf("http://localhost:8080/v1") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("qwen2.5-coder") }
    var autoApproveRead by remember { mutableStateOf(false) }
    var requireTerminalApproval by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val storageGranted = Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()
    LazyColumn(Modifier.fillMaxSize().background(PureBlack), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Settings", style = MaterialTheme.typography.headlineMedium, Modifier.weight(1f)); TextButton({}) { Text("Done", color = AppleBlue) } } }
        item {
            Text("Connection", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            Surface(color = GroupedSurface, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(baseUrl, { baseUrl = it }, Modifier.fillMaxWidth(), label = { Text("Base URL") }, singleLine = true)
                    OutlinedTextField(apiKey, { apiKey = it }, Modifier.fillMaxWidth(), label = { Text("API Key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Active Model") }, singleLine = true)
                }
            }
        }
        item {
            Text("Approvals", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            Surface(color = GroupedSurface, shape = RoundedCornerShape(16.dp)) {
                Column {
                    SettingRow("Auto-approve read tools", autoApproveRead) { autoApproveRead = it }
                    HorizontalDivider(color = Hairline)
                    SettingRow("Require approval for terminal", requireTerminalApproval) { requireTerminalApproval = it }
                }
            }
        }
        item {
            Text("Storage", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            Surface(color = GroupedSurface, shape = RoundedCornerShape(16.dp)) {
                ListItem(
                    { Text(if (storageGranted) "Storage Access (/sdcard)" else "Grant Storage Access (/sdcard)"); Text(if (storageGranted) "Available" else "Tap to open system permission", color = if (storageGranted) AppleGreen else AppleRed, style = MaterialTheme.typography.bodySmall) },
                    leadingContent = { Icon(Icons.Default.Storage, null) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, "Open") },
                    modifier = Modifier.clickable {
                        if (!storageGranted && Build.VERSION.SDK_INT >= 30) context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}")))
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    ListItem({ Text(label) }, trailingContent = { Switch(value, onChange) })
}
