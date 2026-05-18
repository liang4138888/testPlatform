package com.testplatform.modules.casesuite.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CaseNodesSaveRequest {

    @NotNull
    @Valid
    private List<CaseNodeWriteRequest> nodes;

    public List<CaseNodeWriteRequest> getNodes() {
        return nodes;
    }

    public void setNodes(List<CaseNodeWriteRequest> nodes) {
        this.nodes = nodes;
    }
}
