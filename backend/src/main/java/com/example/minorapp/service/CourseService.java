package com.example.minorapp.service;

import com.example.minorapp.model.Course;
import com.example.minorapp.model.User;
import com.example.minorapp.repository.CourseRepository;
import com.example.minorapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Course createCourse(String courseName, Long professorId) {
        User professor = userRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor not found."));

        Course course = new Course();
        course.setCourseName(courseName);
        course.setProfessor(professor);
        return courseRepository.save(course);
    }

    public List<Course> getCourses() {
        return courseRepository.findAll();
    }
}


