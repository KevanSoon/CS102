package com.cs102.attendance.service;

import java.util.UUID;

import com.cs102.attendance.enums.Status;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.Session;
import com.cs102.attendance.model.Student;

public interface AttendanceService {
    AttendanceRecord markAttendance(UUID studentId, UUID sessionId, Status status);
    AttendanceRecord markAttendance(Student student, Session session, Status status);
} 