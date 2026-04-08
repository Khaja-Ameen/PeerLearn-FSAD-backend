package com.peer.controller;

import com.peer.dto.MissingSubmissionGradeRequest;
import com.peer.entity.MissingSubmissionGrade;
import com.peer.entity.User;
import com.peer.repository.UserRepository;
import com.peer.service.TeacherGradingV2Service;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher-grading-v2")
public class TeacherGradingV2Controller {

    private final TeacherGradingV2Service teacherGradingV2Service;
    private final UserRepository userRepository;

    public TeacherGradingV2Controller(TeacherGradingV2Service teacherGradingV2Service, UserRepository userRepository) {
        this.teacherGradingV2Service = teacherGradingV2Service;
        this.userRepository = userRepository;
    }

    @GetMapping("/assignment/{assignmentId}/due-exceeded")
    public Map<String, Object> isDueExceeded(@PathVariable Long assignmentId) {
        boolean exceeded = teacherGradingV2Service.isDueDateExceeded(assignmentId);
        return Map.of("assignmentId", assignmentId, "dueExceeded", exceeded);
    }

    @GetMapping("/assignment/{assignmentId}/graded-missing-students")
    public Map<String, Object> gradedMissingStudents(@PathVariable Long assignmentId) {
        return Map.of(
                "assignmentId", assignmentId,
                "studentIds", teacherGradingV2Service.getAlreadyGradedMissingStudentIds(assignmentId)
        );
    }

    @PostMapping("/grade-missing")
    public MissingSubmissionGrade gradeMissing(
            @Valid @RequestBody MissingSubmissionGradeRequest request,
            Authentication authentication
    ) {
        String principal = String.valueOf(authentication.getName());
        User teacher = userRepository.findByEmail(principal)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new RuntimeException("Only teachers can grade missing submissions");
        }

        Long teacherId = teacher.getId();
        return teacherGradingV2Service.gradeMissingWithZero(request, teacherId);
    }
}