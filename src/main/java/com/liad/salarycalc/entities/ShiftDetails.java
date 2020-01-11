package com.liad.salarycalc.entities;

public class ShiftDetails {

    private String id, hourlyAmount, rideSalary;
    private int rideType;


    public ShiftDetails(String hourlyAmount, String rideSalary, int rideType) {
        this.hourlyAmount = hourlyAmount;
        this.rideSalary = rideSalary;
        this.rideType = rideType;
    }

    public int getRideType() {
        return rideType;
    }

    public String getHourlyAmount() {
        return hourlyAmount;
    }

    public String getRideSalary() {
        return rideSalary;
    }
}
