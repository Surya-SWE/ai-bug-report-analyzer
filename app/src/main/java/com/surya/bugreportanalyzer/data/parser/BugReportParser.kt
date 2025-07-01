package com.surya.bugreportanalyzer.data.parser

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.surya.bugreportanalyzer.data.model.*
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
import java.util.regex.Pattern
import java.io.BufferedReader
import java.util.zip.ZipInputStream
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.net.Uri

@Singleton
class BugReportParser @Inject constructor() {

    fun parseBugReport(content: String): List<CrashReport> {
        val crashes = mutableListOf<CrashReport>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.contains("FATAL EXCEPTION", ignoreCase = true)) {
                // Extract the next 15 lines (including this one)
                val blockLines = lines.subList(i, minOf(i + 15, lines.size))
                val rawBlock = blockLines.joinToString("\n")
                
                // Extract process information from the block
                val processInfo = extractProcessInfoFromBlock(rawBlock)
                
                // Optionally, parse exception type/message from the first line
                val crash = CrashReport(
                    id = generateStableId(rawBlock),
                    exceptionType = extractExceptionInfo(line).first,
                    exceptionMessage = extractExceptionInfo(line).second,
                    stackTrace = blockLines.drop(1).joinToString("\n"),
                    severity = CrashSeverity.HIGH,
                    processName = processInfo.first,
                    processId = processInfo.second,
                    rawBlock = rawBlock
                )
                crashes.add(crash)
                i += 15 // Skip the next 14 lines
            } else {
                i++
            }
        }
        return crashes
    }

    fun parseBugReportFromReader(reader: BufferedReader, fileSize: Long): List<CrashReport> {
        val crashes = mutableListOf<CrashReport>()
        val lines = reader.readLines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.contains("FATAL EXCEPTION", ignoreCase = true)) {
                val blockLines = lines.subList(i, minOf(i + 15, lines.size))
                val rawBlock = blockLines.joinToString("\n")
                
                // Extract process information from the block
                val processInfo = extractProcessInfoFromBlock(rawBlock)
                
                val crash = CrashReport(
                    id = generateStableId(rawBlock),
                    exceptionType = extractExceptionInfo(line).first,
                    exceptionMessage = extractExceptionInfo(line).second,
                    stackTrace = blockLines.drop(1).joinToString("\n"),
                    severity = CrashSeverity.HIGH,
                    processName = processInfo.first,
                    processId = processInfo.second,
                    rawBlock = rawBlock
                )
                crashes.add(crash)
                i += 15
            } else {
                i++
            }
        }
        return crashes
    }
    
    private fun createCrashReportFromData(crashData: Map<String, String>, lineNumber: Int): CrashReport? {
        val exceptionLine = crashData["exception"] ?: return null
        
        val exceptionInfo = extractExceptionInfo(exceptionLine)
        val processInfo = extractProcessInfo(crashData["processInfo"] ?: "")
        
        return CrashReport(
            id = generateId(),
            exceptionType = exceptionInfo.first,
            exceptionMessage = exceptionInfo.second,
            stackTrace = crashData["stackTrace"] ?: "",
            severity = determineSeverity(exceptionInfo.first),
            processName = processInfo.first,
            processId = processInfo.second,
            lineNumber = crashData["lineNumber"]?.toIntOrNull() ?: lineNumber
        )
    }

    private fun extractExceptionInfo(exceptionLine: String): Pair<String, String> {
        // Handle FATAL EXCEPTION pattern
        if (exceptionLine.contains("FATAL EXCEPTION", ignoreCase = true)) {
            val pattern = Pattern.compile("FATAL EXCEPTION:\\s*(.+)", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(exceptionLine)
            return if (matcher.find()) {
                val threadName = matcher.group(1)?.trim() ?: "Unknown"
                Pair("FATAL EXCEPTION: $threadName", "")
            } else {
                Pair("FATAL EXCEPTION", "Unknown fatal error")
            }
        }
        
        // Handle standard exception pattern
        val pattern = Pattern.compile("([A-Za-z.]+Exception):\\s*(.+)")
        val matcher = pattern.matcher(exceptionLine)
        return if (matcher.find()) {
            Pair(matcher.group(1) ?: "UnknownException", matcher.group(2) ?: "")
        } else {
            Pair("UnknownException", "")
        }
    }

    private fun determineSeverity(exceptionType: String): CrashSeverity {
        return when {
            exceptionType.contains("FATAL EXCEPTION") -> CrashSeverity.HIGH
            else -> CrashSeverity.MEDIUM
        }
    }

    private fun analyzeRootCause(exceptionType: String): String {
        return when {
            exceptionType.contains("NullPointerException") -> "Null pointer dereference"
            exceptionType.contains("OutOfMemoryError") -> "Memory exhaustion"
            exceptionType.contains("IllegalStateException") -> "Invalid object state"
            else -> "Unknown root cause"
        }
    }

    private fun generateFix(exceptionType: String): String {
        return when {
            exceptionType.contains("NullPointerException") -> "Add null checks before accessing object properties"
            exceptionType.contains("OutOfMemoryError") -> "Optimize memory usage and implement proper cleanup"
            exceptionType.contains("IllegalStateException") -> "Ensure object is properly initialized"
            else -> "Review stack trace for specific issue"
        }
    }

    private fun extractProcessInfo(exceptionLine: String): Pair<String, String> {
        val processPattern = Pattern.compile("Process:\\s*([^,\n]+)")
        val matcher = processPattern.matcher(exceptionLine)
        if (matcher.find()) {
            return Pair(matcher.group(1) ?: "Unknown", "Unknown")
        }
        val procPattern = Pattern.compile("proc=ProcessRecord\\{[a-zA-Z0-9]+ \\d+:([^/\\s\\}]+)")
        val procMatcher = procPattern.matcher(exceptionLine)
        if (procMatcher.find()) {
            return Pair(procMatcher.group(1) ?: "Unknown", "Unknown")
        }
        val pidPattern = Pattern.compile("PID:\\s*(\\d+)")
        val pidMatcher = pidPattern.matcher(exceptionLine)
        if (pidMatcher.find()) {
            return Pair("Unknown", pidMatcher.group(1) ?: "Unknown")
        }
        return Pair("Unknown", "Unknown")
    }

    private fun extractProcessInfoFromBlock(block: String): Pair<String, String> {
        val processPattern = Pattern.compile("Process:\\s*([^,\n]+)")
        val matcher = processPattern.matcher(block)
        if (matcher.find()) {
            return Pair(matcher.group(1) ?: "Unknown", "Unknown")
        }
        val procPattern = Pattern.compile("proc=ProcessRecord\\{[a-zA-Z0-9]+ \\d+:([^/\\s\\}]+)")
        val procMatcher = procPattern.matcher(block)
        if (procMatcher.find()) {
            return Pair(procMatcher.group(1) ?: "Unknown", "Unknown")
        }
        val pidPattern = Pattern.compile("PID:\\s*(\\d+)")
        val pidMatcher = pidPattern.matcher(block)
        if (pidMatcher.find()) {
            return Pair("Unknown", pidMatcher.group(1) ?: "Unknown")
        }
        return Pair("Unknown", "Unknown")
    }

    private fun generateId(): String {
        return "crash_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"
    }

    private fun generateStableId(content: String): String {
        val hash = content.hashCode().toString().replace("-", "abs")
        return "crash_$hash"
    }

    fun parseAnrFile(file: File): ANRReport {
        val lines = file.readLines()
        val fullTrace = lines.joinToString("\n")
        var subject = ""
        var exception = ""
        var timestamp = ""
        var processName = ""
        var pid = ""
        var summary = ""
        // Simple extraction logic
        for (line in lines) {
            when {
                line.startsWith("Subject:") -> subject = line.removePrefix("Subject:").trim()
                line.startsWith("Exception") -> exception = line.substringAfter(":").trim()
                line.startsWith("TimeStamp") -> timestamp = line.substringAfter(":").trim()
                line.startsWith("ProcessName") -> processName = line.substringAfter(":").trim()
                line.startsWith("Pid") -> pid = line.substringAfter(":").trim()
                line.startsWith("Summary") -> summary = line.substringAfter(":").trim()
            }
        }
        // If summary is empty, use the first 10 lines as a fallback
        if (summary.isEmpty()) summary = lines.take(10).joinToString("\n")
        return ANRReport(
            id = file.nameWithoutExtension + "_" + file.hashCode(),
            fileName = file.name,
            subject = subject,
            exception = exception,
            timestamp = timestamp,
            processName = processName,
            pid = pid,
            summary = summary,
            fullTrace = fullTrace
        )
    }

    companion object {
        /**
         * Unzips the given zip Uri to the target directory.
         * Returns the root extraction directory.
         */
        fun unzipFile(context: Context, zipUri: Uri, targetDir: File): File {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val file = File(targetDir, entry.name)
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            return targetDir
        }

        /**
         * Lists all ANR files in FS/data/anr under the given root directory.
         */
        fun listAnrFiles(rootDir: File): List<File> {
            val anrDir = File(rootDir, "FS/data/anr")
            return if (anrDir.exists() && anrDir.isDirectory) {
                anrDir.listFiles()?.filter { it.isFile } ?: emptyList()
            } else emptyList()
        }
    }
} 