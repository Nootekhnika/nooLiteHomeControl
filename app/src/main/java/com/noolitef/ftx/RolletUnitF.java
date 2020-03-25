package com.noolitef.ftx;

import java.io.Serializable;

public class RolletUnitF implements Serializable {
    public static final int UPDATING = -2;
    public static final int NOT_CONNECTED = -1;
    public static final int CLOSE = 0;
    public static final int OPEN = 2;

    private String id;
    private int index;
    private int presetState;
    private int state;
    private int roomID;
    private String room;
    private String name;
    private boolean inversion;
    private byte[] presetCommand = {2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean preset;
    private int adapterPosition;

    private boolean catFeeder;

    public RolletUnitF(String id, int index, int state, int roomID, String room, String name) {
        this.id = id;
        this.presetCommand[10] = (byte) Integer.parseInt(id.substring(0, 2), 16);
        this.presetCommand[11] = (byte) Integer.parseInt(id.substring(2, 4), 16);
        this.presetCommand[12] = (byte) Integer.parseInt(id.substring(4, 6), 16);
        this.presetCommand[13] = (byte) Integer.parseInt(id.substring(6, 8), 16);
        this.index = index;
        this.state = state;
        this.roomID = roomID;
        this.room = room;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.presetCommand[10] = (byte) Integer.parseInt(id.substring(0, 2), 16);
        this.presetCommand[11] = (byte) Integer.parseInt(id.substring(2, 4), 16);
        this.presetCommand[12] = (byte) Integer.parseInt(id.substring(4, 6), 16);
        this.presetCommand[13] = (byte) Integer.parseInt(id.substring(6, 8), 16);
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
        this.presetState = presetCommand[4] = (byte) presetState;
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

    public boolean isInversion() {
        return inversion;
    }

    public void setInversion(boolean inversion) {
        this.inversion = inversion;
    }

    public byte[] getPresetCommand() {
        return presetCommand;
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
            presetCommand[b] = 0;
        }
    }


    public boolean isCatFeeder() {
        return catFeeder;
    }

    public void setCatFeeder(boolean catFeeder) {
        this.catFeeder = catFeeder;
    }
}
