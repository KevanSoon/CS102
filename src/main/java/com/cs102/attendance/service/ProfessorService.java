package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.ProfessorUpdateDTO;
import com.cs102.attendance.model.Professor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProfessorService extends SupabaseService<Professor> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProfessorService(WebClient webClient) {
        super(webClient, "professors", Professor[].class, Professor.class);
    }

    public Professor update(String id, ProfessorUpdateDTO updatedDto) {
        try {
            String json = objectMapper.writeValueAsString(updatedDto);
            System.out.println("Update request body: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .bodyValue(updatedDto)
                .retrieve()
                .bodyToMono(Professor.class)
                .block();
    }
}
