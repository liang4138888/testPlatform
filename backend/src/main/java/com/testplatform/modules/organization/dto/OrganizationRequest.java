package com.testplatform.modules.organization.dto;

public class OrganizationRequest {
    private Long parentId;
    private String orgCode;
    private String orgName;
    private Long leaderUserId;
    private Integer sortOrder;
    private String status;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
