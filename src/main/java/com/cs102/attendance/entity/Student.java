package com.cs102.attendance.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@jakarta.persistence.Entity
@Table(name = "students")
public class Student extends Entity {
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "class_name")
    private String className;
    
    @Column(name = "student_group")
    private String studentGroup;
    
    private String email;
    
    private String phone;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FaceData> faceData = new ArrayList<>();
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    
    // Constructors
    public Student() {}
    
    public Student(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getStudentGroup() {
        return studentGroup;
    }
    
    public void setStudentGroup(String studentGroup) {
        this.studentGroup = studentGroup;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public List<FaceData> getFaceData() {
        return faceData;
    }
    
    public void setFaceData(List<FaceData> faceData) {
        this.faceData = faceData;
    }
    
    public List<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }
    
    public void setAttendanceRecords(List<AttendanceRecord> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }
} 