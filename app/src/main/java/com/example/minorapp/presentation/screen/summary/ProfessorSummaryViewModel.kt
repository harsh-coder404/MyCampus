package com.example.minorapp.presentation.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.data.summary.LocalProfessorSummaryRepository
import com.example.minorapp.data.summary.PendingTaskData
import com.example.minorapp.data.summary.PerformanceInsightsData
import com.example.minorapp.data.summary.ProfessorSummaryPriority
import com.example.minorapp.data.summary.ProfessorSummaryRepository
import com.example.minorapp.data.summary.ProfessorSummarySnapshot
import com.example.minorapp.data.summary.RecentAttendanceData
import com.example.minorapp.data.summary.SyllabusCoverageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SyllabusCoverageUi(
    val title: String,
    val section: String,
    val percentageText: String,
    val progress: Float
)

data class RecentAttendanceUi(
    val title: String,
    val percentageText: String,
    val highlightRed: Boolean
)

data class PendingTaskUi(
    val tagText: String,
    val priority: ProfessorSummaryPriority,
    val title: String,
    val subtitle: String
)

data class PerformanceInsightsUi(
    val averageGrade: String,
    val engagementText: String,
    val feedbackMessage: String
)

data class ProfessorSummaryUiState(
    val profileImageUri: String? = null,
    val globalEngagement: String = "84%",
    val activeCourses: String = "06",
    val classAttendanceDeltaText: String = "+4% from\nlast week",
    val syllabusCoverage: List<SyllabusCoverageUi> = emptyList(),
    val recentAttendance: List<RecentAttendanceUi> = emptyList(),
    val pendingTasks: List<PendingTaskUi> = emptyList(),
    val performance: PerformanceInsightsUi = PerformanceInsightsUi(
        averageGrade = "B+ (3.4 GPA)",
        engagementText = "Very High",
        feedbackMessage = "\"Student engagement has increased by 15% following the implementation of interactive lab sessions.\""
    )
)

class ProfessorSummaryViewModel(
    private val sessionManager: SessionManager,
    private val repository: ProfessorSummaryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfessorSummaryUiState(profileImageUri = sessionManager.getProfileImageUri())
    )
    val uiState: StateFlow<ProfessorSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val snapshot = repository.fetchSummarySnapshot()
            _uiState.value = snapshot.toUiState(sessionManager.getProfileImageUri())
        }
    }

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfessorSummaryViewModel(
                        sessionManager = sessionManager,
                        repository = LocalProfessorSummaryRepository()
                    ) as T
                }
            }
        }
    }
}

private fun ProfessorSummarySnapshot.toUiState(profileImageUri: String?): ProfessorSummaryUiState {
    return ProfessorSummaryUiState(
        profileImageUri = profileImageUri,
        globalEngagement = globalEngagement,
        activeCourses = activeCourses,
        classAttendanceDeltaText = classAttendanceDeltaText,
        syllabusCoverage = syllabusCoverage.map { it.toUi() },
        recentAttendance = recentAttendance.map { it.toUi() },
        pendingTasks = pendingTasks.map { it.toUi() },
        performance = performance.toUi()
    )
}

private fun SyllabusCoverageData.toUi(): SyllabusCoverageUi =
    SyllabusCoverageUi(title, section, percentageText, progress)

private fun RecentAttendanceData.toUi(): RecentAttendanceUi =
    RecentAttendanceUi(title, percentageText, highlightRed)

private fun PendingTaskData.toUi(): PendingTaskUi =
    PendingTaskUi(tagText, priority, title, subtitle)

private fun PerformanceInsightsData.toUi(): PerformanceInsightsUi =
    PerformanceInsightsUi(averageGrade, engagementText, feedbackMessage)

