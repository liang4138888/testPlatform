package com.testplatform.common.exception;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.testplatform.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        log.warn("business exception | method={} | path={} | code={} | message={}",
            request.getMethod(), request.getRequestURI(), exception.getCode(), exception.getMessage());
        HttpStatus status = "UNAUTHORIZED".equals(exception.getCode()) ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(Exception exception, HttpServletRequest request) {
        org.springframework.validation.BindingResult bindingResult;
        if (exception instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) exception).getBindingResult();
        } else {
            bindingResult = ((BindException) exception).getBindingResult();
        }
        String message = bindingResult.getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.warn("validation exception | method={} | path={} | message={}", request.getMethod(), request.getRequestURI(), message);
        return ApiResponse.fail("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception, HttpServletRequest request) {
        log.error("system exception | method={} | path={}", request.getMethod(), request.getRequestURI(), exception);
        return ApiResponse.fail("INTERNAL_ERROR", "系统异常，请稍后重试");
    }
}
