package com.ahnaffarid0098.kumpultugas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahnaffarid0098.kumpultugas.data.local.SettingsDataStore
import com.ahnaffarid0098.kumpultugas.data.local.TaskDao
import com.ahnaffarid0098.kumpultugas.data.local.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val dao: TaskDao,
    private val dataStore: SettingsDataStore
) : ViewModel() {

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

        insertTask(titleInput, priorityInput)

        titleInput = ""
        priorityInput = "Sedang"
        titleError = null
        
        return true
    }

    val tasks: StateFlow<List<TaskEntity>> = dao.getTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isListLayout: StateFlow<Boolean> = dataStore.layoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleLayout(isList: Boolean) {
        viewModelScope.launch {
            dataStore.saveLayout(isList)
        }
    }

    fun insertTask(title: String, priority: String) {
        viewModelScope.launch {
            dao.insert(TaskEntity(title = title, priority = priority))
        }
    }

    suspend fun getTaskById(id: Long): TaskEntity? {
        return dao.getTaskById(id)
    }

    fun updateTask(id: Long, title: String, priority: String) {
        viewModelScope.launch {
            dao.update(TaskEntity(id = id, title = title, priority = priority))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.delete(task)
        }
    }
}