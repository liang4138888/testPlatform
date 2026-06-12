package com.testplatform.modules.bug.dto;

import java.util.List;

import com.testplatform.modules.bug.entity.Bug;

public class BugDetailResponse extends BugListItemResponse {
    private String description;
    private Long caseSuiteId;
    private List<BugAttachmentResponse> attachments;
    private List<BugCommentResponse> comments;
    private List<BugHistoryResponse> histories;

    public static BugDetailResponse from(Bug bug, Integer attachmentCount) {
        BugDetailResponse response = new BugDetailResponse();
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
        response.setDescription(bug.getDescription());
        response.setCaseSuiteId(bug.getCaseSuiteId());
        return response;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCaseSuiteId() { return caseSuiteId; }
    public void setCaseSuiteId(Long caseSuiteId) { this.caseSuiteId = caseSuiteId; }
    public List<BugAttachmentResponse> getAttachments() { return attachments; }
    public void setAttachments(List<BugAttachmentResponse> attachments) { this.attachments = attachments; }
    public List<BugCommentResponse> getComments() { return comments; }
    public void setComments(List<BugCommentResponse> comments) { this.comments = comments; }
    public List<BugHistoryResponse> getHistories() { return histories; }
    public void setHistories(List<BugHistoryResponse> histories) { this.histories = histories; }
}
