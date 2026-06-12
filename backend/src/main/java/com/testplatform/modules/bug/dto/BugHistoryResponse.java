package com.testplatform.modules.bug.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.bug.entity.BugHistory;

public class BugHistoryResponse {
    private Long id;
    private String actionType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long operatorId;
    private LocalDateTime createdAt;

    public static BugHistoryResponse from(BugHistory history) {
        BugHistoryResponse response = new BugHistoryResponse();
        response.setId(history.getId());
        response.setActionType(history.getActionType());
        response.setFieldName(history.getFieldName());
        response.setOldValue(history.getOldValue());
        response.setNewValue(history.getNewValue());
        response.setOperatorId(history.getOperatorId());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
