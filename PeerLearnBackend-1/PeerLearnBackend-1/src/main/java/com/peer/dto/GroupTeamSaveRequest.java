package com.peer.dto;

import java.util.ArrayList;
import java.util.List;

public class GroupTeamSaveRequest {

    private List<GroupItem> groups = new ArrayList<>();

    public List<GroupItem> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupItem> groups) {
        this.groups = groups;
    }

    public static class GroupItem {
        private String groupCode;
        private List<Long> studentIds = new ArrayList<>();

        public String getGroupCode() { return groupCode; }
        public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

        public List<Long> getStudentIds() { return studentIds; }
        public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
    }
}