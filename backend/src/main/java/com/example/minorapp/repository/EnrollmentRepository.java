package com.example.minorapp.repository;

import com.example.minorapp.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
	List<Enrollment> findByStudentId(Long studentId);
	List<Enrollment> findByCourseId(Long courseId);
	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}


