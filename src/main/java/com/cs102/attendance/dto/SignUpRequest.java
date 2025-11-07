package com.cs102.attendance.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignUpRequest {
    private String email;
    private String password;

    @JsonProperty("userMetadata")
    private Map<String, String> userMetadata;

    private String name;
    private String code;
    private String phone;


    public SignUpRequest() {
    }

    public SignUpRequest(String email, String password, Map<String, String> userMetadata) {
        this.email = email;
        this.password = password;
        this.userMetadata = userMetadata;
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

    public Map<String, String> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, String> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
