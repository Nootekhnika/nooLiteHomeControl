package com.noolitef.rx;

public interface ListLogListener {
    void temperatureLog(double temperature, String elapsedTime, String logSizeKB);

    void humidityTemperatureLog(double temperature, int humidity, String elapsedTime, String logSizeKB);

    void motionLog(String elapsedTime, String logSizeKB);

    void logComplete(boolean successfully, String message);
}
