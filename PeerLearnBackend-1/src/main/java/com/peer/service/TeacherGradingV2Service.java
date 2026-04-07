package com.peer.service;

import com.peer.dto.MissingSubmissionGradeRequest;
import com.peer.entity.Assignment;
import com.peer.entity.MissingSubmissionGrade;
import com.peer.repository.AssignmentRepository;
import com.peer.repository.GroupTeamMemberRepository;
import com.peer.repository.MissingSubmissionGradeRepository;
import com.peer.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherGradingV2Service {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GroupTeamMemberRepository groupTeamMemberRepository;
    private final MissingSubmissionGradeRepository missingSubmissionGradeRepository;

    public TeacherGradingV2Service(
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            GroupTeamMemberRepository groupTeamMemberRepository,
            MissingSubmissionGradeRepository missingSubmissionGradeRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.groupTeamMemberRepository = groupTeamMemberRepository;
        this.missingSubmissionGradeRepository = missingSubmissionGradeRepository;
    }

    public boolean isDueDateExceeded(Long assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return a.getDueDate() != null && LocalDateTime.now().isAfter(a.getDueDate());
    }

    public boolean canGradeGroupSubmission(Long assignmentId, boolean isGroup, boolean teacherSelectMode) {
        if (!isGroup || !teacherSelectMode) return true;

        // Overdue override: allow grading after due date even if not all submitted
        if (isDueDateExceeded(assignmentId)) return true;

        List<Long> memberIds = groupTeamMemberRepository.findAllStudentIdsByAssignmentId(assignmentId);
        if (memberIds == null || memberIds.isEmpty()) return false;

        for (Long memberId : memberIds) {
            boolean submitted = submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, memberId);
            if (!submitted) return false;
        }
        return true;
    }

    @Transactional
    public MissingSubmissionGrade gradeMissingWithZero(MissingSubmissionGradeRequest req, Long teacherId) {
        if (!isDueDateExceeded(req.getAssignmentId())) {
            throw new RuntimeException("Cannot assign 0 before due date.");
        }

        boolean alreadySubmitted = submissionRepository.existsByAssignmentIdAndStudentId(req.getAssignmentId(), req.getStudentId());
        if (alreadySubmitted) {
            throw new RuntimeException("Student has already submitted. Use normal grading endpoint.");
        }

        MissingSubmissionGrade row = missingSubmissionGradeRepository
                .findByAssignmentIdAndStudentId(req.getAssignmentId(), req.getStudentId())
                .orElseGet(MissingSubmissionGrade::new);

        row.setAssignmentId(req.getAssignmentId());
        row.setStudentId(req.getStudentId());
        row.setScore(req.getScore());
        row.setFeedback(req.getFeedback());
        row.setGradedByTeacherId(teacherId);
        row.setReason("NO_SUBMISSION");

        return missingSubmissionGradeRepository.save(row);
    }
}