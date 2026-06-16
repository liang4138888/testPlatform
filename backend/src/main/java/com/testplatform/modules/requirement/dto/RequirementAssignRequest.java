package com.testplatform.modules.requirement.dto;

import javax.validation.constraints.Size;

public class RequirementAssignRequest {

    @Size(max = 200)
    private String devAssigneeIds;

    @Size(max = 200)
    private String testAssigneeIds;

    public String getDevAssigneeIds() {
        return devAssigneeIds;
    }

    public void setDevAssigneeIds(String devAssigneeIds) {
        this.devAssigneeIds = devAssigneeIds;
    }

    public String getTestAssigneeIds() {
        return testAssigneeIds;
    }

    public void setTestAssigneeIds(String testAssigneeIds) {
        this.testAssigneeIds = testAssigneeIds;
    }
}
