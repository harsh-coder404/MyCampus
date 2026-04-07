package com.example.minorapp.controller;

import com.example.minorapp.dto.ApiResponse;
import com.example.minorapp.dto.AttendanceRequest;
import com.example.minorapp.model.Attendance;
import com.example.minorapp.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark")
    public ApiResponse<Attendance> markAttendance(@Valid @RequestBody AttendanceRequest request) {
        return new ApiResponse<>("SUCCESS", "Attendance marked.", attendanceService.markAttendance(request));
    }

    @GetMapping("/student/{id}")
    public ApiResponse<List<Attendance>> getByStudent(@PathVariable("id") Long studentId) {
        return new ApiResponse<>("SUCCESS", "Attendance fetched.", attendanceService.getByStudent(studentId));
    }
}


