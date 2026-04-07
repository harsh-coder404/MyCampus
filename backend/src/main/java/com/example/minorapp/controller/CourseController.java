package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.model.Course;
import com.example.minorapp.service.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ApiResponse<Course> create(@RequestBody Map<String, Object> payload) {
        String courseName = String.valueOf(payload.get("courseName"));
        Long professorId = Long.valueOf(String.valueOf(payload.get("professorId")));
        Course course = courseService.createCourse(courseName, professorId);
        return new ApiResponse<>("SUCCESS", "Course created.", course);
    }

    @GetMapping
    public ApiResponse<List<Course>> getAll() {
        return new ApiResponse<>("SUCCESS", "Courses fetched.", courseService.getCourses());
    }
}


