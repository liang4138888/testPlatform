package com.testplatform.modules.notification.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.notification.entity.Notification;

public class NotificationResponse {
    private Long id;
    private Long bizId;
    private String bizType;
    private String title;
    private String content;
    private String readStatus;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setBizId(notification.getBizId());
        response.setBizType(notification.getBizType());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setReadStatus(notification.getReadStatus());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReadStatus() { return readStatus; }
    public void setReadStatus(String readStatus) { this.readStatus = readStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
