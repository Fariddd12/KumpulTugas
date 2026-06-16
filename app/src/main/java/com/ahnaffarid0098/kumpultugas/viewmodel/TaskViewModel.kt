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
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TaskViewModel(
    private val apiService: TaskApiService,
    private val settingsDataStore: SettingsDataStore,
    private val userDataStore: UserDataStore
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

    var apiStatus by mutableStateOf(ApiStatus.LOADING)
        private set

    var tasksOnline by mutableStateOf(emptyList<TaskResponse>())
        private set

    val isListLayout: StateFlow<Boolean> = settingsDataStore.layoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUser: StateFlow<User> = userDataStore.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User())


    fun getTasksFromServer(email: String) {
        viewModelScope.launch {
            apiStatus = ApiStatus.LOADING
            try {
                tasksOnline = apiService.getTasks(email)
                apiStatus = ApiStatus.SUCCESS
            } catch (e: Exception) {
                tasksOnline = emptyList()
                apiStatus = ApiStatus.ERROR
            }
        }
    }

    fun uploadTaskToServer(email: String, imagePart: MultipartBody.Part, onComplete: (Boolean) -> Unit) {
        if (titleInput.isBlank()) {
            titleError = "empty"
            onComplete(false)
            return
        }
        if (titleInput.length < 3) {
            titleError = "short"
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                val titleBody = titleInput.trim().toRequestBody(MultipartBody.FORM)
                val priorityBody = priorityInput.toRequestBody(MultipartBody.FORM)
                val emailBody = email.toRequestBody(MultipartBody.FORM)

                apiService.uploadTask(titleBody, priorityBody, emailBody, imagePart)

                titleInput = ""
                priorityInput = "Sedang"
                titleError = null

                getTasksFromServer(email)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun deleteTaskFromServer(id: Long, email: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                apiService.deleteTask(id)
                getTasksFromServer(email)
                onComplete()
            } catch (e: Exception) {

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