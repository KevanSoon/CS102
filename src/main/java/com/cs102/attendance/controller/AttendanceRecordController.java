package com.cs102.attendance.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.dto.AttendanceRecordUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.service.AttendanceRecordService;

@RestController
@RequestMapping("/api/attendance_records")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    public AttendanceRecordController(AttendanceRecordService attendanceRecordService) {
        this.attendanceRecordService = attendanceRecordService;
    }

    @PostMapping
    public AttendanceRecord createRecord(@RequestBody AttendanceRecord attendanceRecord) {
        return attendanceRecordService.create(attendanceRecord);
    }

    @GetMapping
    public List<AttendanceRecord> getAttendanceRecords(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String student_id) {
        
        List<AttendanceRecord> allRecords = attendanceRecordService.getAll();
        
        // Filter by session_id if provided
        if (session_id != null) {
            allRecords = allRecords.stream()
                    .filter(r -> session_id.equals(r.getSession_id()))
                    .collect(Collectors.toList());
        }
        
        // Filter by student_id if provided
        if (student_id != null) {
            allRecords = allRecords.stream()
                    .filter(r -> student_id.equals(r.getStudent_id()))
                    .collect(Collectors.toList());
        }
        
        return allRecords;
    }


  
    @PatchMapping("/{id}")
    public AttendanceRecord updateRecord(@PathVariable String id, @RequestBody AttendanceRecordUpdateDTO updateDTO) {
        return attendanceRecordService.update(id, updateDTO);
    }

    
}