package com.cs102.attendance.service;


import java.util.Arrays;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;

public abstract class SupabaseService<T> {

    protected final WebClient webClient;
    protected final String tableName;
    private final Class<T[]> arrayType;
    private final Class<T> singleType;

    protected SupabaseService(WebClient webClient, String tableName, Class<T[]> arrayType, Class<T> singleType) {
        this.webClient = webClient;
        this.tableName = tableName;
        this.arrayType = arrayType;
        this.singleType = singleType;
    }

    public T create(T entity) {
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

    public T update(Long id, T updatedEntity) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .bodyValue(updatedEntity)
                .retrieve()
                .bodyToMono(singleType)
                .block();
    }

    public void delete(Long id) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path(tableName).queryParam("id", "eq." + id).build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
