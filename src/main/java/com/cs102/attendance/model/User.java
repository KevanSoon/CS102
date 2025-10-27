package com.cs102.attendance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String id;
    private String email;
    private String role;

    @JsonProperty("user_metadata")
    private UserMetadata userMetadata;

    @JsonProperty("email_confirmed_at")
    private String emailConfirmedAt;

    public User() {
        // No-args constructor
    }

    public User(String id, String email, String role, UserMetadata userMetadata) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.userMetadata = userMetadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UserMetadata getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(UserMetadata userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getEmailConfirmedAt() {
        return emailConfirmedAt;
    }

    public void setEmailConfirmedAt(String emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
    }

    public boolean isEmailVerified() {
        return emailConfirmedAt != null && !emailConfirmedAt.isEmpty();
    }

    public static class UserMetadata {
        private String role;
        private String name;
        private String code;
        private String phone;

        @JsonProperty("class_name")
        private String className;

        @JsonProperty("student_group")
        private String studentGroup;

        public UserMetadata() {
        }

        public UserMetadata(String role, String name) {
            this.role = role;
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
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
    }
}
