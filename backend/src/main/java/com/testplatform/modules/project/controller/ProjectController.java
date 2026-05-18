package com.testplatform.modules.project.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.project.dto.ProjectCreateRequest;
import com.testplatform.modules.project.dto.ProjectResponse;
import com.testplatform.modules.project.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> listProjects() {
        return ApiResponse.ok(projectService.listProjects());
    }

    @PostMapping
    public ApiResponse<ProjectResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return ApiResponse.ok(projectService.createProject(request));
    }
}
