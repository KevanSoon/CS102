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

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.entity.Student;
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.repository.AttendanceRepository;
import com.cs102.attendance.repository.StudentRepository;
import com.cs102.attendance.repository.SessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Handles manual attendance marking logic.
 * Triggered when a teacher manually marks attendance via the REST API.
 */
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
     * Marks a student's attendance manually.
     * Manual changes override auto-marks, logged with "Manual" method.
     *
     * @param studentId The student's ID.
     * @param sessionId The session's ID.
     * @param statusStr Attendance status string (Present, Absent, Late).
     * @return The updated or newly created AttendanceRecord.
     */
    public AttendanceRecord mark(Long studentId, Long sessionId, String statusStr) {
        // 1️⃣ Validate entities
        // FIX: Use .orElseThrow() on Optional<Student>
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // FIX: Use .orElseThrow() on Optional<Session>
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // 2️⃣ Convert status string to Enum
        Status status = Status.valueOf(statusStr.toUpperCase());
        
        // 3️⃣ Check if a record already exists for this student & session
        AttendanceRecord record = attendanceRepository
                .findByStudentIdAndSessionId(studentId, sessionId);

        if (record == null) {
            // New record: Manual changes override auto-marks (default to manual)
            record = new AttendanceRecord(student, session, status, Method.MANUAL);
        }

        // 4️⃣ Apply manual overrides (overwrites any existing AUTO or MANUAL mark)
        record.setStatus(status);
        record.setMethod(Method.MANUAL);
        record.setMarkedAt(LocalDateTime.now());
        record.setLastSeen(LocalDateTime.now()); // Update lastSeen

        // 5️⃣ Save the updated record
        return attendanceRepository.save(record);
    }
}
