package com.example.minorapp.repository;

import com.example.minorapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
	Optional<Course> findByCourseNameIgnoreCase(String courseName);
	List<Course> findByProfessorId(Long professorId);
}


