package com.testplatform.modules.requirement.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.requirement.entity.Requirement;

public class RequirementResponse {

    private final Long id;
    private final Long projectId;
    private final String requirementNo;
    private final String name;
    private final String ownerName;
    private final String proposedDate;
    private final String proposedIteration;
    private final String releaseIteration;
    private final String priority;
    private final String description;
    private final String prd;
    private final String prototype;
    private final String participantDomains;
    private final String involvedModules;
    private final String devAssigneeIds;
    private final String testAssigneeIds;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RequirementResponse(
        Long id,
        Long projectId,
        String requirementNo,
        String name,
        String ownerName,
        String proposedDate,
        String proposedIteration,
        String releaseIteration,
        String priority,
        String description,
        String prd,
        String prototype,
        String participantDomains,
        String involvedModules,
        String devAssigneeIds,
        String testAssigneeIds,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.requirementNo = requirementNo;
        this.name = name;
        this.ownerName = ownerName;
        this.proposedDate = proposedDate;
        this.proposedIteration = proposedIteration;
        this.releaseIteration = releaseIteration;
        this.priority = priority;
        this.description = description;
        this.prd = prd;
        this.prototype = prototype;
        this.participantDomains = participantDomains;
        this.involvedModules = involvedModules;
        this.devAssigneeIds = devAssigneeIds;
        this.testAssigneeIds = testAssigneeIds;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RequirementResponse from(Requirement requirement) {
        return new RequirementResponse(
            requirement.getId(),
            requirement.getProjectId(),
            requirement.getRequirementNo(),
            requirement.getName(),
            requirement.getOwnerName(),
            requirement.getProposedDate(),
            requirement.getProposedIteration(),
            requirement.getReleaseIteration(),
            requirement.getPriority(),
            requirement.getDescription(),
            requirement.getPrd(),
            requirement.getPrototype(),
            requirement.getParticipantDomains(),
            requirement.getInvolvedModules(),
            requirement.getDevAssigneeIds(),
            requirement.getTestAssigneeIds(),
            requirement.getStatus(),
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

    public String getOwnerName() {
        return ownerName;
    }

    public String getProposedDate() {
        return proposedDate;
    }

    public String getProposedIteration() {
        return proposedIteration;
    }

    public String getReleaseIteration() {
        return releaseIteration;
    }

    public String getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public String getPrd() {
        return prd;
    }

    public String getPrototype() {
        return prototype;
    }

    public String getParticipantDomains() {
        return participantDomains;
    }

    public String getInvolvedModules() {
        return involvedModules;
    }

    public String getDevAssigneeIds() {
        return devAssigneeIds;
    }

    public String getTestAssigneeIds() {
        return testAssigneeIds;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
