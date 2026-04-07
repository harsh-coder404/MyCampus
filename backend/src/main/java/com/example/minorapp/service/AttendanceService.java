package com.example.minorapp.service;

import com.example.minorapp.dto.AttendanceRequest;
import com.example.minorapp.model.Attendance;
import com.example.minorapp.model.Course;
import com.example.minorapp.model.User;
import com.example.minorapp.repository.AttendanceRepository;
import com.example.minorapp.repository.CourseRepository;
import com.example.minorapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AttendanceService(
        AttendanceRepository attendanceRepository,
        UserRepository userRepository,
        CourseRepository courseRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public Attendance markAttendance(AttendanceRequest request) {
        User student = userRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus().toUpperCase());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }
}


