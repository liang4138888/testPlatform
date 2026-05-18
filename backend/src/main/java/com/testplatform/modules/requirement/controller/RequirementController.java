package com.testplatform.modules.requirement.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.requirement.dto.RequirementCreateRequest;
import com.testplatform.modules.requirement.dto.RequirementResponse;
import com.testplatform.modules.requirement.service.RequirementService;

@RestController
@RequestMapping("/api/projects/{projectId}/requirements")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping
    public ApiResponse<List<RequirementResponse>> listRequirements(@PathVariable Long projectId) {
        return ApiResponse.ok(requirementService.listRequirements(projectId));
    }

    @PostMapping
    public ApiResponse<RequirementResponse> createRequirement(
        @PathVariable Long projectId,
        @Valid @RequestBody RequirementCreateRequest request
    ) {
        return ApiResponse.ok(requirementService.createRequirement(projectId, request));
    }
}
