package com.example.minorapp.data.tasks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TaskStatus {
    PENDING,
    COMPLETED
}

data class TaskData(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val isPriority: Boolean,
    val dateText: String,
    val dueText: String?,
    val isDueSoon: Boolean,
    val deadlineSortKey: Int?,
    val issuedSortKey: Int,
    val uploadedPdfUri: String? = null,
    val submissionTimestampText: String? = null,
    val completedBeforeDeadline: Boolean? = null,
    val editedTimestampText: String? = null
)

interface TasksRepository {
    fun getTasks(): List<TaskData>

    suspend fun fetchRemoteTasks(accessToken: String?): RemoteTasksSyncResult {
        return RemoteTasksSyncResult.Failure("Remote sync not supported.")
    }

    suspend fun submitTask(accessToken: String?, taskId: String, submissionRef: String?): TaskSubmissionSyncResult {
        return TaskSubmissionSyncResult.Failure("Submission sync not supported.")
    }
}

sealed class RemoteTasksSyncResult {
    data class Success(val tasks: List<TaskData>) : RemoteTasksSyncResult()
    data class Failure(val message: String) : RemoteTasksSyncResult()
}

sealed class TaskSubmissionSyncResult {
    object Success : TaskSubmissionSyncResult()
    data class Failure(val message: String) : TaskSubmissionSyncResult()
}

class BackendTasksRepository(
    private val baseUrl: String,
    private val fallback: TasksRepository = LocalTasksRepository()
) : TasksRepository {
    override fun getTasks(): List<TaskData> = fallback.getTasks()

    override suspend fun fetchRemoteTasks(accessToken: String?): RemoteTasksSyncResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext RemoteTasksSyncResult.Failure("Missing access token.")
        }

        var connection: HttpURLConnection? = null
        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}tasks/my")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            when (val statusCode = connection.responseCode) {
                in 200..299 -> RemoteTasksSyncResult.Success(parseRemoteTasks(connection))
                401, 403 -> RemoteTasksSyncResult.Failure("Session expired. Please login again.")
                else -> RemoteTasksSyncResult.Failure("Unable to load tasks ($statusCode).")
            }
        } catch (_: IOException) {
            RemoteTasksSyncResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            RemoteTasksSyncResult.Failure("Tasks service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun submitTask(accessToken: String?, taskId: String, submissionRef: String?): TaskSubmissionSyncResult = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrBlank()) {
            return@withContext TaskSubmissionSyncResult.Failure("Missing access token.")
        }

        val taskIdLong = taskId.trim().takeIf { it.matches(Regex("\\d+")) }?.toLongOrNull()
            ?: return@withContext TaskSubmissionSyncResult.Success

        var connection: HttpURLConnection? = null
        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}submissions")
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
                .put("taskId", taskIdLong)
                .put("status", "SUBMITTED")
                .put("submissionRef", submissionRef ?: "")
                .toString()

            connection.outputStream.use {
                it.write(payload.toByteArray(Charsets.UTF_8))
                it.flush()
            }

            when (connection.responseCode) {
                in 200..299 -> TaskSubmissionSyncResult.Success
                401, 403 -> TaskSubmissionSyncResult.Failure("Session expired. Please login again.")
                else -> TaskSubmissionSyncResult.Failure("Unable to submit task right now.")
            }
        } catch (_: IOException) {
            TaskSubmissionSyncResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            TaskSubmissionSyncResult.Failure("Submission service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseRemoteTasks(connection: HttpURLConnection): List<TaskData> {
        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseBody)
        val data = root.optJSONArray("data") ?: JSONArray()
        val now = LocalDate.now()

        return buildList {
            for (index in 0 until data.length()) {
                val item = data.optJSONObject(index) ?: continue
                val id = item.optLong("id", -1L)
                if (id <= 0L) continue

                val title = item.optString("title").ifBlank { "Task" }
                val description = item.optString("description").ifBlank { "No description provided." }
                val deadlineText = item.optString("deadline")
                val deadlineDate = runCatching { LocalDate.parse(deadlineText) }.getOrNull()
                val sortKey = deadlineDate?.let { toSortKey(it) }
                val editedTimestampText = item.optString("updatedAt").ifBlank { null }?.toEditedTimestampText()

                add(
                    TaskData(
                        id = id.toString(),
                        title = title,
                        description = description,
                        status = TaskStatus.PENDING,
                        isPriority = false,
                        dateText = deadlineDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))?.uppercase(Locale.ENGLISH)
                            ?: deadlineText.ifBlank { "TBA" },
                        dueText = deadlineDate?.toDueText(now),
                        isDueSoon = deadlineDate?.isDueSoon(now) == true,
                        deadlineSortKey = sortKey,
                        issuedSortKey = now.let { toSortKey(it) },
                        editedTimestampText = editedTimestampText
                    )
                )
            }
        }
    }

    private fun String.toEditedTimestampText(): String? {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a", Locale.ENGLISH)

        runCatching {
            return "Edited ${OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).format(formatter)}"
        }
        runCatching {
            return "Edited ${LocalDateTime.parse(this).atZone(ZoneId.systemDefault()).format(formatter)}"
        }
        return null
    }

    private fun toSortKey(date: LocalDate): Int = (date.year * 10000) + (date.monthValue * 100) + date.dayOfMonth

    private fun LocalDate.isDueSoon(now: LocalDate): Boolean {
        val days = java.time.temporal.ChronoUnit.DAYS.between(now, this)
        return days in 0..1
    }

    private fun LocalDate.toDueText(now: LocalDate): String {
        val days = java.time.temporal.ChronoUnit.DAYS.between(now, this)
        return when {
            days < 0 -> "OVERDUE"
            days == 0L -> "DUE TODAY"
            days == 1L -> "DUE TOMORROW"
            else -> "DUE IN ${days} DAYS"
        }
    }
}

