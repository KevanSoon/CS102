package com.cs102.attendance.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.service.AttendanceRecordService;
import com.cs102.attendance.service.SessionService;

@Component
public class SessionScheduler {
    private final AttendanceRecordService attendanceRecordService;
    private final SessionService sessionService;
    
    public SessionScheduler(SessionService sessionService, AttendanceRecordService attendanceRecordService) {
        this.sessionService = sessionService;
        this.attendanceRecordService = attendanceRecordService;
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
                            session.getStartTime()  // LocalTime
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
                            
                            markAbsentStudents(session);

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

    /**
     * Marks all students who haven't been marked yet as ABSENT for the given session
     */
    private void markAbsentStudents(Session session) {
        try {
            System.out.println("[SCHEDULER] Marking absent students for session " + session.getId());
            
            // Get all attendance records for this session
            List<AttendanceRecord> existingRecords = attendanceRecordService.getBySession(session.getId().toString());
            
            // Get the list of students who should be in this session
            List<String> expectedStudents = sessionService.getSessionStudentIds(session.getId().toString());
            
            if (expectedStudents == null || expectedStudents.isEmpty()) {
                System.out.println("[SCHEDULER] No student list found for session " + session.getId());
                return;
            }
            
            System.out.println("[SCHEDULER] Expected students: " + expectedStudents.size());
            System.out.println("[SCHEDULER] Existing attendance records: " + existingRecords.size());
            
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
                        
                        System.out.println("[SCHEDULER] Marked student " + studentId + " as ABSENT");
                    } catch (Exception e) {
                        System.err.println("[SCHEDULER] Error marking student " + studentId + " as absent: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("[SCHEDULER] Marked " + absentCount + " student(s) as ABSENT for session " + session.getId());
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER] Error marking absent students: " + e.getMessage());
            e.printStackTrace();
        }
    }
}