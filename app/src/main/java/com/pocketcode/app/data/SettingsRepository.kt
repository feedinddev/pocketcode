package com.pocketcode.app.data

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var baseUrl: String get() = prefs.getString("base_url", "http://127.0.0.1:8080/v1") ?: "http://127.0.0.1:8080/v1"; set(value) { prefs.edit().putString("base_url", value.trimEnd('/')).apply() }
    var apiKey: String get() = prefs.getString("api_key", "sk-local") ?: "sk-local"; set(value) { prefs.edit().putString("api_key", value).apply() }
    var activeModel: String get() = prefs.getString("active_model", "claude-3-7-sonnet") ?: "claude-3-7-sonnet"; set(value) { prefs.edit().putString("active_model", value).apply() }
    var activeWorkspacePath: String get() = prefs.getString("workspace", "/storage/emulated/0/Projects") ?: "/storage/emulated/0/Projects"; set(value) { prefs.edit().putString("workspace", value).apply() }
    var workspaceUri: String? get() = prefs.getString("workspace_uri", null); set(value) { prefs.edit().putString("workspace_uri", value).apply() }
    var autoApproveRead: Boolean get() = prefs.getBoolean("auto_approve_read", true); set(value) { prefs.edit().putBoolean("auto_approve_read", value).apply() }
    var requireTerminalApproval: Boolean get() = prefs.getBoolean("require_terminal_approval", true); set(value) { prefs.edit().putBoolean("require_terminal_approval", value).apply() }
    var model: String get() = activeModel; set(value) { activeModel = value }
    var workspace: String get() = activeWorkspacePath; set(value) { activeWorkspacePath = value }
}
