package com.peer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "missing_submission_grades",
    uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
)
public class MissingSubmissionGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 2000)
    private String feedback;

    @Column(name = "graded_by_teacher_id", nullable = false)
    private Long gradedByTeacherId;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt = LocalDateTime.now();

    @Column(name = "reason", nullable = false)
    private String reason = "NO_SUBMISSION";

    public Long getId() { return id; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Long getGradedByTeacherId() { return gradedByTeacherId; }
    public void setGradedByTeacherId(Long gradedByTeacherId) { this.gradedByTeacherId = gradedByTeacherId; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}