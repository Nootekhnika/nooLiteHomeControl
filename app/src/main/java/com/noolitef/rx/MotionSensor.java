package com.noolitef.rx;

public class MotionSensor {
    private int channel;
    private boolean batteryOK;
    private int roomID;
    private String room;
    private String name;

    public MotionSensor(int channel, boolean batteryOK, int roomID, String room, String name) {
        this.channel = channel;
        this.batteryOK = batteryOK;
        this.roomID = roomID;
        this.room = room;
        this.name = name;
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
}
