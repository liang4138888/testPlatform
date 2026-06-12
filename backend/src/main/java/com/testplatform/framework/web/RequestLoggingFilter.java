package com.testplatform.framework.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(RequestLoggingFilter.class);
    private static final int MAX_BODY_LENGTH = 5000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        ThreadContext.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String path = query == null ? uri : uri + "?" + query;
        String clientIp = clientIp(request);

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        log.info("request start | method={} | path={} | clientIp={} | query={}", method, path, clientIp, emptyToDash(query));
        try {
            filterChain.doFilter(cachingRequest, cachingResponse);
        } finally {
            long cost = System.currentTimeMillis() - start;
            String requestBody = body(cachingRequest.getContentAsByteArray(), cachingRequest.getCharacterEncoding());
            String responseBody = body(cachingResponse.getContentAsByteArray(), cachingResponse.getCharacterEncoding());
            String requestParams = requestParams(cachingRequest);
            log.info("request end | method={} | path={} | status={} | cost={}ms | requestParams={} | requestBody={} | responseBody={}",
                method, path, cachingResponse.getStatus(), cost, requestParams, requestBody, responseBody);
            cachingResponse.copyBodyToResponse();
            ThreadContext.clearMap();
        }
    }

    private String requestParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "-";
        }
        return parameterMap.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
            .collect(Collectors.joining("&"));
    }

    private String body(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "-";
        }
        String charset = encoding == null || encoding.trim().isEmpty() ? StandardCharsets.UTF_8.name() : encoding;
        String body = new String(content, java.nio.charset.Charset.forName(charset)).replaceAll("\\s+", " ").trim();
        if (body.length() > MAX_BODY_LENGTH) {
            return body.substring(0, MAX_BODY_LENGTH) + "...";
        }
        return body;
    }

    private String emptyToDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
