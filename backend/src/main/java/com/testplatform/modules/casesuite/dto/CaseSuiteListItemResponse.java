package com.testplatform.modules.casesuite.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.casesuite.entity.CaseSuite;
import com.testplatform.modules.project.entity.Project;
import com.testplatform.modules.requirement.entity.Requirement;

public class CaseSuiteListItemResponse {

    private final Long id;
    private final Long requirementId;
    private final Long projectId;
    private final String name;
    private final String status;
    private final String requirementNo;
    private final String requirementName;
    private final String projectName;
    private final Long originalFileId;
    private final Long exportedFileId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CaseSuiteListItemResponse(
        Long id,
        Long requirementId,
        Long projectId,
        String name,
        String status,
        String requirementNo,
        String requirementName,
        String projectName,
        Long originalFileId,
        Long exportedFileId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.requirementId = requirementId;
        this.projectId = projectId;
        this.name = name;
        this.status = status;
        this.requirementNo = requirementNo;
        this.requirementName = requirementName;
        this.projectName = projectName;
        this.originalFileId = originalFileId;
        this.exportedFileId = exportedFileId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CaseSuiteListItemResponse from(CaseSuite suite, Requirement requirement, Project project) {
        return new CaseSuiteListItemResponse(
            suite.getId(),
            suite.getRequirementId(),
            requirement.getProjectId(),
            suite.getName(),
            suite.getStatus(),
            requirement.getRequirementNo(),
            requirement.getName(),
            project.getName(),
            suite.getOriginalFileId(),
            suite.getExportedFileId(),
            suite.getCreatedAt(),
            suite.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getRequirementNo() {
        return requirementNo;
    }

    public String getRequirementName() {
        return requirementName;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getOriginalFileId() {
        return originalFileId;
    }

    public Long getExportedFileId() {
        return exportedFileId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
