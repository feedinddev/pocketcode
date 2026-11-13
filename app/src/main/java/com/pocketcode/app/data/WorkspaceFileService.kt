package com.pocketcode.app.data

import java.io.File

class WorkspaceFileService(private val root: File) {
    private fun safe(path: String): File {
        val base = root.canonicalFile
        val file = File(base, path).canonicalFile
        require(file.path == base.path || file.path.startsWith(base.path + File.separator)) { "Path outside workspace" }
        return file
    }
    
    fun read(path: String): String = safe(path).readText()
    
    fun readFile(relativePath: String): String = read(relativePath)
    
    fun write(path: String, content: String) { 
        safe(path).apply { 
            parentFile?.mkdirs()
            writeText(content) 
        } 
    }
    
    fun writeFile(relativePath: String, content: String) = write(relativePath, content)
    
    fun list(path: String = ""): List<String> = safe(path).listFiles().orEmpty()
        .filterNot { it.name == ".git" || it.name == "node_modules" }
        .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name })
        .map { it.relativeTo(root).path }
        
    fun listFiles(dirPath: String = ""): List<String> = list(dirPath)
    
    fun searchGrep(query: String, path: String = ""): List<String> = walk(safe(path))
        .filter { it.isFile && it.length() < 2_000_000 }
        .flatMap { file -> 
            runCatching { 
                file.readLines().mapIndexedNotNull { i, line -> 
                    if (line.contains(query, true)) "${file.relativeTo(root).path}:${i + 1}:$line" else null 
                } 
            }.getOrDefault(emptyList()) 
        }
        .toList()
        
    private fun walk(file: File): Sequence<File> = 
        if (file.isDirectory && file.name !in setOf(".git", "node_modules")) 
            sequenceOf(file) + file.listFiles().orEmpty().asSequence().flatMap(::walk) 
        else sequenceOf(file)
}