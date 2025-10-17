package com.cs102.attendance.service;

import java.io.File;
import java.util.Objects;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FaceCompareService {

    private static final String API_URL = "https://face.miniai.live/api/face_match";

    public JsonNode faceCompare(String frame1Path, String frame2Path) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Wrap files in FileSystemResource
            FileSystemResource image1 = new FileSystemResource(new File(frame1Path));
            FileSystemResource image2 = new FileSystemResource(new File(frame2Path));

            // Use MultiValueMap instead of HashMap
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image1", image1);
            body.add("image2", image2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(Objects.requireNonNull(response.getBody()));

            System.out.println(jsonNode);

            // Extract similarity as a JsonNode
            JsonNode matchArray = jsonNode.get("match");
            if (matchArray != null && matchArray.isArray() && matchArray.size() > 0) {
                JsonNode matchObj = matchArray.get(0);
                if (matchObj.has("similarity")) {
                    return matchObj.get("similarity"); // Return as JsonNode
                }
            }
            System.err.println("Similarity field not found in response.");
            return null;
         

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
