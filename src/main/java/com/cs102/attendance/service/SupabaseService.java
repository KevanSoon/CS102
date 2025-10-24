package com.cs102.attendance.service;


import java.util.Arrays;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.AttendanceRecordUpdateDTO;
import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.dto.StudentUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
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
        System.out.println("Insert request body: " + json);
    } 
    catch (Exception e) {
        e.printStackTrace();
    }
        return webClient.post()
                .uri(tableName)
                .bodyValue(entity)
                .retrieve()
                .bodyToMono(singleType)
                .block();
    }

    public List<T> getAll() {
        T[] results = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("select", "*").build())
                .retrieve()
                .bodyToMono(arrayType)
                .block();
        return Arrays.asList(results);
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



    public void delete(String id) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
