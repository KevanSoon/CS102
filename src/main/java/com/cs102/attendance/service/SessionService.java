package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.model.Session;


@Service
public class SessionService extends SupabaseService<Session> {

    public SessionService(WebClient webClient) {
        super(webClient, "sessions", Session[].class, Session.class);
    }

    public Session update(String id, SessionUpdateDTO updatedDto) {
        // Calls the generic update method but with DTO object for patch
        return super.update(id, updatedDto);
    }

    
}
