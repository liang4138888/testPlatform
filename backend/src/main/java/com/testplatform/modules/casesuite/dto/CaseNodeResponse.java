package com.testplatform.modules.casesuite.dto;

import java.util.List;

import com.testplatform.modules.casesuite.entity.CaseNode;

public class CaseNodeResponse {

    private final Long id;
    private final Long parentId;
    private final String nodeType;
    private final String name;
    private final String description;
    private final Integer sortOrder;
    private final String executionStatus;
    private final List<CaseNodeResponse> children;

    public CaseNodeResponse(
        Long id,
        Long parentId,
        String nodeType,
        String name,
        String description,
        Integer sortOrder,
        String executionStatus,
        List<CaseNodeResponse> children
    ) {
        this.id = id;
        this.parentId = parentId;
        this.nodeType = nodeType;
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.executionStatus = executionStatus;
        this.children = children;
    }

    public static CaseNodeResponse from(CaseNode node, List<CaseNodeResponse> children) {
        return new CaseNodeResponse(
            node.getId(),
            node.getParentId(),
            node.getNodeType(),
            node.getName(),
            node.getDescription(),
            node.getSortOrder(),
            node.getExecutionStatus(),
            children
        );
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public List<CaseNodeResponse> getChildren() {
        return children;
    }
}
