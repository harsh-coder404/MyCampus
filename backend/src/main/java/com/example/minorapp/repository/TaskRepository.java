package com.example.minorapp.repository;

import com.example.minorapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCourseId(Long courseId);
    List<Task> findByCourseIdIn(List<Long> courseIds);
}


