package com.example.minorapp.data.tasks

import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

enum class ProfessorTaskPriority {
    HIGH,
    NORMAL,
    LOW,
    DRAFT
}

data class ProfessorTaskInventoryData(
    val id: String,
    val statusLabel: String,
    val priority: ProfessorTaskPriority,
    val dueDateText: String,
    val title: String,
    val enrolledCount: Int?,
    val actionText: String,
    val referencesCount: Int = 0,
    val isDraft: Boolean = false,
    val draftHint: String = ""
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
                    statusLabel = "HIGH PRIORITY",
                    priority = ProfessorTaskPriority.HIGH,
                    dueDateText = "Due in 2 days",
                    title = "System\nDesign Midterm",
                    enrolledCount = 142,
                    actionText = "Review Submissions ->"
                ),
                ProfessorTaskInventoryData(
                    id = "task-ongoing-1",
                    statusLabel = "ONGOING",
                    priority = ProfessorTaskPriority.NORMAL,
                    dueDateText = "Due in 12 days",
                    title = "Machine Learning\nLab",
                    enrolledCount = 86,
                    actionText = "Edit Details",
                    referencesCount = 3
                ),
                ProfessorTaskInventoryData(
                    id = "task-draft-1",
                    statusLabel = "DRAFT",
                    priority = ProfessorTaskPriority.DRAFT,
                    dueDateText = "Not Published",
                    title = "Ethical Frameworks in AI",
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
                401 -> ProfessorTaskCreateResult.Failure("Session expired. Please login again.")
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

                401 -> SubmissionChecklistResult.Failure("Session expired. Please login again.")
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

    private fun fetchClassTargets(accessToken: String): List<ProfessorClassTargetData> {
        var connection: HttpURLConnection? = null
        return try {
            val normalizedBaseUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val endpoint = URL("${normalizedBaseUrl}courses")
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
                    add(
                        ProfessorTaskInventoryData(
                            id = id.toString(),
                            statusLabel = "ONGOING",
                            priority = ProfessorTaskPriority.NORMAL,
                            dueDateText = if (deadline.isBlank()) "TBA" else "Due $deadline",
                            title = title,
                            enrolledCount = null,
                            actionText = "View Submissions",
                            referencesCount = 0,
                            isDraft = false,
                            draftHint = ""
                        )
                    )
                }
            }
        } finally {
            connection?.disconnect()
        }
    }
}

