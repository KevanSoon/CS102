package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.service.SessionService;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Session createSession(@RequestBody Session session) {
        return sessionService.create(session);
    }

    @GetMapping
    public List<Session> getAllSessions() {
        return sessionService.getAll();
    }

    // Implement update and delete as needed, example:
  
    @PatchMapping("/{id}")
    public Session updateSession(@PathVariable String id, @RequestBody SessionUpdateDTO updateDTO) {
        return sessionService.update(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable String id) {
        sessionService.delete(id);
    }
    
}
