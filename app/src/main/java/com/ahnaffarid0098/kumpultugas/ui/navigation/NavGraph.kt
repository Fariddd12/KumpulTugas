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
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToForm = { navController.navigate(Screen.Form.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.Form.route) {
            FormScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
    }
}