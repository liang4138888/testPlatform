package com.testplatform.modules.requirement.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("requirement")
public class Requirement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String requirementNo;
    private String name;
    private String ownerName;
    private String proposedDate;
    private String proposedIteration;
    private String releaseIteration;
    private String priority;
    private String description;
    private String prd;
    private String prototype;
    private String participantDomains;
    private String involvedModules;
    private String devAssigneeIds;
    private String testAssigneeIds;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
