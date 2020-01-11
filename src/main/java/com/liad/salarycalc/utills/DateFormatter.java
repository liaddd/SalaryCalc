package com.liad.salarycalc.utills;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatter {

    private static DateFormatter instance;

    public synchronized static DateFormatter getInstance() {
        if (instance == null) {
            instance = new DateFormatter();
        }
        return instance;
    }

    public long getTimeStampFromStr(String dateStr) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date != null ? date.getTime() : 0;
    }

    public String getStrFromTimeStamp(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
        Date d = new Date(time);
        return sdf.format(d);
    }

    public String getHourFromMillis(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public String getMonthAndDay(long dateStr) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Date date = new Date(dateStr);
        return sdf.format(date);
    }

    public int getMonth(long date) {
        Date currentDate = new Date(date);
        return currentDate.getMonth() + 1;
    }

    public int getMonth(String month) {
        switch (month) {
            case "ינואר":
                return 1;
            case "פברואר":
                return 2;
            case "מרץ":
                return 3;
            case "אפריל":
                return 4;
            case "מאי":
                return 5;
            case "יוני":
                return 6;
            case "יולי":
                return 7;
            case "אוגוסט":
                return 8;
            case "ספטמבר":
                return 9;
            case "אוקטובר":
                return 10;
            case "נובמבר":
                return 11;
            case "דצמבר":
                return 12;
        }
        return 0;
    }

    public int getYear(long date) {
        Date currentDate = new Date(date);
        return currentDate.getYear() + 1900;
    }

    public String getFormattedHour(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date d = new Date(date);
        return sdf.format(d);
    }

    public String getRangeBetweenHours(long startTime, long endTime) {
        double diff = endTime - startTime;
        if (diff <= 0) return "00:00";
        long diffMinutes = (long) ((diff / (60 * 1000)) % 60);
        long diffHours = (long) (diff / (60 * 60 * 1000));
        return (diffHours < 10 ? "0" + diffHours : diffHours) + ":" + (diffMinutes < 10 ? "0" + diffMinutes : diffMinutes);
    }

    public String getDay(int date) {
        switch (date) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return null;
    }

    public String getHebrewDay(int day) {
        switch (day) {
            case 1:
                return "ראשון";
            case 2:
                return "שני";
            case 3:
                return "שלישי";
            case 4:
                return "רביעי";
            case 5:
                return "חמישי";
            case 6:
                return "שישי";
            case 7:
                return "שישי";
        }
        return null;
    }

    private String getCharOfDay(int day) {
        switch (day) {
            case 1:
                return "א׳";
            case 2:
                return "ב׳";
            case 3:
                return "ג׳";
            case 4:
                return "ד׳";
            case 5:
                return "ה׳";
            case 6:
                return "ו׳";
            case 7:
                return "ש׳";
        }
        return null;
    }

    public String getMonth(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
        }
        return "";
    }


    public String getHebrewMonth(int month) {
        switch (month) {
            case 1:
                return "ינואר";
            case 2:
                return "פברואר";
            case 3:
                return "מרץ";
            case 4:
                return "אפריל";
            case 5:
                return "מאי";
            case 6:
                return "יוני";
            case 7:
                return "יולי";
            case 8:
                return "אוגוסט";
            case 9:
                return "ספטמבר";
            case 10:
                return "אוקטובר";
            case 11:
                return "נובמבר";
            case 12:
                return "דצמבר";
        }
        return null;
    }

    public String getFormattedDateInHebrew(long startTime) {
        Date d = new Date(startTime);
        String day = getCharOfDay(d.getDay() + 1);
        String date = getMonthAndDay(d.getTime());

        return "יום " + day + "," + date;
    }
}
