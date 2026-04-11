package com.example.minorapp.presentation.screen.tasks

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.tasks.LocalTasksRepository
import com.example.minorapp.data.tasks.RemoteTasksSyncResult
import com.example.minorapp.data.tasks.TaskData
import com.example.minorapp.data.tasks.TaskSubmissionSyncResult
import com.example.minorapp.data.tasks.TaskStatus
import com.example.minorapp.data.tasks.TasksRepository
import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TasksFilter(val label: String) {
    ALL("All Tasks"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    CLOSED("Closed")
}

enum class TasksSortOption(val label: String) {
    BY_DEADLINE("by deadline"),
    BY_ISSUED_DATE("by issued date")
}

data class TaskItemUi(
    val id: String,
    val title: String,
    val description: String,
    val status: String, // PENDING, COMPLETED
    val isPriority: Boolean,
    val dateText: String,
    val dueText: String?, // DUE IN 2 DAYS, due tomorrow, 1 week left, etc.
    val isDueSoon: Boolean = false, // If true, make "dueText" red
    val initialDueText: String? = dueText,
    val initialIsDueSoon: Boolean = isDueSoon,
    val deadlineSortKey: Int? = null,
    val issuedSortKey: Int = 0,
    val requiresSubmission: Boolean = true,
    val uploadedPdfUri: String? = null,
    val submissionTimestampText: String? = null,
    val completedBeforeDeadline: Boolean? = null
)

data class TasksUiState(
    val profileImageUri: String? = null,
    val selectedFilter: TasksFilter = TasksFilter.ALL,
    val selectedSortOption: TasksSortOption = TasksSortOption.BY_DEADLINE,
    val isSortMenuExpanded: Boolean = false,
    val selectedPriorityTaskId: String? = null,
    val isPriorityManuallyCleared: Boolean = false,
    val uploadDialogTaskId: String? = null,
    val completionRate: Int = 84,
    val tasks: List<TaskItemUi> = emptyList(),
    val taskHistory: List<TaskHistoryEventUi> = emptyList(),
    val issuedHistoryTaskIds: Set<String> = emptySet(),
    val closedHistoryTaskIds: Set<String> = emptySet(),
    val shouldForceReauth: Boolean = false
) {
    val activeTasks: List<TaskItemUi>
        get() = tasks.filterNot { isTaskExpired(it) }

    val hasPendingTasks: Boolean
        get() = activeTasks.any { it.status == TaskStatus.PENDING.name && !isTaskClosed(it) }

    val priorityTask: TaskItemUi?
        get() {
            val pendingTasks = activeTasks.filter {
                it.status == TaskStatus.PENDING.name && !isTaskClosed(it)
            }
            if (pendingTasks.isEmpty()) return null

            selectedPriorityTaskId?.let { selectedId ->
                pendingTasks.firstOrNull { it.id == selectedId }?.let { return it }
            }

            if (isPriorityManuallyCleared) return null

            return pendingTasks.sortedWith(
                compareBy<TaskItemUi> { it.deadlineSortKey ?: Int.MAX_VALUE }
                    .thenByDescending { it.issuedSortKey }
            ).firstOrNull()
        }

    val activeUploadTask: TaskItemUi?
        get() = activeTasks.firstOrNull { it.id == uploadDialogTaskId }

    fun isTaskExpired(task: TaskItemUi): Boolean {
        val deadline = task.getDeadlineDateOrNull() ?: return false
        return LocalDate.now().isAfter(deadline.plusDays(2))
    }

    fun isTaskClosed(task: TaskItemUi): Boolean {
        val deadline = task.getDeadlineDateOrNull() ?: return false
        val today = LocalDate.now()
        return today.isAfter(deadline) && !isTaskExpired(task)
    }

    fun isClosedSuccess(task: TaskItemUi): Boolean = task.completedBeforeDeadline == true

    val displayedTasks: List<TaskItemUi>
        get() {
            val filtered = when (selectedFilter) {
                TasksFilter.ALL -> activeTasks
                TasksFilter.PENDING -> activeTasks.filter {
                    it.status == TaskStatus.PENDING.name && !isTaskClosed(it)
                }

                TasksFilter.COMPLETED -> activeTasks.filter {
                    it.status == TaskStatus.COMPLETED.name && !isTaskClosed(it)
                }

                TasksFilter.CLOSED -> activeTasks.filter { isTaskClosed(it) }
            }

            return when (selectedSortOption) {
                TasksSortOption.BY_DEADLINE -> filtered.sortedWith(
                    compareBy<TaskItemUi> { it.deadlineSortKey ?: Int.MAX_VALUE }
                        .thenByDescending { it.issuedSortKey }
                )

                TasksSortOption.BY_ISSUED_DATE -> filtered.sortedByDescending { it.issuedSortKey }
            }
        }
}

data class TaskHistoryEventUi(
    val id: String,
    val text: String,
    val eventEpochMillis: Long
)

class TasksViewModel(
    private val repository: TasksRepository = LocalTasksRepository(),
    private val sessionManager: SessionManager
) : ViewModel() {
    var uiState by mutableStateOf(loadInitialUiState())
        private set

    init {
        refreshTasksFromBackend()
    }

    fun onFilterSelected(filter: TasksFilter) {
        uiState = uiState.copy(selectedFilter = filter)
    }

    fun onSortMenuExpandedChange(expanded: Boolean) {
        uiState = uiState.copy(isSortMenuExpanded = expanded)
    }

    fun onSortSelected(sortOption: TasksSortOption) {
        uiState = uiState.copy(
            selectedSortOption = sortOption,
            isSortMenuExpanded = false
        )
    }

    fun onPendingTaskClicked(taskId: String) {
        val task = uiState.tasks.firstOrNull { it.id == taskId } ?: return
        if (uiState.isTaskClosed(task) || uiState.isTaskExpired(task)) return

        uiState = uiState.copy(uploadDialogTaskId = taskId)
    }

    fun onPriorityTaskSelected(taskId: String) {
        val task = uiState.tasks.firstOrNull { it.id == taskId } ?: return
        if (uiState.isTaskClosed(task) || uiState.isTaskExpired(task)) return

        val isPending = task.status == TaskStatus.PENDING.name
        if (!isPending) return

        val updatedState = if (uiState.priorityTask?.id == taskId) {
            // Explicit unmark: keep priority card empty until user marks again.
            uiState.copy(
                selectedPriorityTaskId = null,
                isPriorityManuallyCleared = true
            )
        } else {
            uiState.copy(
                selectedPriorityTaskId = taskId,
                isPriorityManuallyCleared = false
            )
        }

        uiState = updatedState
        persistTaskSubmissionSnapshot(
            tasks = updatedState.tasks,
            selectedPriorityTaskId = updatedState.selectedPriorityTaskId,
            isPriorityManuallyCleared = updatedState.isPriorityManuallyCleared,
            taskHistory = updatedState.taskHistory,
            issuedHistoryTaskIds = updatedState.issuedHistoryTaskIds,
            closedHistoryTaskIds = updatedState.closedHistoryTaskIds
        )
    }

    fun onUploadDialogDismissed() {
        uiState = uiState.copy(uploadDialogTaskId = null)
    }

    fun onDeleteSubmittedPdf(taskId: String) {
        val updatedTasks = uiState.tasks.map { task ->
            if (task.id == taskId && task.uploadedPdfUri != null) {
                task.copy(
                    status = TaskStatus.PENDING.name,
                    dueText = task.initialDueText,
                    isDueSoon = task.initialIsDueSoon,
                    uploadedPdfUri = null,
                    submissionTimestampText = null,
                    completedBeforeDeadline = null
                )
            } else {
                task
            }
        }

        uiState = uiState.copy(
            tasks = updatedTasks,
            completionRate = calculateCompletionRate(updatedTasks)
        )
        persistTaskSubmissionSnapshot(
            tasks = updatedTasks,
            selectedPriorityTaskId = uiState.selectedPriorityTaskId,
            isPriorityManuallyCleared = uiState.isPriorityManuallyCleared,
            taskHistory = uiState.taskHistory,
            issuedHistoryTaskIds = uiState.issuedHistoryTaskIds,
            closedHistoryTaskIds = uiState.closedHistoryTaskIds
        )
    }

    fun onPdfUploaded(pdfUri: String) {
        val activeTaskId = uiState.uploadDialogTaskId ?: return
        val submissionTimestampText = createSubmissionTimestampText()
        val uploadedTask = uiState.tasks.firstOrNull { it.id == activeTaskId }

        val updatedTasks = uiState.tasks.map { task ->
            if (task.id == activeTaskId) {
                task.copy(
                    status = TaskStatus.COMPLETED.name,
                    isDueSoon = false,
                    dueText = null,
                    uploadedPdfUri = pdfUri,
                    submissionTimestampText = submissionTimestampText,
                    completedBeforeDeadline = wasCompletedBeforeDeadline(task)
                )
            } else {
                task
            }
        }
        val updatedSelectedPriorityTaskId =
            if (uiState.selectedPriorityTaskId == activeTaskId) null else uiState.selectedPriorityTaskId
        val updatedIsPriorityManuallyCleared =
            if (uiState.selectedPriorityTaskId == activeTaskId) false else uiState.isPriorityManuallyCleared

        var updatedHistory = uiState.taskHistory
        uploadedTask?.let { task ->
            updatedHistory = appendHistoryEvent(
                updatedHistory,
                TaskHistoryEventUi(
                    id = "upload:${task.id}:${System.currentTimeMillis()}",
                    text = "pdf ${extractPdfName(pdfUri)} uploaded for task ${task.title}",
                    eventEpochMillis = System.currentTimeMillis()
                )
            )
        }
        val closedHistory = appendClosedEventsIfMissing(
            tasks = updatedTasks,
            existingHistory = updatedHistory,
            closedTaskIds = uiState.closedHistoryTaskIds
        )

        uiState = uiState.copy(
            tasks = updatedTasks,
            selectedPriorityTaskId = updatedSelectedPriorityTaskId,
            isPriorityManuallyCleared = updatedIsPriorityManuallyCleared,
            uploadDialogTaskId = null,
            completionRate = calculateCompletionRate(updatedTasks),
            taskHistory = closedHistory.first,
            closedHistoryTaskIds = closedHistory.second
        )
        persistTaskSubmissionSnapshot(
            tasks = updatedTasks,
            selectedPriorityTaskId = updatedSelectedPriorityTaskId,
            isPriorityManuallyCleared = updatedIsPriorityManuallyCleared,
            taskHistory = closedHistory.first,
            issuedHistoryTaskIds = uiState.issuedHistoryTaskIds,
            closedHistoryTaskIds = closedHistory.second
        )

        viewModelScope.launch {
            when (val result = repository.submitTask(sessionManager.getAccessToken(), activeTaskId)) {
                is TaskSubmissionSyncResult.Success -> Unit
                is TaskSubmissionSyncResult.Failure -> {
                    if (isUnauthorizedError(result.message)) {
                        triggerForcedReauth(result.message)
                    }
                }
            }
        }
    }

    fun onForceReauthHandled() {
        uiState = uiState.copy(shouldForceReauth = false)
    }

    fun onDeleteCustomTask(taskId: String) {
        if (!taskId.startsWith("custom-")) return
        if (uiState.tasks.none { it.id == taskId }) return

        val updatedTasks = uiState.tasks.filterNot { it.id == taskId }
        val updatedSelectedPriorityTaskId =
            if (uiState.selectedPriorityTaskId == taskId) null else uiState.selectedPriorityTaskId
        val updatedIsPriorityManuallyCleared =
            if (uiState.selectedPriorityTaskId == taskId) false else uiState.isPriorityManuallyCleared

        val updatedHistory = uiState.taskHistory.filterNot { event ->
            event.id == "issued:$taskId" ||
                event.id == "closed:$taskId" ||
                event.id.startsWith("upload:$taskId:")
        }
        val updatedIssuedIds = uiState.issuedHistoryTaskIds - taskId
        val updatedClosedIds = uiState.closedHistoryTaskIds - taskId

        uiState = uiState.copy(
            tasks = updatedTasks,
            selectedPriorityTaskId = updatedSelectedPriorityTaskId,
            isPriorityManuallyCleared = updatedIsPriorityManuallyCleared,
            uploadDialogTaskId = if (uiState.uploadDialogTaskId == taskId) null else uiState.uploadDialogTaskId,
            completionRate = calculateCompletionRate(updatedTasks),
            taskHistory = updatedHistory,
            issuedHistoryTaskIds = updatedIssuedIds,
            closedHistoryTaskIds = updatedClosedIds
        )

        persistTaskSubmissionSnapshot(
            tasks = updatedTasks,
            selectedPriorityTaskId = updatedSelectedPriorityTaskId,
            isPriorityManuallyCleared = updatedIsPriorityManuallyCleared,
            taskHistory = updatedHistory,
            issuedHistoryTaskIds = updatedIssuedIds,
            closedHistoryTaskIds = updatedClosedIds
        )
    }

    fun onCreateTask(
        title: String,
        description: String,
        deadline: LocalDate,
        submitWork: Boolean
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val now = LocalDate.now()
        val createdTask = TaskItemUi(
            id = "custom-${System.currentTimeMillis()}",
            title = trimmedTitle,
            description = description.trim(),
            status = TaskStatus.PENDING.name,
            isPriority = false,
            dateText = deadline.toDateText(),
            dueText = deadline.toDueText(now),
            isDueSoon = deadline.isDueSoon(now),
            initialDueText = deadline.toDueText(now),
            initialIsDueSoon = deadline.isDueSoon(now),
            deadlineSortKey = deadline.toSortKey(),
            issuedSortKey = now.toSortKey(),
            requiresSubmission = submitWork
        )

        val updatedTasks = (uiState.tasks + createdTask)
        val issuedHistory = appendIssuedEventsIfMissing(
            tasks = listOf(createdTask),
            existingHistory = uiState.taskHistory,
            issuedTaskIds = uiState.issuedHistoryTaskIds
        )

        uiState = uiState.copy(
            tasks = updatedTasks,
            completionRate = calculateCompletionRate(updatedTasks),
            taskHistory = issuedHistory.first,
            issuedHistoryTaskIds = issuedHistory.second,
            isPriorityManuallyCleared = if (uiState.priorityTask == null) false else uiState.isPriorityManuallyCleared
        )

        persistTaskSubmissionSnapshot(
            tasks = updatedTasks,
            selectedPriorityTaskId = uiState.selectedPriorityTaskId,
            isPriorityManuallyCleared = uiState.isPriorityManuallyCleared,
            taskHistory = uiState.taskHistory,
            issuedHistoryTaskIds = uiState.issuedHistoryTaskIds,
            closedHistoryTaskIds = uiState.closedHistoryTaskIds
        )
    }

    private fun calculateCompletionRate(tasks: List<TaskItemUi>): Int {
        val today = LocalDate.now()
        val activeTasks = tasks.filterNot { task ->
            val deadline = task.getDeadlineDateOrNull() ?: return@filterNot false
            today.isAfter(deadline.plusDays(2))
        }
        if (activeTasks.isEmpty()) return 0
        val completedCount = activeTasks.count { it.status == TaskStatus.COMPLETED.name }
        return (completedCount * 100) / activeTasks.size
    }

    private fun TaskData.toUi(): TaskItemUi = TaskItemUi(
        id = id,
        title = title,
        description = description,
        status = status.name,
        isPriority = isPriority,
        dateText = dateText,
        dueText = dueText,
        isDueSoon = isDueSoon,
        initialDueText = dueText,
        initialIsDueSoon = isDueSoon,
        deadlineSortKey = deadlineSortKey,
        issuedSortKey = issuedSortKey,
        requiresSubmission = true,
        uploadedPdfUri = uploadedPdfUri,
        submissionTimestampText = submissionTimestampText,
        completedBeforeDeadline = completedBeforeDeadline
    )

    private fun loadInitialUiState(): TasksUiState {
        val baseTasks = repository.getTasks().map { it.toUi() }
        val snapshot = sessionManager.getTasksSubmissionSnapshot()?.toTasksSubmissionSnapshotOrNull()
        if (snapshot == null) {
            val issuedHistory = appendIssuedEventsIfMissing(
                tasks = baseTasks,
                existingHistory = emptyList(),
                issuedTaskIds = emptySet()
            )
            val closedHistory = appendClosedEventsIfMissing(
                tasks = baseTasks,
                existingHistory = issuedHistory.first,
                closedTaskIds = emptySet()
            )
            return TasksUiState(
                profileImageUri = sessionManager.getProfileImageUri(),
                tasks = baseTasks,
                completionRate = calculateCompletionRate(baseTasks),
                taskHistory = closedHistory.first,
                issuedHistoryTaskIds = issuedHistory.second,
                closedHistoryTaskIds = closedHistory.second
            )
        }

        val mergedTasks = (baseTasks + snapshot.customTasks.map { it.toUi() })
            .distinctBy { it.id }

        val restoredTasks = mergedTasks.map { baseTask ->
            val saved = snapshot.tasks[baseTask.id] ?: return@map baseTask
            val savedStatus = runCatching { TaskStatus.valueOf(saved.status) }.getOrDefault(TaskStatus.PENDING)
            if (savedStatus == TaskStatus.COMPLETED) {
                baseTask.copy(
                    status = TaskStatus.COMPLETED.name,
                    dueText = null,
                    isDueSoon = false,
                    uploadedPdfUri = saved.uploadedPdfUri,
                    submissionTimestampText = saved.submissionTimestampText,
                    completedBeforeDeadline = saved.completedBeforeDeadline
                )
            } else {
                baseTask
            }
        }

        val restoredPriorityTaskId = snapshot.selectedPriorityTaskId
            ?.takeIf { priorityId ->
                restoredTasks.any { it.id == priorityId && it.status == TaskStatus.PENDING.name }
            }
        val restoredIsPriorityManuallyCleared =
            snapshot.isPriorityManuallyCleared && restoredPriorityTaskId == null

        val restoredHistory = snapshot.taskHistory
            .sortedByDescending { it.eventEpochMillis }
            .take(TASK_HISTORY_LIMIT)
        val issuedHistory = appendIssuedEventsIfMissing(
            tasks = restoredTasks,
            existingHistory = restoredHistory,
            issuedTaskIds = snapshot.issuedEventTaskIds
        )
        val closedHistory = appendClosedEventsIfMissing(
            tasks = restoredTasks,
            existingHistory = issuedHistory.first,
            closedTaskIds = snapshot.closedEventTaskIds
        )

        return TasksUiState(
            profileImageUri = sessionManager.getProfileImageUri(),
            tasks = restoredTasks,
            selectedPriorityTaskId = restoredPriorityTaskId,
            isPriorityManuallyCleared = restoredIsPriorityManuallyCleared,
            completionRate = calculateCompletionRate(restoredTasks),
            taskHistory = closedHistory.first,
            issuedHistoryTaskIds = issuedHistory.second,
            closedHistoryTaskIds = closedHistory.second
        )
    }

    private fun persistTaskSubmissionSnapshot(
        tasks: List<TaskItemUi>,
        selectedPriorityTaskId: String?,
        isPriorityManuallyCleared: Boolean,
        taskHistory: List<TaskHistoryEventUi>,
        issuedHistoryTaskIds: Set<String>,
        closedHistoryTaskIds: Set<String>
    ) {
        sessionManager.saveTasksSubmissionSnapshot(
            TaskSubmissionSnapshot(
                selectedPriorityTaskId = selectedPriorityTaskId,
                isPriorityManuallyCleared = isPriorityManuallyCleared,
                taskHistory = taskHistory,
                issuedEventTaskIds = issuedHistoryTaskIds,
                closedEventTaskIds = closedHistoryTaskIds,
                customTasks = tasks.mapNotNull { it.toSnapshotCustomTaskOrNull() },
                tasks = tasks.associateBy(
                    keySelector = { it.id },
                    valueTransform = {
                        TaskSubmissionSnapshotItem(
                            status = it.status,
                            uploadedPdfUri = it.uploadedPdfUri,
                            submissionTimestampText = it.submissionTimestampText,
                            completedBeforeDeadline = it.completedBeforeDeadline
                        )
                    }
                )
            ).toJson()
        )
    }

    private fun wasCompletedBeforeDeadline(task: TaskItemUi): Boolean? {
        val deadline = task.getDeadlineDateOrNull() ?: return null
        return !LocalDate.now().isAfter(deadline)
    }

    private fun createSubmissionTimestampText(nowMillis: Long = System.currentTimeMillis()): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return "Submitted ${formatter.format(Date(nowMillis))}"
    }

    private fun appendIssuedEventsIfMissing(
        tasks: List<TaskItemUi>,
        existingHistory: List<TaskHistoryEventUi>,
        issuedTaskIds: Set<String>
    ): Pair<List<TaskHistoryEventUi>, Set<String>> {
        var history = existingHistory
        val updatedIssuedTaskIds = issuedTaskIds.toMutableSet()

        tasks.forEach { task ->
            if (task.id in updatedIssuedTaskIds) return@forEach
            val issueDateText = task.issuedSortKey.toSortKeyDateTextOrNull() ?: task.dateText
            val issueEpoch = task.issuedSortKey.toEpochMillisOrNull() ?: System.currentTimeMillis()
            history = appendHistoryEvent(
                history,
                TaskHistoryEventUi(
                    id = "issued:${task.id}",
                    text = "task ${task.title} issued on $issueDateText",
                    eventEpochMillis = issueEpoch
                )
            )
            updatedIssuedTaskIds.add(task.id)
        }

        return history to updatedIssuedTaskIds
    }

    private fun appendClosedEventsIfMissing(
        tasks: List<TaskItemUi>,
        existingHistory: List<TaskHistoryEventUi>,
        closedTaskIds: Set<String>
    ): Pair<List<TaskHistoryEventUi>, Set<String>> {
        var history = existingHistory
        val updatedClosedTaskIds = closedTaskIds.toMutableSet()

        tasks.forEach { task ->
            val deadline = task.getDeadlineDateOrNull() ?: return@forEach
            if (!LocalDate.now().isAfter(deadline) || task.id in updatedClosedTaskIds) return@forEach

            val deadlineText = task.deadlineSortKey?.toSortKeyDateTextOrNull() ?: task.dateText
            history = appendHistoryEvent(
                history,
                TaskHistoryEventUi(
                    id = "closed:${task.id}",
                    text = "task ${task.title} closed on $deadlineText",
                    eventEpochMillis = deadline.atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
            )
            updatedClosedTaskIds.add(task.id)
        }

        return history to updatedClosedTaskIds
    }

    private fun appendHistoryEvent(
        existingHistory: List<TaskHistoryEventUi>,
        newEvent: TaskHistoryEventUi
    ): List<TaskHistoryEventUi> {
        val withoutDuplicate = existingHistory.filterNot { it.id == newEvent.id }
        return (listOf(newEvent) + withoutDuplicate)
            .sortedByDescending { it.eventEpochMillis }
            .take(TASK_HISTORY_LIMIT)
    }

    private fun extractPdfName(pdfUri: String): String {
        val lastSegment = Uri.parse(pdfUri).lastPathSegment.orEmpty()
        return lastSegment.substringAfterLast(':').substringAfterLast('/').ifBlank { "file.pdf" }
    }

    private fun refreshTasksFromBackend() {
        viewModelScope.launch {
            when (val result = repository.fetchRemoteTasks(sessionManager.getAccessToken())) {
                is RemoteTasksSyncResult.Success -> {
                    if (result.tasks.isEmpty()) return@launch

                    val localById = uiState.tasks.associateBy { it.id }
                    val remoteTasks = result.tasks.map { serverTask ->
                        val remoteUi = serverTask.toUi()
                        val local = localById[remoteUi.id]
                        if (local != null && local.status == TaskStatus.COMPLETED.name) {
                            remoteUi.copy(
                                status = TaskStatus.COMPLETED.name,
                                dueText = null,
                                isDueSoon = false,
                                uploadedPdfUri = local.uploadedPdfUri,
                                submissionTimestampText = local.submissionTimestampText,
                                completedBeforeDeadline = local.completedBeforeDeadline
                            )
                        } else {
                            remoteUi
                        }
                    }
                    val mergedTasks = (remoteTasks + uiState.tasks.filter { it.id.startsWith("custom-") })
                        .distinctBy { it.id }

                    val issuedHistory = appendIssuedEventsIfMissing(
                        tasks = mergedTasks,
                        existingHistory = uiState.taskHistory,
                        issuedTaskIds = uiState.issuedHistoryTaskIds
                    )
                    val closedHistory = appendClosedEventsIfMissing(
                        tasks = mergedTasks,
                        existingHistory = issuedHistory.first,
                        closedTaskIds = uiState.closedHistoryTaskIds
                    )

                    uiState = uiState.copy(
                        tasks = mergedTasks,
                        completionRate = calculateCompletionRate(mergedTasks),
                        taskHistory = closedHistory.first,
                        issuedHistoryTaskIds = issuedHistory.second,
                        closedHistoryTaskIds = closedHistory.second
                    )

                    persistTaskSubmissionSnapshot(
                        tasks = mergedTasks,
                        selectedPriorityTaskId = uiState.selectedPriorityTaskId,
                        isPriorityManuallyCleared = uiState.isPriorityManuallyCleared,
                        taskHistory = uiState.taskHistory,
                        issuedHistoryTaskIds = uiState.issuedHistoryTaskIds,
                        closedHistoryTaskIds = uiState.closedHistoryTaskIds
                    )
                }

                is RemoteTasksSyncResult.Failure -> {
                    if (isUnauthorizedError(result.message)) {
                        triggerForcedReauth(result.message)
                    }
                }
            }
        }
    }

    private fun triggerForcedReauth(message: String) {
        sessionManager.clearSession()
        uiState = uiState.copy(shouldForceReauth = true)
    }

    private fun isUnauthorizedError(message: String): Boolean {
        val normalized = message.lowercase()
        return normalized.contains("unauthorized") || normalized.contains("session expired") || normalized.contains("login again")
    }

    companion object {
        private const val TASK_HISTORY_LIMIT = 20

        fun factory(
            repository: TasksRepository,
            sessionManager: SessionManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TasksViewModel(repository, sessionManager) as T
                }
            }
    }
}

