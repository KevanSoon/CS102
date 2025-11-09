package com.cs102.attendance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupUpdateDTO {
    
    @JsonProperty("student_list")
    private String[] studentList;

    public GroupUpdateDTO() {
    }

    public GroupUpdateDTO(String[] studentList) {
        this.studentList = studentList;
    }

    public String[] getStudentList() {
        return studentList;
    }

    public void setStudentList(String[] studentList) {
        this.studentList = studentList;
    }
}
