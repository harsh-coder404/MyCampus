package com.example.minorapp.service;

import com.example.minorapp.dto.AuthResponse;
import com.example.minorapp.dto.ChangePasswordRequest;
import com.example.minorapp.dto.LoginRequest;
import com.example.minorapp.dto.RegisterRequest;
import com.example.minorapp.dto.VerifyIdentityRequest;
import com.example.minorapp.model.Course;
import com.example.minorapp.model.Enrollment;
import com.example.minorapp.model.User;
import com.example.minorapp.repository.CourseRepository;
import com.example.minorapp.repository.EnrollmentRepository;
import com.example.minorapp.repository.UserRepository;
import com.example.minorapp.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
        UserRepository userRepository,
        CourseRepository courseRepository,
        EnrollmentRepository enrollmentRepository,
        PasswordEncoder passwordEncoder,
        JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void seedDemoUsers() {
        if (!userRepository.existsByEmail("student@abc.com")) {
            User student = new User();
            student.setName("Harsh");
            student.setEmail("student@abc.com");
            student.setRollNumber("STU001");
            student.setPassword(passwordEncoder.encode("Stu@12"));
            student.setRole("STUDENT");
            userRepository.save(student);
        }

        if (!userRepository.existsByEmail("proff@abc.com")) {
            User professor = new User();
            professor.setName("Sharma");
            professor.setEmail("proff@abc.com");
            professor.setRollNumber("PROF001");
            professor.setPassword(passwordEncoder.encode("Proff@12"));
            professor.setRole("PROFESSOR");
            userRepository.save(professor);
        }

        User professor = userRepository.findByEmail("proff@abc.com").orElse(null);
        if (professor != null) {
            seedClassWithStudents(
                "CSE-A",
                professor,
                new String[]{"Harsh", "Aryan", "Aryan", "Raju", "Bheem", "Hari", "Arjun", "Krishna", "Virat", "Rohit"}
            );
            seedClassWithStudents(
                "IT",
                professor,
                new String[]{"Harsh", "Aryan", "Aryan", "Raju", "Bheem", "Hari", "Arjun", "Krishna", "Virat", "Rohit"}
            );
            seedClassWithStudents(
                "DSA",
                professor,
                new String[]{"Harsh", "Aryan", "Aryan", "Raju", "Bheem", "Hari", "Arjun", "Krishna", "Virat", "Rohit"}
            );
        }

        userRepository.findByEmail("student@abc.com").ifPresent(user -> {
            if (user.getRollNumber() == null || user.getRollNumber().isBlank()) {
                user.setRollNumber("STU001");
                userRepository.save(user);
            }
        });

        userRepository.findByEmail("proff@abc.com").ifPresent(user -> {
            if (user.getRollNumber() == null || user.getRollNumber().isBlank()) {
                user.setRollNumber("PROF001");
                userRepository.save(user);
            }
        });
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        User user = new User();
        String resolvedName = request.getName();
        if (resolvedName == null || resolvedName.isBlank()) {
            resolvedName = request.getRollNumber();
        }
        user.setName(resolvedName);
        user.setEmail(request.getEmail().toLowerCase());
        user.setRollNumber(request.getRollNumber().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());
        if (request.getClassSection() != null && !request.getClassSection().isBlank()) {
            user.setClassSection(request.getClassSection().trim().toUpperCase());
        }

        User saved = userRepository.save(user);
        return buildSuccessAuthResponse(saved, "Registration successful.");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        String requestedRole = request.getRole() == null ? "" : request.getRole().trim().toUpperCase(Locale.ENGLISH);
        if (!"STUDENT".equals(requestedRole) && !"PROFESSOR".equals(requestedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role selected.");
        }

        String actualRole = user.getRole() == null ? "" : user.getRole().trim().toUpperCase(Locale.ENGLISH);
        if (!requestedRole.equals(actualRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Selected role does not match this account.");
        }

        return buildSuccessAuthResponse(user, "Login successful.");
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void verifyIdentity(VerifyIdentityRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the entered credentials."));

        if (!user.getRollNumber().equalsIgnoreCase(request.getRollNumber().trim())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for the entered credentials.");
        }
    }

    public void changePassword(ChangePasswordRequest request, String authenticatedEmail) {
        String effectiveEmail = authenticatedEmail;
        if (effectiveEmail == null || effectiveEmail.isBlank()) {
            effectiveEmail = request.getEmail();
        }

        User user = userRepository.findByEmail(effectiveEmail.toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        String currentPassword = request.getCurrentPassword();
        if (currentPassword != null && !currentPassword.isBlank()) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
            }
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private AuthResponse buildSuccessAuthResponse(User user, String message) {
        String accessToken = jwtUtil.generateAccessToken(
            user.getEmail(),
            Map.of("role", user.getRole(), "uid", user.getId())
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        AuthResponse.UserData userData = new AuthResponse.UserData(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getName(),
            user.getClassSection()
        );

        AuthResponse.AuthData authData = new AuthResponse.AuthData(accessToken, refreshToken, userData);
        return new AuthResponse("SUCCESS", message, authData);
    }

    private void seedClassWithStudents(String classSection, User professor, String[] names) {
        Course course = courseRepository.findByCourseNameIgnoreCase(classSection).orElseGet(() -> {
            Course created = new Course();
            created.setCourseName(classSection);
            created.setProfessor(professor);
            return courseRepository.save(created);
        });

        // Keep seeded courses owned by the seeded professor so QR start ownership checks pass consistently.
        if (course.getProfessor() == null || !course.getProfessor().getId().equals(professor.getId())) {
            course.setProfessor(professor);
            course = courseRepository.save(course);
        }

        String sectionToken = classSection.toLowerCase().replace("-", "").replace(" ", "");
        Map<String, Integer> duplicateCount = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            String baseName = names[i];
            int serial = i + 1;
            String normalizedBase = baseName.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]", "");
            int occurrence = duplicateCount.getOrDefault(normalizedBase, 0) + 1;
            duplicateCount.put(normalizedBase, occurrence);

            String credentialToken = occurrence == 1 ? normalizedBase : normalizedBase + occurrence;
            String email = credentialToken + "_@abc.com";
            String passwordSeed = occurrence == 1 ? baseName : baseName + occurrence;
            String password = passwordSeed + "_A@12";

            User student = userRepository.findByEmail(email).orElseGet(() -> {
                User created = new User();
                created.setName(baseName);
                created.setEmail(email);
                created.setRollNumber(sectionToken.toUpperCase() + String.format("%03d", serial));
                created.setPassword(passwordEncoder.encode(password));
                created.setRole("STUDENT");
                created.setClassSection(classSection);
                return userRepository.save(created);
            });

            if ((student.getClassSection() == null || student.getClassSection().isBlank()) && "STUDENT".equalsIgnoreCase(student.getRole())) {
                student.setClassSection(classSection);
                userRepository.save(student);
            }

            if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setCourse(course);
                enrollmentRepository.save(enrollment);
            }
        }
    }
}




