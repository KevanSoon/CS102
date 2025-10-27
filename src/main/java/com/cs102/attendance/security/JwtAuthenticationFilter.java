package com.cs102.attendance.security;

import com.cs102.attendance.service.SupabaseAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SupabaseAuthService supabaseAuthService;

    public JwtAuthenticationFilter(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // Skip JWT validation for public endpoints
        if (requestUri.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            // Validate token is not empty
            if (token.isEmpty()) {
                System.err.println("JWT validation error for " + requestUri + ": Token is empty (Authorization header was: " + authHeader + ")");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                String jwtSecret = supabaseAuthService.getJwtSecret();
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String email = claims.get("email", String.class);
                String userId = claims.getSubject();

                @SuppressWarnings("unchecked")
                Map<String, Object> userMetadata = claims.get("user_metadata", Map.class);
                String role = "ROLE_STUDENT"; // Default role

                if (userMetadata != null && userMetadata.containsKey("role")) {
                    String metadataRole = (String) userMetadata.get("role");
                    role = "ROLE_" + metadataRole.toUpperCase();
                }

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                System.err.println("JWT validation error for " + requestUri + ": " + e.getMessage());
                // Continue without authentication
            }
        } else if (authHeader != null && !authHeader.startsWith("Bearer ")) {
            System.err.println("Invalid Authorization header format for " + requestUri + ": '" + authHeader + "'");
        }

        filterChain.doFilter(request, response);
    }
}
