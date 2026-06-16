package com.testplatform.modules.requirement.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.requirement.entity.RequirementTask;

public class RequirementTaskResponse {

    private final Long id;
    private final Long requirementId;
    private final String taskType;
    private final String roleType;
    private final String name;
    private final Long assigneeId;
    private final String assigneeName;
    private final String status;
    private final String remark;
    private final Integer sortOrder;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public RequirementTaskResponse(Long id, Long requirementId, String taskType, String roleType, String name,
            Long assigneeId, String assigneeName, String status, String remark, Integer sortOrder,
            LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.requirementId = requirementId;
        this.taskType = taskType;
        this.roleType = roleType;
        this.name = name;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.status = status;
        this.remark = remark;
        this.sortOrder = sortOrder;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static RequirementTaskResponse from(RequirementTask task) {
        return new RequirementTaskResponse(
            task.getId(),
            task.getRequirementId(),
            task.getTaskType(),
            task.getRoleType(),
            task.getName(),
            task.getAssigneeId(),
            task.getAssigneeName(),
            task.getStatus(),
            task.getRemark(),
            task.getSortOrder(),
            task.getStartTime(),
            task.getEndTime()
        );
    }

    public Long getId() { return id; }
    public Long getRequirementId() { return requirementId; }
    public String getTaskType() { return taskType; }
    public String getRoleType() { return roleType; }
    public String getName() { return name; }
    public Long getAssigneeId() { return assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public String getStatus() { return status; }
    public String getRemark() { return remark; }
    public Integer getSortOrder() { return sortOrder; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
