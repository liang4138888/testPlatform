package com.testplatform.modules.bug.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.bug.entity.Bug;

public class BugListItemResponse {
    private Long id;
    private String bugNo;
    private String title;
    private Long projectId;
    private Long requirementId;
    private String status;
    private String severity;
    private String priority;
    private Long reporterId;
    private Long assigneeId;
    private Integer attachmentCount;
    private LocalDateTime updatedAt;

    public static BugListItemResponse from(Bug bug, Integer attachmentCount) {
        BugListItemResponse response = new BugListItemResponse();
        response.setId(bug.getId());
        response.setBugNo(bug.getBugNo());
        response.setTitle(bug.getTitle());
        response.setProjectId(bug.getProjectId());
        response.setRequirementId(bug.getRequirementId());
        response.setStatus(bug.getStatus());
        response.setSeverity(bug.getSeverity());
        response.setPriority(bug.getPriority());
        response.setReporterId(bug.getReporterId());
        response.setAssigneeId(bug.getAssigneeId());
        response.setAttachmentCount(attachmentCount);
        response.setUpdatedAt(bug.getUpdatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBugNo() { return bugNo; }
    public void setBugNo(String bugNo) { this.bugNo = bugNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getRequirementId() { return requirementId; }
    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public Integer getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(Integer attachmentCount) { this.attachmentCount = attachmentCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
