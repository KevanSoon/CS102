package com.cs102.attendance.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.service.SessionService;

@Component
public class SessionScheduler {
    
    private final SessionService sessionService;
    
    public SessionScheduler(SessionService sessionService) {
        this.sessionService = sessionService;
    }
    
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void deactivateExpiredSessions() {
        System.out.println("[SCHEDULER] Checking for expired sessions...");
        
        try {
            List<Session> allSessions = sessionService.getAll();
            LocalDateTime now = LocalDateTime.now();
            
            int deactivatedCount = 0;
            
            for (Session session : allSessions) {
                // Only check active sessions
                if (session.getActive() != null && session.getActive()) {
                    
                    // Check if we have both date and startTime
                    if (session.getDate() != null && session.getStartTime() != null) {
                        
                        // Combine date + startTime to get session start DateTime
                        LocalDateTime sessionStartTime = LocalDateTime.of(
                            session.getDate(),      // LocalDate
                            session.getStartTime()  // LocalTime (camelCase!)
                        );
                        
                        // Calculate minutes since session started
                        long minutesSinceStart = Duration
                            .between(sessionStartTime, now)
                            .toMinutes();
                        
                        // Skip if session hasn't started yet (future session)
                        if (minutesSinceStart < 0) {
                            System.out.println("[SCHEDULER] Session " + session.getId() + 
                                " (" + session.getName() + ") hasn't started yet. Skipping...");
                            continue;
                        }
                        
                        // If session started 15+ minutes ago, deactivate
                        if (minutesSinceStart >= 15) {
                            System.out.println("[SCHEDULER] Session " + session.getId() + 
                                " (" + session.getName() + ") started at " + 
                                sessionStartTime + ", " + minutesSinceStart + 
                                " minutes ago. Deactivating...");
                            
                            SessionUpdateDTO updateDTO = new SessionUpdateDTO();
                            updateDTO.setActive(false);
                            
                            sessionService.update(session.getId().toString(), updateDTO);
                            
                            deactivatedCount++;
                            System.out.println("[SCHEDULER] Session " + session.getId() + " deactivated successfully");
                        } else {
                            long minutesRemaining = 15 - minutesSinceStart;
                            System.out.println("[SCHEDULER] Session " + session.getId() + 
                                " (" + session.getName() + ") has " + minutesRemaining + 
                                " minutes remaining");
                        }
                    } else {
                        System.out.println("[SCHEDULER] Warning: Session " + session.getId() + 
                            " is missing date or startTime");
                    }
                }
            }
            
            if (deactivatedCount > 0) {
                System.out.println("[SCHEDULER] Deactivated " + deactivatedCount + " expired session(s)");
            } else {
                System.out.println("[SCHEDULER] No expired sessions found");
            }
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER] Error checking sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}