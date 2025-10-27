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
     * Body: { "email": "user@example.com", "password": "password123", "userMetadata": { "role": "student", "name": "Bob Tan" } }
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            System.out.println("=== SIGNUP REQUEST START ===");
            System.out.println("Sign-up request received for email: " + signUpRequest.getEmail());
            System.out.println("Name: " + signUpRequest.getName());

            AuthResponse response = supabaseAuthService.signUp(signUpRequest);

            System.out.println("Auth response received: " + (response != null));
            System.out.println("Response user: " + (response != null && response.getUser() != null));
            if (response != null && response.getUser() != null) {
                System.out.println("User ID from response: " + response.getUser().getId());
                System.out.println("User email: " + response.getUser().getEmail());
            }

            String role = signUpRequest.getUserMetadata() != null ?
                         signUpRequest.getUserMetadata().get("role") : null;

            System.out.println("Role from metadata: " + role);

            if (role == null) {
                System.err.println("Warning: No role specified in userMetadata");
                return ResponseEntity.ok(response); // Return auth response even if no role
            }

            if ("student".equalsIgnoreCase(role)) {
                try {
                    // Get the auth user ID from the response
                    String authUserId = response.getUser() != null ? response.getUser().getId() : null;

                    if (authUserId == null) {
                        System.err.println("Failed to create student: Auth user ID is null");
                    } else {
                        Student student = new Student(
                            signUpRequest.getName(),
                            signUpRequest.getEmail(),
                            signUpRequest.getCode(),
                            signUpRequest.getPhone(),
                            signUpRequest.getClassName(),
                            signUpRequest.getStudentGroup()
                        );
                        student.setId(authUserId);  // Set the ID to match auth user
                        System.out.println("Creating student with ID: " + authUserId + ", Name: " + signUpRequest.getName());
                        Student createdStudent = studentService.create(student);
                        if (createdStudent != null) {
                            System.out.println("Student record created successfully: " + createdStudent.getName() + " (ID: " + createdStudent.getId() + ")");
                        } else {
                            System.out.println("Student record created (response was null, but insert was successful)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to create student record: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if ("professor".equalsIgnoreCase(role)) {
                try {
                    // Get the auth user ID from the response
                    String authUserId = response.getUser() != null ? response.getUser().getId() : null;

                    if (authUserId == null) {
                        System.err.println("Failed to create professor: Auth user ID is null");
                    } else {
                        Professor professor = new Professor(authUserId, signUpRequest.getName());
                        System.out.println("Creating professor with ID: " + authUserId + ", Name: " + signUpRequest.getName());
                        Professor createdProfessor = professorService.create(professor);
                        if (createdProfessor != null) {
                            System.out.println("Professor record created successfully: " + createdProfessor.getName() + " (ID: " + createdProfessor.getId() + ")");
                        } else {
                            System.out.println("Professor record created (response was null, but insert was successful)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to create professor record: " + e.getMessage());
                    e.printStackTrace();
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
