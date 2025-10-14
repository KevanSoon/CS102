package com.cs102.attendance.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

//@jakarta.persistence.Entity
//@Table(name = "sessions")
public class Session extends Entity {
    
    //@Column(nullable = false)
    private String name;
    
    //@Column(nullable = false)
    private LocalDate date;
    
    //@Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    //@Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    //@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    
    // Constructors
    public Session() {}
    
    public Session(String name, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public List<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }
    
    public void setAttendanceRecords(List<AttendanceRecord> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }
} 