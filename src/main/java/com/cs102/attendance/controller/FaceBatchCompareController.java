package com.cs102.attendance.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

    @PostMapping("/face-compare-all")
    public ResponseEntity<List<JsonNode>> getAllStudentImages(@RequestBody Map<String, String> payload) {
        String testImageUrl = payload.get("testImageUrl");

        if (testImageUrl == null || testImageUrl.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Fetch all students
        List<FaceData> students = faceDataRepository.findAll();

        // Prepare result list
        List<JsonNode> results = new ArrayList<>();

        for (FaceData student : students) {
            try {
            // Request body for internal call
            Map<String, String> requestBody = Map.of(
                        "imageUrl1", testImageUrl,
                        "imageUrl2", student.getImageUrl()
            );

            // Sequential call — RestTemplate waits for response
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                        "http://localhost:8080/api/face-compare-url",
                        requestBody,
                        JsonNode.class
            );


            // Store result with student info
            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.put("studentId", student.getStudentId());
            resultNode.put("studentUrl", student.getImageUrl());
            resultNode.set("comparisonResult", response.getBody());
            results.add(resultNode);
            

            // ObjectNode resultNode = mapper.createObjectNode();
            // resultNode.put("studentId", student.getStudentId());
            // resultNode.put("imageUrl", student.getImageUrl());
            // results.add(resultNode);

            }
            catch (Exception e ) {
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("studentId", student.getStudentId());
                errorNode.put("error", e.getMessage());
                results.add(errorNode);
            }

        }

        // Return list of student image URLs
        return ResponseEntity.ok(results);
    }
}
