package com.testplatform.modules.organization.dto;

import java.util.ArrayList;
import java.util.List;

import com.testplatform.modules.organization.entity.SystemOrganization;

public class OrganizationResponse {
    private Long id;
    private Long parentId;
    private String orgCode;
    private String orgName;
    private Long leaderUserId;
    private String leaderName;
    private Integer sortOrder;
    private String status;
    private List<OrganizationResponse> children = new ArrayList<OrganizationResponse>();

    public static OrganizationResponse from(SystemOrganization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setParentId(organization.getParentId());
        response.setOrgCode(organization.getOrgCode());
        response.setOrgName(organization.getOrgName());
        response.setLeaderUserId(organization.getLeaderUserId());
        response.setSortOrder(organization.getSortOrder());
        response.setStatus(organization.getStatus());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrganizationResponse> getChildren() { return children; }
    public void setChildren(List<OrganizationResponse> children) { this.children = children; }
}
