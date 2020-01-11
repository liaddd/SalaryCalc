package com.liad.salarycalc.entities;

import com.google.firebase.database.Exclude;

import java.text.DecimalFormat;

public class ShiftItem {

    @Exclude
    private boolean isVisible;
    private String id;
    private long startTime, endtime;
    private double hourSalary;
    private int month, year;


    public ShiftItem() {

    }

    public ShiftItem(long startTime, long endtime, double hourSalary, int month, int year) {
        this.endtime = endtime;
        this.hourSalary = hourSalary;
        this.month = month;
        this.startTime = startTime;
        this.year = year;
    }

    public long getStartTime() {
        return startTime;
    }

    /* created due to wrong server side calculation */

    @Exclude
    public long getStartTimeInMillis() {
        return startTime * 1000;
    }

    public long getEndtime() {
        return endtime;
    }

    /* created due to wrong server side calculation */

    @Exclude
    public long getEndtimeInMillis() {
        return endtime * 1000;
    }

    public double getHourSalary() {
        return hourSalary;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    private long getTotalShiftTime() {
        return endtime - startTime;
    }

    @Exclude
    public String getTotalGross() {
        double salaryPerSecond = (hourSalary / 60) / 60;
        salaryPerSecond = (getTotalShiftTime() * salaryPerSecond);
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(salaryPerSecond);
    }

    @Exclude
    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
