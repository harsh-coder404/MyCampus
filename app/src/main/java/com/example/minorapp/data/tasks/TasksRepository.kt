package com.example.minorapp.data.tasks

enum class TaskStatus {
    PENDING,
    COMPLETED
}

data class TaskData(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val isPriority: Boolean,
    val dateText: String,
    val dueText: String?,
    val isDueSoon: Boolean,
    val deadlineSortKey: Int?,
    val issuedSortKey: Int,
    val uploadedPdfUri: String? = null,
    val submissionTimestampText: String? = null,
    val completedBeforeDeadline: Boolean? = null
)

interface TasksRepository {
    fun getTasks(): List<TaskData>
}

class LocalTasksRepository : TasksRepository {
    override fun getTasks(): List<TaskData> = listOf(
        TaskData(
            id = "task-deadline-mar-30-2026",
            title = "Macroeconomic Quiz 04",
            description = "Dummy test task with deadline on Mar 30, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "MAR 30, 2026",
            dueText = "OVERDUE",
            isDueSoon = false,
            deadlineSortKey = 20260330,
            issuedSortKey = 20260320
        ),
        TaskData(
            id = "task-deadline-apr-01-2026",
            title = "Systems Design Assignment",
            description = "Dummy test task with deadline on Apr 1, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 01, 2026",
            dueText = "DUE TOMORROW",
            isDueSoon = true,
            deadlineSortKey = 20260401,
            issuedSortKey = 20260322
        ),
        TaskData(
            id = "task-deadline-apr-02-2026",
            title = "Digital Signal Processing Lab",
            description = "Dummy test task with deadline on Apr 2, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 02, 2026",
            dueText = "DUE IN 2 DAYS",
            isDueSoon = false,
            deadlineSortKey = 20260402,
            issuedSortKey = 20260324
        ),
        TaskData(
            id = "task-deadline-apr-03-2026",
            title = "Database Migration Report",
            description = "Dummy test task with deadline on Apr 3, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 03, 2026",
            dueText = "DUE IN 3 DAYS",
            isDueSoon = false,
            deadlineSortKey = 20260403,
            issuedSortKey = 20260326
        ),
        TaskData(
            id = "task-deadline-apr-04-2026",
            title = "Applied Statistics Worksheet",
            description = "Dummy test task with deadline on Apr 4, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 04, 2026",
            dueText = "DUE IN 4 DAYS",
            isDueSoon = false,
            deadlineSortKey = 20260404,
            issuedSortKey = 20260327
        )
    )
}

