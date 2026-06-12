package com.testplatform.modules.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.auth.dto.LoginRequest;
import com.testplatform.modules.auth.dto.LoginResponse;
import com.testplatform.modules.auth.entity.AuthToken;
import com.testplatform.modules.auth.mapper.AuthTokenMapper;
import com.testplatform.modules.user.dto.CurrentUserResponse;
import com.testplatform.modules.user.entity.SystemUser;
import com.testplatform.modules.user.service.UserService;

@Service
public class AuthService {

    private final AuthTokenMapper authTokenMapper;
    private final UserService userService;

    public AuthService(AuthTokenMapper authTokenMapper, UserService userService) {
        this.authTokenMapper = authTokenMapper;
        this.userService = userService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        SystemUser user = userService.getByUsername(request.getUsername());
        if (user == null || !user.getPasswordHash().equals(request.getPassword())) {
            throw new BusinessException("LOGIN_FAILED", "用户名或密码错误");
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
        return userService.buildCurrentUser(userService.getRequiredUser(authToken.getUserId()));
    }
}
