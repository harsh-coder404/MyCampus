package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.model.Attendance;
import com.example.minorapp.model.Enrollment;
import com.example.minorapp.model.Submission;
import com.example.minorapp.model.Task;
import com.example.minorapp.model.User;
import com.example.minorapp.repository.AttendanceRepository;
import com.example.minorapp.repository.EnrollmentRepository;
import com.example.minorapp.repository.SubmissionRepository;
import com.example.minorapp.repository.UserRepository;
import com.example.minorapp.service.TaskService;
import com.example.minorapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TaskService taskService;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardController(
        JwtUtil jwtUtil,
        UserRepository userRepository,
        EnrollmentRepository enrollmentRepository,
        TaskService taskService,
        SubmissionRepository submissionRepository,
        AttendanceRepository attendanceRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.taskService = taskService;
        this.submissionRepository = submissionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/student")
    public ApiResponse<Map<String, Object>> getStudentDashboard(HttpServletRequest request) {
        String email = extractAuthenticatedEmail(request);
        User student = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only student dashboard is available on this endpoint.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<String> subjects = enrollments.stream()
            .map(Enrollment::getCourse)
            .filter(course -> course != null && course.getCourseName() != null && !course.getCourseName().isBlank())
            .map(course -> course.getCourseName().trim())
            .distinct()
            .toList();

        List<Task> tasks = taskService.getTasksForStudentEmail(email);
        List<Submission> submissions = submissionRepository.findByStudentId(student.getId());
        Set<Long> submittedTaskIds = submissions.stream()
            .map(Submission::getTask)
            .filter(task -> task != null && task.getId() != null)
            .map(Task::getId)
            .collect(Collectors.toSet());

        int pendingCount = (int) tasks.stream().filter(task -> !submittedTaskIds.contains(task.getId())).count();
        int completedCount = Math.max(0, tasks.size() - pendingCount);

        List<Attendance> attendanceList = attendanceRepository.findByStudentId(student.getId());
        int totalAttendance = attendanceList.size();
        int presentAttendance = (int) attendanceList.stream()
            .filter(item -> "PRESENT".equalsIgnoreCase(item.getStatus()))
            .count();
        int percent = totalAttendance == 0 ? 0 : (int) Math.round((presentAttendance * 100.0) / totalAttendance);

        Map<String, Object> attendance = new LinkedHashMap<>();
        attendance.put("percentageText", percent + "%");
        attendance.put("thresholdLabel", percent >= 75 ? "ABOVE\nTHRESHOLD" : "BELOW\nTHRESHOLD");
        attendance.put("lastUpdatedText", "Last updated: Today");
        attendance.put("progress", percent / 100.0);

        Map<String, Object> taskStatus = new LinkedHashMap<>();
        taskStatus.put("pending", pendingCount);
        taskStatus.put("completed", completedCount);

        Map<String, Object> weeklyGoal = new LinkedHashMap<>();
        weeklyGoal.put("title", "WEEKLY GOAL");
        weeklyGoal.put("description", "Maintain attendance above 85% and submit all pending tasks.");
        weeklyGoal.put("tag", "ON TRACK");

        List<Map<String, Object>> upcomingLectures = new ArrayList<>();
        for (int index = 0; index < Math.min(3, subjects.size()); index++) {
            String subject = subjects.get(index);
            Map<String, Object> lecture = new HashMap<>();
            lecture.put("title", subject);
            lecture.put("time", "Room 302 • 10:30 AM");
            lecture.put("status", "PRESENT");
            upcomingLectures.add(lecture);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("displayName", student.getName());
        data.put("overviewMessage", "Welcome back, " + student.getName() + ".");
        data.put("attendance", attendance);
        data.put("taskStatus", taskStatus);
        data.put("weeklyGoal", weeklyGoal);
        data.put("upcomingLectures", upcomingLectures);
        data.put("subjects", subjects);

        return new ApiResponse<>("SUCCESS", "Dashboard fetched.", data);
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

