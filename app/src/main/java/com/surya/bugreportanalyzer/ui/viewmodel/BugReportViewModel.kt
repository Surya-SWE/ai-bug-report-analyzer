package com.surya.bugreportanalyzer.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surya.bugreportanalyzer.data.model.BugReportFile
import com.surya.bugreportanalyzer.data.model.CrashReport
import com.surya.bugreportanalyzer.data.parser.BugReportParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import com.surya.bugreportanalyzer.data.model.ANRReport
import java.io.File
import java.util.UUID

@HiltViewModel
class BugReportViewModel @Inject constructor(
    private val parser: BugReportParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(BugReportUiState())
    val uiState: StateFlow<BugReportUiState> = _uiState.asStateFlow()

    fun analyzeFile(content: String, fileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessingFile = true,
                processingMessage = "Reading file content...",
                isLoading = false,
                error = null
            )
            
            try {
                // Simulate processing time for large files
                if (content.length > 1000000) { // 1MB
                    _uiState.value = _uiState.value.copy(
                        processingMessage = "Processing large file, please wait..."
                    )
                    kotlinx.coroutines.delay(100) // Small delay to show progress
                }
                
                _uiState.value = _uiState.value.copy(
                    processingMessage = "Analyzing crash reports..."
                )
                
                // Move parsing to background thread
                val crashes = withContext(Dispatchers.IO) {
                    parser.parseBugReport(content)
                }
                
                _uiState.value = _uiState.value.copy(
                    processingMessage = "Finalizing analysis..."
                )
                
                val bugReportFile = BugReportFile(
                    fileName = fileName,
                    fileSize = content.length.toLong(),
                    content = "", // Do NOT store the full content!
                    crashes = crashes
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessingFile = false,
                    processingMessage = "",
                    bugReportFile = bugReportFile,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessingFile = false,
                    processingMessage = "",
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun analyzeFileFromUri(context: Context, uri: Uri, fileName: String, fileSize: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessingFile = true,
                processingMessage = "Reading file content...",
                isLoading = false,
                error = null
            )
            
            try {
                if (fileSize > 1000000) { // 1MB
                    _uiState.value = _uiState.value.copy(
                        processingMessage = "Processing large file, please wait..."
                    )
                    delay(100)
                }
                
                _uiState.value = _uiState.value.copy(
                    processingMessage = "Analyzing crash reports..."
                )
                
                // Stream content directly to parser without loading it all into memory
                val crashes = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.use { stream ->
                        val reader = BufferedReader(InputStreamReader(stream))
                        parser.parseBugReportFromReader(reader, fileSize)
                    } ?: emptyList()
                }
                
                println("BugReportViewModel: Parsed crashes: ${crashes.size}")
                println("BugReportViewModel: Crash IDs: ${crashes.map { it.id }}")
                
                _uiState.value = _uiState.value.copy(
                    processingMessage = "Finalizing analysis..."
                )
                
                val bugReportFile = BugReportFile(
                    fileName = fileName,
                    fileSize = fileSize,
                    content = "", // Do NOT store the full content!
                    crashes = crashes
                )
                
                println("BugReportViewModel: Created BugReportFile with crashes: ${bugReportFile.crashes.size}")
                println("BugReportViewModel: BugReportFile crash IDs: ${bugReportFile.crashes.map { it.id }}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessingFile = false,
                    processingMessage = "",
                    bugReportFile = bugReportFile,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessingFile = false,
                    processingMessage = "",
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun selectCrash(crash: CrashReport) {
        _uiState.value = _uiState.value.copy(selectedCrash = crash)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = BugReportUiState()
    }

    fun processAnrZipFile(context: Context, zipUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessingFile = true,
                processingMessage = "Extracting ANR files...",
                isLoading = false,
                error = null
            )
            try {
                val cacheDir = File(context.cacheDir, "anr_zip_${UUID.randomUUID()}")
                cacheDir.mkdirs()
                val rootDir = withContext(Dispatchers.IO) {
                    com.surya.bugreportanalyzer.data.parser.BugReportParser.unzipFile(context, zipUri, cacheDir)
                }
                val anrFiles = withContext(Dispatchers.IO) {
                    com.surya.bugreportanalyzer.data.parser.BugReportParser.listAnrFiles(rootDir)
                }
                val anrReports = anrFiles.map { file ->
                    parser.parseAnrFile(file)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessingFile = false,
                    processingMessage = "",
                    anrReports = anrReports,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isProcessingFile = false,
                    processingMessage = "",
                    error = e.message ?: "Failed to process ANR zip file"
                )
            }
        }
    }

    fun selectAnr(anr: ANRReport) {
        _uiState.value = _uiState.value.copy(selectedAnr = anr)
    }
}

data class BugReportUiState(
    val isLoading: Boolean = false,
    val isProcessingFile: Boolean = false,
    val processingMessage: String = "",
    val bugReportFile: BugReportFile? = null,
    val selectedCrash: CrashReport? = null,
    val anrReports: List<ANRReport> = emptyList(),
    val selectedAnr: ANRReport? = null,
    val error: String? = null
) 