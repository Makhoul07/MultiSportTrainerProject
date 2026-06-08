package com.example.multisporttrainer.models;

public class AuthResponse {

    private int userId;
    private String fullName;
    private String email;
    private String role;
    private String sportFocus;
    private String token;
    private String message;

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getSportFocus() {
        return sportFocus;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }
}