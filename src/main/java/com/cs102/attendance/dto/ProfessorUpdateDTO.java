package com.cs102.attendance.dto;

public class ProfessorUpdateDTO {
    private String name;

    public ProfessorUpdateDTO() {
    }

    public ProfessorUpdateDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
