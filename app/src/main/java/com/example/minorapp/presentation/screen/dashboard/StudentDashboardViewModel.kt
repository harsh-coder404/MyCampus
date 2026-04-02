package com.example.minorapp.presentation.screen.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.dashboard.DashboardRepository
import com.example.minorapp.data.dashboard.DashboardResult
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.domain.model.UserRole
import com.example.minorapp.presentation.common.SharedAcademicMetricsResolver
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class StudentDashboardLectureUi(
    val title: String,
    val time: String,
    val status: String
)

data class StudentDashboardUiState(
    val profileImageUri: String? = null,
    val displayName: String = "Harsh",
    val overviewMessage: String = "Your academic performance remains stable. You have 3 tasks requiring attention before the weekend.",
    val attendancePercentageText: String = "75%",
    val attendanceThresholdLabel: String = "ABOVE\nTHRESHOLD",
    val attendanceLastUpdatedText: String = "Last updated: Today,\n09:00 AM",
    val attendanceProgress: Float = 0.75f,
    val pendingTasksText: String = "04",
    val completedTasksText: String = "12",
    val weeklyGoalTitle: String = "WEEKLY GOAL",
    val weeklyGoalDescription: String = "Achieve 90% accuracy in\n${DummyDataConstants.dummySubjects[1]}.",
    val weeklyGoalTag: String = "PREMIUM TRACK",
    val subjects: List<String> = DummyDataConstants.dummySubjects,
    val lectures: List<StudentDashboardLectureUi> = listOf(
        StudentDashboardLectureUi(DummyDataConstants.dummySubjects[0], "Room 302 • 10:30 AM", "PRESENT"),
        StudentDashboardLectureUi(DummyDataConstants.dummySubjects[1], "Lab B • 12:45 PM", "LATE"),
        StudentDashboardLectureUi(DummyDataConstants.dummySubjects[2], "Studio 1 • 03:00 PM", "ABSENT")
    ),
    val errorMessage: String? = null
)

class StudentDashboardViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {
    var uiState by mutableStateOf(initialUiState(sessionManager))
        private set

    init {
        refreshDashboard()
    }

    companion object {
        fun factory(
            sessionManager: SessionManager,
            dashboardRepository: DashboardRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentDashboardViewModel(sessionManager, dashboardRepository) as T
                }
            }
        }
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            when (val result = dashboardRepository.fetchStudentDashboard(sessionManager.getAccessToken())) {
                is DashboardResult.Success -> {
                    val data = result.data
                    uiState = uiState.copy(
                        displayName = resolveStudentDisplayName(sessionManager, data.displayName),
                        overviewMessage = data.overviewMessage,
                        attendancePercentageText = data.attendance.percentageText,
                        attendanceThresholdLabel = data.attendance.thresholdLabel,
                        attendanceLastUpdatedText = data.attendance.lastUpdatedText,
                        attendanceProgress = data.attendance.progress,
                        pendingTasksText = data.taskStatus.pending.toString().padStart(2, '0'),
                        completedTasksText = data.taskStatus.completed.toString().padStart(2, '0'),
                        weeklyGoalTitle = data.weeklyGoal.title,
                        weeklyGoalDescription = data.weeklyGoal.description,
                        weeklyGoalTag = data.weeklyGoal.tag,
                        subjects = data.subjects,
                        lectures = data.upcomingLectures.map {
                            StudentDashboardLectureUi(
                                title = it.title,
                                time = it.time,
                                status = it.status
                            )
                        },
                        errorMessage = null
                    )
                    sessionManager.saveStudentDashboardSnapshot(uiState.toSnapshotJson())
                    uiState = uiState.withSharedMetrics(sessionManager)
                    sessionManager.saveStudentDashboardSnapshot(uiState.toSnapshotJson())
                }

                is DashboardResult.Failure -> {
                    val cached = sessionManager.getStudentDashboardSnapshot()?.toDashboardUiStateOrNull()
                    if (cached != null) {
                        uiState = cached.copy(
                            profileImageUri = sessionManager.getProfileImageUri(),
                            displayName = resolveStudentDisplayName(sessionManager, cached.displayName),
                            errorMessage = result.message
                        )
                        uiState = uiState.withSharedMetrics(sessionManager)
                    } else {
                        uiState = uiState.copy(errorMessage = result.message)
                    }
                }
            }
        }
    }
}

private fun initialUiState(sessionManager: SessionManager): StudentDashboardUiState {
    val cached = sessionManager.getStudentDashboardSnapshot()?.toDashboardUiStateOrNull()
    if (cached != null) {
        return cached.copy(
            profileImageUri = sessionManager.getProfileImageUri(),
            displayName = resolveStudentDisplayName(sessionManager, cached.displayName),
            errorMessage = null
        ).withSharedMetrics(sessionManager)
    }

    return StudentDashboardUiState(
        profileImageUri = sessionManager.getProfileImageUri(),
        displayName = resolveStudentDisplayName(sessionManager)
    ).withSharedMetrics(sessionManager)
}

