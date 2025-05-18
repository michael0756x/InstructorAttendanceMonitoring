package com.example.argeneratorapp.navigation

import com.example.argeneratorapp.screens.AddARDatesScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.argeneratorapp.screens.*

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        addLoginComposable(navController)
        addHomeComposable(navController)
        addAddARComposable(navController)
        addARManagementComposable(navController)
        addListOfArDatesComposable(navController)
        addAddARDatesComposable(navController)
        addSummaryComposable(navController)
        addTimeLogComposable(navController) // Add TimeLogScreen
        addFinalPartSummaryComposable(navController) // Add FinalPartSummaryScreen
    }
}

private fun NavGraphBuilder.addLoginComposable(navController: NavHostController) {
    composable("login") { LoginScreen(navController) }
}

private fun NavGraphBuilder.addHomeComposable(navController: NavHostController) {
    composable("home/{userId}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        HomeScreen(navController = navController, userId = userId)
    }
}

private fun NavGraphBuilder.addAddARComposable(navController: NavHostController) {
    composable("add_ar/{userId}/{mainStartDate}/{mainEndDate}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val mainStartDate = backStackEntry.arguments?.getString("mainStartDate") ?: ""
        val mainEndDate = backStackEntry.arguments?.getString("mainEndDate") ?: ""
        AddARScreen(navController, userId, mainStartDate, mainEndDate)
    }
}

private fun NavGraphBuilder.addARManagementComposable(navController: NavHostController) {
    composable("ar_management/{userId}/{mainStartDate}/{mainEndDate}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val mainStartDate = backStackEntry.arguments?.getString("mainStartDate") ?: ""
        val mainEndDate = backStackEntry.arguments?.getString("mainEndDate") ?: ""
        ARManagementScreen(navController, userId, mainStartDate, mainEndDate)
    }
}

private fun NavGraphBuilder.addListOfArDatesComposable(navController: NavHostController) {
    composable("list_of_ar_dates/{userId}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        ListOfArDatesScreen(navController, userId)
    }
}

private fun NavGraphBuilder.addAddARDatesComposable(navController: NavHostController) {
    composable("add_ar_dates/{userId}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        AddARDatesScreen(navController, userId)
    }
}

private fun NavGraphBuilder.addSummaryComposable(navController: NavHostController) {
    composable("summary/{userId}/{mainStartDate}/{mainEndDate}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val mainStartDate = backStackEntry.arguments?.getString("mainStartDate") ?: ""
        val mainEndDate = backStackEntry.arguments?.getString("mainEndDate") ?: ""
        SummaryScreen(navController, userId, mainStartDate, mainEndDate)
    }
}

// Add the TimeLogScreen
private fun NavGraphBuilder.addTimeLogComposable(navController: NavHostController) {
    composable("time_log/{userId}/{mainStartDate}/{mainEndDate}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val mainStartDate = backStackEntry.arguments?.getString("mainStartDate") ?: ""
        val mainEndDate = backStackEntry.arguments?.getString("mainEndDate") ?: ""
        TimeLogScreen(navController, userId, mainStartDate, mainEndDate)
    }
}

// Add the FinalPartSummaryScreen
private fun NavGraphBuilder.addFinalPartSummaryComposable(navController: NavHostController) {
    composable("final_part_summary/{userId}/{mainStartDate}/{mainEndDate}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val mainStartDate = backStackEntry.arguments?.getString("mainStartDate") ?: ""
        val mainEndDate = backStackEntry.arguments?.getString("mainEndDate") ?: ""
        FinalPartSummaryScreen(navController, userId, mainStartDate, mainEndDate)
    }
}
