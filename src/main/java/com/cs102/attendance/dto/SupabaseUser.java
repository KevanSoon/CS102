package com.cs102.attendance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

public class SupabaseUser {
    private String id;
    private String email;
    
    @JsonProperty("email_confirmed_at")
    private LocalDateTime emailConfirmedAt;
    
    @JsonProperty("phone_confirmed_at")
    private LocalDateTime phoneConfirmedAt;
    
    @JsonProperty("last_sign_in_at")
    private LocalDateTime lastSignInAt;
    
    @JsonProperty("app_metadata")
    private Map<String, Object> appMetadata;
    
    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;
    
    private String role;
    private String aud;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public SupabaseUser() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getEmailConfirmedAt() {
        return emailConfirmedAt;
    }
    
    public void setEmailConfirmedAt(LocalDateTime emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
    }
    
    public LocalDateTime getPhoneConfirmedAt() {
        return phoneConfirmedAt;
    }
    
    public void setPhoneConfirmedAt(LocalDateTime phoneConfirmedAt) {
        this.phoneConfirmedAt = phoneConfirmedAt;
    }
    
    public LocalDateTime getLastSignInAt() {
        return lastSignInAt;
    }
    
    public void setLastSignInAt(LocalDateTime lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }
    
    public Map<String, Object> getAppMetadata() {
        return appMetadata;
    }
    
    public void setAppMetadata(Map<String, Object> appMetadata) {
        this.appMetadata = appMetadata;
    }
    
    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }
    
    public void setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getAud() {
        return aud;
    }
    
    public void setAud(String aud) {
        this.aud = aud;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
