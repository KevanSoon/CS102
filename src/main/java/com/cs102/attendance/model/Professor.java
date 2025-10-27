package com.cs102.attendance.model;

public class Professor {
    private String id;
    private String name;

    public Professor() {
        // No-args constructor
    }

    public Professor(String name) {
        this.name = name;
    }

    public Professor(String id, String name) {
        this.id = id;
        this.name = name;
    }

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
}
