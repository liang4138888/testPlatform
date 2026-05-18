package com.testplatform.modules.requirement.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.requirement.entity.Requirement;

public class RequirementResponse {

    private final Long id;
    private final Long projectId;
    private final String requirementNo;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RequirementResponse(
        Long id,
        Long projectId,
        String requirementNo,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.requirementNo = requirementNo;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RequirementResponse from(Requirement requirement) {
        return new RequirementResponse(
            requirement.getId(),
            requirement.getProjectId(),
            requirement.getRequirementNo(),
            requirement.getName(),
            requirement.getDescription(),
            requirement.getCreatedAt(),
            requirement.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getRequirementNo() {
        return requirementNo;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
