package com.cs102.attendance.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cs102.attendance.dto.AttendanceRecordUpdateDTO;
import com.cs102.attendance.dto.FaceVerificationResult;
import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.service.AttendanceRecordService;
import com.cs102.attendance.service.FaceDataService;
import com.cs102.attendance.service.FaceCompareService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

@RestController
@RequestMapping("/api/face-batch")
public class FaceBatchCompareController {

    @Autowired
    private FaceDataService faceDataService;

    @Autowired
    private AttendanceRecordService attendanceRecordService;

    @Autowired
    private FaceCompareService faceCompareService;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Accepts image file via FormData and compares with all stored student images.
     */
    @PostMapping("/face-compare-all")
    public ResponseEntity<ObjectNode> compareUploadedFace(@RequestParam("image") MultipartFile image) throws IOException {
        System.out.println("=== Face Batch Compare Request Received ===");
        System.out.println("Image filename: " + image.getOriginalFilename());
        System.out.println("Image size: " + image.getSize() + " bytes");
        System.out.println("Content type: " + image.getContentType());
        
        ObjectNode finalResponse = mapper.createObjectNode();

        if (image.isEmpty()) {
            System.err.println("ERROR: No image file received");
            finalResponse.put("error", "No image file received");
            return ResponseEntity.badRequest().body(finalResponse);
        }

        // ✅ Convert uploaded file to Base64 string
        String base64Image = java.util.Base64.getEncoder().encodeToString(image.getBytes());
        System.out.println("Base64 image length: " + base64Image.length());

        // ✅ Fetch all students from DB
        List<FaceData> students = faceDataService.getAll();
        System.out.println("Fetched " + students.size() + " students from database");
        
        List<JsonNode> results = new ArrayList<>();
        double highestScore = 0.0;
        String bestStudentId = null;

        // Create temp file for uploaded image once (reuse for all comparisons)
        File uploadedImageFile = null;
        try {
            uploadedImageFile = base64ToTempFile(base64Image);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to create temp file from uploaded image: " + e.getMessage());
            finalResponse.put("error", "Failed to process uploaded image");
            return ResponseEntity.badRequest().body(finalResponse);
        }

        for (FaceData student : students) {
            File studentImageFile = null;
            try {
                System.out.println("Comparing with student: " + student.getStudentId());
                
                // Download student image to temp file
                studentImageFile = downloadUrlToTempFile(student.getImageUrl());
                
                // Call FaceCompareService directly instead of making HTTP request
                FaceVerificationResult verificationResult = faceCompareService.faceCompare(
                        uploadedImageFile.getAbsolutePath(),
                        studentImageFile.getAbsolutePath()
                );

                if (verificationResult == null) {
                    System.err.println("WARNING: Received null result from FaceCompareService for student " + student.getStudentId());
                    continue;
                }

                boolean verified = verificationResult.isVerified();
                double confidence = verificationResult.getConfidence();
                
                System.out.println("Student " + student.getStudentId() + 
                                 " - Verified: " + verified + 
                                 ", Confidence: " + confidence);

                ObjectNode resultNode = mapper.createObjectNode();
                resultNode.put("studentId", student.getStudentId());
                resultNode.put("studentUrl", student.getImageUrl());
                resultNode.put("verified", verified);
                resultNode.put("confidence", confidence);
                results.add(resultNode);

                // Use DeepFace's verified flag instead of manual threshold
                if (verified && confidence > highestScore) {
                    highestScore = confidence;
                    bestStudentId = student.getStudentId();
                    System.out.println("New best match: " + bestStudentId + " with confidence: " + highestScore);
                }

            } catch (Exception e) {
                System.err.println("Error comparing with student " + student.getStudentId() + ": " + e.getMessage());
                e.printStackTrace();
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

                //Auto marker method
                String sessionId = "129a7eeb-c163-428e-b639-ab0107358114";  // Hardcoded for now 

                AttendanceRecordUpdateDTO updateDTO = new AttendanceRecordUpdateDTO();
                updateDTO.setStatus("PRESENT");
                updateDTO.setMethod("AUTO");
                updateDTO.setConfidence(highestScore);  // Store the confidence score

                try {
                    attendanceRecordService.updateBySessionAndStudent(sessionId, bestStudentId, updateDTO);
                    // Optionally add info to finalResponse
                    finalResponse.put("autoMarkerStatus", "Success");
                } catch (Exception e) {
                    finalResponse.put("autoMarkerError", "Failed to update attendance record: " + e.getMessage());
                }



            } catch (Exception e) {
                finalResponse.put("bestMatchStudentId", bestStudentId);
                finalResponse.put("error", "Error fetching student name: " + e.getMessage());
            }
        } else {
            System.out.println("No verified match found. Highest confidence was: " + highestScore);
            finalResponse.put("message", "No verified face match found");
            finalResponse.put("highestConfidence", highestScore);
            finalResponse.put("totalStudentsCompared", students.size());
        }

        System.out.println("Final response: " + finalResponse);
        System.out.println("=== Face Batch Compare Request Complete ===");
        return ResponseEntity.ok(finalResponse);
    }

    /**
     * Helper method to convert Base64 string to temporary file
     * @param base64Image Base64 encoded image string
     * @return Temporary File object
     * @throws IOException if file creation fails
     */
    private File base64ToTempFile(String base64Image) throws IOException {
        File tempFile = File.createTempFile("uploaded_", ".jpg");
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(decodedBytes);
        }
        System.out.println("Created temp file from Base64: " + tempFile.getAbsolutePath() + " (" + tempFile.length() + " bytes)");
        return tempFile;
    }

    /**
     * Helper method to download image from URL to temporary file
     * @param imageUrl URL of the image to download
     * @return Temporary File object
     * @throws IOException if download or file creation fails
     */
    private File downloadUrlToTempFile(String imageUrl) throws IOException {
        File tempFile = File.createTempFile("student_", ".png");
        try (InputStream in = URI.create(imageUrl).toURL().openStream()) {
            long bytesCopied = Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Downloaded image from URL: " + imageUrl + " (" + bytesCopied + " bytes)");
        }
        return tempFile;
    }
}
