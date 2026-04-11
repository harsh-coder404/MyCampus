package com.example.minorapp.service;

import com.example.minorapp.dto.AttendanceRequest;
import com.example.minorapp.dto.FinalizeAttendanceSessionRequest;
import com.example.minorapp.dto.FinalizeAttendanceSessionResponse;
import com.example.minorapp.dto.ProfessorAttendanceRosterResponse;
import com.example.minorapp.dto.ProfessorCourseResponse;
import com.example.minorapp.dto.QrAttendanceMarkRequest;
import com.example.minorapp.dto.StartAttendanceSessionRequest;
import com.example.minorapp.dto.StartAttendanceSessionResponse;
import com.example.minorapp.model.Attendance;
import com.example.minorapp.model.AttendanceSession;
import com.example.minorapp.model.Course;
import com.example.minorapp.model.Enrollment;
import com.example.minorapp.model.User;
import com.example.minorapp.repository.AttendanceRepository;
import com.example.minorapp.repository.AttendanceSessionRepository;
import com.example.minorapp.repository.CourseRepository;
import com.example.minorapp.repository.EnrollmentRepository;
import com.example.minorapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceService {

    private static final int DEFAULT_QR_TTL_SECONDS = 90;
    private static final int MAX_REQUEST_SKEW_SECONDS = 120;

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AttendanceService(
        AttendanceRepository attendanceRepository,
        AttendanceSessionRepository attendanceSessionRepository,
        EnrollmentRepository enrollmentRepository,
        UserRepository userRepository,
        CourseRepository courseRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public Attendance markAttendanceManual(AttendanceRequest request) {
        User student = userRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus().toUpperCase());
        attendance.setSessionId(request.getSessionId());
        return attendanceRepository.save(attendance);
    }

    public List<ProfessorCourseResponse> getProfessorCourses() {
        User professor = getAuthenticatedUser();
        if (!"PROFESSOR".equalsIgnoreCase(professor.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only professors can access course roster.");
        }

        List<Course> courses = courseRepository.findByProfessorId(professor.getId());
        courses.sort(Comparator.comparing(Course::getCourseName, String.CASE_INSENSITIVE_ORDER));

        return courses.stream().map(course -> {
            int enrolledCount = (int) enrollmentRepository.findByCourseId(course.getId()).stream()
                .map(Enrollment::getStudent)
                .filter(this::isSeededRosterStudent)
                .count();
            return new ProfessorCourseResponse(course.getId(), course.getCourseName(), enrolledCount);
        }).toList();
    }

    public ProfessorAttendanceRosterResponse getProfessorCourseRoster(Long courseId) {
        User professor = getAuthenticatedUser();
        if (!"PROFESSOR".equalsIgnoreCase(professor.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only professors can access course roster.");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        if (course.getProfessor() == null || !course.getProfessor().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own course roster.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<ProfessorAttendanceRosterResponse.StudentRosterItem> students = enrollments.stream()
            .map(Enrollment::getStudent)
            .filter(student -> student != null)
            .filter(this::isSeededRosterStudent)
            .sorted(Comparator.comparing(User::getRollNumber, Comparator.nullsLast(String::compareToIgnoreCase)))
            .map(student -> {
                String section = (student.getClassSection() == null || student.getClassSection().isBlank())
                    ? "Section"
                    : student.getClassSection();
                String roll = (student.getRollNumber() == null || student.getRollNumber().isBlank())
                    ? "NA"
                    : student.getRollNumber();
                String details = roll + " - " + section;
                return new ProfessorAttendanceRosterResponse.StudentRosterItem(
                    String.valueOf(student.getId()),
                    student.getName(),
                    details
                );
            })
            .toList();

        return new ProfessorAttendanceRosterResponse(
            course.getId(),
            course.getCourseName(),
            resolveCourseCode(course.getCourseName()),
            students.size(),
            students
        );
    }

    private String resolveCourseCode(String courseName) {
        if (courseName == null) {
            return "";
        }
        return switch (courseName.trim().toUpperCase()) {
            case "CSE-A" -> "CS-052";
            case "IT" -> "IT-201";
            case "DSA" -> "CS-305";
            default -> "";
        };
    }

    private boolean isSeededRosterStudent(User student) {
        if (student == null) {
            return false;
        }
        String role = student.getRole();
        if (role == null || !"STUDENT".equalsIgnoreCase(role.trim())) {
            return false;
        }
        String email = student.getEmail();
        return email != null && email.toLowerCase().endsWith("_@abc.com");
    }

    public StartAttendanceSessionResponse startAttendanceSession(StartAttendanceSessionRequest request) {
        User professor = getAuthenticatedUser();
        if (!"PROFESSOR".equalsIgnoreCase(professor.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only professors can start attendance sessions.");
        }

        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        if (course.getProfessor() == null || !course.getProfessor().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only start attendance for your own course.");
        }

        int ttlSeconds = request.getTtlSeconds() == null ? DEFAULT_QR_TTL_SECONDS : request.getTtlSeconds();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(ttlSeconds);

        AttendanceSession session = new AttendanceSession();
        session.setCourse(course);
        session.setProfessor(professor);
        session.setSessionId("SESSION_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        session.setCreatedAt(createdAt);
        session.setExpiresAt(expiresAt);
        attendanceSessionRepository.save(session);

        long timestamp = createdAt.getEpochSecond();
        String qrPayload = "{\"courseId\":" + course.getId()
            + ",\"sessionId\":\"" + session.getSessionId() + "\""
            + ",\"timestamp\":" + timestamp + "}";

        return new StartAttendanceSessionResponse(
            course.getId(),
            session.getSessionId(),
            timestamp,
            expiresAt.getEpochSecond(),
            qrPayload
        );
    }

    public void markAttendanceFromQr(QrAttendanceMarkRequest request) {
        User student = getAuthenticatedUser();
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can mark attendance via QR.");
        }

        AttendanceSession session = attendanceSessionRepository.findBySessionId(request.getSessionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR expired or invalid"));

        if (!session.getCourse().getId().equals(request.getCourseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR expired or invalid");
        }

        Instant now = Instant.now();
        if (now.isAfter(session.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR expired or invalid");
        }

        long qrTimestamp = request.getTimestamp() == null ? 0L : request.getTimestamp();
        long nowEpoch = now.getEpochSecond();
        if (qrTimestamp <= 0 || Math.abs(nowEpoch - qrTimestamp) > MAX_REQUEST_SKEW_SECONDS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR expired or invalid");
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), session.getCourse().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not enrolled in this course.");
        }

        if (attendanceRepository.existsByStudentIdAndSessionId(student.getId(), session.getSessionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance already marked for this session.");
        }

        bindDeviceIfNeeded(student, request.getDeviceId());

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourse(session.getCourse());
        attendance.setSessionId(session.getSessionId());
        attendance.setDate(LocalDate.now());
        attendance.setStatus("PRESENT");
        attendanceRepository.save(attendance);
    }

    public FinalizeAttendanceSessionResponse finalizeAttendanceSession(FinalizeAttendanceSessionRequest request) {
        User professor = getAuthenticatedUser();
        if (!"PROFESSOR".equalsIgnoreCase(professor.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only professors can finalize attendance sessions.");
        }

        AttendanceSession session = attendanceSessionRepository.findBySessionId(request.getSessionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found."));

        if (!session.getCourse().getId().equals(request.getCourseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not belong to this course.");
        }

        if (session.getProfessor() == null || !session.getProfessor().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only finalize your own course session.");
        }

        if (Instant.now().isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session has not expired yet.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(request.getCourseId());
        List<Attendance> sessionAttendance = attendanceRepository.findBySessionIdAndCourseId(request.getSessionId(), request.getCourseId());

        Map<Long, Attendance> presentByStudentId = new HashMap<>();
        for (Attendance attendance : sessionAttendance) {
            if (attendance.getStudent() == null) {
                continue;
            }
            if ("PRESENT".equalsIgnoreCase(attendance.getStatus())) {
                presentByStudentId.put(attendance.getStudent().getId(), attendance);
            }
        }

        LocalDate sessionDate = LocalDate.ofInstant(session.getCreatedAt(), ZoneId.systemDefault());
        List<FinalizeAttendanceSessionResponse.StudentAttendanceStatus> students = new java.util.ArrayList<>();

        int presentCount = 0;
        for (Enrollment enrollment : enrollments) {
            User student = enrollment.getStudent();
            if (student == null) {
                continue;
            }

            boolean isPresent = presentByStudentId.containsKey(student.getId());
            String status = isPresent ? "PRESENT" : "ABSENT";

            if (!isPresent && !attendanceRepository.existsByStudentIdAndSessionId(student.getId(), request.getSessionId())) {
                Attendance absentRecord = new Attendance();
                absentRecord.setStudent(student);
                absentRecord.setCourse(session.getCourse());
                absentRecord.setSessionId(request.getSessionId());
                absentRecord.setDate(sessionDate);
                absentRecord.setStatus("ABSENT");
                attendanceRepository.save(absentRecord);
            }

            if (isPresent) {
                presentCount++;
            }

            String details = student.getRollNumber() + " - " +
                ((student.getClassSection() == null || student.getClassSection().isBlank()) ? "Section" : student.getClassSection());

            students.add(
                new FinalizeAttendanceSessionResponse.StudentAttendanceStatus(
                    String.valueOf(student.getId()),
                    student.getName(),
                    details,
                    status
                )
            );
        }

        int total = students.size();
        int absentCount = Math.max(0, total - presentCount);

        return new FinalizeAttendanceSessionResponse(
            request.getSessionId(),
            total,
            presentCount,
            absentCount,
            students
        );
    }

    public List<Attendance> getByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public Map<String, Object> getMonthlyInsightsForAuthenticatedStudent() {
        User student = getAuthenticatedUser();
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can access attendance insights.");
        }

        YearMonth currentMonth = YearMonth.now();
        List<Attendance> monthly = attendanceRepository.findByStudentId(student.getId()).stream()
            .filter(item -> item.getDate() != null)
            .filter(item -> YearMonth.from(item.getDate()).equals(currentMonth))
            .toList();
        return buildInsightsPayload(monthly);
    }

    public Map<String, Object> getSemesterInsightsForAuthenticatedStudent() {
        User student = getAuthenticatedUser();
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can access attendance insights.");
        }

        List<Attendance> semester = attendanceRepository.findByStudentId(student.getId());
        return buildInsightsPayload(semester);
    }

    private Map<String, Object> buildInsightsPayload(List<Attendance> records) {
        int totalClasses = records.size();
        int totalPresent = (int) records.stream()
            .filter(item -> "PRESENT".equalsIgnoreCase(item.getStatus()))
            .count();
        int absences = Math.max(0, totalClasses - totalPresent);

        List<Map<String, Object>> summaryStats = new ArrayList<>();
        summaryStats.add(statRow("TOTAL CLASSES", totalClasses));
        summaryStats.add(statRow("TOTAL PRESENT", totalPresent));
        summaryStats.add(statRow("EXCUSED ABSENCES", absences));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summaryStats", summaryStats);
        payload.put("totalClasses", totalClasses);
        payload.put("totalPresent", totalPresent);
        payload.put("excusedAbsences", absences);
        return payload;
    }

    private Map<String, Object> statRow(String label, int value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("value", String.valueOf(value));
        return row;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth == null ? null : String.valueOf(auth.getPrincipal());
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private void bindDeviceIfNeeded(User student, String incomingDeviceId) {
        if (incomingDeviceId == null || incomingDeviceId.isBlank()) {
            return;
        }

        String normalizedDeviceId = incomingDeviceId.trim();
        String existing = student.getDeviceId();
        if (existing == null || existing.isBlank()) {
            student.setDeviceId(normalizedDeviceId);
            userRepository.save(student);
            return;
        }

        if (!existing.equals(normalizedDeviceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device mismatch for this account.");
        }
    }
}
