package com.example.recipe_android_project.features.plan.domain.model;

public class DayModel {
    private String dayName;
    private int dayNumber;
    private int month;
    private int year;
    private boolean isSelected;
    private boolean isToday;
    private boolean isPast;

    public DayModel(String dayName, int dayNumber, int month, int year,
                    boolean isSelected, boolean isToday, boolean isPast) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.month = month;
        this.year = year;
        this.isSelected = isSelected;
        this.isToday = isToday;
        this.isPast = isPast;
    }

    public DayModel(String dayName, int dayNumber, int month, int year,
                    boolean isSelected, boolean isToday) {
        this(dayName, dayNumber, month, year, isSelected, isToday, false);
    }

    public String getDayName() { return dayName; }
    public int getDayNumber() { return dayNumber; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public boolean isSelected() { return isSelected; }
    public boolean isToday() { return isToday; }
    public boolean isPast() { return isPast; }

    public void setDayName(String dayName) { this.dayName = dayName; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
    public void setMonth(int month) { this.month = month; }
    public void setYear(int year) { this.year = year; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public void setToday(boolean today) { isToday = today; }
    public void setPast(boolean past) { isPast = past; }
}
