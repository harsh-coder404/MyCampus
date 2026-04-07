package com.example.minorapp.repository;

import com.example.minorapp.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByStudentId(Long studentId);
    List<Submission> findByTaskId(Long taskId);
    Optional<Submission> findByTaskIdAndStudentId(Long taskId, Long studentId);
}


