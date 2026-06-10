package com.ahnaffarid0098.kumpultugas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ahnaffarid0098.kumpultugas.data.Task

class TaskViewModel : ViewModel() {

    var taskList by mutableStateOf(listOf<Task>())
        private set

    var titleInput by mutableStateOf("")
        private set

    var priorityInput by mutableStateOf("Tinggi")
        private set

    var titleError by mutableStateOf<String?>(null)
        private set

    fun onTitleChange(newValue: String) {
        titleInput = newValue
        if (newValue.isNotBlank() && newValue.length >= 3) {
            titleError = null
        }
    }

    fun onPriorityChange(newValue: String) {
        priorityInput = newValue
    }

    fun validateAndAddTask(): Boolean {
        return when {
            titleInput.isBlank() -> {
                titleError = "empty"
                false
            }
            titleInput.length < 3 -> {
                titleError = "short"
                false
            }
            else -> {
                titleError = null
                val newTask = Task(title = titleInput, priority = priorityInput)
                taskList = taskList + newTask
                clearForm()
                true
            }
        }
    }

    private fun clearForm() {
        titleInput = ""
        priorityInput = "Tinggi"
        titleError = null
    }
}