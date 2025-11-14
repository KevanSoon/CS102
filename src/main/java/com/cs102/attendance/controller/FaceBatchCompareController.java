package com.cs102.attendance.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cs102.attendance.dto.FaceVerificationResult;
import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.service.FaceCompareService;
import com.cs102.attendance.service.FaceDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/face-batch")
public class FaceBatchCompareController {

    @Autowired
    private FaceDataService faceDataService;

    @Autowired
    private FaceCompareService faceCompareService;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    
    
    //Accepts image file via FormData from face-detection-model.js and compares it with student images stored in Supabase
    @PostMapping("/face-compare-all")
    public ResponseEntity<ObjectNode> compareUploadedFace(@RequestParam("image") MultipartFile image) throws IOException {
        ObjectNode finalResponse = mapper.createObjectNode();

        if (image.isEmpty()) {
            finalResponse.put("error", "No image file received");
            return ResponseEntity.badRequest().body(finalResponse);
        }

        //Convert file to Base64 string
        String base64Image = java.util.Base64.getEncoder().encodeToString(image.getBytes());

        // Fetch all students from Supabase
        List<FaceData> students = faceDataService.getAll();
        
        List<JsonNode> results = new ArrayList<>();
        double highestScore = 0.0;
        String bestStudentId = null;

        // Create temp file for uploaded image once (reuse for all comparisons)
        File uploadedImageFile = null;
        try {
            uploadedImageFile = base64ToTempFile(base64Image);
        } catch (IOException e) {
            finalResponse.put("error", "Failed to process uploaded image");
            return ResponseEntity.badRequest().body(finalResponse);
        }

        for (FaceData student : students) {
            File studentImageFile = null;
            try {
                // Download student image to temp file
                studentImageFile = downloadUrlToTempFile(student.getImageUrl());
                
                // Calls FaceCompareService 
                FaceVerificationResult verificationResult = faceCompareService.faceCompare(
                        uploadedImageFile.getAbsolutePath(),
                        studentImageFile.getAbsolutePath()
                );

                if (verificationResult == null) {
                    continue;
                }

                boolean verified = verificationResult.isVerified();
                double confidence = verificationResult.getConfidence();

                ObjectNode resultNode = mapper.createObjectNode();
                resultNode.put("studentId", student.getStudentId());
                resultNode.put("studentUrl", student.getImageUrl());
                resultNode.put("verified", verified);
                resultNode.put("confidence", confidence);
                results.add(resultNode);

                // Use DeepFace's verified flag and highest confidence score
                if (verified && confidence > highestScore) {
                    highestScore = confidence;
                    bestStudentId = student.getStudentId();
                }

            } catch (Exception e) {
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("studentId", student.getStudentId());
                errorNode.put("error", e.getMessage());
                results.add(errorNode);
            } finally {
                // Clean up student image temp file
                if (studentImageFile != null && studentImageFile.exists()) {
                    studentImageFile.delete();
                }
            }
        }

        // Clean up uploaded image temp file
        if (uploadedImageFile != null && uploadedImageFile.exists()) {
            uploadedImageFile.delete();
        }

        if (bestStudentId != null) {
            try {
                //retrieving best student data for frontend
                ResponseEntity<JsonNode> studentResponse = restTemplate.getForEntity(
                        "http://localhost:8080/api/students/" + bestStudentId,
                        JsonNode.class
                );

                JsonNode studentData = studentResponse.getBody();
                String studentName = (studentData != null && studentData.has("name"))
                        ? studentData.get("name").asText()
                        : "Unknown";

                finalResponse.put("bestMatchStudentId", bestStudentId);
                finalResponse.put("bestMatchName", studentName);
                finalResponse.put("highestConfidence", highestScore);
                finalResponse.put("verified", true);

            } catch (Exception e) {
                finalResponse.put("bestMatchStudentId", bestStudentId);
                finalResponse.put("error", "Error fetching student name: " + e.getMessage());
            }
        } else {
            finalResponse.put("message", "No verified face match found");
            finalResponse.put("highestConfidence", highestScore);
            finalResponse.put("totalStudentsCompared", students.size());
        }

        System.out.println("Final response: " + finalResponse);
        System.out.println("=== Face Batch Compare Request Complete ===");
        return ResponseEntity.ok(finalResponse);
    }

    
    //Helper method to convert Base64 string to temporary file
    private File base64ToTempFile(String base64Image) throws IOException {
        File tempFile = File.createTempFile("uploaded_", ".jpg");
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(decodedBytes);
        }
        return tempFile;
    }

    //Helper method to download image from URL to temporary file
    private File downloadUrlToTempFile(String imageUrl) throws IOException {
        File tempFile = File.createTempFile("student_", ".png");
        try (InputStream in = URI.create(imageUrl).toURL().openStream()) {
            Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }
}
