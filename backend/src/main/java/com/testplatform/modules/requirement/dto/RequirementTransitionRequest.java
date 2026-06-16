package com.testplatform.modules.requirement.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RequirementTransitionRequest {

    @NotBlank
    private String targetStatus;

    private String remark;

    @Size(max = 200)
    private String devAssigneeIds;

    @Size(max = 200)
    private String testAssigneeIds;

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

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
