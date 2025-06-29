package com.surya.bugreportanalyzer.ui.navigation

sealed class Screen(val route: String) {
    object BugReport : Screen("bug_report")
    object CrashDetail : Screen("crash_detail")
    object CrashBlock : Screen("crash_block")
} 