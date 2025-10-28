package com.cs102.attendance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Groups {

    @JsonProperty("group_number")
    private String groupNumber;

    @JsonProperty("class_code")
    private String classCode;

    // This assumes student_list is a comma-separated string or a JSON array, you can change type to List<String> if it is an array in JSON.
    @JsonProperty("student_list")
    private String[] studentList;

    @JsonProperty("professor_id")
    private String professorId;

    public Groups() {
        // No-args constructor
    }

    public Groups(String groupNumber, String classCode, String[] studentList, String professorId) {
        this.groupNumber = groupNumber;
        this.classCode = classCode;
        this.studentList = studentList;
        this.professorId = professorId;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String[] getStudentList() {
        return studentList;
    }

    public void setStudentList(String[] studentList) {
        this.studentList = studentList;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }
}
