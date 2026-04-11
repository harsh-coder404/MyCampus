package com.example.minorapp.repository;

import com.example.minorapp.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    boolean existsByStudentIdAndSessionId(Long studentId, String sessionId);
    List<Attendance> findBySessionIdAndCourseId(String sessionId, Long courseId);
}
