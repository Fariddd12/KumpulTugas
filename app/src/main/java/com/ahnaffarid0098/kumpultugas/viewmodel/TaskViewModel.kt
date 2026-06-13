package com.ahnaffarid0098.kumpultugas.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahnaffarid0098.kumpultugas.data.local.LayoutDataStore
import com.ahnaffarid0098.kumpultugas.data.local.TaskDatabase
import com.ahnaffarid0098.kumpultugas.data.local.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = TaskDatabase.getDatabase(application).taskDao()
    private val layoutDataStore = LayoutDataStore(application)

    val allTasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isGridView: StateFlow<Boolean> = layoutDataStore.isGridView
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI State for Form
    var titleInput by mutableStateOf("")
        private set

    var priorityInput by mutableStateOf("Sedang")
        private set

    var titleError by mutableStateOf<String?>(null)
        private set

    fun onTitleChange(newValue: String) {
        titleInput = newValue
        titleError = null
    }

    fun onPriorityChange(newValue: String) {
        priorityInput = newValue
    }

    fun validateAndAddTask(): Boolean {
        if (titleInput.isBlank()) {
            titleError = "empty"
            return false
        }
        if (titleInput.length < 3) {
            titleError = "short"
            return false
        }
        addTask(titleInput, priorityInput)
        // Reset inputs after success
        titleInput = ""
        priorityInput = "Sedang"
        return true
    }

    fun addTask(title: String, priority: String) {
        viewModelScope.launch {
            taskDao.insertTask(TaskEntity(title = title, priority = priority))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun toggleLayout(isGrid: Boolean) {
        viewModelScope.launch {
            layoutDataStore.saveLayoutPreference(isGrid)
        }
    }
}
