package com.noolitef.rx;

public class OpenCloseSensor {
    private int channel;
    private boolean batteryOK;
    private boolean closed;
    private int roomID;
    private String room;
    private String name;
    private int lastUpdateYear;
    private int lastUpdateMonth;
    private int lastUpdateDay;
    private int lastUpdateWeekDay;
    private int lastUpdateHour;
    private int lastUpdateMinute;
    private int lastUpdateSecond;
    private int adapterPosition;

    public OpenCloseSensor(int channel, boolean batteryOK, boolean closed, int roomID, String room, String name, int lastUpdateYear, int lastUpdateMonth, int lastUpdateDay, int lastUpdateWeekDay, int lastUpdateHour, int lastUpdateMinute, int lastUpdateSecond) {
        this.channel = channel;
        this.batteryOK = batteryOK;
        this.closed = closed;
        this.roomID = roomID;
        this.room = room;
        this.name = name;
        this.lastUpdateYear = lastUpdateYear;
        this.lastUpdateMonth = lastUpdateMonth;
        this.lastUpdateDay = lastUpdateDay;
        this.lastUpdateWeekDay = lastUpdateWeekDay;
        this.lastUpdateHour = lastUpdateHour;
        this.lastUpdateMinute = lastUpdateMinute;
        this.lastUpdateSecond = lastUpdateSecond;
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

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
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

    public int getLastUpdateYear() {
        return lastUpdateYear;
    }

    public void setLastUpdateYear(int lastUpdateYear) {
        this.lastUpdateYear = lastUpdateYear;
    }

    public int getLastUpdateMonth() {
        return lastUpdateMonth;
    }

    public void setLastUpdateMonth(int lastUpdateMonth) {
        this.lastUpdateMonth = lastUpdateMonth;
    }

    public int getLastUpdateDay() {
        return lastUpdateDay;
    }

    public void setLastUpdateDay(int lastUpdateDay) {
        this.lastUpdateDay = lastUpdateDay;
    }

    public int getLastUpdateWeekDay() {
        return lastUpdateWeekDay;
    }

    public void setLastUpdateWeekDay(int lastUpdateWeekDay) {
        this.lastUpdateWeekDay = lastUpdateWeekDay;
    }

    public int getLastUpdateHour() {
        return lastUpdateHour;
    }

    public void setLastUpdateHour(int lastUpdateHour) {
        this.lastUpdateHour = lastUpdateHour;
    }

    public int getLastUpdateMinute() {
        return lastUpdateMinute;
    }

    public void setLastUpdateMinute(int lastUpdateMinute) {
        this.lastUpdateMinute = lastUpdateMinute;
    }

    public int getLastUpdateSecond() {
        return lastUpdateSecond;
    }

    public void setLastUpdateSecond(int lastUpdateSecond) {
        this.lastUpdateSecond = lastUpdateSecond;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }
}
