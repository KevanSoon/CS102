package com.cs102.attendance.service;

import com.cs102.attendance.dto.AuthResponse;
import com.cs102.attendance.dto.SignInRequest;
import com.cs102.attendance.dto.SignUpRequest;
import com.cs102.attendance.dto.SupabaseSignUpRequest;
import com.cs102.attendance.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseAuthService {

    private final WebClient authWebClient;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    public SupabaseAuthService(@Value("${supabase.url}") String supabaseUrl,
                               @Value("${supabase.api-key}") String apiKey) {
        this.authWebClient = WebClient.builder()
                .baseUrl(supabaseUrl + "/auth/v1/")
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Sign up a new user with email and password
    public AuthResponse signUp(SignUpRequest signUpRequest) {
        System.out.println("Sending signup request to Supabase...");

        SupabaseSignUpRequest supabaseRequest = new SupabaseSignUpRequest(
            signUpRequest.getEmail(),
            signUpRequest.getPassword(),
            signUpRequest.getUserMetadata()
        );

        return authWebClient.post()
                .uri("signup")
                .bodyValue(supabaseRequest)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Supabase error response: " + errorBody);
                            return Mono.error(new RuntimeException("Supabase signup failed: " + errorBody));
                        })
                )
                .bodyToMono(AuthResponse.class)
                .block();
    }

    // Sign in an existing user with email and password
    public AuthResponse signIn(SignInRequest signInRequest) {
        System.out.println("Signing in user: " + signInRequest.getEmail());
        AuthResponse response = authWebClient.post()
                .uri("token?grant_type=password")
                .bodyValue(signInRequest)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .onErrorResume(error -> {
                    System.err.println("Sign-in error: " + error.getMessage());
                    return Mono.error(new RuntimeException("Invalid credentials"));
                })
                .block();

        System.out.println("Sign-in response received");
        if (response != null && response.getUser() != null) {
            System.out.println("User ID: " + response.getUser().getId());
            System.out.println("User Email: " + response.getUser().getEmail());
            System.out.println("User Metadata: " + response.getUser().getUserMetadata());
            if (response.getUser().getUserMetadata() != null) {
                System.out.println("User Metadata Role: " + response.getUser().getUserMetadata().getRole());
                System.out.println("User Metadata Name: " + response.getUser().getUserMetadata().getName());
            } else {
                System.err.println("WARNING: User metadata is NULL in sign-in response!");
            }
        }

        return response;
    }

    // Sign out the current user
    public void signOut(String accessToken) {
        authWebClient.post()
                .uri("logout")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    // Get user info from access token
    public User getUser(String accessToken) {
        return authWebClient.get()
                .uri("user")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(User.class)
                .block();
    }

    // Refresh access token
    public AuthResponse refreshToken(String refreshToken) {
        return authWebClient.post()
                .uri("token?grant_type=refresh_token")
                .bodyValue(new RefreshTokenRequest(refreshToken))
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .block();
    }

    // Resend verification email
    public void resendVerificationEmail(String email) {
        System.out.println("Resending verification email to: " + email);
        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        authWebClient.post()
                .uri("resend")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Supabase error response: " + errorBody);
                            return Mono.error(new RuntimeException("Failed to resend verification email: " + errorBody));
                        })
                )
                .bodyToMono(Void.class)
                .block();
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    private static class RefreshTokenRequest {
        private String refresh_token;

        public RefreshTokenRequest(String refreshToken) {
            this.refresh_token = refreshToken;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }
    }
}
