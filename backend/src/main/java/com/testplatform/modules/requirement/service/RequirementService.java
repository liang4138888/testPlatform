package com.testplatform.modules.requirement.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.project.service.ProjectService;
import com.testplatform.modules.requirement.dto.RequirementAssignRequest;
import com.testplatform.modules.requirement.dto.RequirementCreateRequest;
import com.testplatform.modules.requirement.dto.RequirementResponse;
import com.testplatform.modules.requirement.dto.RequirementTransitionRequest;
import com.testplatform.modules.requirement.entity.Requirement;
import com.testplatform.modules.requirement.entity.RequirementTask;
import com.testplatform.modules.requirement.mapper.RequirementMapper;
import com.testplatform.modules.requirement.mapper.RequirementTaskMapper;
import com.testplatform.modules.user.service.UserService;

@Service
public class RequirementService {

    private static final List<String> STATUSES = Arrays.asList(
        "COLLECTING",
        "PRE_REVIEW",
        "DETAIL_REVIEW",
        "ASSIGNING",
        "DEVELOPING",
        "TESTING",
        "UI_ACCEPTING",
        "PRODUCT_ACCEPTING",
        "DONE"
    );

    private final RequirementMapper requirementMapper;
    private final RequirementTaskMapper requirementTaskMapper;
    private final ProjectService projectService;
    private final UserService userService;

    public RequirementService(RequirementMapper requirementMapper, RequirementTaskMapper requirementTaskMapper, ProjectService projectService, UserService userService) {
        this.requirementMapper = requirementMapper;
        this.requirementTaskMapper = requirementTaskMapper;
        this.projectService = projectService;
        this.userService = userService;
    }

    public Requirement getRequiredRequirement(Long requirementId) {
        Requirement requirement = requirementMapper.selectById(requirementId);
        if (requirement == null) {
            throw new BusinessException("REQUIREMENT_NOT_FOUND", "需求不存在");
        }
        if (!userService.canViewAllData() && !CurrentUserContext.getUserId().equals(requirement.getCreatedBy())) {
            projectService.getRequiredProject(requirement.getProjectId());
        }
        return requirement;
    }

