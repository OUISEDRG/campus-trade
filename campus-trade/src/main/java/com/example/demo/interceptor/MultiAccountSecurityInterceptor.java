package com.example.demo.interceptor;

import com.example.demo.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
public class MultiAccountSecurityInterceptor implements HandlerInterceptor {

    private static final List<String> EXCLUDE_PATHS = List.of(
        "/user/login",
        "/user/register",
        "/goods/carousel",
        "/goods/list",
        "/goods/getAll",
        "/goods/",
        "/ws/**",
        "/chat/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.startsWith(excludePath.replace("/**", ""))) {
                return true;
            }
        }

        String headerUserId = request.getHeader("X-User-Id");

        if (headerUserId == null || headerUserId.trim().isEmpty()) {
            return true;
        }

        return true;
    }
}
