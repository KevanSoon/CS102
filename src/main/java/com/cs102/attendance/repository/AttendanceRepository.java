//package com.cs102.attendance.repository;

//import org.springframework.stereotype.Repository;

//@Repository
//public interface AttendanceRepository /*extends JpaRepository<AttendanceRecord, UUID>*/ {
    //Optional<AttendanceRecord> findBySessionAndStudent(Session session, Student student);
    //long countBySessionAndStatus(Session session, Status status);
//} 

package com.cs102.attendance.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.entity.AttendanceRecord;
import com.cs102.attendance.service.SupabaseRestService;

@Repository
public class AttendanceRepository {
    private static final String TABLE = "attendance_records";
    private final SupabaseRestService supabaseService;

    @Autowired
    public AttendanceRepository(SupabaseRestService supabaseService) {
        this.supabaseService = supabaseService;
    }

    // ✅ Create new attendance record (POST)
    public AttendanceRecord create(AttendanceRecord record) {
        return supabaseService.create(TABLE, record, AttendanceRecord.class);
    }

    // ✅ Get all attendance records (GET)
    public List<AttendanceRecord> findAll() {
        return supabaseService.read(TABLE, null, AttendanceRecord[].class);
    }

    // ✅ Find by session ID
    public List<AttendanceRecord> findBySessionId(Long sessionId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("session_id", "eq." + sessionId);
        return supabaseService.read(TABLE, queryParams, AttendanceRecord[].class);
    }

    // ✅ Find by student ID
    public List<AttendanceRecord> findByStudentId(Long studentId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("student_id", "eq." + studentId);
        return supabaseService.read(TABLE, queryParams, AttendanceRecord[].class);
    }

    // ✅ Find by student ID and session ID (one specific record)
    public Optional<AttendanceRecord> findByStudentIdAndSessionId(Long studentId, Long sessionId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("student_id", "eq." + studentId);
        queryParams.put("session_id", "eq." + sessionId);

        List<AttendanceRecord> result = supabaseService.read(TABLE, queryParams, AttendanceRecord[].class);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    // ✅ Update an existing attendance record
    // FIX: Change ID type to String to match UUIDs being sent to Supabase
    public AttendanceRecord update(String id, AttendanceRecord record) {
        // The id is now passed directly as a String (the UUID's string representation)
        return supabaseService.update(TABLE, id, record, AttendanceRecord.class);
    }

    // ✅ Delete an attendance record by ID
    // FIX: Change ID type to String
    public void delete(String id) {
        supabaseService.delete(TABLE, id);
    }

    // ✅ Find a single attendance record by ID
    // FIX: Change ID type to String
    public Optional<AttendanceRecord> findById(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id);

        List<AttendanceRecord> result = supabaseService.read(TABLE, queryParams, AttendanceRecord[].class);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}

