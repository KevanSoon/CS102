package com.cs102.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class SessionDto {
    private UUID id;
    private String name;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long totalStudents;
    
    // Constructors
    public SessionDto() {}
    
    public SessionDto(UUID id, String name, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
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
    
    public long getPresentCount() {
        return presentCount;
    }
    
    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }
    
    public long getAbsentCount() {
        return absentCount;
    }
    
    public void setAbsentCount(long absentCount) {
        this.absentCount = absentCount;
    }
    
    public long getLateCount() {
        return lateCount;
    }
    
    public void setLateCount(long lateCount) {
        this.lateCount = lateCount;
    }
    
    public long getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }
} 