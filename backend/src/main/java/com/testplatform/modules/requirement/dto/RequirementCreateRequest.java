package com.testplatform.modules.requirement.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RequirementCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String requirementNo;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String ownerName;

    @Size(max = 20)
    private String proposedDate;

    @Size(max = 50)
    private String proposedIteration;

    @Size(max = 50)
    private String releaseIteration;

    @Size(max = 10)
    private String priority;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String prd;

    @Size(max = 500)
    private String prototype;

    @Size(max = 200)
    private String participantDomains;

    @Size(max = 200)
    private String involvedModules;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getProposedDate() {
        return proposedDate;
    }

    public void setProposedDate(String proposedDate) {
        this.proposedDate = proposedDate;
    }

    public String getProposedIteration() {
        return proposedIteration;
    }

    public void setProposedIteration(String proposedIteration) {
        this.proposedIteration = proposedIteration;
    }

    public String getReleaseIteration() {
        return releaseIteration;
    }

    public void setReleaseIteration(String releaseIteration) {
        this.releaseIteration = releaseIteration;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrd() {
        return prd;
    }

    public void setPrd(String prd) {
        this.prd = prd;
    }

    public String getPrototype() {
        return prototype;
    }

    public void setPrototype(String prototype) {
        this.prototype = prototype;
    }

    public String getParticipantDomains() {
        return participantDomains;
    }

    public void setParticipantDomains(String participantDomains) {
        this.participantDomains = participantDomains;
    }

    public String getInvolvedModules() {
        return involvedModules;
    }

    public void setInvolvedModules(String involvedModules) {
        this.involvedModules = involvedModules;
    }
}
