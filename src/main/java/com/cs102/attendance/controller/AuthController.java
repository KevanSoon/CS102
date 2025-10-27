package com.cs102.attendance.controller;

import com.cs102.attendance.dto.AuthResponse;
import com.cs102.attendance.dto.SignInRequest;
import com.cs102.attendance.dto.SignUpRequest;
import com.cs102.attendance.model.Professor;
import com.cs102.attendance.model.Student;
import com.cs102.attendance.model.User;
import com.cs102.attendance.service.ProfessorService;
import com.cs102.attendance.service.StudentService;
import com.cs102.attendance.service.SupabaseAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SupabaseAuthService supabaseAuthService;
    private final StudentService studentService;
    private final ProfessorService professorService;

    public AuthController(SupabaseAuthService supabaseAuthService,
                         StudentService studentService,
                         ProfessorService professorService) {
        this.supabaseAuthService = supabaseAuthService;
        this.studentService = studentService;
        this.professorService = professorService;
    }

    /**
     * Sign up a new user (student or professor)
     * POST /api/auth/signup
     * Body: { "email": "user@example.com", "password": "password123", "userMetadata": { "role": "student", "name": "John Doe" } }
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            System.out.println("Sign-up request received for email: " + signUpRequest.getEmail());

            AuthResponse response = supabaseAuthService.signUp(signUpRequest);

            String role = signUpRequest.getUserMetadata() != null ?
                         signUpRequest.getUserMetadata().get("role") : null;

            if (role == null) {
                System.err.println("Warning: No role specified in userMetadata");
                return ResponseEntity.ok(response); // Return auth response even if no role
            }

            if ("student".equalsIgnoreCase(role)) {
                try {
                    Student student = new Student(
                        signUpRequest.getName(),
                        signUpRequest.getEmail(),
                        signUpRequest.getCode(),
                        signUpRequest.getPhone(),
                        signUpRequest.getClassName(),
                        signUpRequest.getStudentGroup()
                    );
                    Student createdStudent = studentService.create(student);
                    System.out.println("Student record created with ID: " + createdStudent);
                } catch (Exception e) {
                    System.err.println("Failed to create student record: " + e.getMessage());
                    // Auth user is created, but student record failed
                }
            } else if ("professor".equalsIgnoreCase(role)) {
                try {
                    Professor professor = new Professor(signUpRequest.getName());
                    Professor createdProfessor = professorService.create(professor);
                    System.out.println("Professor record created: " + createdProfessor.getName());
                } catch (Exception e) {
                    System.err.println("Failed to create professor record: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Sign-up error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Sign in an existing user
     * POST /api/auth/signin
     * Body: { "email": "user@example.com", "password": "password123" }
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody SignInRequest signInRequest) {
        try {
            AuthResponse response = supabaseAuthService.signIn(signInRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Sign out the current user
     * POST /api/auth/signout
     * Header: Authorization: Bearer <access_token>
     */
    @PostMapping("/signout")
    public ResponseEntity<Map<String, String>> signOut(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            supabaseAuthService.signOut(token);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully signed out");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user information
     * GET /api/auth/user
     * Header: Authorization: Bearer <access_token>
     */
    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            User user = supabaseAuthService.getUser(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     * Body: { "refreshToken": "<refresh_token>" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            AuthResponse response = supabaseAuthService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
