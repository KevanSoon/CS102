package com.cs102.attendance.service;

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

    
}
