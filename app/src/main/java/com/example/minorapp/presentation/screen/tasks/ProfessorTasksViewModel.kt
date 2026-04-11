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
import com.example.minorapp.data.tasks.ProfessorTaskDeleteResult
import com.example.minorapp.data.tasks.ProfessorTaskInventoryData
import com.example.minorapp.data.tasks.ProfessorTaskPriority
import com.example.minorapp.data.tasks.ProfessorTaskUpdateResult
import com.example.minorapp.data.tasks.ProfessorTasksRepository
import com.example.minorapp.data.tasks.SubmissionChecklistResult
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.constants.AppTimingConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ProfessorTaskInventoryUi(
    val id: String,
    val classTargetId: Long?,
    val statusLabel: String,
    val priority: ProfessorTaskPriority,
    val dueDate: String,
    val title: String,
    val description: String,
    val enrolledCount: Int?,
    val actionText: String,
    val referencesCount: Int,
    val isDraft: Boolean,
    val draftHint: String,
    val editedTimestampText: String?
)

data class ProfessorTasksUiState(
    val editingTaskId: String? = null,
    val isEditDialogVisible: Boolean = false,
    val title: String = "",
    val description: String = "",
    val deadlineDate: String = "",
    val category: String = "Laboratory",
    val editTitle: String = "",
    val editDescription: String = "",
    val editDeadlineDate: String = "",
    val editCategory: String = "Laboratory",
    val editSelectedClassTargetId: Long? = null,
    val categoryOptions: List<String> = listOf("Laboratory", "Class Work", "Assignment"),
    val activeTasksCount: Int = 0,
    val departmentsCount: Int = 0,
    val activeInventory: List<ProfessorTaskInventoryUi> = emptyList(),
    val classTargets: List<ProfessorClassTargetData> = emptyList(),
    val selectedClassTargetId: Long? = null,
    val selectedChecklistTaskId: String? = null,
    val submissionChecklist: List<SubmissionChecklistItemUi> = emptyList(),
    val pendingDeleteTaskTitle: String? = null,
    val undoSecondsRemaining: Int = 0,
    val isUndoDeleteVisible: Boolean = false,
    val deleteCommitNotice: String? = null,
    val profileImageUri: Uri? = null,
    val isCategoryDropdownExpanded: Boolean = false,
    val isEditCategoryDropdownExpanded: Boolean = false,
    val editStatusMessage: String? = null,
    val showUpdateConfirmationDialog: Boolean = false,
    val updateConfirmationMessage: String? = null,
    val statusMessage: String? = null,
    val shouldForceReauth: Boolean = false
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
    private var pendingDeleteJob: Job? = null
    private var pendingDeleteCommitJob: Job? = null
    private var pendingDeleteTask: ProfessorTaskInventoryUi? = null
    private var pendingDeleteTaskIndex: Int = -1
    private val deleteCommitScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val uriStr = sessionManager.getProfileImageUri()
        val uri = if (uriStr != null) Uri.parse(uriStr) else null
        uiState = uiState.copy(profileImageUri = uri)
        loadTasksSnapshot()
    }

    private fun loadTasksSnapshot() {
        viewModelScope.launch {
            val snapshot = repository.fetchTasksSnapshot()
            val previousSelectedTaskId = uiState.selectedChecklistTaskId
            val mappedInventory = snapshot.inventory.map { it.toUi() }
            val resolvedSelectedTaskId = previousSelectedTaskId
                ?.takeIf { selected -> mappedInventory.any { it.id == selected } }
                ?: mappedInventory.firstOrNull()?.id

            uiState = uiState.copy(
                categoryOptions = snapshot.categoryOptions,
                category = snapshot.categoryOptions.firstOrNull() ?: uiState.category,
                activeTasksCount = snapshot.activeTasksCount,
                departmentsCount = snapshot.departmentsCount,
                activeInventory = mappedInventory,
                classTargets = snapshot.classTargets,
                selectedClassTargetId = uiState.selectedClassTargetId
                    ?: snapshot.classTargets.firstOrNull()?.id,
                selectedChecklistTaskId = resolvedSelectedTaskId,
                submissionChecklist = if (resolvedSelectedTaskId == null) emptyList() else uiState.submissionChecklist,
                statusMessage = null
            )

            if (resolvedSelectedTaskId != null) {
                loadChecklistForTask(resolvedSelectedTaskId, fromPolling = false)
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

        val isoDeadline = parseUiDateToIso(uiState.deadlineDate) ?: return

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
                    if (isUnauthorizedError(result.message)) {
                        triggerForcedReauth(result.message)
                        return@launch
                    }
                    uiState = uiState.copy(statusMessage = result.message)
                }
            }
        }
    }

    fun onEditTask(taskId: String) {
        val task = uiState.activeInventory.firstOrNull { it.id == taskId } ?: return
        val normalizedDeadline = normalizeDueDateForEdit(task.dueDate)

        uiState = uiState.copy(
            editingTaskId = task.id,
            isEditDialogVisible = true,
            editTitle = task.title,
            editDescription = task.description,
            editDeadlineDate = normalizedDeadline,
            editCategory = uiState.category,
            editSelectedClassTargetId = task.classTargetId ?: uiState.selectedClassTargetId,
            isEditCategoryDropdownExpanded = false,
            editStatusMessage = null,
            statusMessage = null
        )
    }

    fun onEditTitleChange(title: String) {
        uiState = uiState.copy(editTitle = title, editStatusMessage = null)
    }

    fun onEditDescriptionChange(description: String) {
        uiState = uiState.copy(editDescription = description, editStatusMessage = null)
    }

    fun onEditDeadlineChange(date: String) {
        uiState = uiState.copy(editDeadlineDate = date, editStatusMessage = null)
    }

    fun onEditCategorySelect(category: String) {
        uiState = uiState.copy(
            editCategory = category,
            isEditCategoryDropdownExpanded = false,
            editStatusMessage = null
        )
    }

    fun toggleEditCategoryDropdown() {
        uiState = uiState.copy(isEditCategoryDropdownExpanded = !uiState.isEditCategoryDropdownExpanded)
    }

    fun hideEditCategoryDropdown() {
        uiState = uiState.copy(isEditCategoryDropdownExpanded = false)
    }

    fun onEditClassTargetSelected(classTargetId: Long) {
        uiState = uiState.copy(editSelectedClassTargetId = classTargetId, editStatusMessage = null)
    }

    fun onSubmitTaskUpdate() {
        val editingTaskId = uiState.editingTaskId ?: return
        if (
            uiState.editTitle.isBlank() ||
            uiState.editDescription.isBlank() ||
            uiState.editDeadlineDate.isBlank() ||
            uiState.editCategory.isBlank() ||
            uiState.editSelectedClassTargetId == null
        ) {
            uiState = uiState.copy(editStatusMessage = "Please fill all update fields.")
            return
        }

        val isoDeadline = parseUiDateToIso(uiState.editDeadlineDate)
        if (isoDeadline == null) {
            uiState = uiState.copy(editStatusMessage = "Invalid deadline format. Pick a valid date.")
            return
        }

        viewModelScope.launch {
            when (
                val result = repository.updateTask(
                    taskId = editingTaskId,
                    title = uiState.editTitle.trim(),
                    description = uiState.editDescription.trim(),
                    deadlineIsoDate = isoDeadline
                )
            ) {
                is ProfessorTaskUpdateResult.Success -> {
                    uiState = uiState.copy(
                        editingTaskId = null,
                        isEditDialogVisible = false,
                        editTitle = "",
                        editDescription = "",
                        editDeadlineDate = "",
                        editCategory = uiState.categoryOptions.firstOrNull() ?: "Laboratory",
                        editSelectedClassTargetId = null,
                        isEditCategoryDropdownExpanded = false,
                        editStatusMessage = null,
                        statusMessage = null,
                        showUpdateConfirmationDialog = true,
                        updateConfirmationMessage = "Task updated successfully."
                    )
                    loadTasksSnapshot()
                }

                is ProfessorTaskUpdateResult.Failure -> {
                    if (isUnauthorizedError(result.message)) {
                        triggerForcedReauth(result.message)
                        return@launch
                    }
                    uiState = uiState.copy(editStatusMessage = result.message, statusMessage = null)
                }
            }
        }
    }

    fun onCancelEditTask() {
        uiState = uiState.copy(
            editingTaskId = null,
            isEditDialogVisible = false,
            editTitle = "",
            editDescription = "",
            editDeadlineDate = "",
            editCategory = uiState.categoryOptions.firstOrNull() ?: "Laboratory",
            editSelectedClassTargetId = null,
            isEditCategoryDropdownExpanded = false,
            editStatusMessage = null,
            statusMessage = null
        )
    }

    fun onUpdateConfirmationDismissed() {
        uiState = uiState.copy(
            showUpdateConfirmationDialog = false,
            updateConfirmationMessage = null
        )
    }

    fun onForceReauthHandled() {
        uiState = uiState.copy(shouldForceReauth = false)
    }

    fun onDeleteCommitNoticeShown() {
        uiState = uiState.copy(deleteCommitNotice = null)
    }

    fun onSelectChecklistTask(taskId: String) {
        uiState = uiState.copy(selectedChecklistTaskId = taskId)
        loadChecklistForTask(taskId, fromPolling = false)
    }

    fun onDeleteTask(taskId: String) {
        val currentIndex = uiState.activeInventory.indexOfFirst { it.id == taskId }
        if (currentIndex == -1) return

        // If a previous pending delete exists, restore it before starting a new one.
        clearPendingDelete(restoreTask = true, statusMessage = null)

        val task = uiState.activeInventory[currentIndex]
        pendingDeleteTask = task
        pendingDeleteTaskIndex = currentIndex

        val updatedInventory = uiState.activeInventory.toMutableList().apply { removeAt(currentIndex) }
        val selectedAfterDelete = uiState.selectedChecklistTaskId.takeIf { it != taskId }
            ?: updatedInventory.firstOrNull()?.id

        uiState = uiState.copy(
            activeInventory = updatedInventory,
            activeTasksCount = (uiState.activeTasksCount - 1).coerceAtLeast(0),
            selectedChecklistTaskId = selectedAfterDelete,
            submissionChecklist = if (uiState.selectedChecklistTaskId == taskId) emptyList() else uiState.submissionChecklist,
            pendingDeleteTaskTitle = task.title,
            undoSecondsRemaining = 5,
            isUndoDeleteVisible = true,
            statusMessage = null
        )

        if (selectedAfterDelete != null && selectedAfterDelete != taskId) {
            loadChecklistForTask(selectedAfterDelete, fromPolling = false)
        }

        pendingDeleteJob?.cancel()
        pendingDeleteJob = viewModelScope.launch {
            var secondsRemaining = 5
            while (secondsRemaining > 0) {
                uiState = uiState.copy(
                    undoSecondsRemaining = secondsRemaining,
                    isUndoDeleteVisible = true
                )
                delay(1_000)
                secondsRemaining--
            }
            uiState = uiState.copy(isUndoDeleteVisible = false, undoSecondsRemaining = 0)
        }

        pendingDeleteCommitJob?.cancel()
        pendingDeleteCommitJob = deleteCommitScope.launch {
            delay(5_000)
            commitPendingDelete()
        }
    }

    fun onUndoDeleteTask() {
        pendingDeleteJob?.cancel()
        pendingDeleteCommitJob?.cancel()
        clearPendingDelete(restoreTask = true, statusMessage = "Task deletion undone.")
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
        pendingDeleteJob?.cancel()
        stopChecklistPolling()
        super.onCleared()
    }

    private suspend fun commitPendingDelete() {
        val taskToDelete = pendingDeleteTask
        if (taskToDelete == null) {
            withContext(Dispatchers.Main) {
                clearPendingDelete(restoreTask = false, statusMessage = null, cancelCommitJob = false)
            }
            return
        }

        when (val result = repository.deleteTask(taskToDelete.id)) {
            is ProfessorTaskDeleteResult.Success -> {
                withContext(Dispatchers.Main) {
                    clearPendingDelete(
                        restoreTask = false,
                        statusMessage = "Task deleted successfully.",
                        cancelCommitJob = false,
                        deleteCommitNotice = "Task permanently deleted."
                    )
                }
            }

            is ProfessorTaskDeleteResult.Failure -> {
                if (isUnauthorizedError(result.message)) {
                    withContext(Dispatchers.Main) {
                        triggerForcedReauth(result.message)
                    }
                    return
                }
                withContext(Dispatchers.Main) {
                    clearPendingDelete(
                        restoreTask = true,
                        statusMessage = result.message,
                        cancelCommitJob = false,
                        deleteCommitNotice = result.message
                    )
                }
            }
        }
    }

    private fun clearPendingDelete(
        restoreTask: Boolean,
        statusMessage: String?,
        cancelCommitJob: Boolean = true,
        deleteCommitNotice: String? = null
    ) {
        val pendingTask = pendingDeleteTask
        var inventory = uiState.activeInventory
        var activeTasksCount = uiState.activeTasksCount

        if (restoreTask && pendingTask != null) {
            val insertIndex = pendingDeleteTaskIndex.coerceIn(0, inventory.size)
            inventory = inventory.toMutableList().apply { add(insertIndex, pendingTask) }
            activeTasksCount += 1
        }

        val selectedTaskId = uiState.selectedChecklistTaskId
            ?.takeIf { selected -> inventory.any { it.id == selected } }
            ?: inventory.firstOrNull()?.id

        pendingDeleteTask = null
        pendingDeleteTaskIndex = -1
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        if (cancelCommitJob) {
            pendingDeleteCommitJob?.cancel()
        }
        pendingDeleteCommitJob = null

        uiState = uiState.copy(
            activeInventory = inventory,
            activeTasksCount = activeTasksCount,
            selectedChecklistTaskId = selectedTaskId,
            submissionChecklist = if (selectedTaskId == null) emptyList() else uiState.submissionChecklist,
            pendingDeleteTaskTitle = null,
            undoSecondsRemaining = 0,
            isUndoDeleteVisible = false,
            statusMessage = statusMessage,
            deleteCommitNotice = deleteCommitNotice
        )

        if (restoreTask && selectedTaskId != null) {
            loadChecklistForTask(selectedTaskId, fromPolling = false)
        }
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
                    if (isUnauthorizedError(result.message)) {
                        triggerForcedReauth(result.message)
                        return@launch
                    }
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

    private fun triggerForcedReauth(message: String) {
        sessionManager.clearSession()
        stopChecklistPolling()
        uiState = uiState.copy(
            statusMessage = message,
            shouldForceReauth = true
        )
    }

    private fun isUnauthorizedError(message: String): Boolean {
        val normalized = message.lowercase()
        return normalized.contains("unauthorized") || normalized.contains("session expired") || normalized.contains("login again")
    }

    private fun parseUiDateToIso(uiDate: String): String? {
        val value = uiDate.trim()
        if (value.isBlank()) return null

        // Already ISO date.
        runCatching {
            return LocalDate.parse(value).toString()
        }

        // UI date format (MM/dd/yy).
        return runCatching {
            val parts = value.split("/")
            val month = parts.getOrNull(0)?.toIntOrNull() ?: return@runCatching null
            val day = parts.getOrNull(1)?.toIntOrNull() ?: return@runCatching null
            val year = parts.getOrNull(2)?.toIntOrNull() ?: return@runCatching null
            val fullYear = if (year < 100) 2000 + year else year
            LocalDate.of(fullYear, month, day).toString()
        }.getOrNull()
    }

    private fun normalizeDueDateForEdit(dueDateText: String): String {
        val raw = dueDateText.removePrefix("Due ").trim()
        if (raw.equals("TBA", ignoreCase = true) || raw.equals("Not Published", ignoreCase = true)) {
            return ""
        }

        // Convert ISO/server date to the MM/dd/yy value expected by the edit field.
        runCatching {
            val parsed = LocalDate.parse(raw)
            return parsed.format(DateTimeFormatter.ofPattern("MM/dd/yy", Locale.US))
        }

        // If already in UI format, keep it unchanged.
        return raw
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
        classTargetId = classTargetId,
        statusLabel = statusLabel,
        priority = priority,
        dueDate = dueDateText,
        title = title,
        description = description,
        enrolledCount = enrolledCount,
        actionText = actionText,
        referencesCount = referencesCount,
        isDraft = isDraft,
        draftHint = draftHint,
        editedTimestampText = editedTimestampText
    )
}

