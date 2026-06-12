package com.testplatform.modules.role.dto;

import java.util.List;

import com.testplatform.modules.user.entity.SystemRole;

public class RoleResponse {

    private Long id;
    private String roleCode;
    private String roleName;
    private List<String> permissions;

    public static RoleResponse from(SystemRole role, List<String> permissions) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getRoleName());
        response.setPermissions(permissions);
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
