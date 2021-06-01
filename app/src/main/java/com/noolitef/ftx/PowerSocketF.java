package com.noolitef.ftx;

public class PowerSocketF {
    public static final int UPDATING = -2;
    public static final int NOT_CONNECTED = -1;
    public static final int OFF = 0;
    public static final int ON = 2;
    public static final int TEMPORARY_ON = 25;

    private String id;
    private int index;
    private int presetState;
    private int state;
    private int roomID;
    private String room;
    private String name;
    private byte[] command = {2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean preset;
    private int adapterPosition;

    public PowerSocketF(String id, int index, int presetState, int state, int roomID, String room, String name, boolean preset) {
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
        if (presetState == TEMPORARY_ON) {
            this.command[5] = 5;
        } else {
            this.command[5] = 0;
            this.command[6] = 0;
        }
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

    public void setAdapterPosition(int homeAdapterPosition) {
        this.adapterPosition = homeAdapterPosition;
    }


    private void flushCommand() {
        for (int b = 4; b < 10; b++) {
            command[b] = 0;
        }
    }
}
