package com.testplatform.modules.role.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.role.dto.PermissionResponse;
import com.testplatform.modules.role.dto.PermissionSaveRequest;
import com.testplatform.modules.role.dto.RolePermissionUpdateRequest;
import com.testplatform.modules.role.dto.RoleResponse;
import com.testplatform.modules.role.dto.RoleSaveRequest;
import com.testplatform.modules.role.service.RoleService;

@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.ok(roleService.listRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleService.createRole(request));
    }

    @PutMapping("/roles/{roleId}")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long roleId, @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleService.updateRole(roleId, request));
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> permissions() {
        return ApiResponse.ok(roleService.listPermissions());
    }

    @PostMapping("/permissions")
    public ApiResponse<PermissionResponse> createPermission(@RequestBody PermissionSaveRequest request) {
        return ApiResponse.ok(roleService.createPermission(request));
    }

    @PutMapping("/permissions/{permissionId}")
    public ApiResponse<PermissionResponse> updatePermission(@PathVariable Long permissionId, @RequestBody PermissionSaveRequest request) {
        return ApiResponse.ok(roleService.updatePermission(permissionId, request));
    }

    @DeleteMapping("/permissions/{permissionId}")
    public ApiResponse<Void> deletePermission(@PathVariable Long permissionId) {
        roleService.deletePermission(permissionId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ApiResponse<RoleResponse> updateRolePermissions(@PathVariable Long roleId, @RequestBody RolePermissionUpdateRequest request) {
        return ApiResponse.ok(roleService.updateRolePermissions(roleId, request));
    }
}
