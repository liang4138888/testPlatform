package com.testplatform.modules.auth.support;

import com.testplatform.modules.user.dto.CurrentUserResponse;

public final class CurrentUserContext {

    private static final ThreadLocal<CurrentUserResponse> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(CurrentUserResponse user) {
        HOLDER.set(user);
    }

    public static CurrentUserResponse get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        CurrentUserResponse user = get();
        return user == null ? null : user.getId();
    }

    public static boolean hasPermission(String permission) {
        CurrentUserResponse user = get();
        return user != null && user.getPermissions() != null && user.getPermissions().contains(permission);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