private data class TaskSubmissionSnapshot(
    val selectedPriorityTaskId: String?,
    val isPriorityManuallyCleared: Boolean,
    val taskHistory: List<TaskHistoryEventUi>,
    val issuedEventTaskIds: Set<String>,
    val closedEventTaskIds: Set<String>,
    val customTasks: List<TaskSnapshotItem>,
    val tasks: Map<String, TaskSubmissionSnapshotItem>
)

private data class TaskSnapshotItem(
    val id: String,
    val title: String,
    val description: String,
    val dateText: String,
    val dueText: String?,
    val isDueSoon: Boolean,
    val initialDueText: String?,
    val initialIsDueSoon: Boolean,
    val deadlineSortKey: Int?,
    val issuedSortKey: Int,
    val requiresSubmission: Boolean
)

private data class TaskSubmissionSnapshotItem(
    val status: String,
    val uploadedPdfUri: String?,
    val submissionTimestampText: String?,
    val completedBeforeDeadline: Boolean?
)

private fun TaskSubmissionSnapshot.toJson(): String {
    val tasksArray = JSONArray()
    tasks.forEach { (taskId, taskSnapshot) ->
        tasksArray.put(
            JSONObject()
                .put("id", taskId)
                .put("status", taskSnapshot.status)
                .put("uploadedPdfUri", taskSnapshot.uploadedPdfUri)
                .put("submissionTimestampText", taskSnapshot.submissionTimestampText)
                .put("completedBeforeDeadline", taskSnapshot.completedBeforeDeadline)
        )
    }

    val historyArray = JSONArray()
    taskHistory.forEach { event ->
        historyArray.put(
            JSONObject()
                .put("id", event.id)
                .put("text", event.text)
                .put("eventEpochMillis", event.eventEpochMillis)
        )
    }

    val issuedTaskIdsArray = JSONArray()
    issuedEventTaskIds.forEach { issuedTaskIdsArray.put(it) }

    val closedTaskIdsArray = JSONArray()
    closedEventTaskIds.forEach { closedTaskIdsArray.put(it) }

    val customTasksArray = JSONArray()
    customTasks.forEach { task ->
        customTasksArray.put(
            JSONObject()
                .put("id", task.id)
                .put("title", task.title)
                .put("description", task.description)
                .put("dateText", task.dateText)
                .put("dueText", task.dueText)
                .put("isDueSoon", task.isDueSoon)
                .put("initialDueText", task.initialDueText)
                .put("initialIsDueSoon", task.initialIsDueSoon)
                .put("deadlineSortKey", task.deadlineSortKey)
                .put("issuedSortKey", task.issuedSortKey)
                .put("requiresSubmission", task.requiresSubmission)
        )
    }

    return JSONObject()
        .put("selectedPriorityTaskId", selectedPriorityTaskId)
        .put("isPriorityManuallyCleared", isPriorityManuallyCleared)
        .put("taskHistory", historyArray)
        .put("issuedEventTaskIds", issuedTaskIdsArray)
        .put("closedEventTaskIds", closedTaskIdsArray)
        .put("customTasks", customTasksArray)
        .put("tasks", tasksArray)
        .toString()
}

