package com.example.minorapp.service;

import com.example.minorapp.dto.TaskRequest;
import com.example.minorapp.model.Course;
import com.example.minorapp.model.Enrollment;
import com.example.minorapp.model.Submission;
import com.example.minorapp.model.Task;
import com.example.minorapp.model.User;
import com.example.minorapp.repository.CourseRepository;
import com.example.minorapp.repository.EnrollmentRepository;
import com.example.minorapp.repository.SubmissionRepository;
import com.example.minorapp.repository.TaskRepository;
import com.example.minorapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public TaskService(
        TaskRepository taskRepository,
        SubmissionRepository submissionRepository,
        CourseRepository courseRepository,
        EnrollmentRepository enrollmentRepository,
        UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    public Task createTask(TaskRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        Task task = new Task();
        task.setCourse(course);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        return taskRepository.save(task);
    }

    public List<Task> getByCourse(Long courseId) {
        return taskRepository.findByCourseId(courseId);
    }

    public List<Task> getTasksForStudentEmail(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail.toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).toList();
        return taskRepository.findByCourseIdIn(courseIds);
    }

    public List<Task> getTasksForProfessorEmail(String professorEmail) {
        User professor = userRepository.findByEmail(professorEmail.toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor not found."));

        List<Course> courses = courseRepository.findByProfessorId(professor.getId());
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        return taskRepository.findByCourseIdIn(courseIds);
    }

    public Submission submit(Long taskId, Long studentId, String status) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));

        Submission submission = submissionRepository.findByTaskIdAndStudentId(taskId, studentId).orElseGet(Submission::new);
        submission.setTask(task);
        submission.setStudent(student);
        submission.setStatus(status.toUpperCase());
        submission.setSubmissionDate(LocalDate.now());
        return submissionRepository.save(submission);
    }

    public Submission submitForAuthenticatedStudent(Long taskId, String status, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail.toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        return submit(taskId, student.getId(), status);
    }

    public List<Submission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public List<Map<String, Object>> getSubmissionChecklistForTask(Long taskId, String professorEmail) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));

        User professor = userRepository.findByEmail(professorEmail.toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor not found."));

        if (task.getCourse() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task course not found.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(task.getCourse().getId());
        List<Submission> submissions = submissionRepository.findByTaskId(taskId);
        Map<Long, Submission> submissionByStudentId = new HashMap<>();
        for (Submission submission : submissions) {
            if (submission.getStudent() != null) {
                submissionByStudentId.put(submission.getStudent().getId(), submission);
            }
        }

        return enrollments.stream().map(enrollment -> {
            User student = enrollment.getStudent();
            Submission submission = student == null ? null : submissionByStudentId.get(student.getId());

            Map<String, Object> row = new HashMap<>();
            row.put("studentId", student != null ? student.getId() : null);
            row.put("name", student != null ? student.getName() : "Unknown");
            row.put("email", student != null ? student.getEmail() : null);
            row.put("rollNumber", student != null ? student.getRollNumber() : null);
            row.put("submitted", submission != null);
            row.put("status", submission != null ? submission.getStatus() : "PENDING");
            row.put("submissionDate", submission != null ? submission.getSubmissionDate() : null);
            return row;
        }).toList();
    }
}


