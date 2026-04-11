package com.example.minorapp.data.attendance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AttendanceQrSessionData(
    val courseId: Long,
    val sessionId: String,
    val timestamp: Long,
    val expiresAtEpochSec: Long,
    val qrPayload: String
)

data class FinalizedAttendanceStudentData(
    val studentId: String,
    val name: String,
    val details: String,
    val status: String
)

data class FinalizedAttendanceSessionData(
    val sessionId: String,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int,
    val students: List<FinalizedAttendanceStudentData>
)

sealed class AttendanceQrResult {
    data class Success(val message: String) : AttendanceQrResult()
    data class Failure(val message: String) : AttendanceQrResult()
}

sealed class StartAttendanceQrSessionResult {
    data class Success(val session: AttendanceQrSessionData) : StartAttendanceQrSessionResult()
    data class Failure(val message: String) : StartAttendanceQrSessionResult()
}

sealed class FinalizeAttendanceQrSessionResult {
    data class Success(val data: FinalizedAttendanceSessionData, val message: String) : FinalizeAttendanceQrSessionResult()
    data class Failure(val message: String) : FinalizeAttendanceQrSessionResult()
}

class QrAttendanceRepository(private val baseUrl: String) {

    suspend fun startAttendanceSession(
        accessToken: String?,
        courseId: Long,
        ttlSeconds: Int = 90
    ): StartAttendanceQrSessionResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext StartAttendanceQrSessionResult.Failure("Missing access token.")
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = URL("${normalizedBaseUrl()}${AttendanceConfig.ATTENDANCE_START_SESSION_PATH}")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val body = JSONObject()
                .put("courseId", courseId)
                .put("ttlSeconds", ttlSeconds)
                .toString()
            connection.outputStream.use { it.write(body.toByteArray()) }

