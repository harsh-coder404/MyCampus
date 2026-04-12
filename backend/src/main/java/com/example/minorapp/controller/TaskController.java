package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.dto.TaskRequest;
import com.example.minorapp.dto.TaskUpdateRequest;
import com.example.minorapp.model.Task;
import com.example.minorapp.service.TaskService;
import com.example.minorapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    public TaskController(TaskService taskService, JwtUtil jwtUtil) {
        this.taskService = taskService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Task> createTask(@Valid @RequestBody TaskRequest request, HttpServletRequest httpRequest) {
        String email = extractAuthenticatedEmail(httpRequest);
        return new ApiResponse<>("SUCCESS", "Task created.", taskService.createTask(request, email));
    }

    @GetMapping("/course/{id}")
    public ApiResponse<List<Task>> getByCourse(@PathVariable("id") Long courseId) {
        return new ApiResponse<>("SUCCESS", "Tasks fetched.", taskService.getByCourse(courseId));
    }

    @GetMapping("/my")
    public ApiResponse<List<Task>> getForAuthenticatedStudent(HttpServletRequest request) {
        String email = extractAuthenticatedEmail(request);
        return new ApiResponse<>("SUCCESS", "Tasks fetched.", taskService.getTasksForStudentEmail(email));
    }

    @GetMapping("/professor/my")
    public ApiResponse<List<Task>> getForAuthenticatedProfessor(HttpServletRequest request) {
        String email = extractAuthenticatedEmail(request);
        return new ApiResponse<>("SUCCESS", "Tasks fetched.", taskService.getTasksForProfessorEmail(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTaskForAuthenticatedProfessor(@PathVariable("id") Long taskId, HttpServletRequest request) {
        try {
            String email = extractAuthenticatedEmail(request);
            taskService.deleteTaskForProfessor(taskId, email);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Task deleted.", null));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTaskForAuthenticatedProfessor(
        @PathVariable("id") Long taskId,
        @Valid @RequestBody TaskUpdateRequest request,
        HttpServletRequest httpRequest
    ) {
        try {
            String email = extractAuthenticatedEmail(httpRequest);
            Task updated = taskService.updateTaskForProfessor(taskId, request, email);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Task updated.", updated));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    private String extractAuthenticatedEmail(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token.");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isAccessTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token.");
        }

        return jwtUtil.extractEmail(token);
    }
}
