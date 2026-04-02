package com.example.minorapp.presentation.screen.tasks

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.tasks.LocalProfessorTasksRepository
import com.example.minorapp.data.tasks.ProfessorTaskInventoryData
import com.example.minorapp.data.tasks.ProfessorTaskPriority
import com.example.minorapp.data.tasks.ProfessorTasksRepository
import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.launch

data class ProfessorTaskInventoryUi(
    val id: String,
    val statusLabel: String,
    val priority: ProfessorTaskPriority,
    val dueDate: String,
    val title: String,
    val enrolledCount: Int?,
    val actionText: String,
    val referencesCount: Int,
    val isDraft: Boolean,
    val draftHint: String
)

data class ProfessorTasksUiState(
    val title: String = "",
    val description: String = "",
    val deadlineDate: String = "",
    val category: String = "Laboratory",
    val categoryOptions: List<String> = listOf("Laboratory", "Class Work", "Assignment"),
    val activeTasksCount: Int = 0,
    val departmentsCount: Int = 0,
    val activeInventory: List<ProfessorTaskInventoryUi> = emptyList(),
    val profileImageUri: Uri? = null,
    val isCategoryDropdownExpanded: Boolean = false
)

class ProfessorTasksViewModel(
    private val sessionManager: SessionManager,
    private val repository: ProfessorTasksRepository
) : ViewModel() {
    var uiState by mutableStateOf(ProfessorTasksUiState())
        private set

    init {
        val uriStr = sessionManager.getProfileImageUri()
        val uri = if (uriStr != null) Uri.parse(uriStr) else null
        uiState = uiState.copy(profileImageUri = uri)
        loadTasksSnapshot()
    }

    private fun loadTasksSnapshot() {
        viewModelScope.launch {
            val snapshot = repository.fetchTasksSnapshot()
            uiState = uiState.copy(
                categoryOptions = snapshot.categoryOptions,
                category = snapshot.categoryOptions.firstOrNull() ?: uiState.category,
                activeTasksCount = snapshot.activeTasksCount,
                departmentsCount = snapshot.departmentsCount,
                activeInventory = snapshot.inventory.map { it.toUi() }
            )
        }
    }

    fun onTitleChange(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun onDescriptionChange(desc: String) {
        uiState = uiState.copy(description = desc)
    }

    fun onDeadlineChange(date: String) {
        uiState = uiState.copy(deadlineDate = date)
    }

    fun onCategorySelect(category: String) {
        uiState = uiState.copy(category = category, isCategoryDropdownExpanded = false)
    }

    fun toggleCategoryDropdown() {
        uiState = uiState.copy(isCategoryDropdownExpanded = !uiState.isCategoryDropdownExpanded)
    }

    fun hideCategoryDropdown() {
        uiState = uiState.copy(isCategoryDropdownExpanded = false)
    }

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfessorTasksViewModel(
                        sessionManager = sessionManager,
                        repository = LocalProfessorTasksRepository()
                    ) as T
                }
            }
    }
}

private fun ProfessorTaskInventoryData.toUi(): ProfessorTaskInventoryUi {
    return ProfessorTaskInventoryUi(
        id = id,
        statusLabel = statusLabel,
        priority = priority,
        dueDate = dueDateText,
        title = title,
        enrolledCount = enrolledCount,
        actionText = actionText,
        referencesCount = referencesCount,
        isDraft = isDraft,
        draftHint = draftHint
    )
}

