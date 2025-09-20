package com.cs102.attendance.dto;

import com.cs102.attendance.enums.Status;
import java.util.UUID;

public class StudentRosterDto {
    private UUID id;
    private String code;
    private String name;
    private Status status;
    
    // Constructors
    public StudentRosterDto() {}
    
    public StudentRosterDto(UUID id, String code, String name, Status status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
} 