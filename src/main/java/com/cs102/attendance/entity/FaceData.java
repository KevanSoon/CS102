package com.cs102.attendance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FaceData extends Entity {

    @JsonProperty("id")
    private String id; // ← Optional, if your table has an ID column

    @JsonProperty("student_id")
    private String studentId; // ✅ Supabase returns UUID string, not Student object

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_data")
    private byte[] imageData;

    // Constructors
    public FaceData() {}

    public FaceData(String studentId, String imageUrl) {
        this.studentId = studentId;
        this.imageUrl = imageUrl;
    }

    public FaceData(String studentId, byte[] imageData) {
        this.studentId = studentId;
        this.imageData = imageData;
    }

    // Getters and Setters

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
