// package com.cs102.attendance.service;

// import com.cs102.attendance.entity.AttendanceRecord;
// import com.cs102.attendance.entity.Session;
// import com.cs102.attendance.entity.Student;
// import com.cs102.attendance.enums.Method;
// import com.cs102.attendance.enums.Status;
// import com.cs102.attendance.repository.AttendanceRepository;
// import com.cs102.attendance.repository.StudentRepository;
// import com.cs102.attendance.repository.SessionRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.stereotype.Component;
// //import org.springframework.transaction.annotation.Transactional;

// import java.util.Optional;
// import java.util.UUID;

// @Component
// @Qualifier("manual")
// //@Transactional
// public class ManualMarker implements AttendanceService {
    
//     @Autowired
//     private AttendanceRepository attendanceRepository;
    
//     @Autowired
//     private StudentRepository studentRepository;
    
//     @Autowired
//     private SessionRepository sessionRepository;
    
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
//         // Check if attendance record already exists
//         Optional<AttendanceRecord> existingRecord = attendanceRepository.findBySessionAndStudent(session, student);
        
//         AttendanceRecord record;
//         if (existingRecord.isPresent()) {
//             record = existingRecord.get();
//             record.setStatus(status);
//             record.setMethod(Method.MANUAL);
//             record.setConfidence(null); // Manual marking doesn't have confidence
//         } else {
//             record = new AttendanceRecord(student, session, status, Method.MANUAL);
//         }
        
//         return attendanceRepository.save(record);
//     }
// } 

package com.cs102.attendance.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.entity.Student;
import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.repository.AttendanceRepository;
import com.cs102.attendance.repository.SessionRepository;
import com.cs102.attendance.repository.StudentRepository; // FIX: Added UUID import for casting

@Service
public class ManualMarker {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public ManualMarker(AttendanceRepository attendanceRepository,
                        StudentRepository studentRepository,
                        SessionRepository sessionRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Marks attendance manually.
     * @param studentId The student's ID (Long).
     * @param sessionId The session's ID (Long).
     * @param statusStr Attendance status string.
     * @return The updated or newly created AttendanceRecord.
     */
    public AttendanceRecord mark(Long studentId, Long sessionId, String statusStr) { // Note parameter excluded
        // 1️⃣ Validate student (Repository findById accepts Long)
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // 2️⃣ Validate session (Repository findById accepts Long)
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // 3️⃣ Convert status string to enum
        Status status;
        try {
            status = Status.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid attendance status: " + statusStr);
        }

        // 4️⃣ Check existing record
        Optional<AttendanceRecord> existingOpt =
                attendanceRepository.findByStudentIdAndSessionId(studentId, sessionId);

        AttendanceRecord record = existingOpt.orElseGet(() ->
                new AttendanceRecord(student, session, status, Method.MANUAL));

        // 5️⃣ Apply manual mark
        record.setStatus(status);
        record.setMethod(Method.MANUAL);
        record.setMarkedAt(LocalDateTime.now());
        record.setLastSeen(LocalDateTime.now());

        // 6️⃣ Save or update
        if (existingOpt.isPresent()) {
            // FIX: Cast to UUID (actual type) and call toString() to match the updated repository signature
            return attendanceRepository.update(((UUID) record.getId()).toString(), record); 
        } else {
            return attendanceRepository.create(record);
        }
    }
}




