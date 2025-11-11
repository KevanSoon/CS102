package com.cs102.attendance.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.service.AttendanceRecordService;
import com.cs102.attendance.service.SessionService;

@Component
public class SessionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SessionScheduler.class);
    
    private final AttendanceRecordService attendanceRecordService;
    private final SessionService sessionService;
    
    public SessionScheduler(SessionService sessionService, AttendanceRecordService attendanceRecordService) {
        this.sessionService = sessionService;
        this.attendanceRecordService = attendanceRecordService;
    }
    
    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void deactivateExpiredSessions() {
        logger.debug("Checking for expired sessions...");
        
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
                            session.getStartTime()  // LocalTime
                        );
                        
                        // Calculate minutes since session started
                        long minutesSinceStart = Duration
                            .between(sessionStartTime, now)
                            .toMinutes();
                        
                        // Skip if session hasn't started yet (future session)
                        if (minutesSinceStart < 0) {
                            logger.debug("Session {} ({}) hasn't started yet. Skipping...", 
                                session.getId(), session.getName());
                            continue;
                        }
                        
                        // If session started 15+ minutes ago, deactivate
                        if (minutesSinceStart >= 15) {
                            logger.info("Session {} ({}) started at {}, {} minutes ago. Deactivating...", 
                                session.getId(), session.getName(), sessionStartTime, minutesSinceStart);
                            
                            markAbsentStudents(session);

                            SessionUpdateDTO updateDTO = new SessionUpdateDTO();
                            updateDTO.setActive(false);
                            
                            sessionService.update(session.getId().toString(), updateDTO);
                            
                            deactivatedCount++;
                            logger.info("Session {} deactivated successfully", session.getId());
                        } else {
                            long minutesRemaining = 15 - minutesSinceStart;
                            logger.debug("Session {} ({}) has {} minutes remaining", 
                                session.getId(), session.getName(), minutesRemaining);
                        }
                    } else {
                        logger.warn("Session {} is missing date or startTime", session.getId());
                    }
                }
            }
            
            if (deactivatedCount > 0) {
                logger.info("Deactivated {} expired session(s)", deactivatedCount);
            } else {
                logger.debug("No expired sessions found");
            }
            
        } catch (Exception e) {
            logger.error("Error checking sessions", e);
        }
    }

    /**
     * Marks all students who haven't been marked yet as ABSENT for the given session
     */
    private void markAbsentStudents(Session session) {
        try {
            logger.debug("Marking absent students for session {}", session.getId());
            
            // Get all attendance records for this session
            List<AttendanceRecord> existingRecords = attendanceRecordService.getBySession(session.getId().toString());
            
            // Get the list of students who should be in this session
            List<String> expectedStudents = sessionService.getSessionStudentIds(session.getId().toString());
            
            if (expectedStudents == null || expectedStudents.isEmpty()) {
                logger.warn("No student list found for session {}", session.getId());
                return;
            }
            
            logger.debug("Expected students: {}, Existing attendance records: {}", 
                expectedStudents.size(), existingRecords.size());
            
            // Track which students already have attendance records
            List<String> markedStudents = existingRecords.stream()
                .map(AttendanceRecord::getStudent_id)
                .toList();
            
            // Mark remaining students as absent
            int absentCount = 0;
            for (String studentId : expectedStudents) {
                if (!markedStudents.contains(studentId)) {
                    try {
                        AttendanceRecord absentRecord = new AttendanceRecord();
                        absentRecord.setSession_id(session.getId().toString());
                        absentRecord.setStudent_id(studentId);
                        absentRecord.setStatus("ABSENT");
                        absentRecord.setMethod("AUTO");
                        absentRecord.setMarked_at(LocalDateTime.now(ZoneId.of("Asia/Singapore")));
                        absentRecord.setConfidence(null);
                        
                        attendanceRecordService.create(absentRecord);
                        absentCount++;
                        
                        logger.debug("Marked student {} as ABSENT", studentId);
                    } catch (Exception e) {
                        logger.error("Error marking student {} as absent", studentId, e);
                    }
                }
            }
            
            logger.info("Marked {} student(s) as ABSENT for session {}", absentCount, session.getId());
            
        } catch (Exception e) {
            logger.error("Error marking absent students for session {}", session.getId(), e);
        }
    }
}