    public List<RequirementResponse> listRequirements(Long projectId) {
        projectService.getRequiredProject(projectId);
        LambdaQueryWrapper<Requirement> query = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getProjectId, projectId)
                .orderByDesc(Requirement::getUpdatedAt);
        if (!userService.canViewAllData()) {
            query.eq(Requirement::getCreatedBy, CurrentUserContext.getUserId());
        }
        return requirementMapper.selectList(query)
            .stream()
            .map(RequirementResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public RequirementResponse createRequirement(Long projectId, RequirementCreateRequest request) {
        projectService.getRequiredProject(projectId);
        Long exists = requirementMapper.selectCount(new LambdaQueryWrapper<Requirement>()
            .eq(Requirement::getProjectId, projectId)
            .eq(Requirement::getRequirementNo, request.getRequirementNo()));
        if (exists > 0) {
            throw new BusinessException("REQUIREMENT_NO_EXISTS", "需求编号已存在");
        }

        Requirement requirement = new Requirement();
        requirement.setProjectId(projectId);
        requirement.setRequirementNo(request.getRequirementNo());
        requirement.setName(request.getName());
        requirement.setOwnerName(request.getOwnerName());
        requirement.setProposedDate(request.getProposedDate());
        requirement.setProposedIteration(request.getProposedIteration());
        requirement.setReleaseIteration(request.getReleaseIteration());
        requirement.setPriority(request.getPriority());
        requirement.setDescription(request.getDescription());
        requirement.setPrd(request.getPrd());
        requirement.setPrototype(request.getPrototype());
        requirement.setParticipantDomains(request.getParticipantDomains());
        requirement.setInvolvedModules(request.getInvolvedModules());
        requirement.setStatus("COLLECTING");
        requirement.setCreatedBy(CurrentUserContext.getUserId());
        requirementMapper.insert(requirement);
        return RequirementResponse.from(requirementMapper.selectById(requirement.getId()));
    }

    @Transactional
    public RequirementResponse assignRequirement(Long requirementId, RequirementAssignRequest request) {
        Requirement requirement = getRequiredRequirement(requirementId);
        String currentStatus = requirement.getStatus() == null ? "COLLECTING" : requirement.getStatus();
        if (!"ASSIGNING".equals(currentStatus)) {
            throw new BusinessException("INVALID_REQUIREMENT_ASSIGNMENT", "仅需求分配阶段可以分配人员");
        }
        requirement.setDevAssigneeIds(request.getDevAssigneeIds());
        requirement.setTestAssigneeIds(request.getTestAssigneeIds());
        requirementMapper.updateById(requirement);
        return RequirementResponse.from(requirementMapper.selectById(requirementId));
    }

    @Transactional
    public RequirementResponse transitionRequirement(Long requirementId, RequirementTransitionRequest request) {
        Requirement requirement = getRequiredRequirement(requirementId);
        String currentStatus = requirement.getStatus() == null ? "COLLECTING" : requirement.getStatus();
        String targetStatus = request.getTargetStatus();
        int currentIndex = STATUSES.indexOf(currentStatus);
        int targetIndex = STATUSES.indexOf(targetStatus);
        if (currentIndex < 0 || targetIndex < 0) {
            throw new BusinessException("INVALID_REQUIREMENT_STATUS", "需求状态不合法");
        }
        if (Math.abs(targetIndex - currentIndex) != 1) {
            throw new BusinessException("INVALID_REQUIREMENT_TRANSITION", "只允许流转到上一状态或下一状态");
        }
        if ("ASSIGNING".equals(currentStatus) && "DEVELOPING".equals(targetStatus)) {
            if (isBlank(request.getDevAssigneeIds()) || isBlank(request.getTestAssigneeIds())) {
                throw new BusinessException("REQUIREMENT_ASSIGNEE_REQUIRED", "请先分配开发人员和测试人员");
            }
            requirement.setDevAssigneeIds(request.getDevAssigneeIds());
            requirement.setTestAssigneeIds(request.getTestAssigneeIds());
        }
        if ("DEVELOPING".equals(currentStatus) && "TESTING".equals(targetStatus)) {
            validateTaskGate(requirement.getId(), requirement.getDevAssigneeIds(), "DEV", "DEV_DONE", "仍有开发人员未创建开发子任务", "仍有开发子任务未完成");
        }
        if ("TESTING".equals(currentStatus) && "UI_ACCEPTING".equals(targetStatus)) {
            validateTaskGate(requirement.getId(), requirement.getTestAssigneeIds(), "TEST", "TEST_DONE", "仍有测试人员未创建测试子任务", "仍有测试子任务未完成");
        }
        requirement.setStatus(targetStatus);
        requirementMapper.updateById(requirement);
        return RequirementResponse.from(requirementMapper.selectById(requirementId));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateTaskGate(Long requirementId, String assigneeIds, String taskType, String doneStatus, String missingMessage, String unfinishedMessage) {
        Set<Long> assignedIds = parseIds(assigneeIds);
        if (assignedIds.isEmpty()) {
            throw new BusinessException("REQUIREMENT_ASSIGNEE_REQUIRED", missingMessage);
        }
        List<RequirementTask> tasks = requirementTaskMapper.selectList(new LambdaQueryWrapper<RequirementTask>()
            .eq(RequirementTask::getRequirementId, requirementId)
            .eq(RequirementTask::getTaskType, taskType));
        Set<Long> taskAssigneeIds = tasks.stream().map(RequirementTask::getAssigneeId).collect(Collectors.toSet());
        if (!taskAssigneeIds.containsAll(assignedIds)) {
            throw new BusinessException("REQUIREMENT_TASK_MISSING", missingMessage);
        }
        if (tasks.isEmpty() || tasks.stream().anyMatch(task -> !doneStatus.equals(task.getStatus()))) {
            throw new BusinessException("REQUIREMENT_TASK_UNFINISHED", unfinishedMessage);
        }
    }

    private Set<Long> parseIds(String value) {
        return Arrays.stream(value.split(","))
            .filter(item -> !item.trim().isEmpty())
            .map(item -> Long.valueOf(item.trim()))
            .collect(Collectors.toSet());
    }
}
