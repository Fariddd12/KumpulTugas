package com.ahnaffarid0098.kumpultugas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ahnaffarid0098.kumpultugas.ui.screen.AboutScreen
import com.ahnaffarid0098.kumpultugas.ui.screen.FormScreen
import com.ahnaffarid0098.kumpultugas.ui.screen.MainScreen
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val taskViewModel: TaskViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                viewModel = taskViewModel,
                onNavigateToForm = { navController.navigate("form") },
                onNavigateToAbout = { navController.navigate("about") }
            )
        }
        composable("form") {
            FormScreen(
                viewModel = taskViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("about") {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}