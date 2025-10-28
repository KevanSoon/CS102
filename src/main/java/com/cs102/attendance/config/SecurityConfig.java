package com.cs102.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cs102.attendance.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CorsProperties corsProperties, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsProperties = corsProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Use configured CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessions
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll() // Auth endpoints
                .requestMatchers("/actuator/**").permitAll() // Actuator endpoints
                .requestMatchers("/error").permitAll() // Error page

                // Professor-only endpoints
                .requestMatchers("/api/sessions/**").hasRole("PROFESSOR")
                .requestMatchers("/api/professors/**").hasRole("PROFESSOR")

                // Student and Professor can access their own data
                .requestMatchers("/api/students/**").hasAnyRole("STUDENT", "PROFESSOR")
                .requestMatchers("/api/attendance/**").hasAnyRole("STUDENT", "PROFESSOR")
                .requestMatchers("/api/face-data/**").hasAnyRole("STUDENT", "PROFESSOR")
                .requestMatchers("/api/face-recognition/**").hasAnyRole("STUDENT", "PROFESSOR")
                .requestMatchers("/api/groups/**").hasAnyRole("STUDENT", "PROFESSOR")
                .requestMatchers("/api/classes/**").hasAnyRole("STUDENT", "PROFESSOR")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 