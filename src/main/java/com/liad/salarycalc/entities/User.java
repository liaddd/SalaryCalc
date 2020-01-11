package com.liad.salarycalc.entities;

import androidx.annotation.NonNull;

public class User {

    private String userID, fullName, email, photoUrl;
    private ShiftDetails shiftDetails;
    private int hourSalary, rideType;
    private static User instance = null;


    @NonNull
    public static synchronized User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHourSalary(int hourSalary) {
        this.hourSalary = hourSalary;
    }

    public void setRideType(int rideType) {
        this.rideType = rideType;
    }

    public void setPhotoURL(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserID() {
        return userID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public int getHourSalary() {
        return hourSalary;
    }
}
