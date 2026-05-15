package com.testplatform.requirement.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RequirementCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String requirementNo;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    public String getRequirementNo() {
        return requirementNo;
    }

    public void setRequirementNo(String requirementNo) {
        this.requirementNo = requirementNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
