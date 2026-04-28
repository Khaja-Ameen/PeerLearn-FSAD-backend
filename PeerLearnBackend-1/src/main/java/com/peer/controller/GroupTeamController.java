package com.peer.controller;

import com.peer.dto.GroupTeamMemberResponse;
import com.peer.dto.GroupTeamSaveRequest;
import com.peer.dto.SubmissionResponse;
import com.peer.service.GroupTeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-teams")
public class GroupTeamController {

    private final GroupTeamService groupTeamService;

    public GroupTeamController(GroupTeamService groupTeamService) {
        this.groupTeamService = groupTeamService;
    }

    @PostMapping("/assignment/{assignmentId}")
    public ResponseEntity<Void> saveTeacherGroups(
            @PathVariable Long assignmentId,
            @RequestBody GroupTeamSaveRequest request,
            Authentication authentication
    ) {
        groupTeamService.saveTeacherGroups(assignmentId, request, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/assignment/{assignmentId}/mine")
    public ResponseEntity<List<GroupTeamMemberResponse>> myTeam(
            @PathVariable Long assignmentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(groupTeamService.getMyTeam(assignmentId, authentication.getName()));
    }

    @GetMapping("/teacher/ready-submissions")
    public ResponseEntity<List<SubmissionResponse>> teacherReadySubmissions(Authentication authentication) {
        return ResponseEntity.ok(groupTeamService.getTeacherReadySubmissions(authentication.getName()));
    }

    @PatchMapping("/submission/{submissionId}/grade")
    public ResponseEntity<SubmissionResponse> gradeGroupSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                groupTeamService.gradeGroupFromSubmission(
                        submissionId,
                        request.score(),
                        request.feedback(),
                        authentication.getName()
                )
        );
    }

    public record GradeRequest(Integer score, String feedback) {}
}