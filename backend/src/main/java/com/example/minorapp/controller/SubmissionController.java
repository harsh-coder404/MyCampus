package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.model.Submission;
import com.example.minorapp.service.TaskService;
import com.example.minorapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    public SubmissionController(TaskService taskService, JwtUtil jwtUtil) {
        this.taskService = taskService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ApiResponse<Submission> submit(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Long taskId = Long.valueOf(String.valueOf(payload.get("taskId")));
        String status = String.valueOf(payload.getOrDefault("status", "SUBMITTED"));

        Submission submission;
        String authenticatedEmail = tryExtractAuthenticatedEmail(request);
        if (authenticatedEmail != null) {
            submission = taskService.submitForAuthenticatedStudent(taskId, status, authenticatedEmail);
        } else {
            Long studentId = Long.valueOf(String.valueOf(payload.get("studentId")));
            submission = taskService.submit(taskId, studentId, status);
        }
        return new ApiResponse<>("SUCCESS", "Submission saved.", submission);
    }

    @GetMapping("/student/{id}")
    public ApiResponse<List<Submission>> getByStudent(@PathVariable("id") Long studentId) {
        return new ApiResponse<>("SUCCESS", "Submissions fetched.", taskService.getSubmissionsByStudent(studentId));
    }

    @GetMapping("/task/{id}/checklist")
    public ApiResponse<List<Map<String, Object>>> getChecklist(@PathVariable("id") Long taskId, HttpServletRequest request) {
        String authenticatedEmail = tryExtractAuthenticatedEmail(request);
        if (authenticatedEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token.");
        }
        return new ApiResponse<>(
            "SUCCESS",
            "Submission checklist fetched.",
            taskService.getSubmissionChecklistForTask(taskId, authenticatedEmail)
        );
    }

    private String tryExtractAuthenticatedEmail(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isAccessTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token.");
        }
        return jwtUtil.extractEmail(token);
    }
}


