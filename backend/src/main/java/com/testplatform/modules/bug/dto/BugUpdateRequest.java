package com.testplatform.modules.bug.dto;

public class BugUpdateRequest {
    private String title;
    private String description;
    private String status;
    private String severity;
    private String priority;
    private Long assigneeId;
    private Long caseSuiteId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public Long getCaseSuiteId() { return caseSuiteId; }
    public void setCaseSuiteId(Long caseSuiteId) { this.caseSuiteId = caseSuiteId; }
}
