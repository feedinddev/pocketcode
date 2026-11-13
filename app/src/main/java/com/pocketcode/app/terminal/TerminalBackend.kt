package com.pocketcode.app.terminal

interface TerminalBackend {
    fun execute(command: String): String
}
