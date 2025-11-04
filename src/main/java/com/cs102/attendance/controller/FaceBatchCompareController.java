package com.cs102.attendance.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cs102.attendance.dto.AttendanceRecordUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.service.AttendanceRecordService;
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
    private AttendanceRecordService attendanceRecordService;

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

        for (FaceData student : students) {
            try {
                System.out.println("Comparing with student: " + student.getStudentId());
                
                // Build request body for /api/face-compare-url
                Map<String, String> requestBody = Map.of(
                        "imageBase64", base64Image,
                        "imageUrl2", student.getImageUrl()
                );

                ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                        "http://localhost:8080/api/face-compare-url",
                        requestBody,
                        JsonNode.class
                );

                if (response.getBody() == null) {
                    System.err.println("WARNING: Received null response body for student " + student.getStudentId());
                    continue;
                }

                double similarity = response.getBody().asDouble();
                System.out.println("Student " + student.getStudentId() + " similarity: " + similarity);

                ObjectNode resultNode = mapper.createObjectNode();
                resultNode.put("studentId", student.getStudentId());
                resultNode.put("studentUrl", student.getImageUrl());
                resultNode.put("comparisonResult", similarity);
                results.add(resultNode);

                if (similarity > 0.80 && similarity > highestScore) {
                    highestScore = similarity;
                    bestStudentId = student.getStudentId();
                    System.out.println("New best match: " + bestStudentId + " with score: " + highestScore);
                }

            } catch (Exception e) {
                System.err.println("Error comparing with student " + student.getStudentId() + ": " + e.getMessage());
                e.printStackTrace();
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("studentId", student.getStudentId());
                errorNode.put("error", e.getMessage());
                results.add(errorNode);
            }
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
                finalResponse.put("highestSimilarity", highestScore);

                //Auto marker method
                String sessionId = "129a7eeb-c163-428e-b639-ab0107358114";  // Hardcoded for now 

                AttendanceRecordUpdateDTO updateDTO = new AttendanceRecordUpdateDTO();
                updateDTO.setStatus("PRESENT");
                updateDTO.setMethod("AUTO");

                try {
                    AttendanceRecord updatedRecord = attendanceRecordService.updateBySessionAndStudent(sessionId, bestStudentId, updateDTO);
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
            System.out.println("No match found above 0.80 threshold. Highest score was: " + highestScore);
            finalResponse.put("message", "No match above 0.80 similarity threshold");
            finalResponse.put("highestSimilarity", highestScore);
            finalResponse.put("totalStudentsCompared", students.size());
        }

        System.out.println("Final response: " + finalResponse);
        System.out.println("=== Face Batch Compare Request Complete ===");
        return ResponseEntity.ok(finalResponse);
    }
}
