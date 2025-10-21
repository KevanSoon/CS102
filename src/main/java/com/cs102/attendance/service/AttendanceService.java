//package com.cs102.attendance.service;

//import com.cs102.attendance.entity.AttendanceRecord;
//import com.cs102.attendance.entity.Session;
//import com.cs102.attendance.entity.Student;
//import com.cs102.attendance.enums.Status;

//import java.util.UUID;

//public interface AttendanceService {
    //AttendanceRecord markAttendance(UUID studentId, UUID sessionId, Status status);
    //AttendanceRecord markAttendance(Student student, Session session, Status status);
//} 

package com.cs102.attendance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.repository.AttendanceRepository;

/**
 * AttendanceService handles business logic for attendance records.
 * It uses the Supabase-powered AttendanceRepository for CRUD operations.
 */
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // -------------------------------------------------------------------------
    // BASIC CRUD OPERATIONS
    // -------------------------------------------------------------------------

    /**
     * Retrieves all attendance records.
     *
     * @return List of all AttendanceRecord entities.
     */
    public List<AttendanceRecord> findAllRecords() {
        return attendanceRepository.findAll();
    }

    /**
     * Finds a single attendance record by its ID.
     *
     * @param id The unique ID of the attendance record.
     * @return Optional AttendanceRecord (empty if not found).
     */
    // FIX: Change ID type to String to match the updated repository signature (UUID is stored as a String)
    public Optional<AttendanceRecord> findById(String id) {
        return attendanceRepository.findById(id);
    }

    /**
     * Creates a new attendance record.
     *
     * @param record The AttendanceRecord to create.
     * @return The created AttendanceRecord.
     */
    public AttendanceRecord create(AttendanceRecord record) {
        return attendanceRepository.create(record);
    }

    /**
     * Updates an existing attendance record.
     *
     * @param id     The ID of the record to update.
     * @param record The updated AttendanceRecord data.
     * @return The updated AttendanceRecord.
     */
    // FIX: Change ID type to String to match the updated repository signature
    public AttendanceRecord update(String id, AttendanceRecord record) {
        return attendanceRepository.update(id, record);
    }

    /**
     * Deletes an attendance record by its ID.
     *
     * @param id The ID of the record to delete.
     */
    // FIX: Change ID type to String to match the updated repository signature
    public void delete(String id) {
        attendanceRepository.delete(id);
    }

    // -------------------------------------------------------------------------
    // CUSTOM QUERY METHODS
    // -------------------------------------------------------------------------

    /**
     * Retrieves all attendance records for a specific session.
     *
     * @param sessionId The ID of the session.
     * @return List of AttendanceRecord entities for that session.
     */
    // Note: findBySessionId is correct as Long, as it queries a FK column.
    public List<AttendanceRecord> findBySessionId(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId);
    }

    /**
     * Retrieves all attendance records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return List of AttendanceRecord entities for that student.
     */
    // Note: findByStudentId is correct as Long, as it queries a FK column.
    public List<AttendanceRecord> findByStudentId(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    /**
     * Finds a specific record by student and session.
     *
     * @param studentId The student's ID.
     * @param sessionId The session's ID.
     * @return Optional AttendanceRecord (empty if not found).
     */
    public Optional<AttendanceRecord> findByStudentIdAndSessionId(Long studentId, Long sessionId) {
        return attendanceRepository.findByStudentIdAndSessionId(studentId, sessionId);
    }
}

