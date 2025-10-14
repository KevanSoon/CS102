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