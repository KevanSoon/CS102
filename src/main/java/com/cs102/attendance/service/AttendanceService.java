package com.cs102.attendance.service;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.entity.Student;
import com.cs102.attendance.enums.Status;

import java.util.UUID;

public interface AttendanceService {
    AttendanceRecord markAttendance(UUID studentId, UUID sessionId, Status status);
    AttendanceRecord markAttendance(Student student, Session session, Status status);
} 