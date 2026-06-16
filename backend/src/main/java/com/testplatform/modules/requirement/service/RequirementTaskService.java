package com.testplatform.modules.requirement.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.requirement.dto.RequirementTaskRequest;
import com.testplatform.modules.requirement.dto.RequirementTaskResponse;
import com.testplatform.modules.requirement.entity.Requirement;
import com.testplatform.modules.requirement.entity.RequirementTask;
import com.testplatform.modules.requirement.mapper.RequirementTaskMapper;
import com.testplatform.modules.user.entity.SystemUser;
import com.testplatform.modules.user.service.UserService;

@Service
public class RequirementTaskService {

    private static final List<String> DEV_STATUSES = Arrays.asList("DEV_SCHEDULED", "DEV_TODO", "DEV_DOING", "DEV_INTEGRATING", "DEV_DONE");
    private static final List<String> TEST_STATUSES = Arrays.asList("TEST_SCHEDULED", "TEST_TODO", "TEST_DOING", "TEST_DONE");

    private final RequirementTaskMapper requirementTaskMapper;
    private final RequirementService requirementService;
    private final UserService userService;

    public RequirementTaskService(RequirementTaskMapper requirementTaskMapper, RequirementService requirementService, UserService userService) {
        this.requirementTaskMapper = requirementTaskMapper;
        this.requirementService = requirementService;
        this.userService = userService;
    }

    public List<RequirementTaskResponse> list(Long requirementId) {
        requirementService.getRequiredRequirement(requirementId);
        return requirementTaskMapper.selectList(new LambdaQueryWrapper<RequirementTask>()
                .eq(RequirementTask::getRequirementId, requirementId)
                .orderByAsc(RequirementTask::getSortOrder)
                .orderByAsc(RequirementTask::getId))
            .stream()
            .map(RequirementTaskResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public RequirementTaskResponse create(Long requirementId, RequirementTaskRequest request) {
        Requirement requirement = requirementService.getRequiredRequirement(requirementId);
        validateRequest(requirement, request);
        SystemUser assignee = userService.getRequiredUser(request.getAssigneeId());
        RequirementTask task = new RequirementTask();
        task.setRequirementId(requirementId);
        apply(task, request, assignee);
        task.setCreatedBy(CurrentUserContext.getUserId());
        requirementTaskMapper.insert(task);
        return RequirementTaskResponse.from(requirementTaskMapper.selectById(task.getId()));
    }

    @Transactional
    public RequirementTaskResponse update(Long requirementId, Long taskId, RequirementTaskRequest request) {
        Requirement requirement = requirementService.getRequiredRequirement(requirementId);
        RequirementTask task = getRequiredTask(requirementId, taskId);
        validateRequest(requirement, request);
        SystemUser assignee = userService.getRequiredUser(request.getAssigneeId());
        apply(task, request, assignee);
        requirementTaskMapper.updateById(task);
        return RequirementTaskResponse.from(requirementTaskMapper.selectById(taskId));
    }

    @Transactional
    public void delete(Long requirementId, Long taskId) {
        requirementService.getRequiredRequirement(requirementId);
        getRequiredTask(requirementId, taskId);
        requirementTaskMapper.deleteById(taskId);
    }

    public List<RequirementTask> listEntities(Long requirementId, String taskType) {
        return requirementTaskMapper.selectList(new LambdaQueryWrapper<RequirementTask>()
            .eq(RequirementTask::getRequirementId, requirementId)
            .eq(RequirementTask::getTaskType, taskType));
    }

    private RequirementTask getRequiredTask(Long requirementId, Long taskId) {
        RequirementTask task = requirementTaskMapper.selectById(taskId);
        if (task == null || !requirementId.equals(task.getRequirementId())) {
            throw new BusinessException("REQUIREMENT_TASK_NOT_FOUND", "子任务不存在");
        }
        return task;
    }

    private void validateRequest(Requirement requirement, RequirementTaskRequest request) {
        String status = requirement.getStatus() == null ? "COLLECTING" : requirement.getStatus();
        if ("DEV".equals(request.getTaskType())) {
            if (!"DEVELOPING".equals(status)) {
                throw new BusinessException("INVALID_REQUIREMENT_TASK_STAGE", "仅需求开发阶段可以维护开发子任务");
            }
            if (!DEV_STATUSES.contains(request.getStatus())) {
                throw new BusinessException("INVALID_REQUIREMENT_TASK_STATUS", "开发子任务状态不合法");
            }
            validateAssignee(requirement.getDevAssigneeIds(), request.getAssigneeId(), "开发子任务负责人必须是已分配开发人员");
        } else if ("TEST".equals(request.getTaskType())) {
            if (!"TESTING".equals(status)) {
                throw new BusinessException("INVALID_REQUIREMENT_TASK_STAGE", "仅需求测试阶段可以维护测试子任务");
            }
            if (!TEST_STATUSES.contains(request.getStatus())) {
                throw new BusinessException("INVALID_REQUIREMENT_TASK_STATUS", "测试子任务状态不合法");
            }
            validateAssignee(requirement.getTestAssigneeIds(), request.getAssigneeId(), "测试子任务负责人必须是已分配测试人员");
        } else {
            throw new BusinessException("INVALID_REQUIREMENT_TASK_TYPE", "子任务类型不合法");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException("INVALID_REQUIREMENT_TASK_TIME", "结束时间不能早于开始时间");
        }
    }

    private void validateAssignee(String assigneeIds, Long assigneeId, String message) {
        if (!parseIds(assigneeIds).contains(assigneeId)) {
            throw new BusinessException("INVALID_REQUIREMENT_TASK_ASSIGNEE", message);
        }
    }

    private void apply(RequirementTask task, RequirementTaskRequest request, SystemUser assignee) {
        task.setTaskType(request.getTaskType());
        task.setRoleType(request.getRoleType());
        task.setName(request.getName());
        task.setAssigneeId(request.getAssigneeId());
        task.setAssigneeName(assignee.getDisplayName() == null || assignee.getDisplayName().isEmpty() ? assignee.getUsername() : assignee.getDisplayName());
        task.setStatus(request.getStatus());
        task.setRemark(request.getRemark());
        task.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
    }

    private Set<Long> parseIds(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(value.split(","))
            .filter(item -> !item.trim().isEmpty())
            .map(item -> Long.valueOf(item.trim()))
            .collect(Collectors.toSet());
    }
}
