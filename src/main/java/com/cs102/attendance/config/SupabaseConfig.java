package com.cs102.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api-key}")
    private String supabaseApiKey;

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1/")
                .defaultHeader("apikey", supabaseApiKey)
                .defaultHeader("Authorization", "Bearer " + supabaseApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
