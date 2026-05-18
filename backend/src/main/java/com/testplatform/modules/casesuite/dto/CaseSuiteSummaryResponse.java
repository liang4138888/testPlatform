package com.testplatform.modules.casesuite.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.casesuite.entity.CaseSuite;

public class CaseSuiteSummaryResponse {

    private final Long id;
    private final Long requirementId;
    private final String name;
    private final String status;
    private final Long originalFileId;
    private final Long exportedFileId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CaseSuiteSummaryResponse(
        Long id,
        Long requirementId,
        String name,
        String status,
        Long originalFileId,
        Long exportedFileId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.requirementId = requirementId;
        this.name = name;
        this.status = status;
        this.originalFileId = originalFileId;
        this.exportedFileId = exportedFileId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CaseSuiteSummaryResponse from(CaseSuite suite) {
        return new CaseSuiteSummaryResponse(
            suite.getId(),
            suite.getRequirementId(),
            suite.getName(),
            suite.getStatus(),
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

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
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
