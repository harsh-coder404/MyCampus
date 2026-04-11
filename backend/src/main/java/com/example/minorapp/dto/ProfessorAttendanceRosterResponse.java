package com.example.minorapp.dto;

import java.util.List;

public class ProfessorAttendanceRosterResponse {

    private Long courseId;
    private String courseName;
    private String courseCode;
    private int totalEnrolled;
    private List<StudentRosterItem> students;

    public ProfessorAttendanceRosterResponse(
        Long courseId,
        String courseName,
        String courseCode,
        int totalEnrolled,
        List<StudentRosterItem> students
    ) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.totalEnrolled = totalEnrolled;
        this.students = students;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getTotalEnrolled() {
        return totalEnrolled;
    }

    public List<StudentRosterItem> getStudents() {
        return students;
    }

    public static class StudentRosterItem {
        private String studentId;
        private String name;
        private String details;

        public StudentRosterItem(String studentId, String name, String details) {
            this.studentId = studentId;
            this.name = name;
            this.details = details;
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
    }
}

