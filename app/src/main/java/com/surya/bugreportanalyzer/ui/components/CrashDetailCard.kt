package com.surya.bugreportanalyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surya.bugreportanalyzer.data.model.CrashReport
import com.surya.bugreportanalyzer.data.model.CrashSeverity

@Composable
fun CrashDetailCard(crash: CrashReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when (crash.severity) {
                        CrashSeverity.CRITICAL -> Icons.Default.Error
                        CrashSeverity.HIGH -> Icons.Default.Warning
                        CrashSeverity.MEDIUM -> Icons.Default.Info
                        CrashSeverity.LOW -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when (crash.severity) {
                        CrashSeverity.CRITICAL -> Color.Red
                        CrashSeverity.HIGH -> Color(0xFFFF9800)
                        CrashSeverity.MEDIUM -> Color(0xFF2196F3)
                        CrashSeverity.LOW -> Color(0xFF4CAF50)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crash Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exception Information
            DetailSection(
                title = "Exception Information",
                icon = Icons.Default.BugReport
            ) {
                DetailRow("Type", crash.exceptionType)
                if (crash.exceptionMessage.isNotEmpty()) {
                    DetailRow("Message", crash.exceptionMessage)
                }
                DetailRow("Severity", crash.severity.name)
                if (crash.timestamp != null) {
                    DetailRow("Timestamp", crash.timestamp.toString())
                }
                if (crash.processName.isNotEmpty() && crash.processName != "Unknown") {
                    DetailRow("Process", "${crash.processName} (PID: ${crash.processId})")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Root Cause Analysis
            DetailSection(
                title = "Root Cause Analysis",
                icon = Icons.Default.Search
            ) {
                Text(
                    text = crash.rootCause,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggested Fix
            DetailSection(
                title = "Suggested Fix",
                icon = Icons.Default.Build
            ) {
                Text(
                    text = crash.suggestedFix,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thread Information
            if (crash.threadInfo.isNotEmpty()) {
                DetailSection(
                    title = "Thread Information",
                    icon = Icons.Default.Info
                ) {
                    Text(
                        text = crash.threadInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
} 