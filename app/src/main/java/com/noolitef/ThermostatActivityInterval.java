package com.noolitef;

public class ThermostatActivityInterval {
    private int temperature;
    private int beginHour;
    private int beginMinute;
    private int endHour;
    private int endMinute;

    public ThermostatActivityInterval(int beginHour, int beginMinute, int endHour, int endMinute, int temperature) {
        this.beginHour = beginHour;
        this.beginMinute = beginMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.temperature = temperature;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    public int getBeginMinute() {
        return beginMinute;
    }

    public void setBeginMinute(int beginMinute) {
        this.beginMinute = beginMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}