package com.cs102.attendance.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SupabaseRestService {
    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final HttpHeaders headers;

    public SupabaseRestService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String supabaseKey) {
        this.restTemplate = new RestTemplate();
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        
        this.headers = new HttpHeaders();
        this.headers.set("apikey", supabaseKey);
        this.headers.set("Authorization", "Bearer " + supabaseKey);
        this.headers.set("Content-Type", "application/json");
        this.headers.set("Prefer", "return=representation");
        // this.headers.set("Accept", "application/vnd.pgrst.object+json");
    }

    public <T> T create(String table, T data, Class<T> responseType) {
        String url = String.format("%s/rest/v1/%s", supabaseUrl, table);
        HttpEntity<T> requestEntity = new HttpEntity<>(data, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
        return response.getBody();
    }

    public <T> List<T> read(String table, Map<String, String> queryParams, Class<T[]> responseType) {
        StringBuilder urlBuilder = new StringBuilder(String.format("%s/rest/v1/%s", supabaseUrl, table));
        
        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder.append("?");
            queryParams.forEach((key, value) -> 
                urlBuilder.append(key).append("=").append(value).append("&amp;"));
            urlBuilder.setLength(urlBuilder.length() - 1); // Remove last &amp;
        }

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<T[]> response = restTemplate.exchange(
            urlBuilder.toString(), 
            HttpMethod.GET, 
            requestEntity, 
            responseType
        );

        return Arrays.asList(response.getBody());
    }

    public <T> T update(String table, String id, T data, Class<T> responseType) {
        String url = String.format("%s/rest/v1/%s?id=eq.%s", supabaseUrl, table, id);
        HttpEntity<T> requestEntity = new HttpEntity<>(data, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, responseType);
        return response.getBody();
    }

    public void delete(String table, String id) {
        String url = String.format("%s/rest/v1/%s?id=eq.%s", supabaseUrl, table, id);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
    }
}