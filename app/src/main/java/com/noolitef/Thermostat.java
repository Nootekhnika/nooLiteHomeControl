package com.noolitef;

public class Thermostat {
    public static final int UPDATING = -2;
    public static final int NOT_CONNECTED = -1;
    public static final int OFF = 0;
    public static final int ON = 2;
    public static final int OUTPUT_OFF = 0;
    public static final int OUTPUT_ON = 2;
    public static final int SET_TEMPERATURE = 6;

    private String id;
    private int index;
    private int presetState;
    private int presetTemperature;
    private int state;
    private int currentTemperature;
    private int targetTemperature;
    private int outputState;
    private int roomID;
    private String room;
    private String name;
    private byte[] presetCommand = {2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean preset;
    private int adapterPosition;

    public Thermostat(String id, int index, int state, int currentTemperature, int targetTemperature, int outputState, int roomID, String room, String name) {
        this.id = id;
        this.presetCommand[10] = (byte) Integer.parseInt(id.substring(0, 2), 16);
        this.presetCommand[11] = (byte) Integer.parseInt(id.substring(2, 4), 16);
        this.presetCommand[12] = (byte) Integer.parseInt(id.substring(4, 6), 16);
        this.presetCommand[13] = (byte) Integer.parseInt(id.substring(6, 8), 16);
        this.index = index;
        this.state = state;
        this.currentTemperature = currentTemperature;
        this.targetTemperature = targetTemperature;
        this.outputState = outputState;
        this.roomID = roomID;
        this.room = room;
        this.name = name;

        setPresetTemperature(20); // by default
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
        switch (presetState) {
            case OFF:
                presetCommand[5] = 0;
                presetCommand[6] = 0;
                presetCommand[7] = 0;
                break;
            case SET_TEMPERATURE:
                presetCommand[5] = 2;
                presetCommand[6] = 20;
                presetCommand[7] = 1;
                break;
        }
    }

    public int getPresetTemperature() {
        return presetTemperature;
    }

    public void setPresetTemperature(int presetTemperature) {
        if (4 < presetTemperature && presetTemperature < 51)
            this.presetTemperature = presetCommand[6] = (byte) presetTemperature;
        else
            this.presetTemperature = presetCommand[6] = 20;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(int currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public int getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(int targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public int getOutputState() {
        return outputState;
    }

    public void setOutputState(int outputState) {
        this.outputState = outputState;
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
}
