package com.testplatform.modules.casesuite.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.testplatform.modules.casesuite.entity.CaseSuite;

public class CaseSuiteResponse {

    private final Long id;
    private final Long requirementId;
    private final String name;
    private final String status;
    private final Long originalFileId;
    private final Long exportedFileId;
    private final String requirementNo;
    private final String requirementName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<CaseNodeResponse> nodes;

    public CaseSuiteResponse(
        Long id,
        Long requirementId,
        String name,
        String status,
        Long originalFileId,
        Long exportedFileId,
        String requirementNo,
        String requirementName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CaseNodeResponse> nodes
    ) {
        this.id = id;
        this.requirementId = requirementId;
        this.name = name;
        this.status = status;
        this.originalFileId = originalFileId;
        this.exportedFileId = exportedFileId;
        this.requirementNo = requirementNo;
        this.requirementName = requirementName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nodes = nodes;
    }

    public static CaseSuiteResponse from(
        CaseSuite suite,
        String requirementNo,
        String requirementName,
        List<CaseNodeResponse> nodes
    ) {
        return new CaseSuiteResponse(
            suite.getId(),
            suite.getRequirementId(),
            suite.getName(),
            suite.getStatus(),
            suite.getOriginalFileId(),
            suite.getExportedFileId(),
            requirementNo,
            requirementName,
            suite.getCreatedAt(),
            suite.getUpdatedAt(),
            nodes
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

    public String getRequirementNo() {
        return requirementNo;
    }

    public String getRequirementName() {
        return requirementName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CaseNodeResponse> getNodes() {
        return nodes;
    }
}
