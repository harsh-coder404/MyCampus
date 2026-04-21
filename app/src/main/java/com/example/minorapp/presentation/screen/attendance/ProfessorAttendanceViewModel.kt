package com.example.minorapp.presentation.screen.attendance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.attendance.AttendanceQrSessionData
import com.example.minorapp.data.attendance.BackendProfessorAttendanceRepository
import com.example.minorapp.data.attendance.FinalizeAttendanceQrSessionResult
import com.example.minorapp.data.attendance.FinalizedAttendanceStudentData
import com.example.minorapp.data.attendance.ProfessorCourseData
import com.example.minorapp.data.attendance.ProfessorAttendanceRepository
import com.example.minorapp.data.attendance.ProfessorAttendanceSnapshot
import com.example.minorapp.data.attendance.QrAttendanceRepository
import com.example.minorapp.data.attendance.StartAttendanceQrSessionResult
import com.example.minorapp.data.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class AttendanceStatus { PRESENT, ABSENT, LATE, NONE }
enum class ProfessorAttendanceMode { MANUAL, QR_CODE }

data class ProfessorStudentStatus(
    val studentId: String,
    val name: String,
    val details: String,
    val status: AttendanceStatus = AttendanceStatus.NONE
)

data class ProfessorQrSessionUi(
    val courseId: Long,
    val sessionId: String,
    val timestamp: Long,
    val expiresAtEpochSec: Long,
    val qrPayload: String,
    val secondsRemaining: Int
)

data class ProfessorCourseUi(
    val id: Long,
    val name: String,
    val enrolledCount: Int
)

data class AttendanceHistoryEntryUi(
    val courseName: String,
    val modeLabel: String,
    val recordedAtText: String
)

data class ProfessorAttendanceUiState(
    val profileImageUri: String? = null,
    val courseCode: String = "",
    val titleSubtitle: String = "",
    val subjectName: String = "",
    val subjectCode: String = "",
    val sessionDate: String = "20/04/2026",
    val sessionTime: String = "09:00 AM - 10:00 AM",
    val totalEnrolled: Int = 0,
    val markedPresent: Int = 0,
    val completionPercent: Int = 0,
    val basePresentCount: Int = 0,
    val baseMarkedCount: Int = 0,
    val students: List<ProfessorStudentStatus> = emptyList(),
    val attendanceMode: ProfessorAttendanceMode = ProfessorAttendanceMode.MANUAL,
    val selectedCourseId: Long = -1L,
    val availableCourses: List<ProfessorCourseUi> = emptyList(),
    val isStartingQr: Boolean = false,
    val qrSession: ProfessorQrSessionUi? = null,
    val qrError: String? = null,
    val isQrAttendanceFinalized: Boolean = false,
    val qrFinalizedAtText: String? = null,
    val attendanceHistory: List<AttendanceHistoryEntryUi> = emptyList(),
    val shouldForceReauth: Boolean = false
)

