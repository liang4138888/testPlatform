package com.testplatform.modules.role.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.role.dto.PermissionResponse;
import com.testplatform.modules.role.dto.PermissionSaveRequest;
import com.testplatform.modules.role.dto.RolePermissionUpdateRequest;
import com.testplatform.modules.role.dto.RoleResponse;
import com.testplatform.modules.role.dto.RoleSaveRequest;
import com.testplatform.modules.user.entity.SystemPermission;
import com.testplatform.modules.user.entity.SystemRole;
import com.testplatform.modules.user.entity.SystemRolePermission;
import com.testplatform.modules.user.mapper.SystemPermissionMapper;
import com.testplatform.modules.user.mapper.SystemRoleMapper;
import com.testplatform.modules.user.mapper.SystemRolePermissionMapper;
import com.testplatform.modules.user.service.UserService;

@Service
public class RoleService {

    private final SystemRoleMapper systemRoleMapper;
    private final SystemPermissionMapper systemPermissionMapper;
    private final SystemRolePermissionMapper systemRolePermissionMapper;
    private final UserService userService;

    public RoleService(SystemRoleMapper systemRoleMapper, SystemPermissionMapper systemPermissionMapper,
            SystemRolePermissionMapper systemRolePermissionMapper, UserService userService) {
        this.systemRoleMapper = systemRoleMapper;
        this.systemPermissionMapper = systemPermissionMapper;
        this.systemRolePermissionMapper = systemRolePermissionMapper;
        this.userService = userService;
    }

    public List<RoleResponse> listRoles() {
        requireRoleManage();
        return systemRoleMapper.selectList(new LambdaQueryWrapper<SystemRole>().orderByAsc(SystemRole::getId))
            .stream()
            .map(role -> RoleResponse.from(role, getPermissionCodes(role.getId())))
            .collect(Collectors.toList());
    }

    @Transactional
    public RoleResponse createRole(RoleSaveRequest request) {
        requireRoleManage();
        String code = normalizeRoleCode(request.getRoleCode());
        String name = normalizeRoleName(request.getRoleName());
        Long exists = systemRoleMapper.selectCount(new LambdaQueryWrapper<SystemRole>().eq(SystemRole::getRoleCode, code));
        if (exists > 0) {
            throw new BusinessException("ROLE_CODE_EXISTS", "角色编码已存在");
        }
        SystemRole role = new SystemRole();
        role.setRoleCode(code);
        role.setRoleName(name);
        systemRoleMapper.insert(role);
        return RoleResponse.from(role, Collections.emptyList());
    }

    @Transactional
    public RoleResponse updateRole(Long roleId, RoleSaveRequest request) {
        requireRoleManage();
        SystemRole role = systemRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        String code = normalizeRoleCode(request.getRoleCode());
        String name = normalizeRoleName(request.getRoleName());
        Long exists = systemRoleMapper.selectCount(new LambdaQueryWrapper<SystemRole>()
            .eq(SystemRole::getRoleCode, code)
            .ne(SystemRole::getId, roleId));
        if (exists > 0) {
            throw new BusinessException("ROLE_CODE_EXISTS", "角色编码已存在");
        }
        role.setRoleCode(code);
        role.setRoleName(name);
        systemRoleMapper.updateById(role);
        return RoleResponse.from(role, getPermissionCodes(roleId));
    }

