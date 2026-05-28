package com.example.multisporttrainer.models;

public class UpdateUserRequest {

    private String fullName;
    private String email;
    private String dateOfBirth;
    private String role;
    private String sportFocus;

    public UpdateUserRequest(String fullName, String email, String dateOfBirth, String role, String sportFocus) {
        this.fullName = fullName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        this.sportFocus = sportFocus;
    }
}