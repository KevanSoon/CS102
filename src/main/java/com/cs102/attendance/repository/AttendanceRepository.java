package com.cs102.attendance.repository;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.entity.Session;
import com.cs102.attendance.entity.Student;
import com.cs102.attendance.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findBySessionAndStudent(Session session, Student student);
    long countBySessionAndStatus(Session session, Status status);
} 