package com.example.minorapp.domain.model

data class StudentDashboardData(
    val displayName: String,
    val overviewMessage: String,
    val attendance: AttendanceSummaryData,
    val taskStatus: TaskStatusData,
    val weeklyGoal: WeeklyGoalData,
    val upcomingLectures: List<LectureData>,
    val subjects: List<String>
)

data class AttendanceSummaryData(
    val percentageText: String,
    val thresholdLabel: String,
    val lastUpdatedText: String,
    val progress: Float
)

data class TaskStatusData(
    val pending: Int,
    val completed: Int
)

data class WeeklyGoalData(
    val title: String,
    val description: String,
    val tag: String
)

data class LectureData(
    val title: String,
    val time: String,
    val status: String
)

