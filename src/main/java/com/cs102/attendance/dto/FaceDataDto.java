package com.cs102.attendance.dto;

public class FaceDataDto {
    private String studentId;
    private String imageUrl;

    public FaceDataDto(String studentId, String imageUrl) {
        this.studentId = studentId;
        this.imageUrl = imageUrl;
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
}