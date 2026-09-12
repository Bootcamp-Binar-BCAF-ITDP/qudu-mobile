package com.example.test2.ui.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.DocumentTypes
import com.example.test2.core.Outcome
import com.example.test2.data.dto.CustomerProfileDto
import com.example.test2.data.repository.AuthRepository
import com.example.test2.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var profile by mutableStateOf<CustomerProfileDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var showingCached by mutableStateOf(false)
        private set

    var lastSyncedAt by mutableStateOf<Long?>(null)
        private set

    var uploadingType by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var info by mutableStateOf<String?>(null)
        private set

    val profileComplete: Boolean? get() = profile?.profileComplete

    val missingDocuments: List<String>
        get() = profile?.missingDocuments ?: DocumentTypes.PROFILE

    fun clearMessages() {
        error = null
        info = null
    }

    init {
        viewModelScope.launch {
            repository.cachedProfile.collect { cached ->
                val stored = cached.items.firstOrNull() ?: return@collect
                cached.fetchedAt?.let { lastSyncedAt = it }
                if (profile == null) profile = stored
            }
        }
    }

    fun refresh(userInitiated: Boolean = false) {
        if (isLoading) return

        isLoading = true
        if (userInitiated) isRefreshing = true

        viewModelScope.launch {
            try {
                when (val result = repository.myProfile()) {
                    is Outcome.Success -> {
                        profile = result.value
                        error = null
                        showingCached = false
                    }
                    is Outcome.Failure -> {
                        showingCached = profile != null
                        if (profile == null) error = result.message
                    }
                }
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun uploadDocument(documentType: String, uri: Uri, onUploaded: () -> Unit = {}) {

        if (uploadingType != null) return

        clearMessages()
        uploadingType = documentType

        viewModelScope.launch {
            when (val result = repository.uploadDocument(documentType, uri)) {
                is Outcome.Success -> {
                    info = "${DocumentTypes.label(documentType)} saved."
                    refreshBlocking()
                    onUploaded()
                }
                is Outcome.Failure -> error = result.message
            }
            uploadingType = null
        }
    }

    fun save(phoneNumber: String, address: String, occupation: String, onSaved: () -> Unit = {}) {

        if (isSaving) return

        val validationError = when {
            phoneNumber.isBlank() -> "Phone number is required."
            address.isBlank() -> "Address is required."
            occupation.isBlank() -> "Occupation is required."
            else -> null
        }

        if (validationError != null) {
            error = validationError
            return
        }

        clearMessages()
        isSaving = true

        viewModelScope.launch {
            when (val result = repository.updateProfile(phoneNumber.trim(), address.trim(), occupation.trim())) {
                is Outcome.Success -> {
                    profile = result.value
                    info = "Profile updated."
                    onSaved()
                }
                is Outcome.Failure -> error = result.message
            }
            isSaving = false
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            profile = null
            onLoggedOut()
        }
    }

    private suspend fun refreshBlocking() {
        when (val result = repository.myProfile()) {
            is Outcome.Success -> profile = result.value
            is Outcome.Failure -> error = result.message
        }
    }
}
