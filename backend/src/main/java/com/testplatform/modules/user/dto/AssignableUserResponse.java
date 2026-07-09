package com.testplatform.modules.user.dto;

import java.util.List;

import com.testplatform.modules.user.entity.SystemUser;

public class AssignableUserResponse {

    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String avatar;
    private Long organizationId;
    private String status;
    private List<String> roleCodes;
    private List<String> roleNames;

    public static AssignableUserResponse from(SystemUser user) {
        AssignableUserResponse response = new AssignableUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setOrganizationId(user.getOrganizationId());
        response.setStatus(user.getStatus());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; }
    public List<String> getRoleNames() { return roleNames; }
    public void setRoleNames(List<String> roleNames) { this.roleNames = roleNames; }
}
