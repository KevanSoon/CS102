//package com.cs102.attendance.repository;

//import org.springframework.stereotype.Repository;

//@Repository
//public interface AttendanceRepository /*extends JpaRepository<AttendanceRecord, UUID>*/ {
    //Optional<AttendanceRecord> findBySessionAndStudent(Session session, Student student);
    //long countBySessionAndStatus(Session session, Status status);
//} 

package com.cs102.attendance.repository;

import com.cs102.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AttendanceRepository provides database access methods for AttendanceRecord entities.
 * 
 * It extends JpaRepository, which gives you built-in CRUD operations such as:
 * - findAll()
 * - findById()
 * - save()
 * - deleteById()
 * 
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * Finds all attendance records for a given session.
     *
     * @param sessionId The ID of the session.
     * @return A list of AttendanceRecord objects for that session.
     */
    List<AttendanceRecord> findBySessionId(Long sessionId);

    /**
     * Finds all attendance records for a given student.
     *
     * @param studentId The ID of the student.
     * @return A list of AttendanceRecord objects for that student.
     */
    List<AttendanceRecord> findByStudentId(Long studentId);

    /**
     * Finds a specific record by student and session.
     * Useful when you want to check or update a single student’s attendance for one session.
     *
     * @param studentId The ID of the student.
     * @param sessionId The ID of the session.
     * @return The matching AttendanceRecord, or null if not found.
     */
    AttendanceRecord findByStudentIdAndSessionId(Long studentId, Long sessionId);
}
