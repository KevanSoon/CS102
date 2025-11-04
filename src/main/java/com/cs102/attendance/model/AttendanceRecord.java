package com.cs102.attendance.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;


public class AttendanceRecord {
    private String student_id;
    private String session_id;
    private String status;          // e.g., "Present", "Absent"
    private Double confidence;      // e.g., 0.95
    private String method;          // e.g., "Facial Recognition"
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime marked_at;

    public AttendanceRecord() {
        // No-args constructor
    }

    public AttendanceRecord( String student_id, String session_id, String status, Double confidence, String method, LocalDateTime marked_at) {
        this.student_id = student_id;
        this.session_id = session_id;
        this.status = status;
        this.confidence = confidence;
        this.method = method;
        this.marked_at = marked_at;
    }


    public String getStudent_id() { 
        return student_id; 
    }
    public void setStudent_id(String student_id) { 
        this.student_id = student_id; 
    }
    public String getSession_id() { 
        return session_id; 
    }
    public void setSession_id(String session_id) { 
        this.session_id = session_id; 
    }
    public String getStatus() { 
        return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }
    public Double getConfidence() { 
        return confidence; 
    }
    public void setConfidence(Double confidence) {
         this.confidence = confidence;
         }
    public String getMethod() {
         return method; 
        }
    public void setMethod(String method) {
         this.method = method; 
        }
    public LocalDateTime getMarked_at() {
         return marked_at; 
        }
    public void setMarked_at(LocalDateTime marked_at) { 
        this.marked_at = marked_at; 
    }
}
