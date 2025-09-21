// FastApiCallerService.java
package com.cs102.attendance.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FastApiCallerService {

    private static final String FASTAPI_URL = "https://kevansoon-java-facerecognition-endpoint.hf.space/face-recognition";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FastApiCallerService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String callFaceRecognition() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(FASTAPI_URL))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            return jsonNode.path("result").asText();
        } else {
            throw new RuntimeException("Failed to call FastAPI: HTTP " + response.statusCode());
        }
    }
}
