package com.ahnaffarid0098.kumpultugas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
            MainScreen(navController = navController, viewModel = viewModel)
        }

        composable(
            route = "form_screen?taskId={taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskIdString = backStackEntry.arguments?.getString("taskId")
            val taskId = taskIdString?.toLongOrNull()

            FormScreen(
                navController = navController,
                viewModel = viewModel,
                taskId = taskId
            )
        }

        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
    }
}