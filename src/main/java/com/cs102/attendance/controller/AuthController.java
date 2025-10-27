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

            // Enhance user metadata with all student/professor data for later use
            Map<String, String> userMetadata = signUpRequest.getUserMetadata();
            if (userMetadata != null) {
                String role = userMetadata.get("role");

                // Add student-specific fields to metadata if role is student
                if ("student".equalsIgnoreCase(role)) {
                    if (signUpRequest.getCode() != null) {
                        userMetadata.put("code", signUpRequest.getCode());
                    }
                    if (signUpRequest.getPhone() != null) {
                        userMetadata.put("phone", signUpRequest.getPhone());
                    }
                    if (signUpRequest.getClassName() != null) {
                        userMetadata.put("class_name", signUpRequest.getClassName());
                    }
                    if (signUpRequest.getStudentGroup() != null) {
                        userMetadata.put("student_group", signUpRequest.getStudentGroup());
                    }
                }

                signUpRequest.setUserMetadata(userMetadata);
                System.out.println("Enhanced user metadata with role-specific data");
            }

            AuthResponse response = supabaseAuthService.signUp(signUpRequest);

            System.out.println("Auth response received: " + (response != null));
            System.out.println("Response user: " + (response != null && response.getUser() != null));

            // NOTE: With email verification enabled, user object will be null until email is verified
            // Database records will be created on first successful sign-in instead
            System.out.println("Database record creation deferred until email verification and first sign-in");

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
            System.out.println("=== SIGNIN REQUEST START ===");
            AuthResponse response = supabaseAuthService.signIn(signInRequest);

            if (response != null && response.getUser() != null) {
                String userId = response.getUser().getId();
                User.UserMetadata metadata = response.getUser().getUserMetadata();

                if (metadata != null && metadata.getRole() != null) {
                    String role = metadata.getRole();
                    System.out.println("User role: " + role);

                    // Create database record on first login if it doesn't exist
                    if ("student".equalsIgnoreCase(role)) {
                        try {
                            // Check if student record already exists
                            studentService.getById(userId);
                            System.out.println("Student record already exists for ID: " + userId);
                        } catch (RuntimeException e) {
                            // Record doesn't exist, create it
                            System.out.println("Creating student record for first-time user: " + userId);

                            Student student = new Student(
                                metadata.getName(),
                                response.getUser().getEmail(),
                                getMetadataField(metadata, "code", "STU" + System.currentTimeMillis()),
                                getMetadataField(metadata, "phone", ""),
                                getMetadataField(metadata, "class_name", ""),
                                getMetadataField(metadata, "student_group", "")
                            );
                            student.setId(userId);

                            Student createdStudent = studentService.create(student);
                            if (createdStudent != null) {
                                System.out.println("Student record created successfully: " + createdStudent.getName());
                            }
                        }
                    } else if ("professor".equalsIgnoreCase(role)) {
                        try {
                            // Check if professor record already exists
                            professorService.getById(userId);
                            System.out.println("Professor record already exists for ID: " + userId);
                        } catch (RuntimeException e) {
                            // Record doesn't exist, create it
                            System.out.println("Creating professor record for first-time user: " + userId);

                            Professor professor = new Professor(userId, metadata.getName());
                            Professor createdProfessor = professorService.create(professor);
                            if (createdProfessor != null) {
                                System.out.println("Professor record created successfully: " + createdProfessor.getName());
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Sign-in error: " + e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    // Helper method to safely get metadata fields
    private String getMetadataField(User.UserMetadata metadata, String fieldName, String defaultValue) {
        try {
            switch (fieldName) {
                case "code":
                    return metadata.getCode() != null ? metadata.getCode() : defaultValue;
                case "phone":
                    return metadata.getPhone() != null ? metadata.getPhone() : defaultValue;
                case "class_name":
                    return metadata.getClassName() != null ? metadata.getClassName() : defaultValue;
                case "student_group":
                    return metadata.getStudentGroup() != null ? metadata.getStudentGroup() : defaultValue;
                default:
                    return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
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

    /**
     * Resend verification email
     * POST /api/auth/resend-verification
     * Body: { "email": "user@example.com" }
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.badRequest().body(error);
            }

            supabaseAuthService.resendVerificationEmail(email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Resend verification error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
