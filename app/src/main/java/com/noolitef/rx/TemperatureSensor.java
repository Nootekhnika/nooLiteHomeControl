package com.noolitef.rx;

public class TemperatureSensor {
    private int channel;
    private boolean batteryOK;
    private int roomID;
    private String room;
    private String name;
    private double currentTemperature;
    private int updateYear;
    private int updateMonth;
    private int updateDay;
    private int updateWeekDay;
    private int updateHour;
    private int updateMinute;
    private int updateSecond;
    private int adapterPosition;

    public TemperatureSensor(int channel, boolean batteryOK, int roomID, String room, String name, double currentTemperature, int updateYear, int updateMonth, int updateDay, int updateWeekDay, int updateHour, int updateMinute, int updateSecond) {
        this.channel = channel;
        this.batteryOK = batteryOK;
        this.roomID = roomID;
        this.room = room;
        this.name = name;
        this.currentTemperature = currentTemperature;
        this.updateYear = updateYear;
        this.updateMonth = updateMonth;
        this.updateDay = updateDay;
        this.updateWeekDay = updateWeekDay;
        this.updateHour = updateHour;
        this.updateMinute = updateMinute;
        this.updateSecond = updateSecond;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isBatteryOK() {
        return batteryOK;
    }

    public void setBatteryOK(boolean batteryOK) {
        this.batteryOK = batteryOK;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public int getUpdateYear() {
        return updateYear;
    }

    public void setUpdateYear(int updateYear) {
        this.updateYear = updateYear;
    }

    public int getUpdateMonth() {
        return updateMonth;
    }

    public void setUpdateMonth(int updateMonth) {
        this.updateMonth = updateMonth;
    }

    public int getUpdateDay() {
        return updateDay;
    }

    public void setUpdateDay(int updateDay) {
        this.updateDay = updateDay;
    }

    public int getUpdateWeekDay() {
        return updateWeekDay;
    }

    public void setUpdateWeekDay(int updateWeekDay) {
        this.updateWeekDay = updateWeekDay;
    }

    public int getUpdateHour() {
        return updateHour;
    }

    public void setUpdateHour(int updateHour) {
        this.updateHour = updateHour;
    }

    public int getUpdateMinute() {
        return updateMinute;
    }

    public void setUpdateMinute(int updateMinute) {
        this.updateMinute = updateMinute;
    }

    public int getUpdateSecond() {
        return updateSecond;
    }

    public void setUpdateSecond(int updateSecond) {
        this.updateSecond = updateSecond;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }
}
