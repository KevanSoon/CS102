package com.cs102.attendance.dto;

import java.util.List;

public class MainViewDto {
    private List<SessionDto> todaySessions;
    private List<StudentRosterDto> studentRoster;
    private int totalStudents;
    private int totalSessions;
    
    // Constructors
    public MainViewDto() {}
    
    public MainViewDto(List<SessionDto> todaySessions, List<StudentRosterDto> studentRoster) {
        this.todaySessions = todaySessions;
        this.studentRoster = studentRoster;
        this.totalStudents = studentRoster != null ? studentRoster.size() : 0;
        this.totalSessions = todaySessions != null ? todaySessions.size() : 0;
    }
    
    // Getters and Setters
    public List<SessionDto> getTodaySessions() {
        return todaySessions;
    }
    
    public void setTodaySessions(List<SessionDto> todaySessions) {
        this.todaySessions = todaySessions;
        this.totalSessions = todaySessions != null ? todaySessions.size() : 0;
    }
    
    public List<StudentRosterDto> getStudentRoster() {
        return studentRoster;
    }
    
    public void setStudentRoster(List<StudentRosterDto> studentRoster) {
        this.studentRoster = studentRoster;
        this.totalStudents = studentRoster != null ? studentRoster.size() : 0;
    }
    
    public int getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    public int getTotalSessions() {
        return totalSessions;
    }
    
    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }
} 