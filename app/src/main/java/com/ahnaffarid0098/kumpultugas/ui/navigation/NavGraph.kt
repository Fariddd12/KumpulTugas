package com.ahnaffarid0098.kumpultugas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ahnaffarid0098.kumpultugas.ui.screen.AboutScreen
import com.ahnaffarid0098.kumpultugas.ui.screen.FormScreen
import com.ahnaffarid0098.kumpultugas.ui.screen.MainScreen
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToForm = { navController.navigate("form") },
                onNavigateToAbout = { navController.navigate("about") }
            )
        }
        composable("form") {
            FormScreen(
                viewModel = viewModel,
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