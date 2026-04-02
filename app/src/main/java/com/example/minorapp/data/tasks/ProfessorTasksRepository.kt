package com.example.minorapp.data.tasks

enum class ProfessorTaskPriority {
    HIGH,
    NORMAL,
    LOW,
    DRAFT
}

data class ProfessorTaskInventoryData(
    val id: String,
    val statusLabel: String,
    val priority: ProfessorTaskPriority,
    val dueDateText: String,
    val title: String,
    val enrolledCount: Int?,
    val actionText: String,
    val referencesCount: Int = 0,
    val isDraft: Boolean = false,
    val draftHint: String = ""
)

data class ProfessorTasksSnapshot(
    val activeTasksCount: Int,
    val departmentsCount: Int,
    val categoryOptions: List<String>,
    val inventory: List<ProfessorTaskInventoryData>
)

interface ProfessorTasksRepository {
    suspend fun fetchTasksSnapshot(): ProfessorTasksSnapshot
}

class LocalProfessorTasksRepository : ProfessorTasksRepository {
    override suspend fun fetchTasksSnapshot(): ProfessorTasksSnapshot {
        return ProfessorTasksSnapshot(
            activeTasksCount = 12,
            departmentsCount = 4,
            categoryOptions = listOf("Laboratory", "Class Work", "Assignment"),
            inventory = listOf(
                ProfessorTaskInventoryData(
                    id = "task-high-1",
                    statusLabel = "HIGH PRIORITY",
                    priority = ProfessorTaskPriority.HIGH,
                    dueDateText = "Due in 2 days",
                    title = "System\nDesign Midterm",
                    enrolledCount = 142,
                    actionText = "Review Submissions ->"
                ),
                ProfessorTaskInventoryData(
                    id = "task-ongoing-1",
                    statusLabel = "ONGOING",
                    priority = ProfessorTaskPriority.NORMAL,
                    dueDateText = "Due in 12 days",
                    title = "Machine Learning\nLab",
                    enrolledCount = 86,
                    actionText = "Edit Details",
                    referencesCount = 3
                ),
                ProfessorTaskInventoryData(
                    id = "task-draft-1",
                    statusLabel = "DRAFT",
                    priority = ProfessorTaskPriority.DRAFT,
                    dueDateText = "Not Published",
                    title = "Ethical Frameworks in AI",
                    enrolledCount = null,
                    actionText = "Resume",
                    isDraft = true,
                    draftHint = "Incomplete description"
                )
            )
        )
    }
}

