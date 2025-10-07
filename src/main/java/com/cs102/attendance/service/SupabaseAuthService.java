package com.cs102.attendance.service;

import com.cs102.attendance.config.SupabaseConfig;
import com.cs102.attendance.dto.SupabaseAuthResponse;
import com.cs102.attendance.dto.SupabaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseAuthService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private SupabaseConfig supabaseConfig;

    public SupabaseAuthResponse signUp(String email, String password) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/signup";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseConfig.getSupabaseAnonKey());
            
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<SupabaseAuthResponse> response = restTemplate.postForEntity(url, request, SupabaseAuthResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Sign up error details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to sign up user: " + e.getMessage(), e);
        }
    }

    public SupabaseAuthResponse signIn(String email, String password) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseConfig.getSupabaseAnonKey());
            
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<SupabaseAuthResponse> response = restTemplate.postForEntity(url, request, SupabaseAuthResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign in user", e);
        }
    }

    public SupabaseUser getUser(String accessToken) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/user";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("apikey", supabaseConfig.getSupabaseAnonKey());
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<SupabaseUser> response = restTemplate.exchange(url, HttpMethod.GET, request, SupabaseUser.class);
            
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public void signOut(String accessToken) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/logout";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("apikey", supabaseConfig.getSupabaseAnonKey());
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign out user", e);
        }
    }

    public SupabaseAuthResponse refreshSession(String refreshToken) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=refresh_token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseConfig.getSupabaseAnonKey());
            
            Map<String, String> body = new HashMap<>();
            body.put("refresh_token", refreshToken);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<SupabaseAuthResponse> response = restTemplate.postForEntity(url, request, SupabaseAuthResponse.class);
            
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh session", e);
        }
    }
}
