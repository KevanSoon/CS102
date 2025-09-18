package com.cs102.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_connections")
public class TestConnection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "test_message")
    private String testMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public TestConnection() {
        this.createdAt = LocalDateTime.now();
    }
    
    public TestConnection(String testMessage) {
        this.testMessage = testMessage;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTestMessage() {
        return testMessage;
    }
    
    public void setTestMessage(String testMessage) {
        this.testMessage = testMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 