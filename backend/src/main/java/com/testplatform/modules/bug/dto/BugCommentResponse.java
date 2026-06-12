package com.testplatform.modules.bug.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.bug.entity.BugComment;

public class BugCommentResponse {
    private Long id;
    private Long bugId;
    private String content;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static BugCommentResponse from(BugComment comment) {
        BugCommentResponse response = new BugCommentResponse();
        response.setId(comment.getId());
        response.setBugId(comment.getBugId());
        response.setContent(comment.getContent());
        response.setCreatedBy(comment.getCreatedBy());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBugId() { return bugId; }
    public void setBugId(Long bugId) { this.bugId = bugId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
