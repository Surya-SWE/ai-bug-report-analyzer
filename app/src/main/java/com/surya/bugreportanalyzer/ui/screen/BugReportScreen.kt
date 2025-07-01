package com.surya.bugreportanalyzer.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surya.bugreportanalyzer.ui.components.CrashCard
import com.surya.bugreportanalyzer.ui.components.CrashDetailCard
import com.surya.bugreportanalyzer.ui.viewmodel.BugReportViewModel
import com.surya.bugreportanalyzer.ui.navigation.Screen
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.NavController
import com.surya.bugreportanalyzer.data.model.CrashSeverity
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import androidx.compose.ui.res.painterResource
import com.surya.bugreportanalyzer.R
import com.surya.bugreportanalyzer.ui.components.ANRCard
import com.surya.bugreportanalyzer.data.model.ANRReport
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(
    navController: NavController,
    viewModel: BugReportViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLargeFileWarning by remember { mutableStateOf(false) }
    var largeFileName by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf<CrashSeverity?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Crashes, 1 = ANR
    
    // LazyListState for scrolling to top
    val lazyListState = rememberLazyListState()
    
    // Effect to scroll to top when crashes are detected
    LaunchedEffect(uiState.bugReportFile?.crashes?.size) {
        if (uiState.bugReportFile?.crashes?.isNotEmpty() == true) {
            lazyListState.animateScrollToItem(0)
            println("BugReportScreen: Crashes loaded: ${uiState.bugReportFile?.crashes?.size}")
            println("BugReportScreen: Crash IDs: ${uiState.bugReportFile?.crashes?.map { it.id }}")
        }
    }

    // Effect to scroll to top when severity filter changes
    LaunchedEffect(selectedSeverity) {
        if (uiState.bugReportFile?.crashes?.isNotEmpty() == true) {
            lazyListState.animateScrollToItem(0)
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { readFileContent(context, it, viewModel, coroutineScope, { fileName -> 
            largeFileName = fileName
            showLargeFileWarning = true
        }) }
    }

    val scrollState = rememberScrollState()

    val anrZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processAnrZipFile(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Bug Report Analyzer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            // Show scroll to top FAB when there are more than 5 crashes and user has scrolled down
            val showScrollToTop by remember {
                derivedStateOf {
                    lazyListState.firstVisibleItemIndex > 0
                }
            }
            
            val totalCrashes = uiState.bugReportFile?.crashes?.size ?: 0
            val shouldShowFab = showScrollToTop && totalCrashes > 5
            
            // Debug logging
            LaunchedEffect(showScrollToTop, totalCrashes) {
                println("FAB Debug: showScrollToTop=$showScrollToTop, totalCrashes=$totalCrashes, shouldShowFab=$shouldShowFab")
            }
            
            if (shouldShowFab) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to Top"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Tab Row for Crashes/ANR
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = Color.White,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Crashes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Color(0xFF2196F3),
                        activeContentColor = Color.White,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ANR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (selectedTab == 0) {
                // File Upload Section (modernized)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .background(Color.Transparent),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload Bug Report",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bug Report Uploader",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload a .txt file containing crash logs. Only .txt files are accepted.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tip: For better experience, .txt file should be <10MB.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { fileLauncher.launch("text/*") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isProcessingFile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select File", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Loading State
                if (uiState.isLoading) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Analyzing bug report...")
                        }
                    }
                }

                // File Processing State
                if (uiState.isProcessingFile) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Processing File",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.processingMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Error State
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Results Section
                uiState.bugReportFile?.let { bugReport ->
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Wrap file info, filter chips, and crash list in a Column
                    Column(modifier = Modifier.fillMaxSize()) {
                        // File Info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "File Analysis",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Size",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${bugReport.fileSize} characters",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Crashes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${bugReport.crashes.size}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = if (bugReport.crashes.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Crashes List
                        if (bugReport.crashes.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Crashes Detected",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Check if there are multiple different severities
                            val uniqueSeverities = bugReport.crashes.map { it.severity }.distinct()
                            val showSeverityFilter = uniqueSeverities.size > 1
                            
                            // Severity Filter Chips (only show if there are multiple severities)
                            if (showSeverityFilter) {
                                Column {
                                    Text(
                                        text = "Filter by Severity",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilterChip(
                                            selected = selectedSeverity == null,
                                            onClick = { selectedSeverity = null },
                                            label = { 
                                                Text("ALL", fontWeight = FontWeight.Medium) 
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                        FilterChip(
                                            selected = selectedSeverity == CrashSeverity.HIGH,
                                            onClick = { selectedSeverity = CrashSeverity.HIGH },
                                            label = { 
                                                Text("HIGH", fontWeight = FontWeight.Medium) 
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color.Red,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                        FilterChip(
                                            selected = selectedSeverity == CrashSeverity.MEDIUM,
                                            onClick = { selectedSeverity = CrashSeverity.MEDIUM },
                                            label = { 
                                                Text("MEDIUM", fontWeight = FontWeight.Medium) 
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF2196F3),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                        FilterChip(
                                            selected = selectedSeverity == CrashSeverity.LOW,
                                            onClick = { selectedSeverity = CrashSeverity.LOW },
                                            label = { 
                                                Text("LOW", fontWeight = FontWeight.Medium) 
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF4CAF50),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Filter crashes based on selected filters
                            val filteredCrashes = bugReport.crashes.filter {
                                selectedSeverity == null || it.severity == selectedSeverity
                            }
                            
                            // Show filtered count
                            if (selectedSeverity != null) {
                                Text(
                                    text = "Showing ${filteredCrashes.size} of ${bugReport.crashes.size} crashes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            filteredCrashes.forEach { crash ->
                                CrashCard(
                                    crash = crash,
                                    onClick = { 
                                        val route = Screen.CrashBlock.route + "/${crash.id}"
                                        navController.navigate(route)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No crashes detected in this file")
                                }
                            }
                        }
                    }
                }
            } else {
                // ANR UI
                val clipboardManager = LocalClipboardManager.current
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .background(Color.Transparent),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF2196F3))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    Color(0xFF2196F3).copy(alpha = 0.08f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Upload ANR .zip File",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select a .zip file containing ANR traces (FS/data/anr)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { anrZipLauncher.launch("application/zip") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isProcessingFile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select .zip File", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // ANR Loading State
                if (uiState.isProcessingFile) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Processing ANR Zip",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.processingMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                // ANR List
                if (uiState.anrReports.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ANR's Detected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "(${uiState.anrReports.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    var selectedAnr by remember { mutableStateOf<ANRReport?>(null) }
                    var showFullTrace by remember { mutableStateOf(false) }
                    uiState.anrReports.forEach { anr ->
                        ANRCard(anr = anr, onClick = { selectedAnr = anr })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // ANR Detail Dialog (Preview)
                    if (selectedAnr != null && !showFullTrace) {
                        val previewLines = selectedAnr!!.fullTrace.lines().take(100)
                        AlertDialog(
                            onDismissRequest = { selectedAnr = null },
                            title = { Text("ANR Trace Preview") },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Text(
                                        text = previewLines.joinToString("\n"),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (selectedAnr!!.fullTrace.lines().size > 100) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("... (truncated)", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            confirmButton = {
                                Row {
                                    TextButton(onClick = { showFullTrace = true }) { Text("View Full Trace") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = { selectedAnr = null }) { Text("Close") }
                                }
                            }
                        )
                    }
                    // Full-Screen Dialog for Full Trace
                    if (selectedAnr != null && showFullTrace) {
                        Dialog(onDismissRequest = { showFullTrace = false; selectedAnr = null }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.95f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("ANR Full Trace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { showFullTrace = false; selectedAnr = null }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close")
                                        }
                                    }
                                    Text("File: ${selectedAnr!!.fileName}", style = MaterialTheme.typography.bodySmall)
                                    Text("Process: ${selectedAnr!!.processName} (PID: ${selectedAnr!!.pid})", style = MaterialTheme.typography.bodySmall)
                                    Text("Time: ${selectedAnr!!.timestamp}", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        val lines = selectedAnr?.fullTrace?.lines() ?: emptyList()
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            itemsIndexed(lines) { idx, line ->
                                                Text(
                                                    text = line ?: "",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Sticky footer at the bottom of scrollable content
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Made with love in India - Centered
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Made with",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "in India",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Social Links - Better styled icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Instagram Icon
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/suryapotnuru/"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Color(0xFFE4405F).copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = "Instagram",
                                modifier = Modifier.size(32.dp),
                                tint = Color.Unspecified
                            )
                        }
                        
                        // LinkedIn Icon
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/surya-potnuru-965399209/"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Color(0xFF0077B5).copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_linkedin),
                                contentDescription = "LinkedIn",
                                modifier = Modifier.size(32.dp),
                                tint = Color.Unspecified
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // Large File Warning Dialog
        if (showLargeFileWarning) {
            AlertDialog(
                onDismissRequest = { showLargeFileWarning = false },
                title = { Text("Large File Warning") },
                text = { 
                    Text("The file '$largeFileName' is very large (over 50MB). Processing may take a while and could cause performance issues. Do you want to continue?") 
                },
                confirmButton = {
                    TextButton(onClick = { showLargeFileWarning = false }) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLargeFileWarning = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun readFileContent(context: Context, uri: Uri, viewModel: BugReportViewModel, coroutineScope: CoroutineScope, onLargeFile: (String) -> Unit) {
    coroutineScope.launch {
        try {
            // First, check file size
            val fileSize = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.available().toLong()
                } ?: 0L
            }
            
            // Warn for very large files
            if (fileSize > 50 * 1024 * 1024) { // 50MB
                withContext(Dispatchers.Main) {
                    onLargeFile(uri.lastPathSegment ?: "unknown.txt")
                }
            }
            
            val fileName = uri.lastPathSegment ?: "unknown.txt"
            
            // Stream the file content directly to the parser without loading it all into memory
            withContext(Dispatchers.Main) {
                viewModel.analyzeFileFromUri(context, uri, fileName, fileSize)
            }
        } catch (e: Exception) {
            // Handle error on Main thread
            withContext(Dispatchers.Main) {
                // You can add error handling here if needed
            }
        }
    }
} 