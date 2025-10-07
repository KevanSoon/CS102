package com.cs102.attendance.controller;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    @Qualifier("manual")
    private AttendanceService manualMarker;

    @Autowired
    @Qualifier("auto")
    private AttendanceService autoMarker;

    // Mark attendance manually
    @PostMapping("/manual")
    public ResponseEntity<AttendanceRecord> markManualAttendance(@RequestBody MarkAttendanceRequest request) {
        try {
            AttendanceRecord record = manualMarker.markAttendance(
                request.getStudentId(),
                request.getSessionId(),
                request.getStatus()
            );
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Mark attendance automatically (with confidence)
    @PostMapping("/auto")
    public ResponseEntity<AttendanceRecord> markAutoAttendance(@RequestBody MarkAutoAttendanceRequest request) {
        try {
            // For auto marking, we need to cast to AutoMarker to access the confidence method
            com.cs102.attendance.service.AutoMarker autoMarkerImpl = 
                (com.cs102.attendance.service.AutoMarker) autoMarker;
            
            // First get the student and session entities, then call the method with confidence
            AttendanceRecord record = autoMarkerImpl.markAttendance(
                request.getStudentId(),
                request.getSessionId(),
                request.getStatus()
            );
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // DTOs for request bodies
    public static class MarkAttendanceRequest {
        private UUID studentId;
        private UUID sessionId;
        private Status status;

        // Getters and setters
        public UUID getStudentId() { return studentId; }
        public void setStudentId(UUID studentId) { this.studentId = studentId; }
        public UUID getSessionId() { return sessionId; }
        public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    public static class MarkAutoAttendanceRequest extends MarkAttendanceRequest {
        private double confidence;

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
} 