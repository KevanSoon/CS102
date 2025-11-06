package com.cs102.attendance.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceRecordUpdateDTO {
    private String status;          // e.g., "Present", "Absent"
    private Double confidence;      // e.g., 0.95
    private String method;          // e.g., "Facial Recognition"
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime marked_at;

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

    public LocalDateTime getMarked_at() {
        return marked_at;
    }
    public void setMarked_at(LocalDateTime marked_at) {
        this.marked_at = marked_at;
    }
}
