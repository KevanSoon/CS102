package com.cs102.attendance.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.entity.Session;
import com.cs102.attendance.service.SupabaseRestService;

@Repository
public class SessionRepository {
    private static final String TABLE = "sessions";
    private final SupabaseRestService supabaseService;

    @Autowired
    public SessionRepository(SupabaseRestService supabaseService) {
        this.supabaseService = supabaseService;
    }

    public Optional<Session> findById(Long id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id);

        List<Session> result = supabaseService.read(TABLE, queryParams, Session[].class);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}

