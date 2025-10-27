package com.cs102.attendance.dto;

import java.util.Map;

public class SupabaseSignUpRequest {
    private String email;
    private String password;
    private Map<String, String> data;

    public SupabaseSignUpRequest() {
    }

    public SupabaseSignUpRequest(String email, String password, Map<String, String> data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
