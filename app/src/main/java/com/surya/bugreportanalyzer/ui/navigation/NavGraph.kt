package com.surya.bugreportanalyzer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.surya.bugreportanalyzer.ui.screen.BugReportScreen
import com.surya.bugreportanalyzer.ui.screen.CrashDetailScreen
import com.surya.bugreportanalyzer.ui.screen.CrashBlockScreen
import com.surya.bugreportanalyzer.ui.viewmodel.BugReportViewModel

@Composable
fun BugReportNavGraph(navController: NavHostController) {
    val viewModel: BugReportViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.BugReport.route
    ) {
        composable(Screen.BugReport.route) {
            BugReportScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(
            route = Screen.CrashDetail.route + "/{crashId}",
            arguments = listOf(
                navArgument("crashId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val crashId = backStackEntry.arguments?.getString("crashId")
            CrashDetailScreen(
                crashId = crashId ?: "",
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable(
            route = Screen.CrashBlock.route + "/{crashId}",
            arguments = listOf(
                navArgument("crashId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val crashId = backStackEntry.arguments?.getString("crashId")
            CrashBlockScreen(
                crashId = crashId ?: "",
                navController = navController,
                viewModel = viewModel
            )
        }
    }
} 