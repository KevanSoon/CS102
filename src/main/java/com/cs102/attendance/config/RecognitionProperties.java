package com.cs102.attendance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "recognition")
public class RecognitionProperties {
    
    private double confidence = 0.70;
    private long cooldownMs = 10000;
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public long getCooldownMs() {
        return cooldownMs;
    }
    
    public void setCooldownMs(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }
} 