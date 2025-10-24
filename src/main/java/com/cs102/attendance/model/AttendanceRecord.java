package com.cs102.attendance.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AttendanceRecord {
    private UUID id;
    private UUID student_id;
    private UUID session_id;
    private String status;          // e.g., "Present", "Absent"
    private Double confidence;      // e.g., 0.95
    private String method;          // e.g., "Facial Recognition"
    private OffsetDateTime marked_at;

    public AttendanceRecord() {
        // No-args constructor
    }

    public AttendanceRecord(UUID id, UUID student_id, UUID session_id, String status, Double confidence, String method, OffsetDateTime marked_at) {
        this.id = id;
        this.student_id = student_id;
        this.session_id = session_id;
        this.status = status;
        this.confidence = confidence;
        this.method = method;
        this.marked_at = marked_at;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getStudent_id() { return student_id; }
    public void setStudent_id(UUID student_id) { this.student_id = student_id; }
    public UUID getSession_id() { return session_id; }
    public void setSession_id(UUID session_id) { this.session_id = session_id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public OffsetDateTime getMarked_at() { return marked_at; }
    public void setMarked_at(OffsetDateTime marked_at) { this.marked_at = marked_at; }
}
