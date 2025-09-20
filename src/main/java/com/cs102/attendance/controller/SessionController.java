package com.cs102.attendance.controller;

import com.cs102.attendance.entity.Session;
import com.cs102.attendance.dto.SessionDto;
import com.cs102.attendance.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    // Create a new session
    @PostMapping
    public ResponseEntity<Session> createSession(@RequestBody CreateSessionRequest request) {
        try {
            Session session = sessionService.createSession(
                request.getName(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
            );
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all sessions
    @GetMapping
    public ResponseEntity<List<Session>> getAllSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    // Get session by ID
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable UUID id) {
        Optional<Session> session = sessionService.getSessionById(id);
        return session.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // Get today's sessions
    @GetMapping("/today")
    public ResponseEntity<List<SessionDto>> getTodaySessions() {
        List<SessionDto> sessions = sessionService.getTodaySessionDtos();
        return ResponseEntity.ok(sessions);
    }

    // Get sessions for specific date
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Session>> getSessionsByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Session> sessions = sessionService.getTodaySessions(localDate);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Close session
    @PutMapping("/{id}/close")
    public ResponseEntity<Session> closeSession(@PathVariable UUID id) {
        try {
            Session session = sessionService.closeSession(id);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete session
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID id) {
        try {
            sessionService.deleteSession(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DTO for request body
    public static class CreateSessionRequest {
        private String name;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    }
} 