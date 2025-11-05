package com.cs102.attendance.dto;

/**
 * DTO for face verification result from DeepFace API
 */
public class FaceVerificationResult {
    private boolean verified;
    private double confidence;

    public FaceVerificationResult() {
    }

    public FaceVerificationResult(boolean verified, double confidence) {
        this.verified = verified;
        this.confidence = confidence;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "FaceVerificationResult{" +
                "verified=" + verified +
                ", confidence=" + confidence +
                '}';
    }
}
