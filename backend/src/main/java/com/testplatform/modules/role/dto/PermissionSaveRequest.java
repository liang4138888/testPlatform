package com.testplatform.modules.role.dto;

public class PermissionSaveRequest {

    private String permissionCode;
    private String permissionName;
    private String pageCode;
    private String pageName;
    private Long parentId;
    private String permissionType;
    private Integer sortOrder;

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
