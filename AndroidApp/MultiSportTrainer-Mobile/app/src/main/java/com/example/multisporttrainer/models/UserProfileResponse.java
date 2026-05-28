package com.example.multisporttrainer.models;

public class UserProfileResponse {

    private int userId;
    private String fullName;
    private String email;
    private String dateOfBirth;
    private String role;
    private String sportFocus;
    private int totalTrainings;
    private int bestScore;
    private double avgAccuracy;
    private double avgReaction;

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getRole() {
        return role;
    }

    public String getSportFocus() {
        return sportFocus;
    }

    public int getTotalTrainings() {
        return totalTrainings;
    }

    public int getBestScore() {
        return bestScore;
    }

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public double getAvgReaction() {
        return avgReaction;
    }
}