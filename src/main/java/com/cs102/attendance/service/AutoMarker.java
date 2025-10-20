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

import com.cs102.attendance.dto.FaceDataDto;
import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.entity.Student;
import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.repository.AttendanceRepository;
import com.cs102.attendance.repository.SessionRepository;
import com.cs102.attendance.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime; // <-- ADDED: Needed if getStartTime returns LocalTime
import java.time.LocalDate; // <-- ADDED: Needed for LocalTime to LocalDateTime conversion
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles automatic attendance marking based on face recognition data.
 * Triggered by POST /api/attendance/auto/{sessionId}.
 */
@Service
public class AutoMarker {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;
    
    // Configuration values (MOCK/default values)
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
     *
     * @param sessionId The session ID to which the attendance belongs.
     * @param recognitionResults A list of recognized faces (each contains studentId and imageUrl).
     * @return A list of attendance records that were marked or updated.
     */
    public List<AttendanceRecord> process(Long sessionId, List<FaceDataDto> recognitionResults) {
        List<AttendanceRecord> updatedRecords = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDate today = LocalDate.now(); // Get today's date for LocalTime conversion

        // 1️⃣ Ensure the session exists and get its start time for the late check
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        
        // FIX: The getter should be for the session start time. Assuming it returns LocalTime (from error).
        // We convert it to LocalDateTime for comparison.
        LocalTime sessionLocalTime = session.getStartTime(); 
        LocalDateTime sessionStartTime = sessionLocalTime != null ? 
            LocalDateTime.of(today, sessionLocalTime) : null; 

        // 2️⃣ Loop through each recognized face
        for (FaceDataDto faceData : recognitionResults) {
            Long studentId;
            try {
                studentId = Long.valueOf(faceData.getStudentId());
            } catch (NumberFormatException e) {
                continue; // Skip invalid ID
            }

            // A. Roster Check: Verify student exists
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                continue; // Student not found/not in roster
            }
            Student student = studentOpt.get();


            // B. Find existing record
            AttendanceRecord record = attendanceRepository.findByStudentIdAndSessionId(studentId, sessionId);
            
            // C. Determine Status (Present or Late)
            Status status = determineStatus(sessionStartTime, currentTime);

            if (record != null) {
                // i. Manual Marking Override Check: Manual changes override auto-marks
                if (record.getMethod() == Method.MANUAL) {
                    continue; // Skip: Do not overwrite a manual mark
                }

                // ii. Cooldown Timer Check: Prevent duplicates
                long secondsSinceLastSeen = ChronoUnit.SECONDS.between(record.getLastSeen(), currentTime);
                if (secondsSinceLastSeen <= cooldownSeconds) {
                    // Update “Last Seen” instead.
                    record.setLastSeen(currentTime);
                    updatedRecords.add(attendanceRepository.save(record));
                    continue; // Skip creation/full update
                }
                
                // iii. Update existing auto/absent record
                record.setStatus(status);
                record.setMethod(Method.AUTO);
                record.setMarkedAt(currentTime);
                record.setLastSeen(currentTime);
                record.setConfidence(MOCK_CONFIDENCE);

            } else {
                // D. Create a new attendance record
                record = new AttendanceRecord(student, session, status, Method.AUTO);
                record.setConfidence(MOCK_CONFIDENCE);
                record.setLastSeen(currentTime);
            }

            // E. Save and add to result list
            updatedRecords.add(attendanceRepository.save(record));
        }

        return updatedRecords;
    }

    /**
     * Determines if the mark should be "Present" or "Late".
     */
    private Status determineStatus(LocalDateTime sessionStartTime, LocalDateTime markingTime) {
        if (sessionStartTime == null) return Status.PRESENT; 

        LocalDateTime lateTime = sessionStartTime.plusMinutes(lateThresholdMinutes);
        
        if (markingTime.isAfter(lateTime)) {
            return Status.LATE;
        } else {
            return Status.PRESENT;
        }
    }
}