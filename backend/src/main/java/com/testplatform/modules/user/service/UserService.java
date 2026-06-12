package com.testplatform.modules.user.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.user.dto.AssignableUserResponse;
import com.testplatform.modules.user.dto.CurrentUserResponse;
import com.testplatform.modules.user.dto.UserCreateRequest;
import com.testplatform.modules.user.entity.SystemPermission;
import com.testplatform.modules.user.entity.SystemRole;
import com.testplatform.modules.user.entity.SystemRolePermission;
import com.testplatform.modules.user.entity.SystemUser;
import com.testplatform.modules.user.entity.SystemUserRole;
import com.testplatform.modules.user.mapper.SystemPermissionMapper;
import com.testplatform.modules.user.mapper.SystemRoleMapper;
import com.testplatform.modules.user.mapper.SystemRolePermissionMapper;
import com.testplatform.modules.user.mapper.SystemUserMapper;
import com.testplatform.modules.user.mapper.SystemUserRoleMapper;

@Service
public class UserService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String PERMISSION_DATA_ALL = "DATA_ALL";

    private final SystemUserMapper systemUserMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final SystemPermissionMapper systemPermissionMapper;
    private final SystemUserRoleMapper systemUserRoleMapper;
    private final SystemRolePermissionMapper systemRolePermissionMapper;

    public UserService(SystemUserMapper systemUserMapper, SystemRoleMapper systemRoleMapper,
            SystemPermissionMapper systemPermissionMapper, SystemUserRoleMapper systemUserRoleMapper,
            SystemRolePermissionMapper systemRolePermissionMapper) {
        this.systemUserMapper = systemUserMapper;
        this.systemRoleMapper = systemRoleMapper;
        this.systemPermissionMapper = systemPermissionMapper;
        this.systemUserRoleMapper = systemUserRoleMapper;
        this.systemRolePermissionMapper = systemRolePermissionMapper;
    }

    public SystemUser getRequiredUser(Long userId) {
        SystemUser user = systemUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        return user;
    }

    public SystemUser getByUsername(String username) {
        return systemUserMapper.selectOne(new LambdaQueryWrapper<SystemUser>()
            .eq(SystemUser::getUsername, username));
    }

    public CurrentUserResponse buildCurrentUser(SystemUser user) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setAvatar(user.getAvatar());
        List<SystemRole> roles = getUserRoles(user.getId());
        if (roles.isEmpty() && "admin".equals(user.getUsername())) {
            roles = systemRoleMapper.selectList(new LambdaQueryWrapper<SystemRole>().eq(SystemRole::getRoleCode, ROLE_ADMIN));
            if (!roles.isEmpty()) {
                assignRole(user.getId(), ROLE_ADMIN);
            }
        }
        response.setRoles(roles.stream().map(SystemRole::getRoleCode).collect(Collectors.toList()));
        response.setPermissions(getPermissionCodes(roles));
        return response;
    }

    @Transactional
    public AssignableUserResponse createUser(UserCreateRequest request) {
        if (request.getUsername() == null || !request.getUsername().matches("^[A-Za-z0-9]+$")) {
            throw new BusinessException("INVALID_USERNAME", "用户名只能包含英文或数字");
        }
        if (getByUsername(request.getUsername()) != null) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }
        SystemUser user = new SystemUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword() == null || request.getPassword().isEmpty() ? "123456" : request.getPassword());
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() == null || request.getStatus().isEmpty() ? "ACTIVE" : request.getStatus());
        systemUserMapper.insert(user);
        assignRole(user.getId(), request.getRoleCode() == null || request.getRoleCode().isEmpty() ? "TESTER" : request.getRoleCode());
        return toAssignableUser(user);
    }

    public List<AssignableUserResponse> listAssignableUsers() {
        return systemUserMapper.selectList(new LambdaQueryWrapper<SystemUser>()
                .eq(SystemUser::getStatus, "ACTIVE")
                .orderByAsc(SystemUser::getId))
            .stream()
            .map(this::toAssignableUser)
            .collect(Collectors.toList());
    }

    public boolean hasPermission(String permission) {
        return CurrentUserContext.hasPermission(permission);
    }

    public boolean isAdmin() {
        CurrentUserResponse user = CurrentUserContext.get();
        return user != null && user.getRoles() != null && user.getRoles().contains(ROLE_ADMIN);
    }

    public boolean canViewAllData() {
        return isAdmin() || hasPermission(PERMISSION_DATA_ALL);
    }

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new BusinessException("PERMISSION_DENIED", "无权限操作");
        }
    }

    private AssignableUserResponse toAssignableUser(SystemUser user) {
        AssignableUserResponse response = AssignableUserResponse.from(user);
        List<SystemRole> roles = getUserRoles(user.getId());
        response.setRoleCodes(roles.stream().map(SystemRole::getRoleCode).collect(Collectors.toList()));
        response.setRoleNames(roles.stream().map(SystemRole::getRoleName).collect(Collectors.toList()));
        return response;
    }

    private List<SystemRole> getUserRoles(Long userId) {
        List<SystemUserRole> userRoles = systemUserRoleMapper.selectList(new LambdaQueryWrapper<SystemUserRole>()
            .eq(SystemUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SystemUserRole::getRoleId).collect(Collectors.toList());
        return systemRoleMapper.selectList(new LambdaQueryWrapper<SystemRole>().in(SystemRole::getId, roleIds));
    }

    private List<String> getPermissionCodes(List<SystemRole> roles) {
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        if (roles.stream().anyMatch(role -> ROLE_ADMIN.equals(role.getRoleCode()))) {
            return systemPermissionMapper.selectList(new LambdaQueryWrapper<SystemPermission>().orderByAsc(SystemPermission::getId))
                .stream()
                .map(SystemPermission::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());
        }
        List<Long> roleIds = roles.stream().map(SystemRole::getId).collect(Collectors.toList());
        List<SystemRolePermission> rolePermissions = systemRolePermissionMapper.selectList(new LambdaQueryWrapper<SystemRolePermission>()
            .in(SystemRolePermission::getRoleId, roleIds));
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> permissionIds = rolePermissions.stream().map(SystemRolePermission::getPermissionId).collect(Collectors.toSet());
        return systemPermissionMapper.selectList(new LambdaQueryWrapper<SystemPermission>().in(SystemPermission::getId, new ArrayList<Long>(permissionIds)))
            .stream()
            .map(SystemPermission::getPermissionCode)
            .distinct()
            .collect(Collectors.toList());
    }

    private void assignRole(Long userId, String roleCode) {
        SystemRole role = systemRoleMapper.selectOne(new LambdaQueryWrapper<SystemRole>().eq(SystemRole::getRoleCode, roleCode));
        if (role == null) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        SystemUserRole userRole = new SystemUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        systemUserRoleMapper.insert(userRole);
    }
}
