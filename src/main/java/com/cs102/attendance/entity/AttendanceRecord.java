package com.cs102.attendance.entity;

import java.time.LocalDateTime;

import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;

// @jakarta.persistence.Entity
// @Table(name = "attendance_records")
public class AttendanceRecord extends Entity {
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "session_id", nullable = false)
    private Session session;
    
    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false)
    private Status status;
    
    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false)
    private Method method;
    
    // @Column
    private Double confidence;
    
    // @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;
    
    // @Column(name = "last_seen") // ADDED for Cooldown Timer
    // Making sure that the attendance of an individual is taken only once 
    private LocalDateTime lastSeen;
    
    
    // Constructors
    public AttendanceRecord() {}
    
    // Original constructor (simplified)
    public AttendanceRecord(Student student, Session session, Status status, Method method) {
        this.student = student;
        this.session = session;
        this.status = status;
        this.method = method;
        this.markedAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now(); // Initialize lastSeen
    }

    // Full constructor (for convenience in Marker services)
    public AttendanceRecord(Student student, Session session, Status status, Method method, Double confidence) {
        this.student = student;
        this.session = session;
        this.status = status;
        this.method = method;
        this.confidence = confidence;
        this.markedAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Session getSession() {
        return session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public void setMethod(Method method) {
        this.method = method;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public LocalDateTime getMarkedAt() {
        return markedAt;
    }
    
    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}