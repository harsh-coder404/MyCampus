package com.example.minorapp.data.tasks

import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ProfessorTaskPriority {
    HIGH,
    NORMAL,
    LOW,
    DRAFT
}

data class ProfessorTaskInventoryData(
    val id: String,
    val classTargetId: Long?,
    val statusLabel: String,
    val priority: ProfessorTaskPriority,
    val dueDateText: String,
    val title: String,
    val description: String,
    val enrolledCount: Int?,
    val actionText: String,
    val referencesCount: Int = 0,
    val isDraft: Boolean = false,
    val draftHint: String = "",
    val editedTimestampText: String? = null
)

data class ProfessorTasksSnapshot(
    val activeTasksCount: Int,
    val departmentsCount: Int,
    val categoryOptions: List<String>,
    val inventory: List<ProfessorTaskInventoryData>,
    val classTargets: List<ProfessorClassTargetData>
)

data class ProfessorClassTargetData(
    val id: Long,
    val name: String
)

data class SubmissionChecklistItemData(
    val studentName: String,
    val rollNumber: String,
    val submitted: Boolean
)

sealed class ProfessorTaskCreateResult {
    object Success : ProfessorTaskCreateResult()
    data class Failure(val message: String) : ProfessorTaskCreateResult()
}

sealed class SubmissionChecklistResult {
    data class Success(val items: List<SubmissionChecklistItemData>) : SubmissionChecklistResult()
    data class Failure(val message: String) : SubmissionChecklistResult()
}

sealed class ProfessorTaskDeleteResult {
    object Success : ProfessorTaskDeleteResult()
    data class Failure(val message: String) : ProfessorTaskDeleteResult()
}

sealed class ProfessorTaskUpdateResult {
    object Success : ProfessorTaskUpdateResult()
    data class Failure(val message: String) : ProfessorTaskUpdateResult()
}

interface ProfessorTasksRepository {
    suspend fun fetchTasksSnapshot(): ProfessorTasksSnapshot

    suspend fun createTask(
        title: String,
        description: String,
        deadlineIsoDate: String,
        classTargetId: Long
    ): ProfessorTaskCreateResult {
        return ProfessorTaskCreateResult.Failure("Not supported.")
    }

    suspend fun fetchSubmissionChecklist(taskId: String): SubmissionChecklistResult {
        return SubmissionChecklistResult.Failure("Not supported.")
    }

    suspend fun deleteTask(taskId: String): ProfessorTaskDeleteResult {
        return ProfessorTaskDeleteResult.Failure("Not supported.")
    }

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        deadlineIsoDate: String
    ): ProfessorTaskUpdateResult {
        return ProfessorTaskUpdateResult.Failure("Not supported.")
    }
}

class LocalProfessorTasksRepository : ProfessorTasksRepository {
    override suspend fun fetchTasksSnapshot(): ProfessorTasksSnapshot {
        return ProfessorTasksSnapshot(
            activeTasksCount = 12,
            departmentsCount = 4,
            categoryOptions = listOf("Laboratory", "Class Work", "Assignment"),
            classTargets = listOf(
                ProfessorClassTargetData(1L, "CSE-A"),
                ProfessorClassTargetData(2L, "IT"),
                ProfessorClassTargetData(3L, "DSA")
            ),
            inventory = listOf(
                ProfessorTaskInventoryData(
                    id = "task-high-1",
                    classTargetId = 1L,
                    statusLabel = "HIGH PRIORITY",
                    priority = ProfessorTaskPriority.HIGH,
                    dueDateText = "Due in 2 days",
                    title = "System\nDesign Midterm",
                    description = "Complete system design write-up.",
                    enrolledCount = 142,
                    actionText = "Review Submissions ->"
                ),
                ProfessorTaskInventoryData(
                    id = "task-ongoing-1",
                    classTargetId = 2L,
                    statusLabel = "ONGOING",
                    priority = ProfessorTaskPriority.NORMAL,
                    dueDateText = "Due in 12 days",
                    title = "Machine Learning\nLab",
                    description = "Submit lab report and model outputs.",
                    enrolledCount = 86,
                    actionText = "Edit Details",
                    referencesCount = 3
                ),
                ProfessorTaskInventoryData(
                    id = "task-draft-1",
                    classTargetId = 3L,
                    statusLabel = "DRAFT",
                    priority = ProfessorTaskPriority.DRAFT,
                    dueDateText = "Not Published",
                    title = "Ethical Frameworks in AI",
                    description = "Draft prompt.",
                    enrolledCount = null,
                    actionText = "Resume",
                    isDraft = true,
                    draftHint = "Incomplete description"
                )
            )
        )
    }
}

