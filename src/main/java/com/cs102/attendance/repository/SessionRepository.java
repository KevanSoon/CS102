package com.cs102.attendance.repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.entity.Session;
import com.cs102.attendance.service.SupabaseRestService;

@Repository
public class SessionRepository {
    // Matches your Supabase table name
    private static final String TABLE = "sessions";
    
    // Service to handle API calls to Supabase
    private final SupabaseRestService supabaseService;

    // Constructor injection - Spring Boot provides SupabaseRestService automatically
    @Autowired 
    public SessionRepository(SupabaseRestService supabaseService) {
        this.supabaseService = supabaseService;
    }

    // ===== CREATE Operations =====
    
    // Insert a new session record (POST Method)
    public Session create(Session session) {
        return supabaseService.create(TABLE, session, Session.class);
    }

    // ===== READ Operations =====
    
    // Retrieve all sessions (GET method)
    public List<Session> findAll() {
        return supabaseService.read(TABLE, null, Session[].class);
    }

    // Retrieve a single session by ID (GET Method)
    public Session findById(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id); // Supabase expects ?id=eq.<id>

        List<Session> sessions = supabaseService.read(TABLE, queryParams, Session[].class);
        return sessions.isEmpty() ? null : sessions.get(0);
    }

    // Search sessions by name (partial match, case-insensitive)
    public List<Session> findByName(String name) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("name", "ilike.*" + name + "*"); // Case-insensitive partial match
        return supabaseService.read(TABLE, queryParams, Session[].class);
    }

    // Find sessions by specific date
    public List<Session> findByDate(String date) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("date", "eq." + date); // Exact date match (format: YYYY-MM-DD)
        return supabaseService.read(TABLE, queryParams, Session[].class);
    }

    // Find sessions within a date range
    public List<Session> findByDateRange(String startDate, String endDate) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("date", "gte." + startDate); // Greater than or equal
        queryParams.put("date", "lte." + endDate);   // Less than or equal
        return supabaseService.read(TABLE, queryParams, Session[].class);
    }
    
    // Find sessions that start at or after a specific time on a given date
    public List<Session> findByDateAndStartTime(String date, String startTime) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("date", "eq." + date);
        queryParams.put("start_time", "gte." + startTime);
        return supabaseService.read(TABLE, queryParams, Session[].class);
    }

    // Find today's sessions (you can pass today's date from the service/controller)
    public List<Session> findTodaySessions(String todayDate) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("date", "eq." + todayDate);
        return supabaseService.read(TABLE, queryParams, Session[].class);
    }
    
    // ===== UPDATE Operations =====
    
    // Update an existing session (PUT Method)
    public Session update(Long id, Session session) {
        return supabaseService.update(TABLE, id.toString(), session, Session.class);
    }

    // ===== DELETE Operations =====
    
    // Delete a session (DELETE Method)
    public void delete(Long id) {
        supabaseService.delete(TABLE, id.toString());
    }
}
