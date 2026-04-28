package com.peer.repository;

import com.peer.entity.Assignment;
import com.peer.entity.GroupTeamMember;
import com.peer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // add
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupTeamMemberRepository extends JpaRepository<GroupTeamMember, Long> {
    List<GroupTeamMember> findByAssignment(Assignment assignment);
    List<GroupTeamMember> findByAssignmentAndGroupCode(Assignment assignment, String groupCode);
    Optional<GroupTeamMember> findByAssignmentAndStudent(Assignment assignment, User student);
    void deleteByAssignment(Assignment assignment);

    // add-only method needed by TeacherGradingV2Service
    @Query("select g.student.id from GroupTeamMember g where g.assignment.id = :assignmentId")
    List<Long> findAllStudentIdsByAssignmentId(@Param("assignmentId") Long assignmentId);
}