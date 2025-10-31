package com.cs102.attendance.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public Object getProfessorClasses(String professorId) {
        try {
            // Escape the curly braces by doubling them to prevent template expansion
            String queryValue = "cs.{{" + professorId + "}}";
            
            List<Map<String, Object>> classesResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("classes")
                    .queryParam("professor_list", queryValue)
                    .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

            if (classesResponse == null || classesResponse.isEmpty()) {
                return List.of();
            }

            List<Map<String, Object>> results = new ArrayList<>();

            for (Object obj : classesResponse) {
                if (!(obj instanceof Map)) continue;

                Map<String, Object> classMap = (Map<String, Object>) obj;
                String classCode = (String) classMap.get("class_code");

                List<Map<String, Object>> groupsResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("groups")
                        .queryParam("class_code", "eq." + classCode)
                        .build())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

                classMap.put("groups", groupsResponse != null ? groupsResponse : List.of());
                results.add(classMap);
            }

            return results;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of(Map.of(
                "error", "Failed to fetch professor classes",
                "message", e.getMessage()
            ));
        }
    }
}



