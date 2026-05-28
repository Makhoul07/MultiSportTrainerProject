package com.example.multisporttrainer.models;

public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private String dateOfBirth;
    private String role;
    private String sportFocus;

    public RegisterRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = null;
        this.role = "Player";
        this.sportFocus = "Football & Agility";
    }
}