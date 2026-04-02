package com.example.minorapp.data.summary

enum class ProfessorSummaryPriority {
    URGENT,
    NORMAL,
    LOW
}

data class SyllabusCoverageData(
    val title: String,
    val section: String,
    val percentageText: String,
    val progress: Float
)

data class RecentAttendanceData(
    val title: String,
    val percentageText: String,
    val highlightRed: Boolean
)

data class PendingTaskData(
    val tagText: String,
    val priority: ProfessorSummaryPriority,
    val title: String,
    val subtitle: String
)

data class PerformanceInsightsData(
    val averageGrade: String,
    val engagementText: String,
    val feedbackMessage: String
)

data class ProfessorSummarySnapshot(
    val globalEngagement: String,
    val activeCourses: String,
    val classAttendanceDeltaText: String,
    val syllabusCoverage: List<SyllabusCoverageData>,
    val recentAttendance: List<RecentAttendanceData>,
    val pendingTasks: List<PendingTaskData>,
    val performance: PerformanceInsightsData
)

interface ProfessorSummaryRepository {
    suspend fun fetchSummarySnapshot(): ProfessorSummarySnapshot
}

class LocalProfessorSummaryRepository : ProfessorSummaryRepository {
    override suspend fun fetchSummarySnapshot(): ProfessorSummarySnapshot {
        return ProfessorSummarySnapshot(
            globalEngagement = "84%",
            activeCourses = "06",
            classAttendanceDeltaText = "+4% from\nlast week",
            syllabusCoverage = listOf(
                SyllabusCoverageData("Advanced Architecture III", "Sem V CSE A", "65%", 0.65f),
                SyllabusCoverageData("Digital Anthropology 101", "SEM III DSA", "40%", 0.40f),
                SyllabusCoverageData("Urban Planning Lab", "Sem IV IT", "82%", 0.82f)
            ),
            recentAttendance = listOf(
                RecentAttendanceData("MON - Sem V CSE A", "92%", false),
                RecentAttendanceData("TUE - SEM III DSA", "88%", false),
                RecentAttendanceData("WED - Sem IV IT", "95%", false),
                RecentAttendanceData("THU - SEM I IT", "76%", true)
            ),
            pendingTasks = listOf(
                PendingTaskData("URGENT", ProfessorSummaryPriority.URGENT, "Grade Final Thesis Drafts", "Architecture IV • 12 submissions"),
                PendingTaskData("NORMAL", ProfessorSummaryPriority.NORMAL, "Upload Syllabus Update", "Anthropology • Due Tomorrow"),
                PendingTaskData("LOW", ProfessorSummaryPriority.LOW, "Review Guest Lecture List", "Planning Dept • 4 days left")
            ),
            performance = PerformanceInsightsData(
                averageGrade = "B+ (3.4 GPA)",
                engagementText = "Very High",
                feedbackMessage = "\"Student engagement has increased by 15% following the implementation of interactive lab sessions.\""
            )
        )
    }
}