private fun String.toTasksSubmissionSnapshotOrNull(): TaskSubmissionSnapshot? {
    return runCatching {
        val rootObject = JSONObject(this)
        val selectedPriorityTaskId = rootObject.optString("selectedPriorityTaskId").ifBlank { null }
        val isPriorityManuallyCleared = rootObject.optBoolean("isPriorityManuallyCleared", false)
        val historyArray = rootObject.optJSONArray("taskHistory") ?: JSONArray()
        val issuedTaskIdsArray = rootObject.optJSONArray("issuedEventTaskIds") ?: JSONArray()
        val closedTaskIdsArray = rootObject.optJSONArray("closedEventTaskIds") ?: JSONArray()
        val customTasksArray = rootObject.optJSONArray("customTasks") ?: JSONArray()
        val tasksArray = rootObject.optJSONArray("tasks") ?: JSONArray()

        val parsedHistory = buildList {
            for (index in 0 until historyArray.length()) {
                val item = historyArray.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val text = item.optString("text")
                if (id.isBlank() || text.isBlank()) continue
                add(
                    TaskHistoryEventUi(
                        id = id,
                        text = text,
                        eventEpochMillis = item.optLong("eventEpochMillis", 0L)
                    )
                )
            }
        }

        val parsedIssuedTaskIds = buildSet {
            for (index in 0 until issuedTaskIdsArray.length()) {
                val value = issuedTaskIdsArray.optString(index)
                if (value.isNotBlank()) add(value)
            }
        }

        val parsedClosedTaskIds = buildSet {
            for (index in 0 until closedTaskIdsArray.length()) {
                val value = closedTaskIdsArray.optString(index)
                if (value.isNotBlank()) add(value)
            }
        }

        val parsedTasks = buildMap {
            for (index in 0 until tasksArray.length()) {
                val item = tasksArray.optJSONObject(index) ?: continue
                val id = item.optString("id")
                if (id.isBlank()) continue
                put(
                    id,
                    TaskSubmissionSnapshotItem(
                        status = item.optString("status", TaskStatus.PENDING.name),
                        uploadedPdfUri = item.optString("uploadedPdfUri").ifBlank { null },
                        submissionTimestampText = item.optString("submissionTimestampText").ifBlank { null },
                        completedBeforeDeadline = if (item.has("completedBeforeDeadline")) {
                            item.optBoolean("completedBeforeDeadline")
                        } else {
                            null
                        }
                    )
                )
            }
        }

        val parsedCustomTasks = buildList {
            for (index in 0 until customTasksArray.length()) {
                val item = customTasksArray.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val title = item.optString("title")
                if (id.isBlank() || title.isBlank()) continue
                add(
                    TaskSnapshotItem(
                        id = id,
                        title = title,
                        description = item.optString("description"),
                        dateText = item.optString("dateText"),
                        dueText = item.optString("dueText").ifBlank { null },
                        isDueSoon = item.optBoolean("isDueSoon", false),
                        initialDueText = item.optString("initialDueText").ifBlank { null },
                        initialIsDueSoon = item.optBoolean("initialIsDueSoon", false),
                        deadlineSortKey = if (item.has("deadlineSortKey")) {
                            item.optInt("deadlineSortKey")
                        } else {
                            null
                        },
                        issuedSortKey = item.optInt("issuedSortKey", 0),
                        requiresSubmission = item.optBoolean("requiresSubmission", true)
                    )
                )
            }
        }

        TaskSubmissionSnapshot(
            selectedPriorityTaskId = selectedPriorityTaskId,
            isPriorityManuallyCleared = isPriorityManuallyCleared,
            taskHistory = parsedHistory,
            issuedEventTaskIds = parsedIssuedTaskIds,
            closedEventTaskIds = parsedClosedTaskIds,
            customTasks = parsedCustomTasks,
            tasks = parsedTasks
        )
    }.recoverCatching {
        // Backward compatibility with older array-only snapshot format.
        val legacyArray = JSONArray(this)
        val parsedTasks = buildMap {
            for (index in 0 until legacyArray.length()) {
                val item = legacyArray.optJSONObject(index) ?: continue
                val id = item.optString("id")
                if (id.isBlank()) continue
                put(
                    id,
                    TaskSubmissionSnapshotItem(
                        status = item.optString("status", TaskStatus.PENDING.name),
                        uploadedPdfUri = item.optString("uploadedPdfUri").ifBlank { null },
                        submissionTimestampText = item.optString("submissionTimestampText").ifBlank { null },
                        completedBeforeDeadline = null
                    )
                )
            }
        }

        TaskSubmissionSnapshot(
            selectedPriorityTaskId = null,
            isPriorityManuallyCleared = false,
            taskHistory = emptyList(),
            issuedEventTaskIds = emptySet(),
            closedEventTaskIds = emptySet(),
            customTasks = emptyList(),
            tasks = parsedTasks
        )
    }.getOrNull()
}

