package com.noolitef.rx;

import java.util.ArrayList;

public class TemperatureTimeInterval {
    private ArrayList<TemperatureUnit> temperatureUnits;

    public void addTemperatureUnit(TemperatureUnit temperatureUnit) {
        temperatureUnits.add(temperatureUnit);
    }

    public ArrayList<TemperatureUnit> getTemperatureUnits() {
        return temperatureUnits;
    }
}
