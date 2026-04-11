package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.dto.AttendanceRequest;
import com.example.minorapp.dto.FinalizeAttendanceSessionRequest;
import com.example.minorapp.dto.FinalizeAttendanceSessionResponse;
import com.example.minorapp.dto.ProfessorAttendanceRosterResponse;
import com.example.minorapp.dto.ProfessorCourseResponse;
import com.example.minorapp.dto.QrAttendanceMarkRequest;
import com.example.minorapp.dto.StartAttendanceSessionRequest;
import com.example.minorapp.dto.StartAttendanceSessionResponse;
import com.example.minorapp.model.Attendance;
import com.example.minorapp.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark/manual")
    public ApiResponse<Attendance> markAttendanceManual(@Valid @RequestBody AttendanceRequest request) {
        return new ApiResponse<>("SUCCESS", "Attendance marked.", attendanceService.markAttendanceManual(request));
    }

    @GetMapping("/professor/courses")
    public ApiResponse<List<ProfessorCourseResponse>> getProfessorCourses() {
        return new ApiResponse<>("SUCCESS", "Professor courses fetched.", attendanceService.getProfessorCourses());
    }

    @GetMapping("/professor/roster/{courseId}")
    public ApiResponse<ProfessorAttendanceRosterResponse> getProfessorRoster(@PathVariable("courseId") Long courseId) {
        return new ApiResponse<>("SUCCESS", "Course roster fetched.", attendanceService.getProfessorCourseRoster(courseId));
    }

    @GetMapping("/insights/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyInsights() {
        try {
            return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Monthly attendance insights fetched.", attendanceService.getMonthlyInsightsForAuthenticatedStudent())
            );
        } catch (ResponseStatusException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            HttpStatus resolvedStatus = status == null ? HttpStatus.BAD_REQUEST : status;
            return ResponseEntity.status(resolvedStatus)
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    @GetMapping("/insights/semester")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSemesterInsights() {
        try {
            return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Semester attendance insights fetched.", attendanceService.getSemesterInsightsForAuthenticatedStudent())
            );
        } catch (ResponseStatusException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            HttpStatus resolvedStatus = status == null ? HttpStatus.BAD_REQUEST : status;
            return ResponseEntity.status(resolvedStatus)
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<ApiResponse<StartAttendanceSessionResponse>> startSession(
        @Valid @RequestBody StartAttendanceSessionRequest request
    ) {
        try {
            StartAttendanceSessionResponse data = attendanceService.startAttendanceSession(request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance session started.", data));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    @PostMapping("/sessions/finalize")
    public ResponseEntity<ApiResponse<FinalizeAttendanceSessionResponse>> finalizeSession(
        @Valid @RequestBody FinalizeAttendanceSessionRequest request
    ) {
        try {
            FinalizeAttendanceSessionResponse data = attendanceService.finalizeAttendanceSession(request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance session finalized.", data));
        } catch (ResponseStatusException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            HttpStatus resolvedStatus = status == null ? HttpStatus.BAD_REQUEST : status;
            return ResponseEntity.status(resolvedStatus)
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<Object>> markAttendanceByQr(@Valid @RequestBody QrAttendanceMarkRequest request) {
        try {
            attendanceService.markAttendanceFromQr(request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Attendance marked successfully", null));
        } catch (ResponseStatusException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            HttpStatus resolvedStatus = status == null ? HttpStatus.BAD_REQUEST : status;
            return ResponseEntity.status(resolvedStatus)
                .body(new ApiResponse<>("ERROR", ex.getReason(), null));
        }
    }

    @GetMapping("/student/{id}")
    public ApiResponse<List<Attendance>> getByStudent(@PathVariable("id") Long studentId) {
        return new ApiResponse<>("SUCCESS", "Attendance fetched.", attendanceService.getByStudent(studentId));
    }
}


