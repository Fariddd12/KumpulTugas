package com.ahnaffarid0098.kumpultugas.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Form : Screen("form_screen")
    object About : Screen("about_screen")
}