package com.noolitef.rx;

import com.noolitef.rx.HumidityTemperatureUnit;
import com.noolitef.rx.TemperatureUnit;

import java.util.ArrayList;

public interface GraphLogListener {
    void temperatureLog(boolean successfully, int range, ArrayList<TemperatureUnit> temperatureUnits);

    void humidityTemperatureLog(boolean successfully, int range, ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits);
}
