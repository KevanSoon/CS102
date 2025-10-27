package com.cs102.attendance.service;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.AttendanceRecordUpdateDTO;
import com.cs102.attendance.dto.FaceDataUpdateDTO;
import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.dto.StudentUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class SupabaseService<T> {

    protected final WebClient webClient;
    protected final String tableName;
    private final Class<T[]> arrayType;
    private final Class<T> singleType;
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected SupabaseService(WebClient webClient, String tableName, Class<T[]> arrayType, Class<T> singleType) {
        this.webClient = webClient;
        this.tableName = tableName;
        this.arrayType = arrayType;
        this.singleType = singleType;
    }

    public T create(T entity) {
    //debugging request body
    try {
        String json = objectMapper.writeValueAsString(entity);
        System.out.println("=== SUPABASE INSERT ===");
        System.out.println("Table: " + tableName);
        System.out.println("Insert request body: " + json);
    }
    catch (Exception e) {
        e.printStackTrace();
    }
        try {
            // Supabase returns an array even for single inserts when using Prefer: return=representation
            T[] results = webClient.post()
                    .uri(tableName)
                    .bodyValue(entity)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.err.println("Supabase insert error response: " + errorBody);
                                return reactor.core.publisher.Mono.error(new RuntimeException("Supabase insert failed: " + errorBody));
                            })
                    )
                    .bodyToMono(arrayType)
                    .block();

            if (results != null && results.length > 0) {
                System.out.println("Insert successful! Returned " + results.length + " record(s)");
                return results[0];
            } else {
                System.out.println("Insert completed but no records returned");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception during insert: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<T> getAll() {
        T[] results = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("select", "*").build())
                .retrieve()
                .bodyToMono(arrayType)
                .block();
        return Arrays.asList(results);
    }

    // Get single record by ID
    public T getById(String id) {
        T[] results = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(tableName)
                        .queryParam("id", "eq." + id)
                        .queryParam("select", "*")
                        .build())
                .retrieve()
                .bodyToMono(arrayType)
                .block();

        if (results == null || results.length == 0) {
            throw new RuntimeException("No record found in table '" + tableName + "' with id: " + id);
        }
        return results[0];
    }


    //Student Update
    public Student update(String id, StudentUpdateDTO updateDTO) {
    //debugging request body
    try {
        String json = objectMapper.writeValueAsString(updateDTO);
        System.out.println("Update request body: " + json);
    } 
    catch (Exception e) {
        e.printStackTrace();
    }
    return webClient.patch()
            .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
            .bodyValue(updateDTO)
            .retrieve()
            .bodyToMono(Student.class)
            .block();
    }


    //Session Update
    public Session update(String id, SessionUpdateDTO updateDTO) {
        try {
            String json = objectMapper.writeValueAsString(updateDTO);
            System.out.println("Update request body: " + json);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .bodyValue(updateDTO)
                .retrieve()
                .bodyToMono(Session.class)
                .block();
    }

    //Attendance Record Update
      public AttendanceRecord update(String id, AttendanceRecordUpdateDTO updateDTO) {
        try {
            String json = objectMapper.writeValueAsString(updateDTO);
            System.out.println("Update request body: " + json);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .bodyValue(updateDTO)
                .retrieve()
                .bodyToMono(AttendanceRecord.class)
                .block();
    }

    //Attendance Manual / Auto Marker
      public T updateWithFilters(Map<String, String> filters, Object updateDTO) {
        try {
            String json = objectMapper.writeValueAsString(updateDTO);
            System.out.println("Update request body: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Build query params string for filters, e.g., session_id=eq.x&student_id=eq.y
        String filterQuery = filters.entrySet().stream()
            .map(entry -> entry.getKey() + "=eq." + entry.getValue())
            .collect(Collectors.joining("&"));
        
        // Compose URI with tableName and filter query parameters
        String uri = tableName + "?" + filterQuery;
        
        return webClient.patch()
                .uri(uri)
                .bodyValue(updateDTO)
                .retrieve()
                .bodyToMono(singleType)
                .block();
    }


    //Face Data Update
     public FaceData update(String id, FaceDataUpdateDTO updateDTO) {
        try {
            String json = objectMapper.writeValueAsString(updateDTO);
            System.out.println("Update request body: " + json);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .bodyValue(updateDTO)
                .retrieve()
                .bodyToMono(FaceData.class)
                .block();
    }


    public void delete(String id) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
