package com.example.minorapp.presentation.screen.attendance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.attendance.LocalProfessorAttendanceRepository
import com.example.minorapp.data.attendance.ProfessorAttendanceRepository
import com.example.minorapp.data.attendance.ProfessorAttendanceSnapshot
import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.launch

enum class AttendanceStatus { PRESENT, ABSENT, LATE, NONE }

data class ProfessorStudentStatus(
    val studentId: String,
    val name: String,
    val details: String,
    val status: AttendanceStatus = AttendanceStatus.NONE
)

data class ProfessorAttendanceUiState(
    val profileImageUri: String? = null,
    val courseCode: String = "CS502",
    val titleSubtitle: String = "Advanced Distributed Systems • Lecture Hall B-12",
    val sessionDate: String = "Monday, Oct 24",
    val sessionTime: String = "09:00 AM — 10:30 AM",
    val totalEnrolled: Int = 42,
    val markedPresent: Int = 0,
    val completionPercent: Int = 0,
    val basePresentCount: Int = 0,
    val baseMarkedCount: Int = 0,
    val students: List<ProfessorStudentStatus> = emptyList()
)

class ProfessorAttendanceViewModel(
    private val sessionManager: SessionManager,
    private val repository: ProfessorAttendanceRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        ProfessorAttendanceUiState(profileImageUri = sessionManager.getProfileImageUri())
    )
        private set

    init {
        loadAttendance()
    }

    private fun loadAttendance() {
        viewModelScope.launch {
            val snapshot = repository.fetchAttendanceSnapshot()
            uiState = snapshot.toUiState(profileImageUri = sessionManager.getProfileImageUri())
            updateDerivedMetrics()
        }
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

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfessorAttendanceViewModel(
                        sessionManager = sessionManager,
                        repository = LocalProfessorAttendanceRepository()
                    ) as T
                }
            }
    }
}

private fun ProfessorAttendanceSnapshot.toUiState(profileImageUri: String?): ProfessorAttendanceUiState {
    return ProfessorAttendanceUiState(
        profileImageUri = profileImageUri,
        courseCode = courseCode,
        titleSubtitle = titleSubtitle,
        sessionDate = sessionDate,
        sessionTime = sessionTime,
        totalEnrolled = totalEnrolled,
        basePresentCount = basePresentCount,
        baseMarkedCount = baseMarkedCount,
        students = students.mapIndexed { index, student ->
            ProfessorStudentStatus(
                studentId = student.studentId,
                name = student.name,
                details = student.details,
                status = if (index == 0) AttendanceStatus.PRESENT else AttendanceStatus.NONE
            )
        }
    )
}

