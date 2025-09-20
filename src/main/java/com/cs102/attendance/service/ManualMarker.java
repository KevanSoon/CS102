package com.cs102.attendance.service;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.entity.Student;
import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.repository.AttendanceRepository;
import com.cs102.attendance.repository.StudentRepository;
import com.cs102.attendance.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Qualifier("manual")
@Transactional
public class ManualMarker implements AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Override
    public AttendanceRecord markAttendance(UUID studentId, UUID sessionId, Status status) {
        Optional<Student> student = studentRepository.findById(studentId);
        Optional<Session> session = sessionRepository.findById(sessionId);
        
        if (student.isPresent() && session.isPresent()) {
            return markAttendance(student.get(), session.get(), status);
        }
        
        throw new RuntimeException("Student or Session not found");
    }
    
    @Override
    public AttendanceRecord markAttendance(Student student, Session session, Status status) {
        // Check if attendance record already exists
        Optional<AttendanceRecord> existingRecord = attendanceRepository.findBySessionAndStudent(session, student);
        
        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            record.setStatus(status);
            record.setMethod(Method.MANUAL);
            record.setConfidence(null); // Manual marking doesn't have confidence
        } else {
            record = new AttendanceRecord(student, session, status, Method.MANUAL);
        }
        
        return attendanceRepository.save(record);
    }
} 