package com.example.minorapp.presentation.screen.tasks

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.tasks.BackendProfessorTasksRepository
import com.example.minorapp.data.tasks.LocalProfessorTasksRepository
import com.example.minorapp.data.tasks.ProfessorClassTargetData
import com.example.minorapp.data.tasks.ProfessorTaskCreateResult
import com.example.minorapp.data.tasks.ProfessorTaskInventoryData
import com.example.minorapp.data.tasks.ProfessorTaskPriority
import com.example.minorapp.data.tasks.ProfessorTasksRepository
import com.example.minorapp.data.tasks.SubmissionChecklistResult
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.constants.AppTimingConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    val classTargets: List<ProfessorClassTargetData> = emptyList(),
    val selectedClassTargetId: Long? = null,
    val selectedChecklistTaskId: String? = null,
    val submissionChecklist: List<SubmissionChecklistItemUi> = emptyList(),
    val profileImageUri: Uri? = null,
    val isCategoryDropdownExpanded: Boolean = false,
    val statusMessage: String? = null
)

data class SubmissionChecklistItemUi(
    val studentName: String,
    val rollNumber: String,
    val submitted: Boolean
)

class ProfessorTasksViewModel(
    private val sessionManager: SessionManager,
    private val repository: ProfessorTasksRepository
) : ViewModel() {
    var uiState by mutableStateOf(ProfessorTasksUiState())
        private set

    private var checklistPollingJob: Job? = null

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
                activeInventory = snapshot.inventory.map { it.toUi() },
                classTargets = snapshot.classTargets,
                selectedClassTargetId = uiState.selectedClassTargetId
                    ?: snapshot.classTargets.firstOrNull()?.id,
                statusMessage = null
            )

            val firstTaskId = uiState.activeInventory.firstOrNull()?.id
            if (firstTaskId != null) {
                onSelectChecklistTask(firstTaskId)
            }
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

    fun onClassTargetSelected(classTargetId: Long) {
        uiState = uiState.copy(selectedClassTargetId = classTargetId)
    }

    fun onDeployAssignment() {
        val classId = uiState.selectedClassTargetId ?: return
        if (uiState.title.isBlank() || uiState.description.isBlank() || uiState.deadlineDate.isBlank()) return

        val isoDeadline = runCatching {
            val parts = uiState.deadlineDate.split("/")
            val month = parts.getOrNull(0)?.toIntOrNull() ?: return@runCatching null
            val day = parts.getOrNull(1)?.toIntOrNull() ?: return@runCatching null
            val year = parts.getOrNull(2)?.toIntOrNull() ?: return@runCatching null
            val fullYear = if (year < 100) 2000 + year else year
            "%04d-%02d-%02d".format(fullYear, month, day)
        }.getOrNull() ?: return

        viewModelScope.launch {
            when (
                val result = repository.createTask(
                    title = uiState.title.trim(),
                    description = uiState.description.trim(),
                    deadlineIsoDate = isoDeadline,
                    classTargetId = classId
                )
            ) {
                is ProfessorTaskCreateResult.Success -> {
                    uiState = uiState.copy(
                        title = "",
                        description = "",
                        deadlineDate = "",
                        statusMessage = "Assignment deployed successfully."
                    )
                    loadTasksSnapshot()
                }

                is ProfessorTaskCreateResult.Failure -> {
                    uiState = uiState.copy(statusMessage = result.message)
                }
            }
        }
    }

    fun onSelectChecklistTask(taskId: String) {
        uiState = uiState.copy(selectedChecklistTaskId = taskId)
        loadChecklistForTask(taskId, fromPolling = false)
    }

    fun startChecklistPolling() {
        if (checklistPollingJob?.isActive == true) return

        checklistPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(AppTimingConstants.PROFESSOR_CHECKLIST_POLL_INTERVAL_MS)
                val taskId = uiState.selectedChecklistTaskId ?: continue
                loadChecklistForTask(taskId, fromPolling = true)
            }
        }
    }

    fun stopChecklistPolling() {
        checklistPollingJob?.cancel()
        checklistPollingJob = null
    }

    override fun onCleared() {
        stopChecklistPolling()
        super.onCleared()
    }

    private fun loadChecklistForTask(taskId: String, fromPolling: Boolean) {
        viewModelScope.launch {
            when (val result = repository.fetchSubmissionChecklist(taskId)) {
                is SubmissionChecklistResult.Success -> {
                    uiState = uiState.copy(
                        submissionChecklist = result.items.map {
                            SubmissionChecklistItemUi(
                                studentName = it.studentName,
                                rollNumber = it.rollNumber,
                                submitted = it.submitted
                            )
                        },
                        statusMessage = if (fromPolling) uiState.statusMessage else null
                    )
                }

                is SubmissionChecklistResult.Failure -> {
                    if (!fromPolling) {
                        uiState = uiState.copy(
                            submissionChecklist = emptyList(),
                            statusMessage = result.message
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfessorTasksViewModel(
                        sessionManager = sessionManager,
                        repository = BackendProfessorTasksRepository(
                            baseUrl = BuildConfig.AUTH_BASE_URL,
                            sessionManager = sessionManager,
                            fallback = LocalProfessorTasksRepository()
                        )
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

