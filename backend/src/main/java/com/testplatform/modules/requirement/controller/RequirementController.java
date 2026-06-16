package com.testplatform.modules.requirement.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.requirement.dto.RequirementAssignRequest;
import com.testplatform.modules.requirement.dto.RequirementCreateRequest;
import com.testplatform.modules.requirement.dto.RequirementResponse;
import com.testplatform.modules.requirement.dto.RequirementTaskRequest;
import com.testplatform.modules.requirement.dto.RequirementTaskResponse;
import com.testplatform.modules.requirement.dto.RequirementTransitionRequest;
import com.testplatform.modules.requirement.service.RequirementService;
import com.testplatform.modules.requirement.service.RequirementTaskService;

@RestController
public class RequirementController {

    private final RequirementService requirementService;
    private final RequirementTaskService requirementTaskService;

    public RequirementController(RequirementService requirementService, RequirementTaskService requirementTaskService) {
        this.requirementService = requirementService;
        this.requirementTaskService = requirementTaskService;
    }

    @GetMapping("/api/projects/{projectId}/requirements")
    public ApiResponse<List<RequirementResponse>> listRequirements(@PathVariable Long projectId) {
        return ApiResponse.ok(requirementService.listRequirements(projectId));
    }

    @PostMapping("/api/projects/{projectId}/requirements")
    public ApiResponse<RequirementResponse> createRequirement(
        @PathVariable Long projectId,
        @Valid @RequestBody RequirementCreateRequest request
    ) {
        return ApiResponse.ok(requirementService.createRequirement(projectId, request));
    }

    @PostMapping("/api/requirements/{requirementId}/assign")
    public ApiResponse<RequirementResponse> assignRequirement(
        @PathVariable Long requirementId,
        @Valid @RequestBody RequirementAssignRequest request
    ) {
        return ApiResponse.ok(requirementService.assignRequirement(requirementId, request));
    }

    @PostMapping("/api/requirements/{requirementId}/transition")
    public ApiResponse<RequirementResponse> transitionRequirement(
        @PathVariable Long requirementId,
        @Valid @RequestBody RequirementTransitionRequest request
    ) {
        return ApiResponse.ok(requirementService.transitionRequirement(requirementId, request));
    }

    @GetMapping("/api/requirements/{requirementId}/tasks")
    public ApiResponse<List<RequirementTaskResponse>> listTasks(@PathVariable Long requirementId) {
        return ApiResponse.ok(requirementTaskService.list(requirementId));
    }

    @PostMapping("/api/requirements/{requirementId}/tasks")
    public ApiResponse<RequirementTaskResponse> createTask(
        @PathVariable Long requirementId,
        @Valid @RequestBody RequirementTaskRequest request
    ) {
        return ApiResponse.ok(requirementTaskService.create(requirementId, request));
    }

    @PutMapping("/api/requirements/{requirementId}/tasks/{taskId}")
    public ApiResponse<RequirementTaskResponse> updateTask(
        @PathVariable Long requirementId,
        @PathVariable Long taskId,
        @Valid @RequestBody RequirementTaskRequest request
    ) {
        return ApiResponse.ok(requirementTaskService.update(requirementId, taskId, request));
    }

    @DeleteMapping("/api/requirements/{requirementId}/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long requirementId, @PathVariable Long taskId) {
        requirementTaskService.delete(requirementId, taskId);
        return ApiResponse.ok(null);
    }
}
