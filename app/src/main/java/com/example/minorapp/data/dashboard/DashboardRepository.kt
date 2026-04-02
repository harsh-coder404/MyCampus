package com.example.minorapp.data.dashboard

import com.example.minorapp.domain.model.AttendanceSummaryData
import com.example.minorapp.domain.model.LectureData
import com.example.minorapp.domain.model.StudentDashboardData
import com.example.minorapp.domain.model.TaskStatusData
import com.example.minorapp.domain.model.WeeklyGoalData
import com.example.minorapp.domain.constants.DummyDataConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class DashboardResult {
    data class Success(val data: StudentDashboardData) : DashboardResult()
    data class Failure(val message: String) : DashboardResult()
}

class DashboardRepository(private val baseUrl: String) {
    suspend fun fetchStudentDashboard(accessToken: String?): DashboardResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext DashboardResult.Failure("Missing access token.")
        }

        var connection: HttpURLConnection? = null

        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}dashboard/student")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            when (val statusCode = connection.responseCode) {
                in 200..299 -> DashboardResult.Success(parseStudentDashboard(connection))
                401 -> DashboardResult.Failure("Session expired. Please login again.")
                else -> DashboardResult.Failure("Unable to load dashboard ($statusCode).")
            }
        } catch (_: IOException) {
            DashboardResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            DashboardResult.Failure("Dashboard service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseStudentDashboard(connection: HttpURLConnection): StudentDashboardData {
        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseBody)
        val data = root.optJSONObject("data") ?: root

        val attendance = data.optJSONObject("attendance")
        val tasks = data.optJSONObject("taskStatus") ?: data.optJSONObject("tasks")
        val weeklyGoal = data.optJSONObject("weeklyGoal")

        return StudentDashboardData(
            displayName = data.optFirstNonBlank("displayName", "username", "name") ?: "Harsh",
            overviewMessage = data.optFirstNonBlank("overviewMessage", "summary", "description")
                ?: "Your academic performance remains stable. You have 3 tasks requiring attention before the weekend.",
            attendance = AttendanceSummaryData(
                percentageText = attendance?.optFirstNonBlank("percentageText", "percentage") ?: "75%",
                thresholdLabel = attendance?.optFirstNonBlank("thresholdLabel", "threshold") ?: "ABOVE\nTHRESHOLD",
                lastUpdatedText = attendance?.optFirstNonBlank("lastUpdatedText", "lastUpdated") ?: "Last updated: Today,\n09:00 AM",
                progress = attendance?.optFloatSafe("progress")?.coerceIn(0f, 1f) ?: 0.75f
            ),
            taskStatus = TaskStatusData(
                pending = tasks?.optIntSafe("pending") ?: 4,
                completed = tasks?.optIntSafe("completed") ?: 12
            ),
            weeklyGoal = WeeklyGoalData(
                title = weeklyGoal?.optFirstNonBlank("title") ?: "WEEKLY GOAL",
                description = weeklyGoal?.optFirstNonBlank("description") ?: "Achieve 90% accuracy in\n${DummyDataConstants.dummySubjects[1]}.",
                tag = weeklyGoal?.optFirstNonBlank("tag", "track") ?: "PREMIUM TRACK"
            ),
            upcomingLectures = parseLectures(data.optJSONArray("upcomingLectures")),
            subjects = parseSubjects(data.optJSONArray("subjects"))
        )
    }

    private fun parseLectures(array: JSONArray?): List<LectureData> {
        if (array == null || array.length() == 0) {
            return listOf(
                LectureData(DummyDataConstants.dummySubjects[0], "Room 302 • 10:30 AM", "PRESENT"),
                LectureData(DummyDataConstants.dummySubjects[1], "Lab B • 12:45 PM", "LATE"),
                LectureData(DummyDataConstants.dummySubjects[2], "Studio 1 • 03:00 PM", "ABSENT")
            )
        }

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    LectureData(
                        title = item.optFirstNonBlank("title") ?: "Lecture",
                        time = item.optFirstNonBlank("time", "schedule") ?: "TBA",
                        status = item.optFirstNonBlank("status") ?: "PRESENT"
                    )
                )
            }
        }.ifEmpty {
            listOf(LectureData("Lecture", "TBA", "PRESENT"))
        }
    }

    private fun parseSubjects(array: JSONArray?): List<String> {
        if (array == null || array.length() == 0) {
            return DummyDataConstants.dummySubjects
        }

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.opt(index)
                when (item) {
                    is String -> if (item.isNotBlank()) add(item)
                    is JSONObject -> {
                        val name = item.optFirstNonBlank("name", "title", "subject")
                        if (!name.isNullOrBlank()) add(name)
                    }
                }
            }
        }.ifEmpty { DummyDataConstants.dummySubjects }
    }

    private fun JSONObject.optFirstNonBlank(vararg keys: String): String? {
        for (key in keys) {
            val value = optString(key).trim()
            if (value.isNotBlank()) return value
        }
        return null
    }

    private fun JSONObject.optIntSafe(key: String): Int? {
        return if (has(key) && !isNull(key)) optInt(key) else null
    }

    private fun JSONObject.optFloatSafe(key: String): Float? {
        return if (has(key) && !isNull(key)) optDouble(key, Double.NaN).toFloat().takeIf { !it.isNaN() } else null
    }
}


