package com.example.minorapp.presentation.screen.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.domain.model.ProfessorAssignmentData
import com.example.minorapp.domain.model.ProfessorCohortData
import com.example.minorapp.domain.model.ProfessorSessionData

data class ProfessorDashboardUiState(
    val profileImageUri: String? = null,
    val headerTitle: String = "ScholarMetric",
    val displayName: String = "Prof. Sharma",
    val spotlightTitle: String = "Impact of Visual Aids on Complex Theorem Retention",
    val spotlightMessage: String = "New internal research shows a 12% increase in retention for this cohort using 3D...",
    val spotlightMetric: String = "ACADEMIC SPOTLIGHT",
    val activeCohort: ProfessorCohortData = ProfessorCohortData(
        title = "IV Sem ${DummyDataConstants.dummySubjects[1]}",
        term = "ACTIVE SEMESTER",
        studentsText = "142",
        badgeText = "LIVE"
    ),
    val engagementPercentText: String = "92.4%",
    val engagementDeltaText: String = "2.1%",
    val attendancePercentText: String = "88.7%",
    val attendanceDeltaText: String = "0.4%",
    val todaySessions: List<ProfessorSessionData> = emptyList(),
    val recentAssignments: List<ProfessorAssignmentData> = emptyList()
)

class ProfessorDashboardViewModel(
    sessionManager: SessionManager
) : ViewModel() {

    var uiState by mutableStateOf(initialUiState(sessionManager))
        private set

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfessorDashboardViewModel(sessionManager) as T
                }
            }
        }
    }
}

private fun initialUiState(sessionManager: SessionManager): ProfessorDashboardUiState {
    val displayName = toProfessorDisplayName()

    return ProfessorDashboardUiState(
        profileImageUri = sessionManager.getProfileImageUri(),
        displayName = displayName,
        activeCohort = ProfessorCohortData(
            title = sessionManager.getSavedBranch()?.takeIf { it.isNotBlank() }?.let { "IV Sem $it" } ?: "IV Sem ${DummyDataConstants.dummySubjects[1]}",
            term = "ACTIVE SEMESTER",
            studentsText = "142",
            badgeText = "LIVE"
        ),
        todaySessions = listOf(
            ProfessorSessionData(
                title = "${DummyDataConstants.dummySubjects[1]}\nRecap",
                subtitle = "Lecture Hall B4 • 42 Students",
                durationText = "60m",
                startTimeText = "10:00 AM",
                iconType = "doc"
            ),
            ProfessorSessionData(
                title = "${DummyDataConstants.dummySubjects[3]}\nLab",
                subtitle = "Computing Wing • Section A",
                durationText = "90m",
                startTimeText = "01:30 PM",
                badgeText = "UP NEXT"
            ),
            ProfessorSessionData(
                title = "Post-Grad Seminar",
                subtitle = "Virtual Session • Zoom",
                durationText = "45m",
                startTimeText = "04:00 PM",
                iconType = "video"
            )
        ),
        recentAssignments = listOf(
            ProfessorAssignmentData(
                title = "${DummyDataConstants.dummySubjects[2]} Quiz",
                statusText = "Awaiting manual grading for part B",
                progressText = "120/142",
                progressFraction = 120f / 142f,
                timeAgoText = "2H AGO",
                iconType = "quiz",
                isStatusPositive = false
            ),
            ProfessorAssignmentData(
                title = "${DummyDataConstants.dummySubjects[0]} Lab Report",
                statusText = "Ready to publish grades",
                progressText = "142/142",
                progressFraction = 1f,
                timeAgoText = "YESTERDAY",
                iconType = "lab",
                isStatusPositive = true
            )
        )
    )
}

private fun toProfessorDisplayName(): String {
    return "Prof. Sharma"
}
