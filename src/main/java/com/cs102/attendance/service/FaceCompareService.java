package com.cs102.attendance.service;

import java.io.File;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cs102.attendance.dto.FaceVerificationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FaceCompareService {

    @Value("${face-service.deepface-url}")
    private String apiUrl;

    /**
     * Compare two face images and return verification result
     * @param frame1Path Path to first image
     * @param frame2Path Path to second image
     * @return FaceVerificationResult containing verified status and confidence, or null if comparison fails
     */
    public FaceVerificationResult faceCompare(String frame1Path, String frame2Path) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Wrap files in FileSystemResource
            FileSystemResource image1 = new FileSystemResource(new File(frame1Path));
            FileSystemResource image2 = new FileSystemResource(new File(frame2Path));

            // Use MultiValueMap for multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image1", image1);
            body.add("image2", image2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(Objects.requireNonNull(response.getBody()));

            System.out.println("=== DeepFace API Full Response ===");
            System.out.println(jsonNode.toPrettyString());
            System.out.println("===================================");

            // Extract verified and confidence from DeepFace response
            if (jsonNode.has("verified") && jsonNode.has("confidence")) {
                boolean verified = jsonNode.get("verified").asBoolean();
                double confidencePercentage = jsonNode.get("confidence").asDouble();
                double confidenceDecimal = confidencePercentage / 100.0;
                
                System.out.println("Verified: " + verified);
                System.out.println("Confidence: " + confidencePercentage + "% -> " + confidenceDecimal + " (decimal)");
                
                return new FaceVerificationResult(verified, confidenceDecimal);
            }
            
            System.err.println("Verified or Confidence field not found in response.");
            return null;

        } catch (Exception e) {
            System.err.println("Error in faceCompare: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
