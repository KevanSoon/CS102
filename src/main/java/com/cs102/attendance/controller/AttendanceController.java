// package com.cs102.attendance.controller;

// import com.cs102.attendance.entity.AttendanceRecord;
// import com.cs102.attendance.enums.Status;
// import com.cs102.attendance.service.AttendanceService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/attendance")
// public class AttendanceController {

//     @Autowired
//     @Qualifier("manual")
//     private AttendanceService manualMarker;

//     @Autowired
//     @Qualifier("auto")
//     private AttendanceService autoMarker;

//     // Mark attendance manually
//     @PostMapping("/manual")
//     public ResponseEntity<AttendanceRecord> markManualAttendance(@RequestBody MarkAttendanceRequest request) {
//         try {
//             AttendanceRecord record = manualMarker.markAttendance(
//                 request.getStudentId(),
//                 request.getSessionId(),
//                 request.getStatus()
//             );
//             return ResponseEntity.ok(record);
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest().body(null);
//         }
//     }

//     // Mark attendance automatically (with confidence)
//     @PostMapping("/auto")
//     public ResponseEntity<AttendanceRecord> markAutoAttendance(@RequestBody MarkAutoAttendanceRequest request) {
//         try {
//             // For auto marking, we need to cast to AutoMarker to access the confidence method
//             com.cs102.attendance.service.AutoMarker autoMarkerImpl = 
//                 (com.cs102.attendance.service.AutoMarker) autoMarker;
            
//             // First get the student and session entities, then call the method with confidence
//             AttendanceRecord record = autoMarkerImpl.markAttendance(
//                 request.getStudentId(),
//                 request.getSessionId(),
//                 request.getStatus()
//             );
//             return ResponseEntity.ok(record);
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest().body(null);
//         }
//     }

//     // DTOs for request bodies
//     public static class MarkAttendanceRequest {
//         private UUID studentId;
//         private UUID sessionId;
//         private Status status;

//         // Getters and setters
//         public UUID getStudentId() { return studentId; }
//         public void setStudentId(UUID studentId) { this.studentId = studentId; }
//         public UUID getSessionId() { return sessionId; }
//         public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
//         public Status getStatus() { return status; }
//         public void setStatus(Status status) { this.status = status; }
//     }

//     public static class MarkAutoAttendanceRequest extends MarkAttendanceRequest {
//         private double confidence;

//         public double getConfidence() { return confidence; }
//         public void setConfidence(double confidence) { this.confidence = confidence; }
//     }
// } 

package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.dto.FaceDataDto;
import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.service.AttendanceService;
import com.cs102.attendance.service.AutoMarker;
import com.cs102.attendance.service.ManualMarker;

@RestController
@RequestMapping("/api/attendance")
// GET/api/attendance = get all records
// POST/api/attendance/manual = mark attendance manually
// POST/api/attendance/auto/{sessionId} = mark attendance automatically
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ManualMarker manualMarker;
    private final AutoMarker autoMarker;

    @Autowired
    public AttendanceController(
            AttendanceService attendanceService,
            ManualMarker manualMarker,
            AutoMarker autoMarker) {
        this.attendanceService = attendanceService;
        this.manualMarker = manualMarker;
        this.autoMarker = autoMarker;
    }

    @GetMapping // HTTP GET/api/attendance --> retrieving all stored attendance records
    public ResponseEntity<List<AttendanceRecord>> getAllAttendanceRecords() {
        return ResponseEntity.ok(attendanceService.findAllRecords());
    }


    /**
     * Handles manual attendance marking. 
     * Expects a JSON body with keys: studentId, sessionId, status, and note (optional).
     * @param request The object containing manual marking data.
     * @return The updated AttendanceRecord.
     */
    @PostMapping("/manual")
    public ResponseEntity<AttendanceRecord> markManual(@RequestBody ManualMarkingRequest request) {
        // Delegate to the ManualMarker for all business logic
        AttendanceRecord updatedRecord = manualMarker.mark(
            request.getStudentId(),
            request.getSessionId(),
            request.getStatus()
        );
        return ResponseEntity.ok(updatedRecord);
    }

    // Simple inner class to represent the expected manual marking request body.
    public static class ManualMarkingRequest {
        private Long studentId; // Changed type to Long
        private Long sessionId; // Changed type to Long
        private String status; // Present/Absent/Late
        
        // Getters
        public Long getStudentId() { return studentId; }
        public Long getSessionId() { return sessionId; }
        public String getStatus() { return status; }

        // Setters (required for Spring JSON binding)
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public void setStatus(String status) { this.status = status; }
    }

    // Auto Face Recognition Marking Endpoint (Using FaceDataDto)

    /**
     * Handles automatic marking based on face recognition results, utilizing the AutoMarker service.
     * @param sessionId The ID of the session the records belong to.
     * @param recognitionResults List of FaceDataDto (studentId, imageUrl).
     * @return List of AttendanceRecords that were newly created or updated.
     */
    @PostMapping("/auto/{sessionId}")
    public ResponseEntity<List<AttendanceRecord>> markAuto(
            @PathVariable Long sessionId,
            @RequestBody List<FaceDataDto> recognitionResults) {

        // Delegate to the AutoMarker for all business logic
        List<AttendanceRecord> markedRecords = autoMarker.process(sessionId, recognitionResults);
        
        return ResponseEntity.ok(markedRecords);
    }
}
