package com.example.minorapp.data.attendance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AttendanceInsightStatData(
    val label: String,
    val value: String
)

sealed class AttendanceInsightsResult {
    data class Success(val stats: List<AttendanceInsightStatData>) : AttendanceInsightsResult()
    data class Failure(val message: String) : AttendanceInsightsResult()
}

class AttendanceRepository(private val baseUrl: String) {
    suspend fun fetchMonthlyInsights(accessToken: String?): AttendanceInsightsResult =
        fetchInsights(accessToken, AttendanceConfig.ATTENDANCE_MONTHLY_INSIGHTS_PATH)

    suspend fun fetchSemesterInsights(accessToken: String?): AttendanceInsightsResult =
        fetchInsights(accessToken, AttendanceConfig.ATTENDANCE_SEMESTER_INSIGHTS_PATH)

    private suspend fun fetchInsights(
        accessToken: String?,
        endpointPath: String
    ): AttendanceInsightsResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext AttendanceInsightsResult.Failure("Missing access token.")
        }

        var connection: HttpURLConnection? = null

        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("$normalizedBaseUrl$endpointPath")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            when (val statusCode = connection.responseCode) {
                in 200..299 -> parseInsights(connection)
                401, 403 -> AttendanceInsightsResult.Failure("Session expired. Please login again.")
                else -> AttendanceInsightsResult.Failure("Unable to load attendance insights ($statusCode).")
            }
        } catch (_: IOException) {
            AttendanceInsightsResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            AttendanceInsightsResult.Failure("Attendance service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseInsights(connection: HttpURLConnection): AttendanceInsightsResult {
        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseBody)

        if (root.has("status") && !root.isNull("status")) {
            val status = root.optString("status")
            if (status != AttendanceConfig.SUCCESS_STATUS) {
                val message = root.optString("message").ifBlank { "Unable to load attendance insights." }
                return AttendanceInsightsResult.Failure(message)
            }
        }

        val data = root.optJSONObject("data") ?: root
        val statsArray = data.optJSONArray("summaryStats") ?: data.optJSONArray("stats")

        val parsed = when {
            statsArray != null -> parseStatsArray(statsArray)
            else -> parseStatsFromObject(data)
        }

        return if (parsed.isNotEmpty()) {
            AttendanceInsightsResult.Success(parsed)
        } else {
            AttendanceInsightsResult.Failure("Malformed attendance insights response.")
        }
    }

    private fun parseStatsArray(array: JSONArray): List<AttendanceInsightStatData> {
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val label = item.optFirstNonBlank("label", "name", "title") ?: continue
                val value = item.optValueAsText("value", "count", "total") ?: continue
                add(AttendanceInsightStatData(label = label, value = value))
            }
        }
    }

    private fun parseStatsFromObject(data: JSONObject): List<AttendanceInsightStatData> {
        val totalClasses = data.optValueAsText("totalClasses", "classes", "total")
        val totalPresent = data.optValueAsText("totalPresent", "present")
        val excusedAbsences = data.optValueAsText("excusedAbsences", "excused")

        return buildList {
            if (!totalClasses.isNullOrBlank()) add(AttendanceInsightStatData("TOTAL CLASSES", totalClasses))
            if (!totalPresent.isNullOrBlank()) add(AttendanceInsightStatData("TOTAL PRESENT", totalPresent))
            if (!excusedAbsences.isNullOrBlank()) add(AttendanceInsightStatData("EXCUSED ABSENCES", excusedAbsences))
        }
    }

    private fun JSONObject.optFirstNonBlank(vararg keys: String): String? {
        for (key in keys) {
            val value = optString(key).trim()
            if (value.isNotBlank()) return value
        }
        return null
    }

    private fun JSONObject.optValueAsText(vararg keys: String): String? {
        for (key in keys) {
            if (!has(key) || isNull(key)) continue
            val value = opt(key)
            when (value) {
                is Number -> return value.toString()
                is String -> {
                    val trimmed = value.trim()
                    if (trimmed.isNotBlank()) return trimmed
                }
            }
        }
        return null
    }
}

