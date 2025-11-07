package com.cs102.attendance.model;

public class Student {
    private String id;
    private String name;
    private String email;
    private String code;
    private String phone;


    public Student() {
        // No-args constructor
    }

   public Student(String name, String email, String code, String phone) {
    this.name = name;
    this.email = email;
    //code must be UNIQUE
    this.code = code;
    this.phone = phone;
}


    // Getters and setters for every field

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }
    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) {
        this.email = email; 
    }
    public String getCode() { 
        return code; 
    }
    public void setCode(String code) { 
        this.code = code; 
    }
    public String getPhone() { 
        return phone; 
    }
    public void setPhone(String phone) {
         this.phone = phone; 
    }

    
}