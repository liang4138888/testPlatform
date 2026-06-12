package com.testplatform.modules.role.dto;

import com.testplatform.modules.user.entity.SystemPermission;

public class PermissionResponse {

    private Long id;
    private String permissionCode;
    private String permissionName;

    public static PermissionResponse from(SystemPermission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setPermissionCode(permission.getPermissionCode());
        response.setPermissionName(permission.getPermissionName());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
}
