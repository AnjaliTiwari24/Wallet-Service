package com.dinoventures.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.security.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.security.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.security.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.security.cors.allow-credentials:true}")
    private Boolean allowCredentials;

    @Value("${app.security.cors.max-age:3600}")
    private Long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Handle allowed origins
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            configuration.setAllowedOrigins(Collections.singletonList("*"));
        }

        // Handle allowed methods
        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        } else {
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }

        // Handle allowed headers
        if (allowedHeaders != null && !allowedHeaders.equals("*")) {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        } else {
            configuration.setAllowedHeaders(Collections.singletonList("*"));
        }

        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
