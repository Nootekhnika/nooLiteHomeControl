package com.noolitef.timers;

public class Timer {
    private int index;
    private String name;
    private int type;
    private boolean working;
    private int onHour;
    private int onMinute;
    private int offHour;
    private int offMinute;
    private int workDays;
    private byte[][] commands;

    public Timer(int index, String name, int type, boolean working, int onHour, int onMinute, int offHour, int offMinute, int workDays) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.working = working;
        this.onHour = onHour;
        this.onMinute = onMinute;
        this.offHour = offHour;
        this.offMinute = offMinute;
        this.workDays = workDays;
        this.commands = new byte[8][14];
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public int getOnHour() {
        return onHour;
    }

    public void setOnHour(int onHour) {
        this.onHour = onHour;
    }

    public int getOnMinute() {
        return onMinute;
    }

    public void setOnMinute(int onMinute) {
        this.onMinute = onMinute;
    }

    public int getOffHour() {
        return offHour;
    }

    public void setOffHour(int offHour) {
        this.offHour = offHour;
    }

    public int getOffMinute() {
        return offMinute;
    }

    public void setOffMinute(int offMinute) {
        this.offMinute = offMinute;
    }

    public int getWorkDays() {
        return workDays;
    }

    public void setWorkDays(int workDays) {
        this.workDays = workDays;
    }

    public byte[] getCommand(int index) {
        return commands[index];
    }

    public void setCommand(int index, byte[] command) {
        this.commands[index] = command;
    }
}
