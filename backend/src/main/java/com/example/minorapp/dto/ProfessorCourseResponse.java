package com.example.minorapp.dto;

public class ProfessorCourseResponse {

    private Long id;
    private String courseName;
    private int enrolledCount;

    public ProfessorCourseResponse(Long id, String courseName, int enrolledCount) {
        this.id = id;
        this.courseName = courseName;
        this.enrolledCount = enrolledCount;
    }

    public Long getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }
}

