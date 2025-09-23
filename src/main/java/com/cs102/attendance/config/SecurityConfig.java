package com.cs102.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API testing
            .cors(cors -> cors.disable()) // Disable CORS for testing
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**").permitAll() // Allow all API requests
                .requestMatchers("/actuator/**").permitAll() // Allow actuator endpoints
                .requestMatchers("/error").permitAll() // Allow error page
                .anyRequest().permitAll() // Allow all requests for testing
            );
        
        return http.build();
    }
} 