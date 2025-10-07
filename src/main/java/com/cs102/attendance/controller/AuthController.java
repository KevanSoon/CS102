package com.cs102.attendance.controller;

import com.cs102.attendance.dto.SupabaseAuthResponse;
import com.cs102.attendance.dto.SupabaseUser;
import com.cs102.attendance.service.SupabaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            SupabaseAuthResponse response = supabaseAuthService.signUp(email, password);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            SupabaseAuthResponse response = supabaseAuthService.signIn(email, password);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut(@RequestHeader("Authorization") String authorization) {
        try {
            String accessToken = authorization.replace("Bearer ", "");
            supabaseAuthService.signOut(accessToken);
            return ResponseEntity.ok(Map.of("message", "Successfully signed out"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorization) {
        try {
            String accessToken = authorization.replace("Bearer ", "");
            SupabaseUser user = supabaseAuthService.getUser(accessToken);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshSession(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            SupabaseAuthResponse response = supabaseAuthService.refreshSession(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
