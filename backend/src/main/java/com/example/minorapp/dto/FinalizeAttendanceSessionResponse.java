package com.example.minorapp.dto;

import java.util.List;

public class FinalizeAttendanceSessionResponse {

    private String sessionId;
    private int totalStudents;
    private int presentCount;
    private int absentCount;
    private List<StudentAttendanceStatus> students;

    public FinalizeAttendanceSessionResponse(
        String sessionId,
        int totalStudents,
        int presentCount,
        int absentCount,
        List<StudentAttendanceStatus> students
    ) {
        this.sessionId = sessionId;
        this.totalStudents = totalStudents;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.students = students;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public List<StudentAttendanceStatus> getStudents() {
        return students;
    }

    public static class StudentAttendanceStatus {
        private String studentId;
        private String name;
        private String details;
        private String status;

        public StudentAttendanceStatus(String studentId, String name, String details, String status) {
            this.studentId = studentId;
            this.name = name;
            this.details = details;
            this.status = status;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getName() {
            return name;
        }

        public String getDetails() {
            return details;
        }

        public String getStatus() {
            return status;
        }
    }
}

