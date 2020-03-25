package com.noolitef.rx;

import java.util.ArrayList;

public class DayTemperatureLog {
    private int year;
    private int month;
    private int day;
    private ArrayList<TemperatureUnit> temperatureUnits;

    public DayTemperatureLog(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.temperatureUnits = new ArrayList<>();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public ArrayList<TemperatureUnit> getTemperatureUnits() {
        return temperatureUnits;
    }

    public void setTemperatureUnits(ArrayList<TemperatureUnit> temperatureUnits) {
        this.temperatureUnits = temperatureUnits;
    }

    public void add(TemperatureUnit temperatureUnit) {
        temperatureUnits.add(temperatureUnit);
    }
}
