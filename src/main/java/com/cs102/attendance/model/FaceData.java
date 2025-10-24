package com.cs102.attendance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FaceData {

    private String id;
    @JsonProperty("student_id")
    private String studentId;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("image_data")
    private byte[] imageData;

    public FaceData() {
        // No-args constructor
    }

    public FaceData(String studentId, String imageUrl) {
        this.studentId = studentId;
        this.imageUrl = imageUrl;
    }

    public FaceData(String studentId, byte[] imageData) {
        this.studentId = studentId;
        this.imageData = imageData;
    }

    public String getId() {
        return id;
    }

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
