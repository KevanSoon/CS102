package com.cs102.attendance.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.service.FaceDataService;


@RestController
@RequestMapping("/api/face_data")
public class FaceDataController {

    private final FaceDataService faceDataService;
    private final String supabaseUrl;
    private final String supabaseServiceRoleKey;
    private final WebClient storageWebClient;

    public FaceDataController(FaceDataService faceDataService,
                             @Value("${supabase.url}") String supabaseUrl,
                             @Value("${supabase.service-role-key}") String supabaseServiceRoleKey) {
        this.faceDataService = faceDataService;
        this.supabaseUrl = supabaseUrl;
        this.supabaseServiceRoleKey = supabaseServiceRoleKey;
        this.storageWebClient = WebClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1/")
                .defaultHeader("apikey", supabaseServiceRoleKey)
                .defaultHeader("Authorization", "Bearer " + supabaseServiceRoleKey)
                .build();
    }

    @PostMapping
    public FaceData createSession(@RequestBody FaceData faceData) {
        return faceDataService.create(faceData);
    }
    
    /**
     * Upload face image to Supabase Storage and create face_data record
     * POST /api/face_data/upload
     * Body: multipart/form-data with "image" file and "studentId" parameter
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFaceImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("studentId") String studentId) {
        try {
            System.out.println("=== FACE IMAGE UPLOAD REQUEST ===");
            System.out.println("Student ID: " + studentId);
            System.out.println("Image size: " + image.getSize() + " bytes");
            System.out.println("Image content type: " + image.getContentType());
            
            if (image.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Image file is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (studentId == null || studentId.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Student ID is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Generate unique filename
            String fileName = studentId + "_" + UUID.randomUUID().toString() + ".jpg";
            System.out.println("Generated filename: " + fileName);
            
            // Upload to Supabase Storage bucket "student-images"
            // Supabase Storage API: POST /storage/v1/object/{bucket}/{path}
            String uploadPath = "object/student-images/" + fileName;
            System.out.println("Upload path: " + uploadPath);
            System.out.println("Full URL: " + supabaseUrl + "/storage/v1/" + uploadPath);
            
            byte[] imageBytes = image.getBytes();
            String contentType = image.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg";
            }
            
            System.out.println("Uploading to Supabase Storage...");
            
            // Upload to Supabase Storage using binary data
            try {
                storageWebClient.post()
                        .uri(uploadPath)
                        .header("Content-Type", contentType)
                        .header("x-upsert", "true") // Allow overwriting if file exists
                        .body(BodyInserters.fromValue(imageBytes))
                        .retrieve()
                        .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("Supabase Storage error response: " + errorBody);
                                    System.err.println("Status code: " + clientResponse.statusCode());
                                    return reactor.core.publisher.Mono.error(
                                        new RuntimeException("Failed to upload image to Supabase Storage: " + errorBody)
                                    );
                                })
                        )
                        .bodyToMono(String.class)
                        .block();
                
                System.out.println("Successfully uploaded to Supabase Storage");
            } catch (Exception storageError) {
                System.err.println("Storage upload error: " + storageError.getMessage());
                storageError.printStackTrace();
                throw storageError;
            }
            
            // Construct public URL
            String imageUrl = supabaseUrl + "/storage/v1/object/public/student-images/" + fileName;
            System.out.println("Image URL: " + imageUrl);
            
            // Create face_data record
            FaceData faceData = new FaceData();
            faceData.setStudentId(studentId);
            faceData.setImageUrl(imageUrl);
            
            System.out.println("Creating face_data record...");
            System.out.println("FaceData object - studentId: " + faceData.getStudentId() + ", imageUrl: " + faceData.getImageUrl());
            
            FaceData savedFaceData = faceDataService.create(faceData);
            
            if (savedFaceData != null) {
                System.out.println("Face data record created successfully with ID: " + savedFaceData.getId());
            } else {
                System.err.println("WARNING: Face data record creation returned null");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("faceData", savedFaceData);
            response.put("imageUrl", imageUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error uploading face image: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload face image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    
}
