package com.cs102.attendance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FastApiCallerService {

    private final RestTemplate restTemplate;
    
    @Value("${face-service.fastapi-url}")
    private String fastApiUrl;

    public FastApiCallerService() {
        this.restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
    }

    public String callFaceRecognitionWithImage(MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        
        // Wrap MultipartFile bytes with filename
        ByteArrayResource imageAsResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
            }
        };

        // Prepare multipart form data
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageAsResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to call FastAPI: HTTP " + response.getStatusCode().value());
        }
    }
}
