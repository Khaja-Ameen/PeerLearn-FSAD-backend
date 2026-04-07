package com.peer.controller;

import com.peer.dto.MissingSubmissionGradeRequest;
import com.peer.entity.MissingSubmissionGrade;
import com.peer.service.TeacherGradingV2Service;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher-grading-v2")
public class TeacherGradingV2Controller {

    private final TeacherGradingV2Service teacherGradingV2Service;

    public TeacherGradingV2Controller(TeacherGradingV2Service teacherGradingV2Service) {
        this.teacherGradingV2Service = teacherGradingV2Service;
    }

    @GetMapping("/assignment/{assignmentId}/due-exceeded")
    public Map<String, Object> isDueExceeded(@PathVariable Long assignmentId) {
        boolean exceeded = teacherGradingV2Service.isDueDateExceeded(assignmentId);
        return Map.of("assignmentId", assignmentId, "dueExceeded", exceeded);
    }

    @PostMapping("/grade-missing")
    public MissingSubmissionGrade gradeMissing(
            @Valid @RequestBody MissingSubmissionGradeRequest request,
            Authentication authentication
    ) {
        // adapt this based on your auth principal type
        Long teacherId = Long.parseLong(authentication.getName());
        return teacherGradingV2Service.gradeMissingWithZero(request, teacherId);
    }
}