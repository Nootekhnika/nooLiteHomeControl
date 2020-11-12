package com.noolitef.ftx;

// SUF-1-300-A
public class PowerUnitFA extends PowerUnitF {

    public PowerUnitFA(String id, int index, int presetState, int state, int roomID, String room, String name, int brightness, boolean preset) {
        super(id, index, presetState, state, roomID, room, name, brightness, preset);
    }

    @Override
    public void setPresetBrightness(int percent) {
        this.presetBrightness = percent;
        this.command[5] = 1;
        this.command[6] = (byte) percent;
    }
}
