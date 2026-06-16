package com.ahnaffarid0098.kumpultugas.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahnaffarid0098.kumpultugas.data.network.TaskApiService
import com.ahnaffarid0098.kumpultugas.data.network.UserDataStore
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

class ViewModelFactory(
    private val apiService: TaskApiService,
    private val settingsDataStore: SettingsDataStore,
    private val userDataStore: UserDataStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(apiService, settingsDataStore, userDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}