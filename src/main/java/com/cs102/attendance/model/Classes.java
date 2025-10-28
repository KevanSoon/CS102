package com.cs102.attendance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Classes {

    @JsonProperty("class_code")
    private String classCode;

    @JsonProperty("class_name")
    private String className;

    // This assumes professor_list is a comma-separated string or a JSON array.
    @JsonProperty("professor_list")
    private String[] professorList;

    public Classes() {
        // No-args constructor
    }

    public Classes(String classCode, String className, String[] professorList) {
        this.classCode = classCode;
        this.className = className;
        this.professorList = professorList;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String[] getProfessorList() {
        return professorList;
    }

    public void setProfessorList(String[] professorList) {
        this.professorList = professorList;
    }
}
