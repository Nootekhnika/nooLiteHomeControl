package com.noolitef.rx;

import java.util.ArrayList;

public class DayHumidityTemperatureLog {
    private int year;
    private int month;
    private int day;
    private ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits;

    public DayHumidityTemperatureLog(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.humidityTemperatureUnits = new ArrayList<>();
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

    public ArrayList<HumidityTemperatureUnit> getHumidityTemperatureUnits() {
        return humidityTemperatureUnits;
    }

    public void setHumidityTemperatureUnits(ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits) {
        this.humidityTemperatureUnits = humidityTemperatureUnits;
    }

    public void add(HumidityTemperatureUnit humidityTemperatureUnit) {
        humidityTemperatureUnits.add(humidityTemperatureUnit);
    }
}
