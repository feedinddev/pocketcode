package com.pocketcode.app.terminal

import android.content.Context
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class RealTerminalBackend(workingDirectory: File? = null, fallbackDirectory: File? = null) : TerminalBackend {
    private val prefix = File(fallbackDirectory ?: File("/data/data/com.pocketcode.app/files"), "usr").apply {
        mkdirs()
        listOf("bin", "lib", "etc", "tmp", "var", "home").forEach { File(this, it).mkdirs() }
    }
    private val workingDirectory = (workingDirectory ?: File("/storage/emulated/0/Projects")).let { if (it.isDirectory) it else prefix }
    private val running = AtomicReference<Process?>()

    constructor(context: Context) : this(null, context.applicationContext.filesDir)
    private val history = CopyOnWriteArrayList<String>()
    fun execute(command: String, onLine: (String) -> Unit = {}): Int {
        history += command
        val process = ProcessBuilder("/system/bin/sh", "-c", command).apply {
            directory(workingDirectory)
            environment().putAll(mapOf("HOME" to "/sdcard", "PREFIX" to prefix.path, "PATH" to "${prefix.path}/bin:/system/bin:/system/xbin", "LD_LIBRARY_PATH" to "${prefix.path}/lib"))
            redirectErrorStream(true)
        }.start()
        running.set(process)
        process.inputStream.bufferedReader().forEachLine(onLine)
        val code = process.waitFor(); running.compareAndSet(process, null); return code
    }
    fun cancel() { running.getAndSet(null)?.destroyForcibly() }
    fun history(): List<String> = history.toList()
    override fun execute(command: String): String { val out = StringBuilder(); execute(command) { out.append(it).append('\n') }; return out.toString().trimEnd() }
}
