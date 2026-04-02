package com.example.minorapp.presentation.common

import com.example.minorapp.data.session.SessionManager
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

data class SharedAcademicMetrics(
    val attendancePercent: Int,
    val pendingTasks: Int,
    val completedTasks: Int
) {
    val attendanceProgress: Float
        get() = (attendancePercent / 100f).coerceIn(0f, 1f)

    val totalTasks: Int
        get() = pendingTasks + completedTasks

    val taskEfficiencyPercent: Int
        get() = if (totalTasks > 0) {
            ((completedTasks.toFloat() / totalTasks.toFloat()) * 100f).roundToInt()
        } else {
            0
        }
}

object SharedAcademicMetricsResolver {
    private const val DEFAULT_ATTENDANCE_PERCENT = 75
    private const val DEFAULT_PENDING_TASKS = 4
    private const val DEFAULT_COMPLETED_TASKS = 12

    fun fromSession(sessionManager: SessionManager): SharedAcademicMetrics {
        val dashboard = parseDashboardSnapshot(sessionManager.getStudentDashboardSnapshot())
        val semesterAttendancePercent = parseAttendanceFromSemesterInsights(
            sessionManager.getAttendanceSemesterInsightsSnapshot()
        )
        val taskCounts = parseTasksSnapshotCounts(sessionManager.getTasksSubmissionSnapshot())

        val attendancePercent = semesterAttendancePercent
            ?: dashboard?.attendancePercent
            ?: DEFAULT_ATTENDANCE_PERCENT
        val pendingTasks = taskCounts?.pendingTasks
            ?: dashboard?.pendingTasks
            ?: DEFAULT_PENDING_TASKS
        val completedTasks = taskCounts?.completedTasks
            ?: dashboard?.completedTasks
            ?: DEFAULT_COMPLETED_TASKS

        return SharedAcademicMetrics(
            attendancePercent = attendancePercent,
            pendingTasks = pendingTasks.coerceAtLeast(0),
            completedTasks = completedTasks.coerceAtLeast(0)
        )
    }

    private fun parseDashboardSnapshot(snapshot: String?): SharedAcademicMetrics? {
        if (snapshot.isNullOrBlank()) return null

        return runCatching {
            val json = JSONObject(snapshot)
            val attendancePercent = parsePercent(json.optString("attendancePercentageText"))
            val pendingTasks = json.optString("pendingTasksText").toIntOrNull()
            val completedTasks = json.optString("completedTasksText").toIntOrNull()

            if (attendancePercent == null && pendingTasks == null && completedTasks == null) {
                null
            } else {
                SharedAcademicMetrics(
                    attendancePercent = attendancePercent ?: DEFAULT_ATTENDANCE_PERCENT,
                    pendingTasks = pendingTasks ?: DEFAULT_PENDING_TASKS,
                    completedTasks = completedTasks ?: DEFAULT_COMPLETED_TASKS
                )
            }
        }.getOrNull()
    }

    private data class TaskCounts(
        val pendingTasks: Int,
        val completedTasks: Int
    )

    private fun parseTasksSnapshotCounts(snapshot: String?): TaskCounts? {
        if (snapshot.isNullOrBlank()) return null

        return runCatching {
            val trimmed = snapshot.trim()
            val tasksArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                JSONObject(trimmed).optJSONArray("tasks") ?: JSONArray()
            }

            var pending = 0
            var completed = 0
            for (index in 0 until tasksArray.length()) {
                val item = tasksArray.optJSONObject(index) ?: continue
                when (item.optString("status").trim().uppercase()) {
                    "PENDING" -> pending += 1
                    "COMPLETED" -> completed += 1
                }
            }

            if (pending == 0 && completed == 0) null else TaskCounts(pending, completed)
        }.getOrNull()
    }

    private fun parsePercent(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.trim().removeSuffix("%")
        return clean.toFloatOrNull()?.roundToInt()
    }

    private fun parseAttendanceFromSemesterInsights(snapshot: String?): Int? {
        if (snapshot.isNullOrBlank()) return null

        return runCatching {
            val array = JSONArray(snapshot)
            var totalClasses: Int? = null
            var totalPresent: Int? = null

            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val label = item.optString("label").trim().uppercase()
                val numericValue = parseNumericValue(item.optString("value"))

                when {
                    label.contains("TOTAL CLASSES") -> totalClasses = numericValue
                    label.contains("TOTAL PRESENT") -> totalPresent = numericValue
                }
            }

            val classes = totalClasses ?: return@runCatching null
            val present = totalPresent ?: return@runCatching null
            if (classes <= 0) return@runCatching null

            ((present.toFloat() / classes.toFloat()) * 100f)
                .roundToInt()
                .coerceIn(0, 100)
        }.getOrNull()
    }

    private fun parseNumericValue(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val matched = Regex("-?\\d+(?:\\.\\d+)?").find(raw.trim())?.value ?: return null
        return matched.toFloatOrNull()?.roundToInt()
    }
}




