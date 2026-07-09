package com.testplatform.modules.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.auth.dto.LoginRequest;
import com.testplatform.modules.auth.dto.LoginResponse;
import com.testplatform.modules.auth.dto.RegisterOptionsResponse;
import com.testplatform.modules.auth.dto.RegisterRequest;
import com.testplatform.modules.auth.entity.AuthToken;
import com.testplatform.modules.auth.mapper.AuthTokenMapper;
import com.testplatform.modules.organization.service.OrganizationService;
import com.testplatform.modules.user.dto.CurrentUserResponse;
import com.testplatform.modules.user.entity.SystemUser;
import com.testplatform.modules.user.service.UserService;

@Service
public class AuthService {

    private final AuthTokenMapper authTokenMapper;
    private final UserService userService;
    private final OrganizationService organizationService;

    public AuthService(AuthTokenMapper authTokenMapper, UserService userService, OrganizationService organizationService) {
        this.authTokenMapper = authTokenMapper;
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        SystemUser user = userService.getByUsername(request.getUsername());
        if (user == null || !user.getPasswordHash().equals(request.getPassword())) {
            throw new BusinessException("LOGIN_FAILED", "用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("USER_DISABLED", "用户已禁用");
        }
        AuthToken authToken = new AuthToken();
        authToken.setUserId(user.getId());
        authToken.setToken(UUID.randomUUID().toString().replace("-", ""));
        authToken.setExpiredAt(LocalDateTime.now().plusDays(7));
        authTokenMapper.insert(authToken);

        LoginResponse response = new LoginResponse();
        response.setToken(authToken.getToken());
        response.setUser(userService.buildCurrentUser(user));
        return response;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("INVALID_PASSWORD", "密码长度不能小于 6 位");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("PASSWORD_NOT_MATCH", "两次输入的密码不一致");
        }
        if (request.getDisplayName() == null || request.getDisplayName().trim().isEmpty()) {
            throw new BusinessException("INVALID_DISPLAY_NAME", "昵称不能为空");
        }
        organizationService.getActiveRequired(request.getOrganizationId());
        userService.createRegisteredUser(request.getUsername(), request.getPassword(), request.getDisplayName(), request.getOrganizationId());
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    public RegisterOptionsResponse registerOptions() {
        RegisterOptionsResponse response = new RegisterOptionsResponse();
        response.setOrganizations(organizationService.activeOptions());
        return response;
    }

    @Transactional
    public void logout(String token) {
        if (token == null) {
            return;
        }
        authTokenMapper.delete(new LambdaQueryWrapper<AuthToken>().eq(AuthToken::getToken, token));
    }

    public CurrentUserResponse getUserByToken(String token) {
        AuthToken authToken = authTokenMapper.selectOne(new LambdaQueryWrapper<AuthToken>()
            .eq(AuthToken::getToken, token)
            .gt(AuthToken::getExpiredAt, LocalDateTime.now()));
        if (authToken == null) {
            throw new BusinessException("UNAUTHORIZED", "登录已失效");
        }
        SystemUser user = userService.getRequiredUser(authToken.getUserId());
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("UNAUTHORIZED", "登录已失效");
        }
        return userService.buildCurrentUser(user);
    }
}
