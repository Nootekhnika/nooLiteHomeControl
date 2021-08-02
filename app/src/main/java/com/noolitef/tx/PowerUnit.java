package com.noolitef.tx;

import java.io.Serializable;

public class PowerUnit implements Serializable {
    public static final int DIMMER = 0;
    public static final int RGB_CONTROLLER = 2;
    public static final int RELAY = 3;
    public static final int PULSE_RELAY = 10;
    public static final int ROLLET = 11;

    public static final int RESPONSE_OK = 200;
    public static final int RESPONSE_FAIL = 400;

    public static final int OFF = 0;
    public static final int ON = 2;
    public static final int SET_BRIGHTNESS = 6;
    public static final int TEMPORARY_ON = 25;

    private int type;
    private int channel;
    private int presetState;
    private int brightness;
    private int roomID;
    private String room;
    private String name;
    private byte[] command = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean preset;

    public PowerUnit(int type, int channel, int presetState, int roomID, String room, String name, boolean preset) {
        this.type = type;
        this.channel = command[3] = (byte) channel;
        this.presetState = command[4] = (byte) presetState;
        this.roomID = roomID;
        this.room = room;
        this.name = name;
        this.preset = preset;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = command[3] = (byte) channel;
    }

    public int getPresetState() {
        return presetState;
    }

    public void setPresetState(int presetState) {
        this.presetState = command[4] = (byte) presetState;
        //setFMT
        switch (presetState) {
            case SET_BRIGHTNESS:
                this.command[5] = 1;
                break;
            case TEMPORARY_ON:
                this.command[5] = 5;
                break;
            default:
                this.command[5] = 0;
                this.command[6] = 0;
        }
    }

    public int getBrightness() {
        switch (this.presetState) {
            case ON:
            case TEMPORARY_ON:
                return 100;
        }
        return brightness;
    }

    public void setBrightness(int percent) {
        this.brightness = percent;
        if (type == RGB_CONTROLLER)
            this.command[6] = (byte) (percent * 1.28 + 28.5);
        else
            this.command[6] = (byte) (percent * 1.09 + 43.5);
    }

    public int getTime() {
        if (this.presetState == TEMPORARY_ON) {
            return (int) ((this.command[6] & 0xFF) * 5 / 60);
        } else {
            return 0;
        }
    }

    public void setTime(int minutes) {
        if (minutes > 0) {
            this.command[6] = (byte) (minutes * 60 / 5);
        }
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

    public byte[] getCommand() {
        return command;
    }

    public boolean isPreset() {
        return preset;
    }

    public void setPreset(boolean preset) {
        this.preset = preset;
        if (!preset) flushCommand();
    }


    private void flushCommand() {
        for (int b = 4; b < 10; b++) {
            command[b] = 0;
        }
    }

    public void setCommandSunrise() {
        flushCommand();
        command[4] = 13;
        command[5] = 1;
        command[6] = 1;
    }

    public void setCommandSunset() {
        flushCommand();
        command[4] = 13;
        command[5] = 1;
        command[6] = (byte) 255;
    }
}
