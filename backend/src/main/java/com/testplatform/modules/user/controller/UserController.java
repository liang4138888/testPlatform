package com.testplatform.modules.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.user.dto.AssignableUserResponse;
import com.testplatform.modules.user.dto.PasswordResetRequest;
import com.testplatform.modules.user.dto.UserCreateRequest;
import com.testplatform.modules.user.dto.UserUpdateRequest;
import com.testplatform.modules.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<AssignableUserResponse> createUser(@RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    @GetMapping
    public ApiResponse<List<AssignableUserResponse>> users() {
        return ApiResponse.ok(userService.listUsers());
    }

    @PutMapping("/{userId}")
    public ApiResponse<AssignableUserResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.updateUser(userId, request));
    }

    @PutMapping("/{userId}/password")
    public ApiResponse<AssignableUserResponse> resetPassword(@PathVariable Long userId, @RequestBody PasswordResetRequest request) {
        return ApiResponse.ok(userService.resetPassword(userId, request.getPassword()));
    }

    @GetMapping("/assignable")
    public ApiResponse<List<AssignableUserResponse>> assignableUsers() {
        return ApiResponse.ok(userService.listAssignableUsers());
    }
}
