package com.example.minorapp.presentation.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.SharedAcademicMetricsResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScholarPersona(
    val title: String = "The Editorial Scholar.",
    val description: String = "An authoritative synthesis of your current academic trajectory, attendance integrity, and upcoming research milestones."
)

data class TaskCompletionCategory(
    val name: String,
    val completed: Int,
    val total: Int,
    val colorHex: Long
) {
    val progress: Float get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f
}

data class QuickStat(
    val value: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val iconColorHex: Long = 0xFF64748B
)

data class ImmediateDeadline(
    val title: String,
    val timeLabel: String,
    val isUrgent: Boolean
)

data class SemesterMomentumUi(
    val label: String,
    val gpa: Float?,
    val isCurrent: Boolean = false
) {
    val barRatio: Float
        get() = ((gpa ?: 0f) / 10f).coerceIn(0f, 1f)

    val barColorHex: Long
        get() = when {
            gpa == null -> 0x00000000
            gpa >= 8.0f -> 0xFF16A34A
            gpa > 6.5f -> 0xFFF59E0B
            else -> 0xFFDC2626
        }
}

data class SummaryUiState(
    val profileImageUri: String? = null,
    val persona: ScholarPersona = ScholarPersona(),
    val attendancePercentage: Int = 94,
    val taskEfficiencyRate: Int = 80,
    val taskCategories: List<TaskCompletionCategory> = listOf(
        TaskCompletionCategory("PENDING", 4, 16, 0xFF9A3412),
        TaskCompletionCategory("COMPLETED", 12, 16, 0xFF1E3A8A)
    ),
    val activeModules: String = "6",
    val studyHoursWeek: String = "32.5",
    val predictedGpa: String = "7.88",
    val upcomingDeadlinesCount: String = "4",
    val immediateDeadlines: List<ImmediateDeadline> = listOf(
        ImmediateDeadline("${DummyDataConstants.dummySubjects[1]} thesis", "TOMORROW • 11:59 PM", true),
        ImmediateDeadline("${DummyDataConstants.dummySubjects[2]} paper", "FRIDAY • 5:00 PM", false)
    ),
    val historicalMomentum: List<SemesterMomentumUi> = listOf(
        SemesterMomentumUi(label = "SEM 01", gpa = 8.1f),
        SemesterMomentumUi(label = "SEM 02", gpa = 7.4f),
        SemesterMomentumUi(label = "SEM 03", gpa = 6.2f),
        SemesterMomentumUi(label = "CURRENT", gpa = null, isCurrent = true)
    ),
    val academicTip: String = "\"Quality is not an act, it is a habit. Maintain your current study momentum for high honors.\""
)

class SummaryViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummaryData()
    }

    private fun loadSummaryData() {
        val metrics = SharedAcademicMetricsResolver.fromSession(sessionManager)
        val totalTasks = metrics.totalTasks.coerceAtLeast(1)
        // Fetch from repos later, currently static mock data mapping the wireframe
        _uiState.value = SummaryUiState(
            profileImageUri = sessionManager.getProfileImageUri(),
            attendancePercentage = metrics.attendancePercent,
            taskEfficiencyRate = metrics.taskEfficiencyPercent,
            taskCategories = listOf(
                TaskCompletionCategory("PENDING", metrics.pendingTasks, totalTasks, 0xFF9A3412),
                TaskCompletionCategory("COMPLETED", metrics.completedTasks, totalTasks, 0xFF1E3A8A)
            )
        )
    }

    companion object {
        fun factory(
            sessionManager: SessionManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
                    return SummaryViewModel(sessionManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
