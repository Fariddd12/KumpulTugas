package com.ahnaffarid0098.kumpultugas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.ahnaffarid0098.kumpultugas.data.local.SettingsDataStore
import com.ahnaffarid0098.kumpultugas.data.local.TaskDatabase
import com.ahnaffarid0098.kumpultugas.data.local.ViewModelFactory
import com.ahnaffarid0098.kumpultugas.data.network.TaskApiService
import com.ahnaffarid0098.kumpultugas.data.network.UserDataStore
import com.ahnaffarid0098.kumpultugas.ui.navigation.NavGraph
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TaskDatabase.getInstance(applicationContext)
        val taskDao = database.dao
        val apiService = TaskApiService.create(taskDao)
        val settingsDataStore = SettingsDataStore(applicationContext)
        val userDataStore = UserDataStore(applicationContext)

        val factory = ViewModelFactory(apiService, settingsDataStore, userDataStore)

        val taskViewModel: TaskViewModel by viewModels { factory }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController, viewModel = taskViewModel)
            }
        }
    }
}