class ProfessorAttendanceViewModel(
    private val sessionManager: SessionManager,
    private val repository: ProfessorAttendanceRepository,
    private val qrAttendanceRepository: QrAttendanceRepository
) : ViewModel() {

    private var qrCountdownJob: Job? = null
    private var rosterLoadJob: Job? = null
    private var rosterRequestToken: Long = 0L

    var uiState by mutableStateOf(
        ProfessorAttendanceUiState(profileImageUri = sessionManager.getProfileImageUri())
    )
        private set

    init {
        restoreAttendanceHistory()
        loadCoursesAndAttendance()
    }

    private fun loadCoursesAndAttendance() {
        viewModelScope.launch {
            val token = sessionManager.getAccessToken()
            val coursesResult = repository.fetchProfessorCourses(token)
            val courses = coursesResult.getOrNull()
            if (courses == null) {
                val message = coursesResult.exceptionOrNull()?.message ?: "Unable to load classes."
                if (isUnauthorizedError(message)) {
                    triggerForcedReauth(message)
                    return@launch
                }
                uiState = uiState.copy(qrError = message)
                return@launch
            }

            if (courses.isEmpty()) {
                uiState = uiState.copy(
                    availableCourses = emptyList(),
                    selectedCourseId = -1L,
                    students = emptyList(),
                    totalEnrolled = 0,
                    qrError = "No class found for this professor."
                )
                return@launch
            }

            val courseUi = courses.map { it.toUi() }
            val initialCourseId = uiState.selectedCourseId.takeIf { selected -> courseUi.any { it.id == selected } }
                ?: courseUi.first().id

            uiState = uiState.copy(
                availableCourses = courseUi,
                selectedCourseId = initialCourseId
            )
            loadRosterForCourse(initialCourseId)
        }
    }

    fun onModeSelected(mode: ProfessorAttendanceMode) {
        uiState = uiState.copy(attendanceMode = mode, qrError = null)
        if (mode == ProfessorAttendanceMode.MANUAL) {
            clearQrSession()
        }
    }

    fun onCourseSelected(courseId: Long) {
        uiState = uiState.copy(selectedCourseId = courseId, qrError = null)
        loadRosterForCourse(courseId)
    }

    fun onStartQrAttendance() {
        if (uiState.selectedCourseId <= 0L) {
            uiState = uiState.copy(qrError = "Select a valid class before starting attendance.")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isStartingQr = true, qrError = null)
            when (
                val result = qrAttendanceRepository.startAttendanceSession(
                    accessToken = sessionManager.getAccessToken(),
                    courseId = uiState.selectedCourseId
                )
            ) {
                is StartAttendanceQrSessionResult.Success -> {
                    applyNewQrSession(result.session)
                }

                is StartAttendanceQrSessionResult.Failure -> {
                    if (isUnauthorizedError(result.message)) {
                        triggerForcedReauth(result.message)
                        return@launch
                    }
                    uiState = uiState.copy(isStartingQr = false, qrError = result.message, qrSession = null)
                }
            }
        }
    }

    fun onForceReauthHandled() {
        uiState = uiState.copy(shouldForceReauth = false)
    }

    fun onStudentStatusChange(studentId: String, newStatus: AttendanceStatus) {
        val updatedList = uiState.students.map {
            if (it.studentId == studentId) it.copy(status = newStatus) else it
        }
        uiState = uiState.copy(students = updatedList)
        updateDerivedMetrics()
    }

    fun onMarkAllPresent() {
        val updatedList = uiState.students.map {
            it.copy(status = AttendanceStatus.PRESENT)
        }
        uiState = uiState.copy(students = updatedList)
        updateDerivedMetrics()
    }

    fun onSubmitAttendanceRecord() {
        addAttendanceHistoryEntry(ProfessorAttendanceMode.MANUAL)
    }

    private fun applyNewQrSession(session: AttendanceQrSessionData) {
        val nowEpoch = System.currentTimeMillis() / 1000
        val initialSeconds = (session.expiresAtEpochSec - nowEpoch).coerceAtLeast(0).toInt()

        uiState = uiState.copy(
            isStartingQr = false,
            qrError = null,
            isQrAttendanceFinalized = false,
            qrFinalizedAtText = null,
            qrSession = ProfessorQrSessionUi(
                courseId = session.courseId,
                sessionId = session.sessionId,
                timestamp = session.timestamp,
                expiresAtEpochSec = session.expiresAtEpochSec,
                qrPayload = session.qrPayload,
                secondsRemaining = initialSeconds
            )
        )

        qrCountdownJob?.cancel()
        qrCountdownJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val current = uiState.qrSession ?: break
                if (current.secondsRemaining <= 1) {
                    uiState = uiState.copy(qrSession = null)
                    finalizeExpiredSession(current)
                    break
                }
                uiState = uiState.copy(qrSession = current.copy(secondsRemaining = current.secondsRemaining - 1))
            }
        }
    }

    private suspend fun finalizeExpiredSession(expiredSession: ProfessorQrSessionUi) {
        when (
            val result = qrAttendanceRepository.finalizeAttendanceSession(
                accessToken = sessionManager.getAccessToken(),
                courseId = expiredSession.courseId,
                sessionId = expiredSession.sessionId
            )
        ) {
            is FinalizeAttendanceQrSessionResult.Success -> {
                applyFinalizedStatuses(result.data.students)
                uiState = uiState.copy(
                    qrError = "Session ended. Attendance finalized.",
                    isQrAttendanceFinalized = true,
                    qrFinalizedAtText = formatFinalizedTimestamp(System.currentTimeMillis())
                )
                addAttendanceHistoryEntry(ProfessorAttendanceMode.QR_CODE)
            }

            is FinalizeAttendanceQrSessionResult.Failure -> {
                if (isUnauthorizedError(result.message)) {
                    triggerForcedReauth(result.message)
                    return
                }
                val fallback = uiState.students.map {
                    if (it.status == AttendanceStatus.PRESENT) it else it.copy(status = AttendanceStatus.ABSENT)
                }
                uiState = uiState.copy(
                    students = fallback,
                    qrError = result.message,
                    isQrAttendanceFinalized = true,
                    qrFinalizedAtText = formatFinalizedTimestamp(System.currentTimeMillis())
                )
                updateDerivedMetrics()
                addAttendanceHistoryEntry(ProfessorAttendanceMode.QR_CODE)
            }
        }
    }

    private fun applyFinalizedStatuses(finalStudents: List<FinalizedAttendanceStudentData>) {
        if (finalStudents.isEmpty()) {
            uiState = uiState.copy(qrError = "Session ended, but no enrolled students were returned.")
            return
        }

        val finalized = finalStudents.map { item ->
            ProfessorStudentStatus(
                studentId = item.studentId,
                name = item.name,
                details = item.details,
                status = if (item.status.equals("PRESENT", ignoreCase = true)) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
            )
        }.distinctBy { it.studentId }

        uiState = uiState.copy(
            students = finalized,
            basePresentCount = 0,
            baseMarkedCount = 0,
            totalEnrolled = finalized.size,
            markedPresent = finalized.count { it.status == AttendanceStatus.PRESENT },
            completionPercent = 100
        )
    }

    private fun clearQrSession() {
        qrCountdownJob?.cancel()
        uiState = uiState.copy(qrSession = null)
    }

    private fun loadRosterForCourse(courseId: Long) {
        if (courseId <= 0L) return

        rosterLoadJob?.cancel()
        val requestToken = ++rosterRequestToken

        rosterLoadJob = viewModelScope.launch {
            val token = sessionManager.getAccessToken()
            val snapshotResult = repository.fetchAttendanceSnapshot(token, courseId)
            val snapshot = snapshotResult.getOrNull()
            if (snapshot == null) {
                val message = snapshotResult.exceptionOrNull()?.message ?: "Unable to load class roster."
                if (isUnauthorizedError(message)) {
                    triggerForcedReauth(message)
                    return@launch
                }
                uiState = uiState.copy(qrError = message)
                return@launch
            }

            if (requestToken != rosterRequestToken || uiState.selectedCourseId != courseId) {
                return@launch
            }

            val selected = uiState.selectedCourseId
            val available = uiState.availableCourses
            uiState = snapshot.toUiState(profileImageUri = sessionManager.getProfileImageUri()).copy(
                selectedCourseId = selected,
                availableCourses = available,
                attendanceMode = uiState.attendanceMode,
                isQrAttendanceFinalized = uiState.isQrAttendanceFinalized,
                qrFinalizedAtText = uiState.qrFinalizedAtText,
                attendanceHistory = uiState.attendanceHistory
            )
            updateDerivedMetrics()
        }
    }

    private fun formatFinalizedTimestamp(timestampMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestampMillis))
    }

    private fun addAttendanceHistoryEntry(mode: ProfessorAttendanceMode) {
        val selectedCourseName = uiState.availableCourses
            .firstOrNull { it.id == uiState.selectedCourseId }
            ?.name
            .orEmpty()
            .ifBlank { uiState.courseCode }

        val recordedAtText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))

        val entry = AttendanceHistoryEntryUi(
            courseName = selectedCourseName,
            modeLabel = if (mode == ProfessorAttendanceMode.QR_CODE) "QR Code" else "Manual",
            recordedAtText = recordedAtText
        )

        val updatedHistory = (listOf(entry) + uiState.attendanceHistory).take(HISTORY_LIMIT)
        uiState = uiState.copy(attendanceHistory = updatedHistory)
        persistAttendanceHistory(updatedHistory)
    }

    private fun restoreAttendanceHistory() {
        val snapshot = sessionManager.getProfessorAttendanceHistorySnapshot().orEmpty()
        if (snapshot.isBlank()) {
            return
        }

        val parsed = runCatching {
            val array = JSONArray(snapshot)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val courseName = item.optString(HISTORY_KEY_COURSE_NAME).trim()
                    val modeLabel = item.optString(HISTORY_KEY_MODE_LABEL).trim()
                    val recordedAtText = item.optString(HISTORY_KEY_RECORDED_AT_TEXT).trim()
                    if (courseName.isBlank() || modeLabel.isBlank() || recordedAtText.isBlank()) {
                        continue
                    }
                    add(
                        AttendanceHistoryEntryUi(
                            courseName = courseName,
                            modeLabel = modeLabel,
                            recordedAtText = recordedAtText
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())

        if (parsed.isNotEmpty()) {
            uiState = uiState.copy(attendanceHistory = parsed.take(HISTORY_LIMIT))
        }
    }

    private fun persistAttendanceHistory(history: List<AttendanceHistoryEntryUi>) {
        if (history.isEmpty()) {
            sessionManager.saveProfessorAttendanceHistorySnapshot(null)
            return
        }

        val array = JSONArray()
        history.take(HISTORY_LIMIT).forEach { entry ->
            array.put(
                JSONObject()
                    .put(HISTORY_KEY_COURSE_NAME, entry.courseName)
                    .put(HISTORY_KEY_MODE_LABEL, entry.modeLabel)
                    .put(HISTORY_KEY_RECORDED_AT_TEXT, entry.recordedAtText)
            )
        }
        sessionManager.saveProfessorAttendanceHistorySnapshot(array.toString())
    }

    private fun triggerForcedReauth(message: String) {
        sessionManager.clearSession()
        clearQrSession()
        uiState = uiState.copy(
            isStartingQr = false,
            qrError = message,
            shouldForceReauth = true
        )
    }

    private fun isUnauthorizedError(message: String): Boolean {
        val normalized = message.lowercase(Locale.ENGLISH)
        return normalized.contains("unauthorized") || normalized.contains("session expired") || normalized.contains("login again")
    }

    private fun updateDerivedMetrics() {
        val basePresent = uiState.basePresentCount
        val thisListPresent = uiState.students.count { it.status == AttendanceStatus.PRESENT }
        val thisListMarked = uiState.students.count { it.status != AttendanceStatus.NONE }
        val totalEnrolled = uiState.totalEnrolled.coerceAtLeast(1)

        val totalPresent = basePresent + thisListPresent
        val baseMarked = uiState.baseMarkedCount
        val totalMarked = baseMarked + thisListMarked
        val completion = ((totalMarked / totalEnrolled.toDouble()) * 100).toInt()

        uiState = uiState.copy(
            markedPresent = totalPresent,
            completionPercent = completion
        )
    }

    override fun onCleared() {
        rosterLoadJob?.cancel()
        qrCountdownJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val HISTORY_LIMIT = 10
        private const val HISTORY_KEY_COURSE_NAME = "courseName"
        private const val HISTORY_KEY_MODE_LABEL = "modeLabel"
        private const val HISTORY_KEY_RECORDED_AT_TEXT = "recordedAtText"

        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfessorAttendanceViewModel(
                        sessionManager = sessionManager,
                        repository = BackendProfessorAttendanceRepository(BuildConfig.AUTH_BASE_URL),
                        qrAttendanceRepository = QrAttendanceRepository(BuildConfig.AUTH_BASE_URL)
                    ) as T
                }
            }
    }
}

private fun ProfessorAttendanceSnapshot.toUiState(profileImageUri: String?): ProfessorAttendanceUiState {
    val resolvedSubjectName = subjectName.trim().ifBlank { courseCode }
    val resolvedSubjectCode = subjectCode.trim()
    val resolvedSubtitle = titleSubtitle.trim().ifBlank { courseCode }
    val uniqueStudents = students.distinctBy { it.studentId }

    return ProfessorAttendanceUiState(
        profileImageUri = profileImageUri,
        courseCode = courseCode,
        titleSubtitle = resolvedSubtitle,
        subjectName = resolvedSubjectName,
        subjectCode = resolvedSubjectCode,
        sessionDate = sessionDate,
        sessionTime = sessionTime,
        totalEnrolled = uniqueStudents.size,
        basePresentCount = basePresentCount,
        baseMarkedCount = baseMarkedCount,
        students = uniqueStudents.map { student ->
            ProfessorStudentStatus(
                studentId = student.studentId,
                name = student.name,
                details = student.details,
                status = AttendanceStatus.NONE
            )
        }
    )
}

private fun ProfessorCourseData.toUi(): ProfessorCourseUi {
    return ProfessorCourseUi(id = id, name = name, enrolledCount = enrolledCount)
}

