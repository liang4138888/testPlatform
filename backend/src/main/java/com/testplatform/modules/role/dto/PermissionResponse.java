package com.testplatform.modules.role.dto;

import com.testplatform.modules.user.entity.SystemPermission;

public class PermissionResponse {

    private Long id;
    private String permissionCode;
    private String permissionName;
    private String pageCode;
    private String pageName;
    private Long parentId;
    private String permissionType;
    private Integer sortOrder;

    public static PermissionResponse from(SystemPermission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setPermissionCode(permission.getPermissionCode());
        response.setPermissionName(permission.getPermissionName());
        response.setPageCode(permission.getPageCode());
        response.setPageName(permission.getPageName());
        response.setParentId(permission.getParentId());
        response.setPermissionType(permission.getPermissionType());
        response.setSortOrder(permission.getSortOrder());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getPageCode() { return pageCode; }
    public void setPageCode(String pageCode) { this.pageCode = pageCode; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
