package com.cs102.attendance.service;

import java.util.List;
import java.util.stream.Collectors;

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

    public Session getActiveSession(String professorId) {
        // Get all sessions
        List<Session> allSessions = getAll();
        
        // Filter for active sessions by this professor
        List<Session> activeSessions = allSessions.stream()
                .filter(s -> s.getActive() != null && s.getActive())
                .filter(s -> s.getCreatedBy() != null && 
                            s.getCreatedBy().toString().equals(professorId))
                .collect(Collectors.toList());
        
        // Handle results
        if (activeSessions.isEmpty()) {
            return null;  // No active session
        }
        
        if (activeSessions.size() > 1) {
            System.err.println("WARNING: Professor " + professorId + 
                " has " + activeSessions.size() + " active sessions!");
        }
        
        return activeSessions.get(0);  // Return first active session
    }


    
}
