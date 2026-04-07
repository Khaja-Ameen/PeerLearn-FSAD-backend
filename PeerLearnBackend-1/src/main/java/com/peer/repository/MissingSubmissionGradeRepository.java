package com.peer.repository;

import com.peer.entity.MissingSubmissionGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissingSubmissionGradeRepository extends JpaRepository<MissingSubmissionGrade, Long> {
    Optional<MissingSubmissionGrade> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}