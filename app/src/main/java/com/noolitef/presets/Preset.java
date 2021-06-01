package com.noolitef.presets;

import android.util.StringBuilderPrinter;

import java.util.Locale;

public class Preset {
    public static final int OFF = 0;
    public static final int RUNNING = 1;
    public static final int ON = 2;
    public static final int SET_BRIGHTNESS = 6;
    public static final int TEMPORARY_ON = 25;

    private int index;
    private int state;
    private String name;
    private byte[][] commands;

    public Preset(int index, int state, String name) {
        this.index = index;
        this.state = state;
        this.name = name;
        commands = new byte[73][14];
    }

    public Preset(int index, int state, String name, byte[][] commands) {
        this.index = index;
        this.state = state;
        this.name = name;
        this.commands = commands;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getCommand(int index) {
        return commands[index];
    }

    public void setCommand(int index, byte[] commmand) {
        this.commands[index] = commmand;
    }

    @Override
    public String toString() {
        StringBuffer preset = new StringBuffer();
        preset.append("<Preset");
        preset.append("\nindex: " + index);
        preset.append("\nstate: " + state);
        preset.append("\nname:  " + name);
        preset.append("\ncommands:");
        for (int c = 0; c < 73; c++) {
            preset.append(String.format(Locale.ROOT, "\n%02d: ", c));
            for (int b = 0; b < 14; b++) {
                preset.append(String.format(Locale.ROOT, "%03d ", commands[c][b] & 0xFF));
            }
        }
        preset.append("/>\n");
        return preset.toString();
    }
}
