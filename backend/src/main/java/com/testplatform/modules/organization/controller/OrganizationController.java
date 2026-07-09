package com.testplatform.modules.organization.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.organization.dto.OrganizationRequest;
import com.testplatform.modules.organization.dto.OrganizationResponse;
import com.testplatform.modules.organization.service.OrganizationService;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrganizationResponse>> tree() {
        return ApiResponse.ok(organizationService.tree());
    }

    @PostMapping
    public ApiResponse<OrganizationResponse> create(@RequestBody OrganizationRequest request) {
        return ApiResponse.ok(organizationService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrganizationResponse> update(@PathVariable Long id, @RequestBody OrganizationRequest request) {
        return ApiResponse.ok(organizationService.update(id, request));
    }
}
