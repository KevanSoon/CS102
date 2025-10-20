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

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * AttendanceService is the business logic layer for handling attendance records.
 * It interacts with the AttendanceRepository to fetch, save, and manage data.
 */
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // Constructor-based dependency injection (recommended)
    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // -------------------------------------------------------------------------
    // BASIC CRUD OPERATIONS
    // -------------------------------------------------------------------------

    /**
     * Retrieves all attendance records.
     * Called by AttendanceController.getAllAttendanceRecords().
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
    public Optional<AttendanceRecord> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    /**
     * Creates or updates an attendance record.
     *
     * @param record The AttendanceRecord to save.
     * @return The saved AttendanceRecord.
     */
    public AttendanceRecord save(AttendanceRecord record) {
        return attendanceRepository.save(record);
    }

    /**
     * Deletes an attendance record by its ID.
     *
     * @param id The ID of the record to delete.
     */
    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }

    // other methods: 

    /**
     * Retrieves all attendance records for a specific session.
     */
    public List<AttendanceRecord> findBySessionId(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId);
    }

    /**
     * Retrieves all attendance records for a specific student.
     */
    public List<AttendanceRecord> findByStudentId(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }
}