private fun resolveStudentDisplayName(sessionManager: SessionManager, backendValue: String? = null): String {
    val studentSavedName = sessionManager.getSavedUsernameForRole(UserRole.STUDENT)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
    val backendName = backendValue
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.takeUnless { it.equals("Sharma", ignoreCase = true) || it.startsWith("Prof", ignoreCase = true) }

    return studentSavedName ?: backendName ?: "Harsh"
}

private fun StudentDashboardUiState.withSharedMetrics(sessionManager: SessionManager): StudentDashboardUiState {
    val metrics = SharedAcademicMetricsResolver.fromSession(sessionManager)
    return copy(
        attendancePercentageText = "${metrics.attendancePercent}%",
        attendanceProgress = metrics.attendanceProgress,
        pendingTasksText = metrics.pendingTasks.toString().padStart(2, '0'),
        completedTasksText = metrics.completedTasks.toString().padStart(2, '0')
    )
}

private fun StudentDashboardUiState.toSnapshotJson(): String {
    val json = JSONObject()
        .put("displayName", displayName)
        .put("overviewMessage", overviewMessage)
        .put("attendancePercentageText", attendancePercentageText)
        .put("attendanceThresholdLabel", attendanceThresholdLabel)
        .put("attendanceLastUpdatedText", attendanceLastUpdatedText)
        .put("attendanceProgress", attendanceProgress.toDouble())
        .put("pendingTasksText", pendingTasksText)
        .put("completedTasksText", completedTasksText)
        .put("weeklyGoalTitle", weeklyGoalTitle)
        .put("weeklyGoalDescription", weeklyGoalDescription)
        .put("weeklyGoalTag", weeklyGoalTag)

    val subjectsArray = JSONArray()
    subjects.forEach { subjectsArray.put(it) }
    json.put("subjects", subjectsArray)

    val lecturesArray = JSONArray()
    lectures.forEach { lecture ->
        lecturesArray.put(
            JSONObject()
                .put("title", lecture.title)
                .put("time", lecture.time)
                .put("status", lecture.status)
        )
    }
    json.put("lectures", lecturesArray)

    return json.toString()
}

private fun String.toDashboardUiStateOrNull(): StudentDashboardUiState? {
    return runCatching {
        val json = JSONObject(this)

        val subjects = mutableListOf<String>()
        val subjectsArray = json.optJSONArray("subjects") ?: JSONArray()
        for (i in 0 until subjectsArray.length()) {
            val value = subjectsArray.optString(i)
            if (value.isNotBlank()) subjects.add(value)
        }

        val lectures = mutableListOf<StudentDashboardLectureUi>()
        val lecturesArray = json.optJSONArray("lectures") ?: JSONArray()
        for (i in 0 until lecturesArray.length()) {
            val lecture = lecturesArray.optJSONObject(i) ?: continue
            lectures.add(
                StudentDashboardLectureUi(
                    title = lecture.optString("title", "Lecture"),
                    time = lecture.optString("time", "TBA"),
                    status = lecture.optString("status", "PRESENT")
                )
            )
        }

        StudentDashboardUiState(
            displayName = json.optString("displayName", "Harsh"),
            overviewMessage = json.optString(
                "overviewMessage",
                "Your academic performance remains stable. You have 3 tasks requiring attention before the weekend."
            ),
            attendancePercentageText = json.optString("attendancePercentageText", "75%"),
            attendanceThresholdLabel = json.optString("attendanceThresholdLabel", "ABOVE\nTHRESHOLD"),
            attendanceLastUpdatedText = json.optString("attendanceLastUpdatedText", "Last updated: Today,\n09:00 AM"),
            attendanceProgress = json.optDouble("attendanceProgress", 0.75).toFloat(),
            pendingTasksText = json.optString("pendingTasksText", "04"),
            completedTasksText = json.optString("completedTasksText", "12"),
            weeklyGoalTitle = json.optString("weeklyGoalTitle", "WEEKLY GOAL"),
            weeklyGoalDescription = json.optString("weeklyGoalDescription", "Achieve 90% accuracy in\n${DummyDataConstants.dummySubjects[1]}."),
            weeklyGoalTag = json.optString("weeklyGoalTag", "PREMIUM TRACK"),
            subjects = if (subjects.isNotEmpty()) subjects else DummyDataConstants.dummySubjects,
            lectures = if (lectures.isNotEmpty()) lectures else listOf(
                StudentDashboardLectureUi(DummyDataConstants.dummySubjects[0], "Room 302 • 10:30 AM", "PRESENT"),
                StudentDashboardLectureUi(DummyDataConstants.dummySubjects[1], "Lab B • 12:45 PM", "LATE"),
                StudentDashboardLectureUi(DummyDataConstants.dummySubjects[2], "Studio 1 • 03:00 PM", "ABSENT")
            )
        )
    }.getOrNull()
}
