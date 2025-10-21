// package com.cs102.attendance.service;

// import com.cs102.attendance.entity.AttendanceRecord;
// import com.cs102.attendance.entity.Session;
// import com.cs102.attendance.entity.Student;
// import com.cs102.attendance.enums.Method;
// import com.cs102.attendance.enums.Status;
// import com.cs102.attendance.repository.AttendanceRepository;
// import com.cs102.attendance.repository.StudentRepository;
// import com.cs102.attendance.repository.SessionRepository;
// import com.cs102.attendance.config.RecognitionProperties;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.stereotype.Component;
// //import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.Optional;
// import java.util.UUID;

// @Component
// @Qualifier("auto")
// //@Transactional
// public class AutoMarker implements AttendanceService {
    
//     @Autowired
//     private AttendanceRepository attendanceRepository;
    
//     @Autowired
//     private StudentRepository studentRepository;
    
//     @Autowired
//     private SessionRepository sessionRepository;
    
//     @Autowired
//     private RecognitionProperties recognitionProperties;
    
//     @Override
//     public AttendanceRecord markAttendance(UUID studentId, UUID sessionId, Status status) {
//         Optional<Student> student = studentRepository.findById(studentId);
//         Optional<Session> session = sessionRepository.findById(sessionId);
        
//         if (student.isPresent() && session.isPresent()) {
//             return markAttendance(student.get(), session.get(), status);
//         }
        
//         throw new RuntimeException("Student or Session not found");
//     }
    
//     @Override
//     public AttendanceRecord markAttendance(Student student, Session session, Status status) {
//         return markAttendance(student, session, status, recognitionProperties.getConfidence());
//     }
    
//     public AttendanceRecord markAttendance(Student student, Session session, Status status, double confidence) {
//         // Check confidence threshold
//         if (confidence < recognitionProperties.getConfidence()) {
//             throw new RuntimeException("Recognition confidence too low: " + confidence);
//         }
        
//         // Check if attendance record already exists and cooldown period
//         Optional<AttendanceRecord> existingRecord = attendanceRepository.findBySessionAndStudent(session, student);
        
//         if (existingRecord.isPresent()) {
//             AttendanceRecord record = existingRecord.get();
//             LocalDateTime lastMarked = record.getMarkedAt();
//             LocalDateTime now = LocalDateTime.now();
            
//             // Check cooldown period
//             long timeSinceLastMark = java.time.Duration.between(lastMarked, now).toMillis();
//             if (timeSinceLastMark < recognitionProperties.getCooldownMs()) {
//                 throw new RuntimeException("Cooldown period not elapsed. Please wait " + 
//                     (recognitionProperties.getCooldownMs() - timeSinceLastMark) + "ms");
//             }
            
//             // Update existing record
//             record.setStatus(status);
//             record.setMethod(Method.AUTO);
//             record.setConfidence(confidence);
//             record.setMarkedAt(now);
//             return attendanceRepository.save(record);
//         } else {
//             // Create new record
//             AttendanceRecord record = new AttendanceRecord(student, session, status, Method.AUTO);
//             record.setConfidence(confidence);
//             return attendanceRepository.save(record);
//         }
//     }
// } 
package com.cs102.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cs102.attendance.dto.FaceDataDto;
import com.cs102.attendance.entity.AttendanceRecord;     // FIX: Added back
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.entity.Student;     // FIX: Added back
import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.repository.AttendanceRepository;
import com.cs102.attendance.repository.SessionRepository;
import com.cs102.attendance.repository.StudentRepository;      // FIX: Added UUID import

@Service
public class AutoMarker {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;

    private static final double MOCK_CONFIDENCE = 0.95;

    @Value("${attendance.auto.cooldown-seconds:20}")
    private long cooldownSeconds;

    @Value("${attendance.auto.late-threshold-minutes:15}")
    private long lateThresholdMinutes;

    @Autowired
    public AutoMarker(AttendanceRepository attendanceRepository,
                      StudentRepository studentRepository,
                      SessionRepository sessionRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Processes face recognition results and marks attendance automatically.
     */
    public List<AttendanceRecord> process(Long sessionId, List<FaceDataDto> recognitionResults) {
        List<AttendanceRecord> updatedRecords = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDate today = LocalDate.now(); // FIX: Needed for date conversion

        // ✅ Load session
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // FIX: Combine today's date with the session's LocalTime
        LocalTime sessionLocalTime = session.getStartTime();
        LocalDateTime sessionStartTime =
                sessionLocalTime != null ? LocalDateTime.of(today, sessionLocalTime) : null;
                
        for (FaceDataDto faceData : recognitionResults) {
            String studentIdStr = faceData.getStudentId();
            if (studentIdStr == null || studentIdStr.isBlank()) continue;

            Long studentId;
            try {
                studentId = Long.valueOf(studentIdStr);
            } catch (NumberFormatException e) {
                continue; 
            }

            // ✅ Roster Check
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) continue;
            Student student = studentOpt.get();

            // ✅ Check existing record
            Optional<AttendanceRecord> existingOpt =
                    attendanceRepository.findByStudentIdAndSessionId(studentId, sessionId);

            Status status = determineStatus(sessionStartTime, currentTime);
            AttendanceRecord record;

            if (existingOpt.isPresent()) {
                record = existingOpt.get();

                if (record.getMethod() == Method.MANUAL) continue;

                long secondsSinceLastSeen = ChronoUnit.SECONDS.between(record.getLastSeen(), currentTime);
                if (secondsSinceLastSeen <= cooldownSeconds) {
                    record.setLastSeen(currentTime);
                    // FIX: Convert UUID to String for updated repository method
                    updatedRecords.add(attendanceRepository.update(((UUID) record.getId()).toString(), record));
                    continue;
                }

                record.setStatus(status);
                record.setMethod(Method.AUTO);
                record.setMarkedAt(currentTime);
                record.setLastSeen(currentTime);
                record.setConfidence(MOCK_CONFIDENCE);
                // FIX: Convert UUID to String for updated repository method
                updatedRecords.add(attendanceRepository.update(((UUID) record.getId()).toString(), record));
            } else {
                record = new AttendanceRecord(student, session, status, Method.AUTO);
                record.setConfidence(MOCK_CONFIDENCE);
                record.setLastSeen(currentTime);
                updatedRecords.add(attendanceRepository.create(record));
            }
        }

        return updatedRecords;
    }

    private Status determineStatus(LocalDateTime sessionStartTime, LocalDateTime markingTime) {
        if (sessionStartTime == null) return Status.PRESENT;
        LocalDateTime lateTime = sessionStartTime.plusMinutes(lateThresholdMinutes);
        return markingTime.isAfter(lateTime) ? Status.LATE : Status.PRESENT;
    }
}



