package com.smartwomen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")  // ✅ CORREGIDO: incluye el context-path completo
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("POST", "GET", "OPTIONS", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}