class LocalTasksRepository : TasksRepository {
    override fun getTasks(): List<TaskData> = listOf(
        TaskData(
            id = "task-deadline-mar-30-2026",
            title = "Macroeconomic Quiz 04",
            description = "Dummy test task with deadline on Mar 30, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "MAR 30, 2026",
            dueText = "OVERDUE",
            isDueSoon = false,
            deadlineSortKey = 20260330,
            issuedSortKey = 20260320
        ),
        TaskData(
            id = "task-deadline-apr-01-2026",
            title = "Systems Design Assignment",
            description = "Dummy test task with deadline on Apr 1, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 01, 2026",
            dueText = "DUE TOMORROW",
            isDueSoon = true,
            deadlineSortKey = 20260401,
            issuedSortKey = 20260322
        ),
        TaskData(
            id = "task-deadline-apr-02-2026",
            title = "Digital Signal Processing Lab",
            description = "Dummy test task with deadline on Apr 2, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 02, 2026",
            dueText = "DUE IN 2 DAYS",
            isDueSoon = false,
            deadlineSortKey = 20260402,
            issuedSortKey = 20260324
        ),
        TaskData(
            id = "task-deadline-apr-03-2026",
            title = "Database Migration Report",
            description = "Dummy test task with deadline on Apr 3, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 03, 2026",
            dueText = "DUE IN 3 DAYS",
            isDueSoon = false,
            deadlineSortKey = 20260403,
            issuedSortKey = 20260326
        ),
        TaskData(
            id = "task-deadline-apr-04-2026",
            title = "Applied Statistics Worksheet",
            description = "Dummy test task with deadline on Apr 4, 2026.",
            status = TaskStatus.PENDING,
            isPriority = false,
            dateText = "APR 04, 2026",
            dueText = "DUE IN 4 DAYS",
            isDueSoon = false,
            deadlineSortKey = 20260404,
            issuedSortKey = 20260327
        )
    )
}

