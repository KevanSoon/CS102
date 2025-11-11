package com.cs102.attendance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

 

    @GetMapping("/active/{profId}")
    public Session getActiveSession(@PathVariable String profId) {
        return sessionService.getActiveSession(profId);
    }

    @GetMapping("/{sessionId}/students")
    public ResponseEntity<List<Map<String, Object>>> getSessionStudents(@PathVariable String sessionId) {
        try {
            List<Map<String, Object>> students = sessionService.getSessionStudents(sessionId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Session> closeSession(@PathVariable String id) {
        try {
            // First, mark absent students before closing
            sessionService.markAbsentStudentsForSession(id);
            
            SessionUpdateDTO updateDTO = new SessionUpdateDTO();
            updateDTO.setActive(false);
            
            Session closedSession = sessionService.update(id, updateDTO);
            
            if (closedSession == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(closedSession);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}")
    public Session updateSession(@PathVariable String id, @RequestBody SessionUpdateDTO updateDTO) {
        return sessionService.update(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable String id) {
        sessionService.delete(id);
    }
    
}
