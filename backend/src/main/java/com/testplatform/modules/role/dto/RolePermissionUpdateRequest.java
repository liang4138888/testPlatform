package com.testplatform.modules.role.dto;

import java.util.List;

public class RolePermissionUpdateRequest {

    private List<String> permissions;

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
