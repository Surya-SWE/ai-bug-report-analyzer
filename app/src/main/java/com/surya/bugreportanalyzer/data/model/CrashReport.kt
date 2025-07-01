package com.surya.bugreportanalyzer.data.model

import kotlinx.datetime.Instant

data class CrashReport(
    val id: String = "",
    val timestamp: Instant? = null,
    val exceptionType: String = "",
    val exceptionMessage: String = "",
    val stackTrace: String = "",
    val threadInfo: String = "",
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val severity: CrashSeverity = CrashSeverity.MEDIUM,
    val rootCause: String = "",
    val suggestedFix: String = "",
    val lineNumber: Int? = null,
    val className: String = "",
    val methodName: String = "",
    val processName: String = "",
    val processId: String = "",
    val rawBlock: String = ""
)

data class DeviceInfo(
    val androidVersion: String = "",
    val deviceModel: String = "",
    val appVersion: String = "",
    val buildNumber: String = ""
)

enum class CrashSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class BugReportFile(
    val fileName: String,
    val fileSize: Long,
    val content: String,
    val crashes: List<CrashReport> = emptyList()
)

data class ANRReport(
    val id: String = "",
    val fileName: String = "",
    val subject: String = "",
    val exception: String = "",
    val timestamp: String = "",
    val processName: String = "",
    val pid: String = "",
    val summary: String = "",
    val fullTrace: String = ""
) 