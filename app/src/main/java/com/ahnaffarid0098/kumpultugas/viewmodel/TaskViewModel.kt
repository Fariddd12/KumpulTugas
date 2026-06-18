package com.ahnaffarid0098.kumpultugas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahnaffarid0098.kumpultugas.data.local.SettingsDataStore
import com.ahnaffarid0098.kumpultugas.data.network.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TaskViewModel(
    private val apiService: TaskApiService,
    private val settingsDataStore: SettingsDataStore,
    private val userDataStore: UserDataStore
) : ViewModel() {

    var titleInput by mutableStateOf("")
        private set

    var descriptionInput by mutableStateOf("")
        private set

    var priorityInput by mutableStateOf("Sedang")
        private set

    var titleError by mutableStateOf<String?>(null)
        private set

    var descriptionError by mutableStateOf<String?>(null)
        private set

    var imageError by mutableStateOf(false)
        private set

    fun onTitleChange(newValue: String) {
        titleInput = newValue
        titleError = null
    }

    fun onDescriptionChange(newValue: String) {
        descriptionInput = newValue
        descriptionError = null
    }

    fun resetImageError() {
        imageError = false
    }

    fun onPriorityChange(newValue: String) {
        priorityInput = newValue
    }

    var apiStatus by mutableStateOf(ApiStatus.LOADING)
        private set

    var tasksOnline by mutableStateOf(emptyList<TaskResponse>())
        private set

    val isListLayout: StateFlow<Boolean> = settingsDataStore.layoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUser: StateFlow<User> = userDataStore.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User())


    fun getTasksFromServer(email: String) {
        if (email.isBlank()) {
            tasksOnline = emptyList()
            apiStatus = ApiStatus.SUCCESS
            return
        }

        viewModelScope.launch {
            apiStatus = ApiStatus.LOADING
            try {
                tasksOnline = apiService.getTasks(email)
                apiStatus = ApiStatus.SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                tasksOnline = emptyList()
                apiStatus = ApiStatus.ERROR
            }
        }
    }

    fun setApiError() {
        apiStatus = ApiStatus.ERROR
    }

    fun uploadTaskToServer(email: String, imagePart: MultipartBody.Part?, onComplete: (Boolean) -> Unit) {
        var hasError = false
        if (titleInput.isBlank()) {
            titleError = "empty"
            hasError = true
        } else if (titleInput.length < 3) {
            titleError = "short"
            hasError = true
        }

        if (descriptionInput.isBlank()) {
            descriptionError = "empty"
            hasError = true
        }

        if (imagePart == null) {
            imageError = true
            hasError = true
        }

        if (hasError) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                val titleBody = titleInput.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                val descBody = descriptionInput.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                val priorityBody = priorityInput.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())

                apiService.uploadTask(titleBody, descBody, priorityBody, emailBody, imagePart!!)

                clearInputs()
                getTasksFromServer(email)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun updateTaskOnServer(id: Long, email: String, onComplete: (Boolean) -> Unit) {
        var hasError = false
        if (titleInput.isBlank()) {
            titleError = "empty"
            hasError = true
        }
        
        if (descriptionInput.isBlank()) {
            descriptionError = "empty"
            hasError = true
        }

        if (hasError) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                val titleBody = titleInput.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                val descBody = descriptionInput.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                val priorityBody = priorityInput.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())

                apiService.updateTask(id, titleBody, descBody, priorityBody, emailBody, null)

                clearInputs()
                getTasksFromServer(email)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    private fun clearInputs() {
        titleInput = ""
        descriptionInput = ""
        priorityInput = "Sedang"
        titleError = null
        descriptionError = null
        imageError = false
    }

    fun deleteTaskFromServer(id: Long, email: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                apiService.deleteTask(id)
                getTasksFromServer(email)
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleLayout(isList: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveLayout(isList)
        }
    }

    fun loginUser(user: User) {
        viewModelScope.launch {
            userDataStore.saveData(user)
            getTasksFromServer(user.email)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userDataStore.saveData(User("", "", ""))
            tasksOnline = emptyList()
        }
    }
}