class BackendProfessorTasksRepository(
    private val baseUrl: String,
    private val sessionManager: SessionManager,
    private val fallback: ProfessorTasksRepository = LocalProfessorTasksRepository()
) : ProfessorTasksRepository {
    override suspend fun fetchTasksSnapshot(): ProfessorTasksSnapshot = withContext(Dispatchers.IO) {
        val accessToken = sessionManager.getAccessToken() ?: return@withContext fallback.fetchTasksSnapshot()
        try {
            val classes = fetchClassTargets(accessToken)
            val tasks = fetchProfessorTasks(accessToken)
            ProfessorTasksSnapshot(
                activeTasksCount = tasks.size,
                departmentsCount = classes.size,
                categoryOptions = listOf("Laboratory", "Class Work", "Assignment"),
                classTargets = classes,
                inventory = tasks
            )
        } catch (_: Exception) {
            fallback.fetchTasksSnapshot()
        }
    }

    override suspend fun createTask(
        title: String,
        description: String,
        deadlineIsoDate: String,
        classTargetId: Long
    ): ProfessorTaskCreateResult = withContext(Dispatchers.IO) {
        val accessToken = sessionManager.getAccessToken() ?: return@withContext ProfessorTaskCreateResult.Failure("Missing access token.")

        var connection: HttpURLConnection? = null
        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}tasks")
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
                .put("courseId", classTargetId)
                .put("title", title)
                .put("description", description)
                .put("deadline", deadlineIsoDate)
                .toString()

            connection.outputStream.use {
                it.write(payload.toByteArray(Charsets.UTF_8))
                it.flush()
            }

            when (connection.responseCode) {
                in 200..299 -> ProfessorTaskCreateResult.Success
                401, 403 -> ProfessorTaskCreateResult.Failure("Session expired. Please login again.")
                else -> ProfessorTaskCreateResult.Failure("Unable to create task right now.")
            }
        } catch (_: IOException) {
            ProfessorTaskCreateResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            ProfessorTaskCreateResult.Failure("Task creation service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun fetchSubmissionChecklist(taskId: String): SubmissionChecklistResult = withContext(Dispatchers.IO) {
        val accessToken = sessionManager.getAccessToken() ?: return@withContext SubmissionChecklistResult.Failure("Missing access token.")
        val numericTaskId = taskId.filter { it.isDigit() }.toLongOrNull()
            ?: return@withContext SubmissionChecklistResult.Failure("Invalid task id.")

        var connection: HttpURLConnection? = null
        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}submissions/task/$numericTaskId/checklist")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            when (connection.responseCode) {
                in 200..299 -> {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(body)
                    val data = root.optJSONArray("data")
                    val items = buildList {
                        if (data != null) {
                            for (i in 0 until data.length()) {
                                val row = data.optJSONObject(i) ?: continue
                                add(
                                    SubmissionChecklistItemData(
                                        studentName = row.optString("name", "Student"),
                                        rollNumber = row.optString("rollNumber", "-"),
                                        submitted = row.optBoolean("submitted", false)
                                    )
                                )
                            }
                        }
                    }
                    SubmissionChecklistResult.Success(items)
                }

                401, 403 -> SubmissionChecklistResult.Failure("Session expired. Please login again.")
                else -> SubmissionChecklistResult.Failure("Unable to load submission checklist.")
            }
        } catch (_: IOException) {
            SubmissionChecklistResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            SubmissionChecklistResult.Failure("Submission checklist service is unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun deleteTask(taskId: String): ProfessorTaskDeleteResult = withContext(Dispatchers.IO) {
        val accessToken = sessionManager.getAccessToken()
            ?: return@withContext ProfessorTaskDeleteResult.Failure("Missing access token.")
        val numericTaskId = taskId.trim().toLongOrNull()
            ?: return@withContext ProfessorTaskDeleteResult.Failure("Invalid task id.")

        var connection: HttpURLConnection? = null
        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}tasks/$numericTaskId")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val code = connection.responseCode
            val body = connection.readResponseBody(code)

            when (code) {
                in 200..299 -> ProfessorTaskDeleteResult.Success
                401 -> ProfessorTaskDeleteResult.Failure("Session expired. Please login again.")
                403 -> ProfessorTaskDeleteResult.Failure(
                    parseApiMessage(body, "You are not allowed to delete this task.")
                )
                404 -> ProfessorTaskDeleteResult.Failure(parseApiMessage(body, "Task not found."))
                else -> ProfessorTaskDeleteResult.Failure(parseApiMessage(body, "Unable to delete task right now."))
            }
        } catch (_: IOException) {
            ProfessorTaskDeleteResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            ProfessorTaskDeleteResult.Failure("Task deletion service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        deadlineIsoDate: String
    ): ProfessorTaskUpdateResult = withContext(Dispatchers.IO) {
        val accessToken = sessionManager.getAccessToken()
            ?: return@withContext ProfessorTaskUpdateResult.Failure("Missing access token.")
        val numericTaskId = taskId.trim().toLongOrNull()
            ?: return@withContext ProfessorTaskUpdateResult.Failure("Invalid task id.")

        var connection: HttpURLConnection? = null
        try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}tasks/$numericTaskId")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            val payload = JSONObject()
                .put("title", title)
                .put("description", description)
                .put("deadline", deadlineIsoDate)
                .toString()

            connection.outputStream.use {
                it.write(payload.toByteArray(Charsets.UTF_8))
                it.flush()
            }

            val code = connection.responseCode
            val body = connection.readResponseBody(code)

            when (code) {
                in 200..299 -> ProfessorTaskUpdateResult.Success
                401 -> ProfessorTaskUpdateResult.Failure("Session expired. Please login again.")
                403 -> ProfessorTaskUpdateResult.Failure(parseApiMessage(body, "You are not allowed to update this task."))
                404 -> ProfessorTaskUpdateResult.Failure(parseApiMessage(body, "Task not found."))
                else -> ProfessorTaskUpdateResult.Failure(parseApiMessage(body, "Unable to update task right now."))
            }
        } catch (_: IOException) {
            ProfessorTaskUpdateResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            ProfessorTaskUpdateResult.Failure("Task update service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun fetchClassTargets(accessToken: String): List<ProfessorClassTargetData> {
        var connection: HttpURLConnection? = null
        return try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}attendance/professor/courses")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            if (connection.responseCode !in 200..299) return emptyList()

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val data = root.optJSONArray("data") ?: return emptyList()
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index) ?: continue
                    val id = item.optLong("id", -1)
                    if (id <= 0) continue
                    add(
                        ProfessorClassTargetData(
                            id = id,
                            name = item.optString("courseName", "Class")
                        )
                    )
                }
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun fetchProfessorTasks(accessToken: String): List<ProfessorTaskInventoryData> {
        var connection: HttpURLConnection? = null
        return try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}tasks/professor/my")
            connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $accessToken")
            }

            if (connection.responseCode !in 200..299) return emptyList()

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val data = root.optJSONArray("data") ?: return emptyList()
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index) ?: continue
                    val id = item.optLong("id", -1)
                    if (id <= 0) continue
                    val deadline = item.optString("deadline", "TBA")
                    val title = item.optString("title", "Task")
                    val description = item.optString("description", "")
                    val courseId = item.optJSONObject("course")?.optLong("id", -1L)?.takeIf { it > 0 }
                    val editedTimestampText = item.optString("updatedAt").ifBlank { null }?.toEditedTimestampText()
                    add(
                        ProfessorTaskInventoryData(
                            id = id.toString(),
                            classTargetId = courseId,
                            statusLabel = "ONGOING",
                            priority = ProfessorTaskPriority.NORMAL,
                            dueDateText = if (deadline.isBlank()) "TBA" else "Due $deadline",
                            title = title,
                            description = description,
                            enrolledCount = null,
                            actionText = "View Submissions",
                            referencesCount = 0,
                            isDraft = false,
                            draftHint = "",
                            editedTimestampText = editedTimestampText
                        )
                    )
                }
            }
        } finally {
            connection?.disconnect()
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

    private fun HttpURLConnection.readResponseBody(code: Int): String {
        val stream = if (code in 200..299) inputStream else errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun parseApiMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        val root = runCatching { JSONObject(body) }.getOrNull() ?: return fallback
        val message = root.optString("message").trim()
        return if (message.isNotBlank()) message else fallback
    }
}

