package com.testplatform.modules.auth.dto;

import com.testplatform.modules.user.dto.CurrentUserResponse;

public class LoginResponse {

    private String token;
    private CurrentUserResponse user;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public CurrentUserResponse getUser() { return user; }
    public void setUser(CurrentUserResponse user) { this.user = user; }
}
