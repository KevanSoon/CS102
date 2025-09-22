package com.cs102.attendance.service;

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
    private static final String FASTAPI_URL = "https://kevansoon-java-facerecognition-endpoint.hf.space/face-recognition";

    public FastApiCallerService() {
        this.restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
    }

    public String callFaceRecognitionWithImage(MultipartFile image) throws Exception {
        // Wrap MultipartFile bytes with filename
        ByteArrayResource imageAsResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        // Prepare multipart form data
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageAsResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(FASTAPI_URL, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();  // Or parse JSON if needed
        } else {
            throw new RuntimeException("Failed to call FastAPI: HTTP " + response.getStatusCodeValue());
        }
    }
}
