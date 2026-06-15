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
import com.ahnaffarid0098.kumpultugas.ui.navigation.NavGraph
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = TaskDatabase.getInstance(applicationContext).dao
        val dataStore = SettingsDataStore(applicationContext)

        val factory = ViewModelFactory(dao, dataStore)

        val taskViewModel: TaskViewModel by viewModels { factory }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController, viewModel = taskViewModel)
            }
        }
    }
}