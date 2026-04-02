package com.example.minorapp.domain.model

data class ProfessorDashboardData(
    val displayName: String,
    val spotlightTitle: String,
    val spotlightMessage: String,
    val spotlightMetric: String,
    val activeCohort: ProfessorCohortData,
    val engagementPercentText: String,
    val engagementDeltaText: String,
    val attendancePercentText: String,
    val attendanceDeltaText: String,
    val todaySessions: List<ProfessorSessionData>,
    val recentAssignments: List<ProfessorAssignmentData>
)

data class ProfessorCohortData(
    val title: String,
    val term: String,
    val studentsText: String,
    val badgeText: String
)

data class ProfessorSessionData(
    val title: String,
    val subtitle: String,
    val durationText: String,
    val startTimeText: String,
    val badgeText: String? = null,
    val iconType: String? = null
)

data class ProfessorAssignmentData(
    val title: String,
    val statusText: String,
    val progressText: String,
    val progressFraction: Float,
    val timeAgoText: String = "",
    val iconType: String? = null,
    val isStatusPositive: Boolean = false
)
