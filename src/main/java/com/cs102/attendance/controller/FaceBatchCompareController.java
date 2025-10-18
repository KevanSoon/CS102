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

import com.cs102.attendance.entity.FaceData;
import com.cs102.attendance.repository.FaceDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/face-batch")
public class FaceBatchCompareController {

    @Autowired
    private FaceDataRepository faceDataRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Accepts image file via FormData and compares with all stored student images.
     */
    @PostMapping("/face-compare-all")
    public ResponseEntity<ObjectNode> compareUploadedFace(@RequestParam("image") MultipartFile image) throws IOException {
        ObjectNode finalResponse = mapper.createObjectNode();

        if (image.isEmpty()) {
            finalResponse.put("error", "No image file received");
            return ResponseEntity.badRequest().body(finalResponse);
        }

        // ✅ Convert uploaded file to Base64 string
        String base64Image = java.util.Base64.getEncoder().encodeToString(image.getBytes());

        // ✅ Fetch all students from DB
        List<FaceData> students = faceDataRepository.findAll();

        List<JsonNode> results = new ArrayList<>();
        double highestScore = 0.0;
        String bestStudentId = null;

        for (FaceData student : students) {
            try {
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

                double similarity = response.getBody().asDouble();

                ObjectNode resultNode = mapper.createObjectNode();
                resultNode.put("studentId", student.getStudentId());
                resultNode.put("studentUrl", student.getImageUrl());
                resultNode.put("comparisonResult", similarity);
                results.add(resultNode);

                if (similarity > 0.80 && similarity > highestScore) {
                    highestScore = similarity;
                    bestStudentId = student.getStudentId();
                }

            } catch (Exception e) {
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("studentId", student.getStudentId());
                errorNode.put("error", e.getMessage());
                results.add(errorNode);
            }
        }

        if (bestStudentId != null) {
            try {
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

            } catch (Exception e) {
                finalResponse.put("bestMatchStudentId", bestStudentId);
                finalResponse.put("error", "Error fetching student name: " + e.getMessage());
            }
        } 
        
        // else {
        //     finalResponse.put("message", "No match above 0.80 similarity threshold");
        // }

        return ResponseEntity.ok(finalResponse);
    }
}