            val code = connection.responseCode
            val response = connection.readResponseBody(code)
            parseStartSessionResponse(code, response)
        } catch (_: IOException) {
            StartAttendanceQrSessionResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            StartAttendanceQrSessionResult.Failure("Unable to start attendance session.")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun markAttendanceWithQr(
        accessToken: String?,
        courseId: Long,
        sessionId: String,
        timestamp: Long,
        studentId: String?,
        deviceId: String?
    ): AttendanceQrResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext AttendanceQrResult.Failure("Missing access token.")
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = URL("${normalizedBaseUrl()}${AttendanceConfig.ATTENDANCE_MARK_PATH}")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val payload = JSONObject()
                .put("studentId", studentId ?: "")
                .put("courseId", courseId)
                .put("sessionId", sessionId)
                .put("timestamp", timestamp)
                .put("deviceId", deviceId ?: "")
                .toString()
            connection.outputStream.use { it.write(payload.toByteArray()) }

            val code = connection.responseCode
            val response = connection.readResponseBody(code)
            parseMarkAttendanceResponse(code, response)
        } catch (_: IOException) {
            AttendanceQrResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            AttendanceQrResult.Failure("Unable to mark attendance.")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun finalizeAttendanceSession(
        accessToken: String?,
        courseId: Long,
        sessionId: String
    ): FinalizeAttendanceQrSessionResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext FinalizeAttendanceQrSessionResult.Failure("Missing access token.")
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = URL("${normalizedBaseUrl()}${AttendanceConfig.ATTENDANCE_FINALIZE_SESSION_PATH}")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val body = JSONObject()
                .put("courseId", courseId)
                .put("sessionId", sessionId)
                .toString()
            connection.outputStream.use { it.write(body.toByteArray()) }

            val code = connection.responseCode
            val response = connection.readResponseBody(code)
            parseFinalizeResponse(code, response)
        } catch (_: IOException) {
            FinalizeAttendanceQrSessionResult.Failure("Unable to finalize attendance. Check connection and try again.")
        } catch (_: Exception) {
            FinalizeAttendanceQrSessionResult.Failure("Unable to finalize attendance session.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseStartSessionResponse(code: Int, responseBody: String): StartAttendanceQrSessionResult {
        val fallbackMessage = when (code) {
            401, 403 -> "Session expired or unauthorized. Please login again."
            404 -> "Attendance endpoint not found. Check backend URL/port."
            else -> "Unable to start attendance session."
        }

        if (responseBody.isBlank()) {
            return if (code in 200..299) {
                StartAttendanceQrSessionResult.Failure("Malformed QR session response.")
            } else {
                StartAttendanceQrSessionResult.Failure(fallbackMessage)
            }
        }

        val root = runCatching { JSONObject(responseBody) }.getOrNull()
        if (root == null) {
            return if (code in 200..299) {
                StartAttendanceQrSessionResult.Failure("Malformed QR session response.")
            } else {
                StartAttendanceQrSessionResult.Failure(fallbackMessage)
            }
        }

        val status = root.optString("status")
        val message = root.optString("message").ifBlank { fallbackMessage }
        if (code !in 200..299 || !status.equals(AttendanceConfig.SUCCESS_STATUS, ignoreCase = true)) {
            return StartAttendanceQrSessionResult.Failure(message)
        }

        val data = root.optJSONObject("data")
            ?: return StartAttendanceQrSessionResult.Failure("Malformed QR session response.")

        val courseId = data.optLong("courseId", -1L)
        val sessionId = data.optString("sessionId")
        val timestamp = data.optLong("timestamp", 0L)
        val expiresAtEpochSec = data.optLong("expiresAtEpochSec", 0L)
        val qrPayload = data.optString("qrPayload")

        if (courseId <= 0 || sessionId.isBlank() || timestamp <= 0 || expiresAtEpochSec <= 0 || qrPayload.isBlank()) {
            return StartAttendanceQrSessionResult.Failure("Malformed QR session response.")
        }

        return StartAttendanceQrSessionResult.Success(
            AttendanceQrSessionData(courseId, sessionId, timestamp, expiresAtEpochSec, qrPayload)
        )
    }

    private fun parseFinalizeResponse(code: Int, responseBody: String): FinalizeAttendanceQrSessionResult {
        val fallbackMessage = when (code) {
            401, 403 -> "Session expired or unauthorized. Please login again."
            404 -> "Attendance endpoint not found. Check backend URL/port."
            else -> "Unable to finalize attendance session."
        }

        if (responseBody.isBlank()) {
            return if (code in 200..299) {
                FinalizeAttendanceQrSessionResult.Failure("Malformed finalize response.")
            } else {
                FinalizeAttendanceQrSessionResult.Failure(fallbackMessage)
            }
        }

        val root = runCatching { JSONObject(responseBody) }.getOrNull()
            ?: return if (code in 200..299) {
                FinalizeAttendanceQrSessionResult.Failure("Malformed finalize response.")
            } else {
                FinalizeAttendanceQrSessionResult.Failure(fallbackMessage)
            }

        val status = root.optString("status")
        val message = root.optString("message").ifBlank { fallbackMessage }

        if (code !in 200..299 || !status.equals(AttendanceConfig.SUCCESS_STATUS, ignoreCase = true)) {
            return FinalizeAttendanceQrSessionResult.Failure(message)
        }

        val data = root.optJSONObject("data")
            ?: return FinalizeAttendanceQrSessionResult.Failure("Malformed finalize response.")

        val studentsArray = data.optJSONArray("students")
        val students = mutableListOf<FinalizedAttendanceStudentData>()
        if (studentsArray != null) {
            for (index in 0 until studentsArray.length()) {
                val item = studentsArray.optJSONObject(index) ?: continue
                students.add(
                    FinalizedAttendanceStudentData(
                        studentId = item.optString("studentId"),
                        name = item.optString("name"),
                        details = item.optString("details"),
                        status = item.optString("status")
                    )
                )
            }
        }

        return FinalizeAttendanceQrSessionResult.Success(
            data = FinalizedAttendanceSessionData(
                sessionId = data.optString("sessionId"),
                totalStudents = data.optInt("totalStudents"),
                presentCount = data.optInt("presentCount"),
                absentCount = data.optInt("absentCount"),
                students = students
            ),
            message = message
        )
    }

    private fun parseMarkAttendanceResponse(code: Int, responseBody: String): AttendanceQrResult {
        val fallbackMessage = when (code) {
            401, 403 -> "Session expired or unauthorized. Please login again."
            else -> if (code in 200..299) "Attendance marked successfully" else "QR expired or invalid"
        }

        if (responseBody.isBlank()) {
            return if (code in 200..299) {
                AttendanceQrResult.Failure("Malformed attendance response.")
            } else {
                AttendanceQrResult.Failure(fallbackMessage)
            }
        }

        val root = runCatching { JSONObject(responseBody) }.getOrNull()
            ?: return if (code in 200..299) {
                AttendanceQrResult.Failure("Malformed attendance response.")
            } else {
                AttendanceQrResult.Failure(fallbackMessage)
            }

        val status = root.optString("status")
        val message = root.optString("message").ifBlank { fallbackMessage }

        return if (code in 200..299 && status.equals(AttendanceConfig.SUCCESS_STATUS, ignoreCase = true)) {
            AttendanceQrResult.Success(message)
        } else {
            AttendanceQrResult.Failure(message)
        }
    }

    private fun HttpURLConnection.readResponseBody(code: Int): String {
        val stream = if (code in 200..299) inputStream else errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun normalizedBaseUrl(): String = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
}