private fun TaskSnapshotItem.toUi(): TaskItemUi = TaskItemUi(
    id = id,
    title = title,
    description = description,
    status = TaskStatus.PENDING.name,
    isPriority = false,
    dateText = dateText,
    dueText = dueText,
    isDueSoon = isDueSoon,
    initialDueText = initialDueText,
    initialIsDueSoon = initialIsDueSoon,
    deadlineSortKey = deadlineSortKey,
    issuedSortKey = issuedSortKey,
    requiresSubmission = requiresSubmission
)

private fun TaskItemUi.toSnapshotCustomTaskOrNull(): TaskSnapshotItem? {
    if (!id.startsWith("custom-")) return null
    return TaskSnapshotItem(
        id = id,
        title = title,
        description = description,
        dateText = dateText,
        dueText = dueText,
        isDueSoon = isDueSoon,
        initialDueText = initialDueText,
        initialIsDueSoon = initialIsDueSoon,
        deadlineSortKey = deadlineSortKey,
        issuedSortKey = issuedSortKey,
        requiresSubmission = requiresSubmission
    )
}

private fun Int.toEpochMillisOrNull(): Long? = toDeadlineDateOrNull()
    ?.atStartOfDay(java.time.ZoneId.systemDefault())
    ?.toInstant()
    ?.toEpochMilli()

