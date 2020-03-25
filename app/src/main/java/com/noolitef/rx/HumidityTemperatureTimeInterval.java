package com.noolitef.rx;

import java.util.ArrayList;

public class HumidityTemperatureTimeInterval {
    private ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits;

    public HumidityTemperatureTimeInterval() {
        this.humidityTemperatureUnits = new ArrayList<>();
    }

    public void addHumidityTemperatureUnit(HumidityTemperatureUnit humidityTemperatureUnit) {
        humidityTemperatureUnits.add(humidityTemperatureUnit);
    }

    public ArrayList<HumidityTemperatureUnit> getHumidityTemperatureUnits() {
        return humidityTemperatureUnits;
    }
}
