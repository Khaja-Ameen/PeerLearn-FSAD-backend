package com.peer.service;

import com.peer.dto.GroupTeamMemberResponse;
import com.peer.dto.GroupTeamSaveRequest;
import com.peer.dto.SubmissionResponse;
import com.peer.entity.Assignment;
import com.peer.entity.GroupTeamMember;
import com.peer.entity.Notification;
import com.peer.entity.Submission;
import com.peer.entity.User;
import com.peer.repository.AssignmentRepository;
import com.peer.repository.GroupTeamMemberRepository;
import com.peer.repository.NotificationRepository;
import com.peer.repository.SubmissionRepository;
import com.peer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupTeamService {

    private final GroupTeamMemberRepository groupTeamMemberRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationRepository notificationRepository;

    public GroupTeamService(
            GroupTeamMemberRepository groupTeamMemberRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            NotificationRepository notificationRepository
    ) {
        this.groupTeamMemberRepository = groupTeamMemberRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void saveTeacherGroups(Long assignmentId, GroupTeamSaveRequest request, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getTeacher() == null || !teacherEmail.equals(assignment.getTeacher().getEmail())) {
            throw new RuntimeException("Unauthorized");
        }

        if (!Boolean.TRUE.equals(assignment.isGroup())) {
            throw new RuntimeException("This assignment is not a group assignment");
        }

        List<GroupTeamSaveRequest.GroupItem> groups = request != null ? request.getGroups() : Collections.emptyList();
        if (groups == null || groups.isEmpty()) {
            throw new RuntimeException("At least one group is required");
        }

        groupTeamMemberRepository.deleteByAssignment(assignment);

        Set<Long> seenStudents = new HashSet<>();
        String assignmentSection = String.valueOf(assignment.getSection()).trim().toUpperCase();

        for (GroupTeamSaveRequest.GroupItem g : groups) {
            String groupCode = (g.getGroupCode() == null || g.getGroupCode().isBlank())
                    ? "G" + (groups.indexOf(g) + 1)
                    : g.getGroupCode().trim();

            List<Long> studentIds = g.getStudentIds() == null ? Collections.emptyList() : g.getStudentIds();
            if (studentIds.isEmpty()) continue;

            for (Long studentId : studentIds) {
                if (!seenStudents.add(studentId)) {
                    throw new RuntimeException("Student appears in multiple groups");
                }

                User student = userRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

                if (student.getRole() != User.Role.STUDENT) {
                    throw new RuntimeException("Only students can be grouped");
                }

                String studentSection = String.valueOf(student.getSection()).trim().toUpperCase();
                if (!assignmentSection.equals(studentSection)) {
                    throw new RuntimeException("Student " + student.getFullName() + " is not in assignment section");
                }

                GroupTeamMember member = GroupTeamMember.builder()
                        .assignment(assignment)
                        .student(student)
                        .groupCode(groupCode)
                        .build();
                groupTeamMemberRepository.save(member);
            }
        }
    }

    public List<GroupTeamMemberResponse> getMyTeam(Long assignmentId, String studentEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        User me = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        GroupTeamMember self = groupTeamMemberRepository.findByAssignmentAndStudent(assignment, me)
                .orElseThrow(() -> new RuntimeException("You are not assigned to any team for this assignment"));

        return groupTeamMemberRepository.findByAssignmentAndGroupCode(assignment, self.getGroupCode())
                .stream()
                .map(m -> new GroupTeamMemberResponse(
                        m.getStudent().getId(),
                        m.getStudent().getFullName(),
                        m.getStudent().getEmail(),
                        m.getStudent().getUserId(),
                        m.getGroupCode()
                ))
                .collect(Collectors.toList());
    }

    public List<SubmissionResponse> getTeacherReadySubmissions(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<Submission> recent = submissionRepository.findRecentByTeacher(teacher);

        return recent.stream()
                .filter(this::isReadyForTeacherGrading)
                .map(SubmissionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubmissionResponse gradeGroupFromSubmission(Long submissionId, Integer score, String feedback, String teacherEmail) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        Assignment assignment = submission.getAssignment();
        if (assignment == null || assignment.getTeacher() == null || !teacherEmail.equals(assignment.getTeacher().getEmail())) {
            throw new RuntimeException("Unauthorized");
        }

        if (!Boolean.TRUE.equals(assignment.isGroup())) {
            throw new RuntimeException("Use normal grading for non-group assignments");
        }

        if (!isReadyForTeacherGrading(submission)) {
            throw new RuntimeException("All group members must submit before grading");
        }

        Integer max = assignment.getPoints() == null ? 100 : assignment.getPoints();
        if (score == null || score < 0 || score > max) {
            throw new RuntimeException("Score must be between 0 and " + max);
        }

        GroupTeamMember ref = groupTeamMemberRepository.findByAssignmentAndStudent(assignment, submission.getStudent())
                .orElseThrow(() -> new RuntimeException("Group mapping missing"));

        List<GroupTeamMember> team = groupTeamMemberRepository.findByAssignmentAndGroupCode(assignment, ref.getGroupCode());
        List<Long> memberIds = team.stream().map(m -> m.getStudent().getId()).collect(Collectors.toList());

        List<Submission> allSubsForAssignment = submissionRepository.findByAssignment(assignment);
        List<Submission> teamSubs = allSubsForAssignment.stream()
                .filter(s -> memberIds.contains(s.getStudent().getId()))
                .collect(Collectors.toList());

        if (teamSubs.size() != memberIds.size()) {
            throw new RuntimeException("All group members must submit before grading");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Submission s : teamSubs) {
            s.setTeacherScore(score);
            s.setTeacherFeedback(feedback);
            s.setStatus(Submission.Status.FULLY_GRADED);
            s.setGradedAt(now);
        }
        submissionRepository.saveAll(teamSubs);

        for (Submission s : teamSubs) {
            Notification n = Notification.builder()
                    .user(s.getStudent())
                    .title("Group Grade Posted")
                    .message("Your group submission for '" + assignment.getTitle() + "' was graded: " + score + "/" + max)
                    .type(Notification.NotificationType.GRADE)
                    .build();
            notificationRepository.save(n);
        }

        Submission current = teamSubs.stream()
                .filter(s -> s.getId().equals(submissionId))
                .findFirst()
                .orElse(teamSubs.get(0));

        return new SubmissionResponse(current);
    }

    private boolean isReadyForTeacherGrading(Submission submission) {
        Assignment assignment = submission.getAssignment();
        if (assignment == null || !Boolean.TRUE.equals(assignment.isGroup())) return true;

        GroupTeamMember ref = groupTeamMemberRepository.findByAssignmentAndStudent(assignment, submission.getStudent()).orElse(null);
        if (ref == null) return false;

        List<GroupTeamMember> team = groupTeamMemberRepository.findByAssignmentAndGroupCode(assignment, ref.getGroupCode());
        List<Long> memberIds = team.stream().map(m -> m.getStudent().getId()).collect(Collectors.toList());
        if (memberIds.isEmpty()) return false;

        List<Submission> all = submissionRepository.findByAssignment(assignment);
        long count = all.stream().filter(s -> memberIds.contains(s.getStudent().getId())).count();

        return count == memberIds.size();
    }
}