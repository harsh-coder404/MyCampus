package com.example.minorapp.data.attendance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ProfessorAttendanceStudentData(
    val studentId: String,
    val name: String,
    val details: String
)

data class ProfessorCourseData(
    val id: Long,
    val name: String,
    val enrolledCount: Int
)

data class ProfessorAttendanceSnapshot(
    val courseCode: String,
    val subjectCode: String,
    val titleSubtitle: String,
    val sessionDate: String,
    val sessionTime: String,
    val totalEnrolled: Int,
    val basePresentCount: Int,
    val baseMarkedCount: Int,
    val students: List<ProfessorAttendanceStudentData>
)

interface ProfessorAttendanceRepository {
    suspend fun fetchProfessorCourses(accessToken: String?): Result<List<ProfessorCourseData>>
    suspend fun fetchAttendanceSnapshot(accessToken: String?, courseId: Long): Result<ProfessorAttendanceSnapshot>
}

class LocalProfessorAttendanceRepository : ProfessorAttendanceRepository {
    override suspend fun fetchProfessorCourses(accessToken: String?): Result<List<ProfessorCourseData>> {
        return Result.success(emptyList())
    }

    override suspend fun fetchAttendanceSnapshot(accessToken: String?, courseId: Long): Result<ProfessorAttendanceSnapshot> {
        return Result.failure(IllegalStateException("No backend roster source configured."))
    }
}

class BackendProfessorAttendanceRepository(
    private val baseUrl: String
) : ProfessorAttendanceRepository {

    override suspend fun fetchProfessorCourses(accessToken: String?): Result<List<ProfessorCourseData>> = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Missing access token."))
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = URL("${normalizedBaseUrl()}${AttendanceConfig.ATTENDANCE_PROFESSOR_COURSES_PATH}")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val code = connection.responseCode
            val body = connection.readResponseBody(code)
            if (code !in 200..299) {
                return@withContext Result.failure(IllegalStateException(errorMessageForCode(code, "Unable to load classes.")))
            }

            val root = JSONObject(body)
            val status = root.optString("status")
            if (!status.equals(AttendanceConfig.SUCCESS_STATUS, ignoreCase = true)) {
                return@withContext Result.failure(
                    IllegalStateException(root.optString("message").ifBlank { "Unable to load classes." })
                )
            }

            val data = root.optJSONArray("data") ?: return@withContext Result.success(emptyList())
            val courses = buildList {
                for (i in 0 until data.length()) {
                    val item = data.optJSONObject(i) ?: continue
                    val id = item.optLong("id", -1L)
                    if (id <= 0L) continue
                    add(
                        ProfessorCourseData(
                            id = id,
                            name = item.optString("courseName").ifBlank { "Course $id" },
                            enrolledCount = item.optInt("enrolledCount", 0)
                        )
                    )
                }
            }
            Result.success(courses)
        } catch (_: IOException) {
            Result.failure(IllegalStateException("Unable to reach server. Check connection and try again."))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException(ex.message ?: "Unable to load classes."))
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun fetchAttendanceSnapshot(accessToken: String?, courseId: Long): Result<ProfessorAttendanceSnapshot> = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Missing access token."))
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = URL("${normalizedBaseUrl()}${AttendanceConfig.ATTENDANCE_PROFESSOR_ROSTER_PATH}/$courseId")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val code = connection.responseCode
            val body = connection.readResponseBody(code)
            if (code !in 200..299) {
                return@withContext Result.failure(IllegalStateException(errorMessageForCode(code, "Unable to load class roster.")))
            }

            val root = JSONObject(body)
            val status = root.optString("status")
            if (!status.equals(AttendanceConfig.SUCCESS_STATUS, ignoreCase = true)) {
                return@withContext Result.failure(
                    IllegalStateException(root.optString("message").ifBlank { "Unable to load class roster." })
                )
            }

            val data = root.optJSONObject("data")
                ?: return@withContext Result.failure(IllegalStateException("Malformed roster response."))

            val courseName = data.optString("courseName").ifBlank { "Course $courseId" }
            val studentsArray = data.optJSONArray("students")
            val students = buildList {
                if (studentsArray != null) {
                    for (i in 0 until studentsArray.length()) {
                        val item = studentsArray.optJSONObject(i) ?: continue
                        add(
                            ProfessorAttendanceStudentData(
                                studentId = item.optString("studentId"),
                                name = item.optString("name"),
                                details = item.optString("details")
                            )
                        )
                    }
                }
            }

            Result.success(
                ProfessorAttendanceSnapshot(
                    courseCode = courseName,
                    subjectCode = data.optString("courseCode").ifBlank { "" },
                    titleSubtitle = courseName,
                    sessionDate = "Today",
                    sessionTime = "Live",
                    totalEnrolled = data.optInt("totalEnrolled", students.size),
                    basePresentCount = 0,
                    baseMarkedCount = 0,
                    students = students
                )
            )
        } catch (_: IOException) {
            Result.failure(IllegalStateException("Unable to reach server. Check connection and try again."))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException(ex.message ?: "Unable to load class roster."))
        } finally {
            connection?.disconnect()
        }
    }

    private fun normalizedBaseUrl(): String = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"

    private fun HttpURLConnection.readResponseBody(code: Int): String {
        val stream = if (code in 200..299) inputStream else errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun errorMessageForCode(code: Int, fallback: String): String {
        return when (code) {
            401, 403 -> "Session expired or unauthorized. Please login again."
            else -> fallback
        }
    }
}

