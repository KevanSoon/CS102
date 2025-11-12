package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.model.Classes;


@Service
public class ClassesService extends SupabaseService<Classes> {

    public ClassesService(WebClient webClient) {
        super(webClient, "classes", Classes[].class, Classes.class);
    }
    
}
