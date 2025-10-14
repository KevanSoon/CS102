package com.cs102.attendance.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository /*extends JpaRepository<AttendanceRecord, UUID>*/ {
    //Optional<AttendanceRecord> findBySessionAndStudent(Session session, Student student);
    //long countBySessionAndStatus(Session session, Status status);
} 