    public List<PermissionResponse> listPermissions() {
        requireRoleManage();
        return systemPermissionMapper.selectList(new LambdaQueryWrapper<SystemPermission>().orderByAsc(SystemPermission::getId))
            .stream()
            .map(PermissionResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public PermissionResponse createPermission(PermissionSaveRequest request) {
        requireRoleManage();
        String code = normalizePermissionCode(request.getPermissionCode());
        String name = normalizePermissionName(request.getPermissionName());
        Long exists = systemPermissionMapper.selectCount(new LambdaQueryWrapper<SystemPermission>()
            .eq(SystemPermission::getPermissionCode, code));
        if (exists > 0) {
            throw new BusinessException("PERMISSION_CODE_EXISTS", "权限编码已存在");
        }
        SystemPermission permission = new SystemPermission();
        permission.setPermissionCode(code);
        permission.setPermissionName(name);
        systemPermissionMapper.insert(permission);
        return PermissionResponse.from(permission);
    }

    @Transactional
    public PermissionResponse updatePermission(Long permissionId, PermissionSaveRequest request) {
        requireRoleManage();
        SystemPermission permission = systemPermissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException("PERMISSION_NOT_FOUND", "权限不存在");
        }
        String code = normalizePermissionCode(request.getPermissionCode());
        String name = normalizePermissionName(request.getPermissionName());
        Long exists = systemPermissionMapper.selectCount(new LambdaQueryWrapper<SystemPermission>()
            .eq(SystemPermission::getPermissionCode, code)
            .ne(SystemPermission::getId, permissionId));
        if (exists > 0) {
            throw new BusinessException("PERMISSION_CODE_EXISTS", "权限编码已存在");
        }
        permission.setPermissionCode(code);
        permission.setPermissionName(name);
        systemPermissionMapper.updateById(permission);
        return PermissionResponse.from(permission);
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        requireRoleManage();
        Long used = systemRolePermissionMapper.selectCount(new LambdaQueryWrapper<SystemRolePermission>()
            .eq(SystemRolePermission::getPermissionId, permissionId));
        if (used > 0) {
            throw new BusinessException("PERMISSION_IN_USE", "权限已被角色使用，不能删除");
        }
        systemPermissionMapper.deleteById(permissionId);
    }

    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, RolePermissionUpdateRequest request) {
        requireRoleManage();
        SystemRole role = systemRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        List<String> permissionCodes = request.getPermissions() == null ? Collections.emptyList() : request.getPermissions();
        List<SystemPermission> permissions = permissionCodes.isEmpty()
            ? Collections.emptyList()
            : systemPermissionMapper.selectList(new LambdaQueryWrapper<SystemPermission>().in(SystemPermission::getPermissionCode, permissionCodes));
        if (permissions.size() != permissionCodes.stream().distinct().count()) {
            throw new BusinessException("PERMISSION_NOT_FOUND", "权限不存在");
        }
        systemRolePermissionMapper.delete(new LambdaQueryWrapper<SystemRolePermission>().eq(SystemRolePermission::getRoleId, roleId));
        for (SystemPermission permission : permissions) {
            SystemRolePermission rolePermission = new SystemRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permission.getId());
            systemRolePermissionMapper.insert(rolePermission);
        }
        return RoleResponse.from(role, getPermissionCodes(roleId));
    }

    private List<String> getPermissionCodes(Long roleId) {
        List<SystemRolePermission> rolePermissions = systemRolePermissionMapper.selectList(new LambdaQueryWrapper<SystemRolePermission>()
            .eq(SystemRolePermission::getRoleId, roleId));
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> permissionIds = rolePermissions.stream().map(SystemRolePermission::getPermissionId).collect(Collectors.toSet());
        return systemPermissionMapper.selectList(new LambdaQueryWrapper<SystemPermission>().in(SystemPermission::getId, new ArrayList<Long>(permissionIds)))
            .stream()
            .map(SystemPermission::getPermissionCode)
            .collect(Collectors.toList());
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new BusinessException("INVALID_ROLE_CODE", "角色编码不能为空");
        }
        String code = roleCode.trim().toUpperCase();
        if (!code.matches("^[A-Z0-9_]+$")) {
            throw new BusinessException("INVALID_ROLE_CODE", "角色编码只能包含大写英文、数字和下划线");
        }
        return code;
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BusinessException("INVALID_ROLE_NAME", "角色名称不能为空");
        }
        return roleName.trim();
    }

    private String normalizePermissionCode(String permissionCode) {
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            throw new BusinessException("INVALID_PERMISSION_CODE", "权限编码不能为空");
        }
        String code = permissionCode.trim().toUpperCase();
        if (!code.matches("^[A-Z0-9_]+$")) {
            throw new BusinessException("INVALID_PERMISSION_CODE", "权限编码只能包含大写英文、数字和下划线");
        }
        return code;
    }

    private String normalizePermissionName(String permissionName) {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new BusinessException("INVALID_PERMISSION_NAME", "权限名称不能为空");
        }
        return permissionName.trim();
    }

    private void requireRoleManage() {
        if (!userService.hasPermission("USER_MANAGE") && !userService.hasPermission("ROLE_MANAGE")) {
            throw new BusinessException("PERMISSION_DENIED", "无权限操作");
        }
    }
}
