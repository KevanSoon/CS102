package com.cs102.attendance.entity;

// import jakarta.persistence.Column;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.Lob;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;

// @jakarta.persistence.Entity
// @Table(name = "face_data")
public class FaceData extends Entity {
    
    // @ManyToOne(fetch = FetchType.EAGER)
    // @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    // @Column(name = "image_url")
    private String imageUrl;
    
    // @Lob
    // @Column(name = "image_data")
    private byte[] imageData;
    
    // Constructors
    public FaceData() {}
    
    public FaceData(Student student, String imageUrl) {
        this.student = student;
        this.imageUrl = imageUrl;
    }
    
    public FaceData(Student student, byte[] imageData) {
        this.student = student;
        this.imageData = imageData;
    }
    
    // Getters and Setters
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
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