package com.example.demo.config;

import com.example.demo.interceptor.MultiAccountSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private MultiAccountSecurityInterceptor multiAccountSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(multiAccountSecurityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/user/login",
                    "/user/register",
                    "/goods/carousel",
                    "/goods/list",
                    "/goods/getAll",
                    "/ws/**",
                    "/chat/**"
                );
    }
}
