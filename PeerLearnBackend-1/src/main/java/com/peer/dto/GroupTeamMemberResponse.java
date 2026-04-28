package com.peer.dto;

public class GroupTeamMemberResponse {
    private Long id;
    private String fullName;
    private String email;
    private String userId;
    private String groupCode;

    public GroupTeamMemberResponse() {}

    public GroupTeamMemberResponse(Long id, String fullName, String email, String userId, String groupCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.userId = userId;
        this.groupCode = groupCode;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getUserId() { return userId; }
    public String getGroupCode() { return groupCode; }

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
}