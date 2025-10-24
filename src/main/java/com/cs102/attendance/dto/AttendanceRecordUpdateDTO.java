package com.cs102.attendance.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceRecordUpdateDTO {
    private String status;          // e.g., "Present", "Absent"
    private Double confidence;      // e.g., 0.95
    private String method;          // e.g., "Facial Recognition"
    private OffsetDateTime marked_at;

    public AttendanceRecordUpdateDTO() {}

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

    public OffsetDateTime getMarked_at() {
        return marked_at;
    }
    public void setMarked_at(OffsetDateTime marked_at) {
        this.marked_at = marked_at;
    }
}
