package com.example.minorapp.presentation.screen.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ProfileUiState(
    val username: String = "Harsh",
    val branch: String = "Computer Science Engineering",
    val identityLabel: String = "BATCH",
    val identityValue: String = "Class of 2025",
    val accessLevel: String = "Student",
    val selectedImageUri: String? = null
)

sealed interface ProfileOneOffUiEvent {
    data object UploadingImage : ProfileOneOffUiEvent
    data object UploadComplete : ProfileOneOffUiEvent
    data object RemovingImage : ProfileOneOffUiEvent
    data object ImageRemoved : ProfileOneOffUiEvent
}

class ProfileViewModel(
    private val sessionManager: SessionManager,
    isProfessorFlow: Boolean
) : ViewModel() {
    private val professorFlow = isProfessorFlow
    private val profileRole = if (professorFlow) UserRole.PROFESSOR else UserRole.STUDENT

    var uiState by mutableStateOf(
        ProfileUiState(
            username = normalizeProfileUsername(
                raw = sessionManager.getPreferredDisplayName(profileRole) ?: defaultName(professorFlow),
                isProfessorFlow = professorFlow
            ),
            branch = sessionManager.getSavedBranch() ?: defaultBranch(professorFlow),
            identityLabel = if (professorFlow) "REGISTRATION NUMBER" else "BATCH",
            identityValue = sessionManager.getSavedBatch() ?: defaultIdentityValue(professorFlow),
            accessLevel = if (professorFlow) "Professor" else "Student",
            selectedImageUri = sessionManager.getProfileImageUri()
        )
    )
        private set

    companion object {
        fun factory(
            sessionManager: SessionManager,
            isProfessorFlow: Boolean
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(sessionManager, isProfessorFlow) as T
                }
            }
        }
    }

    fun onUsernameChanged(username: String) {
        val normalized = normalizeProfileUsername(username, professorFlow)
        sessionManager.saveUsernameForRole(profileRole, normalized)
        uiState = uiState.copy(username = normalized)
    }

    fun onBranchChanged(branch: String) {
        sessionManager.saveBranch(branch)
        uiState = uiState.copy(branch = branch)
    }

    fun onBatchChanged(batch: String) {
        sessionManager.saveBatch(batch)
        uiState = uiState.copy(identityValue = batch)
    }

    fun onImagePicked(
        uri: String,
        onUiEvent: (ProfileOneOffUiEvent) -> Unit = {}
    ) {
        sessionManager.saveProfileImageUri(uri)
        uiState = uiState.copy(selectedImageUri = uri)

        viewModelScope.launch {
            onUiEvent(ProfileOneOffUiEvent.UploadingImage)
            delay(1000)
            onUiEvent(ProfileOneOffUiEvent.UploadComplete)
        }
    }

    fun onRemoveImage(onUiEvent: (ProfileOneOffUiEvent) -> Unit = {}) {
        sessionManager.saveProfileImageUri(null)
        uiState = uiState.copy(selectedImageUri = null)

        viewModelScope.launch {
            onUiEvent(ProfileOneOffUiEvent.RemovingImage)
            delay(1000)
            onUiEvent(ProfileOneOffUiEvent.ImageRemoved)
        }
    }

    fun onSaveChangesClick() {
        // Keep behavior unchanged: no extra UI side effect.
        sessionManager.saveUsernameForRole(profileRole, normalizeProfileUsername(uiState.username, professorFlow))
        sessionManager.saveBranch(uiState.branch)
        sessionManager.saveBatch(uiState.identityValue)
    }
}

private fun defaultName(isProfessorFlow: Boolean): String {
    return if (isProfessorFlow) "Sharma" else "Harsh"
}

private fun defaultBranch(isProfessorFlow: Boolean): String {
    return if (isProfessorFlow) "System Design" else "Computer Science Engineering"
}

private fun defaultIdentityValue(isProfessorFlow: Boolean): String {
    return if (isProfessorFlow) "REG-2026-047" else "Class of 2025"
}

private fun normalizeProfileUsername(raw: String, isProfessorFlow: Boolean): String {
    if (!isProfessorFlow) return raw
    return raw.removePrefix("Prof. ").removePrefix("Prof ").trim().ifBlank { "Sharma" }
}
