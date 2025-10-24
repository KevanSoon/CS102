package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
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
    public AttendanceRecord createSession(@RequestBody AttendanceRecord attendanceRecord) {
        return attendanceRecordService.create(attendanceRecord);
    }

    @GetMapping
    public List<AttendanceRecord> getAllSessions() {
        return attendanceRecordService.getAll();
    }


  
    @PatchMapping("/{id}")
    public AttendanceRecord updateSession(@PathVariable String id, @RequestBody AttendanceRecordUpdateDTO updateDTO) {
        return attendanceRecordService.update(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable String id) {
        attendanceRecordService.delete(id);
    }


    @PatchMapping("/auto_manual_marker")
    public AttendanceRecord updateBySessionAndStudent(
            @RequestParam String sessionId,
            @RequestParam String studentId,
            @RequestBody AttendanceRecordUpdateDTO updateDTO) {
        return attendanceRecordService.updateBySessionAndStudent(sessionId, studentId, updateDTO);
    }

    
}