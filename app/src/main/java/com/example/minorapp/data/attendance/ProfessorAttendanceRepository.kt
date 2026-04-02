package com.example.minorapp.data.attendance

import com.example.minorapp.domain.constants.DummyDataConstants

data class ProfessorAttendanceStudentData(
    val studentId: String,
    val name: String,
    val details: String
)

data class ProfessorAttendanceSnapshot(
    val courseCode: String,
    val titleSubtitle: String,
    val sessionDate: String,
    val sessionTime: String,
    val totalEnrolled: Int,
    val basePresentCount: Int,
    val baseMarkedCount: Int,
    val students: List<ProfessorAttendanceStudentData>
)

interface ProfessorAttendanceRepository {
    suspend fun fetchAttendanceSnapshot(): ProfessorAttendanceSnapshot
}

class LocalProfessorAttendanceRepository : ProfessorAttendanceRepository {
    override suspend fun fetchAttendanceSnapshot(): ProfessorAttendanceSnapshot {
        return ProfessorAttendanceSnapshot(
            courseCode = "CS502",
            titleSubtitle = "Advanced Distributed Systems • Lecture Hall B-12",
            sessionDate = "Monday, Oct 24",
            sessionTime = "09:00 AM — 10:30 AM",
            totalEnrolled = 42,
            basePresentCount = 26,
            baseMarkedCount = 27,
            students = listOf(
                ProfessorAttendanceStudentData(
                    studentId = "STU-90234",
                    name = DummyDataConstants.dummyNames[0],
                    details = "STU-90234 • Section A"
                ),
                ProfessorAttendanceStudentData(
                    studentId = "STU-90551",
                    name = DummyDataConstants.dummyNames[1],
                    details = "STU-90551 • Section A"
                ),
                ProfessorAttendanceStudentData(
                    studentId = "STU-90112",
                    name = DummyDataConstants.dummyNames[2],
                    details = "STU-90112 • Section B"
                ),
                ProfessorAttendanceStudentData(
                    studentId = "STU-90887",
                    name = DummyDataConstants.dummyNames[3],
                    details = "STU-90887 • Section B"
                )
            )
        )
    }
}