private fun Int.toSortKeyDateTextOrNull(): String? = toDeadlineDateOrNull()?.let {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    it.format(formatter).uppercase(Locale.ENGLISH)
}

private fun LocalDate.toSortKey(): Int =
    (year * 10000) + (monthValue * 100) + dayOfMonth

private fun LocalDate.toDateText(): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    return format(formatter).uppercase(Locale.ENGLISH)
}

private fun LocalDate.isDueSoon(now: LocalDate): Boolean {
    val days = java.time.temporal.ChronoUnit.DAYS.between(now, this)
    return days in 0..1
}

private fun LocalDate.toDueText(now: LocalDate): String? {
    val days = java.time.temporal.ChronoUnit.DAYS.between(now, this)
    return when {
        days < 0 -> "OVERDUE"
        days == 0L -> "DUE TODAY"
        days == 1L -> "DUE TOMORROW"
        else -> "DUE IN ${days} DAYS"
    }
}

private fun Int.toDeadlineDateOrNull(): LocalDate? {
    val raw = toString()
    if (raw.length != 8) return null

    val year = raw.substring(0, 4).toIntOrNull() ?: return null
    val month = raw.substring(4, 6).toIntOrNull() ?: return null
    val day = raw.substring(6, 8).toIntOrNull() ?: return null

    return runCatching { LocalDate.of(year, month, day) }.getOrNull()
}

private fun TaskItemUi.getDeadlineDateOrNull(): LocalDate? {
    deadlineSortKey?.toDeadlineDateOrNull()?.let { return it }

    val dateFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMM dd, yyyy")
        .toFormatter(Locale.ENGLISH)

    return runCatching { LocalDate.parse(dateText, dateFormatter) }.getOrNull()
}


