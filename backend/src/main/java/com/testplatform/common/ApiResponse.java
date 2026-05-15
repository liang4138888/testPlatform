package com.testplatform.common;

public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>("0", "ok", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<Void>("0", "ok", null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<Void>(code, message, null);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
