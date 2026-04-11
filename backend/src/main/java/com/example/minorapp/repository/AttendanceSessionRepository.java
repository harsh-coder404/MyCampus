package com.example.minorapp.repository;

import com.example.minorapp.model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findBySessionId(String sessionId);
    List<AttendanceSession> findByCourseIdAndExpiresAtAfter(Long courseId, Instant now);
}

