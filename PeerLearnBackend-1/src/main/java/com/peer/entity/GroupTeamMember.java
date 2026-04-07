package com.peer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_team_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_team_assignment_student", columnNames = {"assignment_id", "student_id"})
        }
)
public class GroupTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(nullable = false, length = 40)
    private String groupCode;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Assignment getAssignment() { return assignment; }
    public User getStudent() { return student; }
    public String getGroupCode() { return groupCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    public void setStudent(User student) { this.student = student; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final GroupTeamMember m = new GroupTeamMember();
        public Builder assignment(Assignment v) { m.assignment = v; return this; }
        public Builder student(User v) { m.student = v; return this; }
        public Builder groupCode(String v) { m.groupCode = v; return this; }
        public GroupTeamMember build() { return m; }
    }
}