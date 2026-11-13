package com.pocketcode.app.terminal

/**
 * Stable seam for future Termux or remote shell integration.
 * Current UI uses deterministic mock output, so APK builds without an external terminal artifact.
 */
interface TerminalBackend {
    fun execute(command: String): String
}

class MockTerminalBackend : TerminalBackend {
    override fun execute(command: String): String = when (command.trim()) {
        "ls" -> "app  src  tests  package.json"
        "pwd" -> "/sdcard/Projects/hopehype-next"
        "clear" -> ""
        "help" -> "Commands: ls  pwd  clear  help"
        else -> "sh: ${command.trim()}: command not found"
    }
}
