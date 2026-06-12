package com.testplatform.modules.bug.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.bug.entity.BugAttachment;

public class BugAttachmentResponse {
    private Long id;
    private Long fileId;
    private String attachmentType;
    private LocalDateTime createdAt;

    public static BugAttachmentResponse from(BugAttachment attachment) {
        BugAttachmentResponse response = new BugAttachmentResponse();
        response.setId(attachment.getId());
        response.setFileId(attachment.getFileId());
        response.setAttachmentType(attachment.getAttachmentType());
        response.setCreatedAt(attachment.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
