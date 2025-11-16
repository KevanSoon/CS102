package com.cs102.attendance.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.AttendanceRecordUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;


@Service
public class AttendanceRecordService extends SupabaseService<AttendanceRecord> {

    public AttendanceRecordService(WebClient webClient) {
        super(webClient, "attendance_records", AttendanceRecord[].class, AttendanceRecord.class);
    }

    public AttendanceRecord update(String id, AttendanceRecordUpdateDTO updatedDto) {
        // Calls the generic update method but with DTO object for patch
        return super.update(id, updatedDto);
    }

    public List<AttendanceRecord> getBySession(String sessionId) {
        List<AttendanceRecord> allRecords = getAll();
        return allRecords.stream()
                .filter(r -> sessionId.equals(r.getSession_id()))
                .collect(Collectors.toList());
    }
    
}
