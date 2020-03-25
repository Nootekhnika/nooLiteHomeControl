package com.noolitef.rx;

public class RemoteController {
    private int channel;
    private boolean batteryLow;
    private int roomID;
    private String room;
    private String name;

    public RemoteController(int channel, boolean batteryLow, int roomID, String room, String name) {
        this.channel = channel;
        this.batteryLow = batteryLow;
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

    public boolean isBatteryLow() {
        return batteryLow;
    }

    public void setBatteryLow(boolean batteryLow) {
        this.batteryLow = batteryLow;
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
