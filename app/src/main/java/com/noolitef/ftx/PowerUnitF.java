package com.noolitef.ftx;

public class PowerUnitF {
    public static final int UPDATING = -2;
    public static final int NOT_CONNECTED = -1;
    public static final int OFF = 0;
    public static final int ON = 2;

    private String id;
    private int index;
    private int presetState;
    private int presetBrightness;
    private int state;
    private int roomID;
    private String room;
    private String name;
    private boolean dimmer;
    private int brightness;
    private byte[] command = {2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean preset;
    private int adapterPosition;

    public PowerUnitF(String id, int index, int presetState, int state, int roomID, String room, String name, int brightness, boolean preset) {
        this.id = id;
        this.command[10] = (byte) Integer.parseInt(id.substring(0, 2), 16);
        this.command[11] = (byte) Integer.parseInt(id.substring(2, 4), 16);
        this.command[12] = (byte) Integer.parseInt(id.substring(4, 6), 16);
        this.command[13] = (byte) Integer.parseInt(id.substring(6, 8), 16);
        this.index = index;
        this.presetState = command[4] = (byte) presetState;
        this.state = state;
        this.roomID = roomID;
        this.room = room;
        this.name = name;
        this.dimmer = false;
        this.brightness = brightness;
        this.preset = preset;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.command[10] = (byte) Integer.parseInt(id.substring(0, 2), 16);
        this.command[11] = (byte) Integer.parseInt(id.substring(2, 4), 16);
        this.command[12] = (byte) Integer.parseInt(id.substring(4, 6), 16);
        this.command[13] = (byte) Integer.parseInt(id.substring(6, 8), 16);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPresetState() {
        return presetState;
    }

    public void setPresetState(int presetState) {
        this.presetState = command[4] = (byte) presetState;
    }

    public int getPresetBrightness() {
        return presetBrightness;
    }

    public void setPresetBrightness(int percent) {
        this.presetBrightness = percent;
        this.command[6] = (byte) (percent * 2.55 + .5);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public void setDimming(boolean dimmer) {
        this.dimmer = dimmer;
    }

    public boolean isDimmer() {
        return dimmer;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int percent) {
        this.brightness = percent;
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

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int position) {
        this.adapterPosition = position;
    }


    private void flushCommand() {
        for (int b = 4; b < 10; b++) {
            command[b] = 0;
        }
    }
}
