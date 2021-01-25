package com.noolitef;

import android.util.Log;

import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.presets.Preset;
import com.noolitef.rx.GraphLogListener;
import com.noolitef.rx.HumidityTemperatureSensor;
import com.noolitef.rx.HumidityTemperatureUnit;
import com.noolitef.rx.LeakDetector;
import com.noolitef.rx.LightSensor;
import com.noolitef.rx.ListLogListener;
import com.noolitef.rx.MotionSensor;
import com.noolitef.rx.OpenCloseSensor;
import com.noolitef.rx.RemoteController;
import com.noolitef.rx.TemperatureSensor;
import com.noolitef.rx.TemperatureUnit;
import com.noolitef.settings.Settings;
import com.noolitef.tx.PowerUnit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface BindListener {
    void onSuccess(String message);

    void onTXbind(String message);

    void onFailure(String message);
}

interface HomeListener {
    void onResponse(String home, ArrayList<Room> rooms);

    void onFailure(String message);
}

interface StateListener {
    void onFailure(String message);

    void onResponse(APIFiles apiFiles, ArrayList<Room> rooms, ArrayList<Preset> presets, ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF);

    void onSwitchingTX_Failure(String message);

    void onSwitchingTX_Ok();

    void onSwitchingF_TX_Failure(int position, String message);

    void onSwitchingF_TX_Ok(int position, int state, int out, int brightness, int temperature);

    void onDebugging(String message);
}

public class NooLiteF {
    public final static int TYPE_RX = 1;
    final static int COMMAND_OFF = 0;
    final static int COMMAND_ON = 2;
    final static int COMMAND_TEMPERATURE_AND_HUMIDITY = 21;
    final static int COMMAND_MOTION = 25;
    public static final int AUTO_STATUS_ON = 0;
    public static final int AUTO_TYPE_THERMOSTAT = 6;

    private static Calendar calendar = Calendar.getInstance();

    private String ip = Settings.URL(); // http://192.168.0.170/ http://134.17.24.191/
    public final static String[] hexString = {
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
            "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
            "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
            "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
            "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
            "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
            "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
            "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
            "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
            "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
            "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"};
    private final static String[] getWeekDayOn = {"", "в понедельник", "во вторник", "в среду", "в четверг", "в пятницу", "в субботу", "в воскресенье"};
    private final static String[] weekDayShort = {"", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    public static final String[] weekDays = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};

    private static boolean updating = false;

    private static Timer timer;
    private static TimerTask task;

    private HomeActivity homeActivity;
    private OkHttpClient client;
    private PRF64 nooLitePRF64;
    private APIFiles apiFiles;
    private StringBuilder logFile;
    private byte[] log;
    private Thread thread;

    private BindListener bindListener;
    private HomeListener homeListener;
    private StateListener stateListener;
    private GraphLogListener graphLogListener;
    private ListLogListener listLogListener;

    private ArrayList<PowerUnit> powerUnits;
    private ArrayList<RemoteController> remoteControllers;
    private ArrayList<TemperatureSensor> temperatureSensors;
    private ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors;
    private ArrayList<MotionSensor> motionSensors;
    private ArrayList<OpenCloseSensor> openCloseSensors;
    private ArrayList<LeakDetector> leakDetectors;
    private ArrayList<LightSensor> lightSensors;
    private ArrayList<PowerUnitF> powerUnitsF;
    private ArrayList<PowerSocketF> powerSocketsF;
    private ArrayList<Thermostat> thermostats;
    private ArrayList<RolletUnitF> rolletUnitsF;
    private ArrayList<Preset> presets;
    private ArrayList<Room> rooms;

    NooLiteF(HomeActivity homeActivity, PRF64 nooLitePRF64, OkHttpClient client, APIFiles apiFiles) {
        this.homeActivity = homeActivity;
        this.client = client;
        this.nooLitePRF64 = nooLitePRF64;
        this.apiFiles = apiFiles;
    }

    public void update(ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF, ArrayList<Preset> presets, ArrayList<Room> rooms) {
        this.powerUnits = powerUnits;
        this.remoteControllers = remoteControllers;
        this.temperatureSensors = temperatureSensors;
        this.humidityTemperatureSensors = humidityTemperatureSensors;
        this.motionSensors = motionSensors;
        this.openCloseSensors = openCloseSensors;
        this.leakDetectors = leakDetectors;
        this.lightSensors = lightSensors;
        this.powerUnitsF = powerUnitsF;
        this.powerSocketsF = powerSocketsF;
        this.thermostats = thermostats;
        this.rolletUnitsF = rolletUnitsF;
        this.presets = presets;
        this.rooms = rooms;
    }

    void stateRefreshingStart() {
        if (powerUnitsF == null && temperatureSensors == null && humidityTemperatureSensors == null) {
            Log.i("nooLiteF", "StateRefreshTimer devices null");
            return;
        }
        if (timer == null && task == null) {
            timer = new Timer();
            task = new StateRefresh(this);
            timer.schedule(task, 2000, 10000);
        }
    }

    void stateRefreshingStop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    void setBindListener(BindListener listener) {
        bindListener = listener;
    }

    void setHomeListener(HomeListener listener) {
        homeListener = listener;
    }

    void setStateListener(StateListener listener) {
        stateListener = listener;
    }

    public void setGraphLogListener(GraphLogListener listener) {
        graphLogListener = listener;
    }

    public void setListLogListener(ListLogListener listener) {
        listLogListener = listener;
    }


    public static String getHexString(int b) {
        if (b < 0) return "FF";
        else return hexString[b];
    }

    public static String getWeekDayShort(int day) {
        if (0 < day && day < 8) {
            return weekDayShort[day];
        }
        return "";
    }

    public static long getMillisecond(int year, int month, int date, int hour, int minute, int second) {
        calendar.set(2000 + year, month - 1, date, hour, minute, second);
        return calendar.getTimeInMillis();
    }

    public static long getUNIXtime(int year, int month, int date, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000 + year, month - 1, date, hour, minute, calendar.get(Calendar.SECOND) + 1);
        return calendar.getTimeInMillis() / 1000L;
    }

    public static String time(int year, int month, int day, int weekDay, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR) % 100;
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int d, currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int h, currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int min, currentMinute = calendar.get(Calendar.MINUTE);
        int s, currentSecond = calendar.get(Calendar.SECOND);

        String logEntryTime;
        if (currentYear == year) {
            if (currentMonth == month) {
                if ((d = currentDay - day) == 0) {
                    if ((h = currentHour - hour) == 0) {
                        if ((min = currentMinute - minute) < 6) {
                            logEntryTime = "только что...";
                        } else {
                            logEntryTime = String.format(Locale.ROOT, "сегодня в %02d:%02d:%02d", hour, minute, second);
                        }
                    } else {
                        logEntryTime = String.format(Locale.ROOT, "сегодня в %02d:%02d:%02d", hour, minute, second);
                    }
                } else {
                    if (d == 1) {
                        logEntryTime = String.format(Locale.ROOT, "вчера в %02d:%02d:%02d", hour, minute, second);
                    } else {
                        if (1 < d && d < 7) {
                            logEntryTime = String.format(Locale.ROOT, "%s, %02d:%02d:%02d", getWeekDayOn[weekDay], hour, minute, second);
                        } else {
                            logEntryTime = String.format(Locale.ROOT, "%02d.%02d %02d:%02d:%02d", day, month, hour, minute, second);
                        }
                    }
                }
            } else {
                logEntryTime = String.format(Locale.ROOT, "%02d.%02d %02d:%02d", day, month, hour, minute);
            }
        } else {
            logEntryTime = String.format(Locale.ROOT, "%02d.%02d.%02d %02d:%02d", day, month, year % 100, hour, minute);
        }

        return logEntryTime;
    }

    public static String getStackTrace(Exception e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        StringBuffer stackTrace = new StringBuffer();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            stackTrace.append(stackTraceElement.toString()).append("\n");
        }
        return stackTrace.toString();
    }

    public boolean getState(final int unitsFcount) {
        if (updating) return false;
        updating = true;
        Request request = new Request.Builder()
                .url(Settings.URL() + "send.htm?sd=000200000080000000000000000000")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                updating = false;
                // костылек против падения при пуске приложения без подключения к сети
                try {
                    int wait = 0;
                    while (stateListener == null) {
                        Thread.sleep(250);
                        if ((wait += 250) > 4000) return;
                    }
                    stateListener.onFailure("Failure in getUnitsState");
                } catch (InterruptedException e1) {
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        response.close();
                        call.cancel();
                        // int timeout = 100 * unitsFcount - 5000;
                        // if (timeout > 0) Thread.sleep(timeout);
                        getDevice();
                    } else {
                        response.close();
                        call.cancel();
                        updating = false;
                        if (stateListener == null) return;
                        stateListener.onFailure("Unsuccessful response in getUnitsState");
                    }
                } catch (Exception e) {
                    response.close();
                    call.cancel();
                    updating = false;
                    if (stateListener == null) return;
                    stateListener.onFailure("Exception in getUnitsState");
                }
            }
        });
        return true;
    }

    private void getDevice() {
        powerUnits = new ArrayList<>();
        remoteControllers = new ArrayList<>();
        temperatureSensors = new ArrayList<>();
        humidityTemperatureSensors = new ArrayList<>();
        motionSensors = new ArrayList<>();
        lightSensors = new ArrayList<>();
        openCloseSensors = new ArrayList<>();
        leakDetectors = new ArrayList<>();
        powerUnitsF = new ArrayList<>();
        powerSocketsF = new ArrayList<>();
        thermostats = new ArrayList<>();
        rolletUnitsF = new ArrayList<>();

        int unitsFcount = 0;

        Request request = new Request.Builder()
                .url(Settings.URL() + "device.bin")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                byte[] bytes;
                bytes = response.body().bytes();
                if (bytes.length != 4102) {
                    response.close();
                    call.cancel();
                    updating = false;
                    stateListener.onFailure("Failure in getDevice");
                    return;
                }

                apiFiles.setDevice(bytes);

                int type;
                String id;

                for (int channel = 0, b = 6; b < 518; channel++, b += 8) {
                    id = getHexString(bytes[b] & 0xFF).concat(getHexString(bytes[b + 1] & 0xFF)).concat(getHexString(bytes[b + 2] & 0xFF)).concat(getHexString(bytes[b + 3] & 0xFF));
                    if (!id.equals("FFFFFFFF")) {
                        type = bytes[b + 4] & 0xFF;
                        switch (type) {
                            case 0:
                            case 1:
                            case 4:
                            case 5:
                            case 7:
                            case 9:
                                type = PowerUnit.DIMMER;
                                break;
                            case 3:
                            case 6:
                            case 8:
                                type = PowerUnit.RELAY;
                                break;
                            case 2:
                                type = PowerUnit.RGB_CONTROLLER;
                                break;
                            case 10:
                                type = PowerUnit.PULSE_RELAY;
                                break;
                        }
                        powerUnits.add(new PowerUnit(type, channel, PowerUnit.OFF, -1, "", "", false));
                    }
                }

//                RAM test
//                powerUnits = new ArrayList<>();
//                for (int tx = 0; tx < 64; tx++)
//                    powerUnits.add(new PowerUnit(tx, PowerUnit.PRESET_OFF, -1, "", "" + tx, false));

                for (int channel = 0, b = 1030; b < 1542; channel++, b += 8) {
                    id = getHexString(bytes[b] & 0xFF).concat(getHexString(bytes[b + 1] & 0xFF)).concat(getHexString(bytes[b + 2] & 0xFF)).concat(getHexString(bytes[b + 3] & 0xFF));
                    type = bytes[b + 4];
                    if (!id.equals("FFFFFFFF")) {
                        switch (type) {
                            case 0:
                                remoteControllers.add(new RemoteController(channel, false, -1, "", ""));
                                break;
                            case 1:
                                temperatureSensors.add(new TemperatureSensor(channel, true, -1, "", "", .0, -1, -1, -1, -1, -1, -1, -1));
                                break;
                            case 2:
                                humidityTemperatureSensors.add(new HumidityTemperatureSensor(channel, true, -1, "", "", .0, 0, -1, -1, -1, -1, -1, -1, -1));
                                break;
                            case 5:
                                motionSensors.add(new MotionSensor(channel, true, -1, "", ""));
                                break;
                            case 8:
                                openCloseSensors.add(new OpenCloseSensor(channel, true, true, -1, "", "", -1, -1, -1, -1, -1, -1, -1));
                                break;
                            case 9:
                                leakDetectors.add(new LeakDetector(channel, false, false, -1, "", "", -1, -1, -1, -1, -1, -1, -1));
                                break;
                            case 10:
                                lightSensors.add(new LightSensor(channel, false, true, -1, "", "", -1, -1, -1, -1, -1, -1, -1));
                                break;
                            default:
                                stateListener.onDebugging(String.format(Locale.ROOT, "Неопознанное устройство nooLite RX\nтип: %d", type));
                        }
                    }
                }

//                RAM test
//                temperatureSensors = new ArrayList<>();
//                for (int rx = 0; rx < 64; rx++)
//                    temperatureSensors.add(new TemperatureSensor(rx, true, -1, "", "" + rx, .0, -1, -1, -1, -1, -1, -1, -1));


                for (int index = 0, b = 2054; b < 2566; index++, b += 8) {
                    id = getHexString(bytes[b] & 0xFF).concat(getHexString(bytes[b + 1] & 0xFF)).concat(getHexString(bytes[b + 2] & 0xFF)).concat(getHexString(bytes[b + 3] & 0xFF));
                    type = bytes[b + 4];
                    if (!id.equals("FFFFFFFF")) {
                        unitsFcount++;
                        switch (type) {
                            case 1:
                                //SLF
                                powerUnitsF.add(new PowerUnitF(id, index, PowerUnitF.OFF, PowerUnitF.NOT_CONNECTED, -1, "", "", 0, false));
                                break;
                            case 2:
                                //SLF-10
                                powerUnitsF.add(new PowerUnitF(id, index, PowerUnitF.OFF, PowerUnitF.NOT_CONNECTED, -1, "", "", 0, false));
                                break;
                            case 3:
                                //SRF
                                powerSocketsF.add(new PowerSocketF(id, index, PowerSocketF.OFF, PowerSocketF.NOT_CONNECTED, -1, "", "", false));
                                break;
                            case 4:
                                //SRF
                                powerSocketsF.add(new PowerSocketF(id, index, PowerSocketF.OFF, PowerSocketF.NOT_CONNECTED, -1, "", "", false));
                                break;
                            case 5:
                                //SUF
                                powerUnitsF.add(new PowerUnitF(id, index, PowerUnitF.OFF, PowerUnitF.NOT_CONNECTED, -1, "", "", 0, false));
                                powerUnitsF.get(powerUnitsF.size() - 1).setDimming(true);
                                break;
                            case 6:
                                //SRF-T
                                thermostats.add(new Thermostat(id, index, Thermostat.NOT_CONNECTED, 0, 0, Thermostat.OUTPUT_OFF, -1, "", ""));
                                break;
                            case 7:
                                //Ролета
                                rolletUnitsF.add(new RolletUnitF(id, index, RolletUnitF.NOT_CONNECTED, -1, "", ""));
                                break;
                            default:
                                stateListener.onDebugging(String.format(Locale.ROOT, "Неопознанное устройство nooLite-F TX\nтип: %s, id: %s", type, id));
                        }
                    }
                }

//                RAM test
//                powerUnitsF = new ArrayList<>();
//                for (int ftx = 0; ftx < 64; ftx++)
//                    powerUnitsF.add(new PowerUnitF(String.format(Locale.ROOT, "%08d", ftx), ftx, PowerUnitF.OFF, PowerUnitF.NOT_CONNECTED, -1, "", "" + ftx, 0, false));

                response.close();
                call.cancel();

                int timeout = 100 * unitsFcount - 2250;
                if (timeout > 0) Thread.sleep(timeout);

                getPreset(powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
            } else {
                response.close();
                call.cancel();
                updating = false;
                stateListener.onFailure("Unsuccessful response in getDevice");
            }
        } catch (Exception e) {
            call.cancel();
            updating = false;
            stateListener.onFailure("Exception in getDevice");
        }
    }

    private void getPreset(final ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, final ArrayList<TemperatureSensor> temperatureSensors, final ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, final ArrayList<MotionSensor> motionSensors, final ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, final ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, final ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        presets = new ArrayList<>();

        Request request = new Request.Builder()
                .url(Settings.URL() + "preset.bin")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                byte[] bytes;
                bytes = response.body().bytes();
                if (bytes.length != 32774) {
                    response.close();
                    call.cancel();
                    updating = false;
                    homeListener.onFailure("Failure in getPreset");
                    return;
                }

                apiFiles.setPreset(bytes);

                byte[] command;
                int cbi;

                for (int b = 6, i = 0, p = 0; b < 32774; i++, b += 1024) {
                    if ((bytes[b] & 0xFF) != 255) {
                        presets.add(new Preset(i, Preset.OFF, ""));
                        for (int c = 0; c < 73; c++) {
                            command = new byte[14];
                            cbi = 0;
                            for (int cb = b + (c * 14); cb < b + (c * 14) + 14; cb++) {
                                command[cbi++] = bytes[cb];
                            }
                            presets.get(p).setCommand(c, command);
                        }
                        p++;
                    }
                }

                response.close();
                call.cancel();
                getUser(presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
            } else {
                response.close();
                call.cancel();
                updating = false;
                stateListener.onFailure("Unsuccessful response in getPreset");
            }
        } catch (Exception e) {
            call.cancel();
            updating = false;
            stateListener.onFailure("Exception in getPreset");
        }
    }

    private void getUser(ArrayList<Preset> presets, final ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, final ArrayList<TemperatureSensor> temperatureSensors, final ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, final ArrayList<MotionSensor> motionSensors, final ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, final ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, final ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        try {
            byte[] bytes = new byte[0];
            Request request = new Request.Builder()
                    .url(Settings.URL() + "user.bin")
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()) {
                bytes = response.body().bytes();
                if (bytes.length != 12294) {
                    response.close();
                    call.cancel();
                    updating = false;
                    homeListener.onFailure("Failure in getUser");
                    return;
                }
                response.close();
                call.cancel();
            } else {
                response.close();
                call.cancel();
                updating = false;
                stateListener.onFailure("Unsuccessful response in getUser");
            }

            apiFiles.setUser(bytes);

            rooms = new ArrayList<>();

            String home;
            byte[] homeBytes = new byte[64];
            for (int b = 8710, hb = 0; hb < 64; hb++, b++) {
                if (bytes[b] != 0) {
                    homeBytes[hb] = bytes[b];
                } else {
                    homeBytes[hb] = 20;
                }
            }
            if (homeBytes[63] != -1) {
                home = new String(homeBytes, "cp1251").trim();
            } else {
                home = "";
            }

            String roomName;

            for (int r = 0, b = 8774; b < 10470; b += 53, r++) {
                if (bytes[b + 31] != -1) {
                    byte[] stringBytes = new byte[32];
                    for (int nb = 0; nb < 32; nb++) {
                        if (bytes[b + nb] != 0) {
                            stringBytes[nb] = bytes[b + nb];
                        } else {
                            stringBytes[nb] = 20;
                        }
                    }
                    roomName = new String(stringBytes, "cp1251").trim();
                    rooms.add(new Room(r, roomName));
                }
            }

            homeListener.onResponse(home, rooms);

            int b;
            byte[] stringBytes;
            String name;
            int roomID;
            String room;

            for (int i = 0; i < presets.size(); i++) {
                b = 10470 + (presets.get(i).getIndex() * 33);
                stringBytes = new byte[32];

                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                presets.get(i).setName(name);
            }

            for (int i = 0; i < powerUnits.size(); i++) {
                b = 6 + (powerUnits.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                powerUnits.get(i).setRoomID(roomID);
                powerUnits.get(i).setRoom(room);
                powerUnits.get(i).setName(name);
            }

            for (int i = 0; i < remoteControllers.size(); i++) {
                b = 2182 + (remoteControllers.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                remoteControllers.get(i).setRoomID(roomID);
                remoteControllers.get(i).setRoom(room);
                remoteControllers.get(i).setName(name);
            }

            for (int i = 0; i < temperatureSensors.size(); i++) {
                b = 2182 + (temperatureSensors.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                temperatureSensors.get(i).setRoomID(roomID);
                temperatureSensors.get(i).setRoom(room);
                temperatureSensors.get(i).setName(name);
            }

            for (int i = 0; i < humidityTemperatureSensors.size(); i++) {
                b = 2182 + (humidityTemperatureSensors.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                humidityTemperatureSensors.get(i).setRoomID(roomID);
                humidityTemperatureSensors.get(i).setRoom(room);
                humidityTemperatureSensors.get(i).setName(name);
            }

            for (int i = 0; i < motionSensors.size(); i++) {
                b = 2182 + (motionSensors.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                motionSensors.get(i).setRoomID(roomID);
                motionSensors.get(i).setRoom(room);
                motionSensors.get(i).setName(name);
            }

            for (int i = 0; i < openCloseSensors.size(); i++) {
                b = 2182 + (openCloseSensors.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                openCloseSensors.get(i).setRoomID(roomID);
                openCloseSensors.get(i).setRoom(room);
                openCloseSensors.get(i).setName(name);
            }

            for (int i = 0; i < leakDetectors.size(); i++) {
                b = 2182 + (leakDetectors.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                leakDetectors.get(i).setRoomID(roomID);
                leakDetectors.get(i).setRoom(room);
                leakDetectors.get(i).setName(name);
            }

            for (int i = 0; i < lightSensors.size(); i++) {
                b = 2182 + (lightSensors.get(i).getChannel() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                lightSensors.get(i).setRoomID(roomID);
                lightSensors.get(i).setRoom(room);
                lightSensors.get(i).setName(name);
            }

            for (int i = 0; i < powerUnitsF.size(); i++) {
                b = 4358 + (powerUnitsF.get(i).getIndex() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                powerUnitsF.get(i).setRoomID(roomID);
                powerUnitsF.get(i).setRoom(room);
                powerUnitsF.get(i).setName(name);
            }

            for (int i = 0; i < powerSocketsF.size(); i++) {
                b = 4358 + (powerSocketsF.get(i).getIndex() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                powerSocketsF.get(i).setRoomID(roomID);
                powerSocketsF.get(i).setRoom(room);
                powerSocketsF.get(i).setName(name);
            }

            for (int i = 0; i < thermostats.size(); i++) {
                b = 4358 + (thermostats.get(i).getIndex() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                thermostats.get(i).setRoomID(roomID);
                thermostats.get(i).setRoom(room);
                thermostats.get(i).setName(name);
            }

            for (int i = 0; i < rolletUnitsF.size(); i++) {
                b = 4358 + (rolletUnitsF.get(i).getIndex() * 34);
                stringBytes = new byte[32];
                for (int nb = 0; nb < 32; nb++) {
                    if (bytes[b + nb] != 0) {
                        stringBytes[nb] = bytes[b + nb];
                    } else {
                        stringBytes[nb] = 20;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();
                if (stringBytes[31] == -1) name = "";

                roomID = bytes[b + 33];
                if (roomID != -1) {
                    for (int rb = 0; rb < 32; rb++) {
                        if (bytes[8774 + (roomID * 53) + rb] != 0) {
                            stringBytes[rb] = bytes[8774 + (roomID * 53) + rb];
                        } else {
                            stringBytes[rb] = 20;
                        }
                    }
                    room = new String(stringBytes, "cp1251").trim();
                } else {
                    room = "";
                }

                rolletUnitsF.get(i).setRoomID(roomID);
                rolletUnitsF.get(i).setRoom(room);
                rolletUnitsF.get(i).setName(name);
            }

            getPowerUnitsFState(true, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
        } catch (Exception e) {
            updating = false;
            stateListener.onFailure("Empty response in getUser");
            return;
        }

    }

    public void getPowerUnitsFState() {
        getPowerUnitsFState(false, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
    }

    private void getPowerUnitsFState(boolean updatePRF64, ArrayList<Preset> presets, final ArrayList<PowerUnit> powerUnits, final ArrayList<RemoteController> remoteControllers, final ArrayList<TemperatureSensor> temperatureSensors, final ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, final ArrayList<MotionSensor> motionSensors, final ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, final ArrayList<LightSensor> lightSensors, final ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, final ArrayList<Thermostat> thermostats, final ArrayList<RolletUnitF> rolletUnitsF) {
        Request request = new Request.Builder()
                .url(Settings.URL() + "state.htm")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                String hex;
                hex = response.body().string();
                if (hex.length() != 269) {
                    response.close();
                    call.cancel();
                    updating = false;
                    homeListener.onFailure("Failure in getUnitsState");
                    return;
                }

                if (updatePRF64) {
                    nooLitePRF64.setDevice(apiFiles.getDevice());
                    nooLitePRF64.setPreset(apiFiles.getPreset());
                    nooLitePRF64.setUser(apiFiles.getUser());
                    nooLitePRF64.setState(hex);
                    nooLitePRF64.setLastUpdateTimestamp(hex);
                }

                int b;
                String state;
                int brightness;

                for (int i = 0; i < powerUnitsF.size(); i++) {
                    b = 14 + (3 * powerUnitsF.get(i).getIndex());
                    state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

                    if (state.substring(2, 3).equals("0")) {
                        powerUnitsF.get(i).setState(PowerUnitF.NOT_CONNECTED);
                    } else {
                        if (state.substring(0, 1).equals("0")) {
                            powerUnitsF.get(i).setState(PowerUnitF.OFF);
                        } else {
                            powerUnitsF.get(i).setState(PowerUnitF.ON);
                        }
                    }

                    brightness = Integer.parseInt(hex.substring(b + 1, b + 3), 16);
                    powerUnitsF.get(i).setBrightness((int) (brightness * 100 / 255 + .5));
                }

                for (int i = 0; i < powerSocketsF.size(); i++) {
                    b = 14 + (3 * powerSocketsF.get(i).getIndex());
                    state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

                    if (state.substring(2, 3).equals("0")) {
                        powerSocketsF.get(i).setState(PowerSocketF.NOT_CONNECTED);
                    } else {
                        if (state.substring(0, 1).equals("0")) {
                            powerSocketsF.get(i).setState(PowerSocketF.OFF);
                        } else {
                            powerSocketsF.get(i).setState(PowerSocketF.ON);
                        }
                    }
                }

                int currentTemperature;

                for (int i = 0; i < thermostats.size(); i++) {
                    b = 14 + (3 * thermostats.get(i).getIndex());
                    state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

                    if (state.substring(2, 3).equals("0")) {
                        thermostats.get(i).setState(Thermostat.NOT_CONNECTED);
                    } else {
                        if (state.substring(0, 1).equals("0")) {
                            thermostats.get(i).setState(Thermostat.OFF);
                        } else {
                            thermostats.get(i).setState(Thermostat.ON);
                        }
                    }
                    if (state.substring(1, 2).equals("0")) {
                        thermostats.get(i).setOutputState(Thermostat.OUTPUT_OFF);
                    } else {
                        thermostats.get(i).setOutputState(Thermostat.OUTPUT_ON);
                    }

                    currentTemperature = Integer.parseInt(hex.substring(b + 1, b + 3), 16);
                    thermostats.get(i).setCurrentTemperature(currentTemperature);
                }

                for (int i = 0; i < rolletUnitsF.size(); i++) {
                    b = 14 + (3 * rolletUnitsF.get(i).getIndex());
                    state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

                    if (state.substring(2, 3).equals("0")) {
                        rolletUnitsF.get(i).setState(RolletUnitF.NOT_CONNECTED);
                    } else {
                        if (state.substring(0, 1).equals("0")) {
                            rolletUnitsF.get(i).setState(RolletUnitF.CLOSE);
                        } else {
                            rolletUnitsF.get(i).setState(RolletUnitF.OPEN);
                        }
                    }
                }

                response.close();
                call.cancel();

                getRecentLog(temperatureSensors, humidityTemperatureSensors, openCloseSensors, leakDetectors, lightSensors);

                updating = false;
                stateListener.onResponse(apiFiles, rooms, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
                //stateRefreshingStart();
            } else {
                response.close();
                call.cancel();
                updating = false;
                stateListener.onFailure("Unsuccessful response in getUnitsFState");
            }
        } catch (Exception e) {
            call.cancel();
            updating = false;
            stateListener.onFailure("Empty response in getUnitsFState");
            return;
        }
    }

    public void getPowerUnitFState(final int position, final int index) throws IOException {
        if (position < 0) return;  // for Preset

        Request request = new Request.Builder()
                .url(Settings.URL() + "state.htm")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            String hex;
            hex = response.body().string();
            if (hex.length() != 269) {
                response.close();
                call.cancel();
                updating = false;
                stateListener.onSwitchingF_TX_Failure(position, "getUnitsState #" + index + ": response not full (" + hex.length() + " bytes)");
                return;
            }

            int b = 14 + (3 * index);
            String st = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');
            int state;
            int out;

            if (st.substring(2, 3).equals("0")) {
                state = PowerUnitF.NOT_CONNECTED;
            } else {
                if (st.substring(0, 1).equals("0")) {
                    state = PowerUnitF.OFF;
                } else {
                    state = PowerUnitF.ON;
                }
            }
            if (st.substring(1, 2).equals("0")) {
                out = Thermostat.OUTPUT_OFF;
            } else {
                out = Thermostat.OUTPUT_ON;
            }
            int data = Integer.parseInt(hex.substring(b + 1, b + 3), 16);

            //getOtherPowerUnitsFState(hex);
            stateListener.onSwitchingF_TX_Ok(position, state, out, data, data);
        } else {
            stateListener.onSwitchingF_TX_Failure(position, "getUnitsState #" + index + " connection error " + String.valueOf(response.code()));
        }

        response.close();
        call.cancel();
    }

    private void getOtherPowerUnitsFState(String hex) {
        int b;
        String state;
        int brightness;

        for (int i = 0; i < powerUnitsF.size(); i++) {
            b = 14 + (3 * powerUnitsF.get(i).getIndex());
            state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

            if (state.substring(2, 3).equals("0")) {
                powerUnitsF.get(i).setState(PowerUnitF.NOT_CONNECTED);
            } else {
                if (state.substring(0, 1).equals("0")) {
                    powerUnitsF.get(i).setState(PowerUnitF.OFF);
                } else {
                    powerUnitsF.get(i).setState(PowerUnitF.ON);
                }
            }

            brightness = Integer.parseInt(hex.substring(b + 1, b + 3), 16);
            powerUnitsF.get(i).setBrightness((int) (brightness * 100 / 255 + .5));
        }

        for (int i = 0; i < powerSocketsF.size(); i++) {
            b = 14 + (3 * powerSocketsF.get(i).getIndex());
            state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

            if (state.substring(2, 3).equals("0")) {
                powerSocketsF.get(i).setState(PowerSocketF.NOT_CONNECTED);
            } else {
                if (state.substring(0, 1).equals("0")) {
                    powerSocketsF.get(i).setState(PowerSocketF.OFF);
                } else {
                    powerSocketsF.get(i).setState(PowerSocketF.ON);
                }
            }
        }

        int currentTemperature;

        for (int i = 0; i < thermostats.size(); i++) {
            b = 14 + (3 * thermostats.get(i).getIndex());
            state = String.format("%4s", Integer.toBinaryString(Integer.parseInt(hex.substring(b, b + 1), 16))).replace(' ', '0');

            if (state.substring(2, 3).equals("0")) {
                thermostats.get(i).setState(Thermostat.NOT_CONNECTED);
            } else {
                if (state.substring(0, 1).equals("0")) {
                    thermostats.get(i).setState(Thermostat.OFF);
                } else {
                    thermostats.get(i).setState(Thermostat.ON);
                }
            }
            if (state.substring(1, 2).equals("0")) {
                thermostats.get(i).setOutputState(Thermostat.OUTPUT_OFF);
            } else {
                thermostats.get(i).setOutputState(Thermostat.OUTPUT_ON);
            }

            currentTemperature = Byte.parseByte(hex.substring(b + 1, b + 3), 16);
            thermostats.get(i).setCurrentTemperature(currentTemperature);
        }
    }

    private void getRecentLog(final ArrayList<TemperatureSensor> temperatureSensors, final ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, final ArrayList<OpenCloseSensor> openCloseSensors, final ArrayList<LeakDetector> leakDetectors, final ArrayList<LightSensor> lightSensors) {
        Request request = new Request.Builder()
                .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                stateListener.onFailure("Fail in getRecentLog");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        String state;
                        StringBuilder temperatureSensorsGettingRecentData = new StringBuilder();
                        StringBuilder temperatureSensorsGotRecentData = new StringBuilder();
                        if (temperatureSensors.size() != 0) {
                            for (int b = 0; b < temperatureSensors.size(); b++) {
                                temperatureSensorsGettingRecentData.append('0');
                                temperatureSensorsGotRecentData.append(('1'));
                            }
                        } else {
                            temperatureSensorsGettingRecentData.append('0');
                            temperatureSensorsGotRecentData.append(('0'));
                        }
                        StringBuilder humidityTemperatureSensorsGettingRecentData = new StringBuilder();
                        StringBuilder humidityTemperatureSensorsGotRecentData = new StringBuilder();
                        if (humidityTemperatureSensors.size() != 0) {
                            for (int b = 0; b < humidityTemperatureSensors.size(); b++) {
                                humidityTemperatureSensorsGettingRecentData.append('0');
                                humidityTemperatureSensorsGotRecentData.append('1');
                            }
                        } else {
                            humidityTemperatureSensorsGettingRecentData.append('0');
                            humidityTemperatureSensorsGotRecentData.append('0');
                        }
                        StringBuilder openCloseSensorsGettingRecentData = new StringBuilder();
                        StringBuilder openCloseSensorsGotRecentData = new StringBuilder();
                        if (openCloseSensors.size() != 0) {
                            for (int b = 0; b < openCloseSensors.size(); b++) {
                                openCloseSensorsGettingRecentData.append('0');
                                openCloseSensorsGotRecentData.append('1');
                            }
                        } else {
                            openCloseSensorsGettingRecentData.append('0');
                            openCloseSensorsGotRecentData.append('0');
                        }
                        StringBuilder leakDetectorsGettingRecentData = new StringBuilder();
                        StringBuilder leakDetectorsGotRecentData = new StringBuilder();
                        if (leakDetectors.size() != 0) {
                            for (int b = 0; b < leakDetectors.size(); b++) {
                                leakDetectorsGettingRecentData.append('0');
                                leakDetectorsGotRecentData.append('1');
                            }
                        } else {
                            leakDetectorsGettingRecentData.append('0');
                            leakDetectorsGotRecentData.append('0');
                        }
                        StringBuilder lightSensorsGettingRecentData = new StringBuilder();
                        StringBuilder lightSensorsGotRecentData = new StringBuilder();
                        if (lightSensors.size() != 0) {
                            for (int b = 0; b < lightSensors.size(); b++) {
                                lightSensorsGettingRecentData.append('0');
                                lightSensorsGotRecentData.append('1');
                            }
                        } else {
                            lightSensorsGettingRecentData.append('0');
                            lightSensorsGotRecentData.append('0');
                        }

                        cleanLog();

                        byte[] data = new byte[21];
                        InputStream inputStream = response.body().byteStream();
                        while (inputStream.read(data) != -1) {
                            if ((data[0] & 0xFF) == 1) {
                                for (int ts = 0; ts < temperatureSensors.size(); ts++) {
                                    if ((temperatureSensors.get(ts).getChannel() == (data[3] & 0xFF)) && ((data[4] & 0xFF) == COMMAND_TEMPERATURE_AND_HUMIDITY)) {
                                        if (temperatureSensorsGettingRecentData.charAt(ts) == '0') {
                                            String temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                            if (temp.charAt(0) == '0') {
                                                temperatureSensors.get(ts).setCurrentTemperature((double) Integer.parseInt(temp, 2) / 10);
                                            } else {
                                                temperatureSensors.get(ts).setCurrentTemperature((double) (4096 - Integer.parseInt(temp, 2)) / -10);
                                            }

                                            state = String.format("%12s", Integer.toBinaryString(data[7] & 0xFF)).replace(' ', '0');
                                            if (state.charAt(4) == '0') {
                                                temperatureSensors.get(ts).setBatteryOK(true);
                                            }

                                            temperatureSensors.get(ts).setUpdateSecond(data[14] & 0xFF);
                                            temperatureSensors.get(ts).setUpdateMinute(data[15] & 0xFF);
                                            temperatureSensors.get(ts).setUpdateHour(data[16] & 0xFF);
                                            temperatureSensors.get(ts).setUpdateWeekDay(data[17] & 0xFF);
                                            temperatureSensors.get(ts).setUpdateDay(data[18] & 0xFF);
                                            temperatureSensors.get(ts).setUpdateMonth(data[19] & 0xFF);
                                            temperatureSensors.get(ts).setUpdateYear(data[20] & 0xFF);

                                            temperatureSensorsGettingRecentData.setCharAt(ts, '1');
                                        }
                                    }
                                }
                                for (int hts = 0; hts < humidityTemperatureSensors.size(); hts++) {
                                    if ((humidityTemperatureSensors.get(hts).getChannel() == (data[3] & 0xFF)) && ((data[4] & 0xFF) == COMMAND_TEMPERATURE_AND_HUMIDITY)) {
                                        if (humidityTemperatureSensorsGettingRecentData.charAt(hts) == '0') {
                                            String temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                            if (temp.charAt(0) == '0') {
                                                humidityTemperatureSensors.get(hts).setTemperature((double) Integer.parseInt(temp, 2) / 10);
                                            } else {
                                                humidityTemperatureSensors.get(hts).setTemperature((double) (4096 - Integer.parseInt(temp, 2)) / -10);
                                            }

                                            state = String.format("%12s", Integer.toBinaryString(data[7] & 0xFF)).replace(' ', '0');
                                            if (state.charAt(4) == '0') {
                                                humidityTemperatureSensors.get(hts).setBatteryOK(true);
                                            }

                                            humidityTemperatureSensors.get(hts).setHumidity(data[8] & 0xFF);

                                            humidityTemperatureSensors.get(hts).setLastUpdateSecond(data[14] & 0xFF);
                                            humidityTemperatureSensors.get(hts).setLastUpdateMinute(data[15] & 0xFF);
                                            humidityTemperatureSensors.get(hts).setLastUpdateHour(data[16] & 0xFF);
                                            humidityTemperatureSensors.get(hts).setLastUpdateWeekDay(data[17] & 0xFF);
                                            humidityTemperatureSensors.get(hts).setLastUpdateDay(data[18] & 0xFF);
                                            humidityTemperatureSensors.get(hts).setLastUpdateMonth(data[19] & 0xFF);
                                            humidityTemperatureSensors.get(hts).setLastUpdateYear(data[20] & 0xFF);

                                            humidityTemperatureSensorsGettingRecentData.setCharAt(hts, '1');
                                        }
                                    }
                                }
                                for (int ocs = 0; ocs < openCloseSensors.size(); ocs++) {
                                    if ((openCloseSensors.get(ocs).getChannel() == (data[3] & 0xFF)) && ((data[4] & 0xFF) == COMMAND_OFF || (data[4] & 0xFF) == COMMAND_ON)) {
                                        if (openCloseSensorsGettingRecentData.charAt(ocs) == '0') {
                                            state = String.format("%12s", Integer.toBinaryString(data[7] & 0xFF)).replace(' ', '0');
                                            if (state.charAt(4) == '0') {
                                                openCloseSensors.get(ocs).setBatteryOK(true);
                                            }

                                            switch (data[4] & 0xFF) {
                                                case 0:
                                                    openCloseSensors.get(ocs).setClosed(true);
                                                    break;
                                                case 2:
                                                    openCloseSensors.get(ocs).setClosed(false);
                                                    break;
                                            }

                                            openCloseSensors.get(ocs).setLastUpdateSecond(data[14] & 0xFF);
                                            openCloseSensors.get(ocs).setLastUpdateMinute(data[15] & 0xFF);
                                            openCloseSensors.get(ocs).setLastUpdateHour(data[16] & 0xFF);
                                            openCloseSensors.get(ocs).setLastUpdateWeekDay(data[17] & 0xFF);
                                            openCloseSensors.get(ocs).setLastUpdateDay(data[18] & 0xFF);
                                            openCloseSensors.get(ocs).setLastUpdateMonth(data[19] & 0xFF);
                                            openCloseSensors.get(ocs).setLastUpdateYear(data[20] & 0xFF);

                                            openCloseSensorsGettingRecentData.setCharAt(ocs, '1');
                                        }
                                    }
                                }
                                for (int ld = 0; ld < leakDetectors.size(); ld++) {
                                    if ((leakDetectors.get(ld).getChannel() == (data[3] & 0xFF)) && ((data[4] & 0xFF) == COMMAND_OFF || (data[4] & 0xFF) == COMMAND_ON)) {
                                        if (leakDetectorsGettingRecentData.charAt(ld) == '0') {
                                            state = String.format("%12s", Integer.toBinaryString(data[7] & 0xFF)).replace(' ', '0');
                                            if (state.charAt(4) == '0') {
                                                leakDetectors.get(ld).setBatteryLow(false);
                                            }

                                            if ((data[4] & 0xFF) == COMMAND_OFF) {
                                                leakDetectors.get(ld).setLeakage(false);
                                            }
                                            if ((data[4] & 0xFF) == COMMAND_ON) {
                                                leakDetectors.get(ld).setLeakage(true);
                                            }

                                            leakDetectors.get(ld).setLastUpdateSecond(data[14] & 0xFF);
                                            leakDetectors.get(ld).setLastUpdateMinute(data[15] & 0xFF);
                                            leakDetectors.get(ld).setLastUpdateHour(data[16] & 0xFF);
                                            leakDetectors.get(ld).setLastUpdateWeekDay(data[17] & 0xFF);
                                            leakDetectors.get(ld).setLastUpdateDay(data[18] & 0xFF);
                                            leakDetectors.get(ld).setLastUpdateMonth(data[19] & 0xFF);
                                            leakDetectors.get(ld).setLastUpdateYear(data[20] & 0xFF);

                                            leakDetectorsGettingRecentData.setCharAt(ld, '1');
                                        }
                                    }
                                }
                                for (int ocs = 0; ocs < lightSensors.size(); ocs++) {
                                    if ((lightSensors.get(ocs).getChannel() == (data[3] & 0xFF)) && ((data[4] & 0xFF) == COMMAND_OFF || (data[4] & 0xFF) == COMMAND_ON)) {
                                        if (lightSensorsGettingRecentData.charAt(ocs) == '0') {
                                            state = String.format("%8s", Integer.toBinaryString(data[7] & 0xFF)).replace(' ', '0');
                                            if (state.charAt(0) == '1') {
                                                lightSensors.get(ocs).setBatteryLow();
                                            }

                                            switch (data[4] & 0xFF) {
                                                case 0:
                                                    lightSensors.get(ocs).setDarkness(false);
                                                    break;
                                                case 2:
                                                    lightSensors.get(ocs).setDarkness(true);
                                                    break;
                                            }

                                            lightSensors.get(ocs).setLastUpdateSecond(data[14] & 0xFF);
                                            lightSensors.get(ocs).setLastUpdateMinute(data[15] & 0xFF);
                                            lightSensors.get(ocs).setLastUpdateHour(data[16] & 0xFF);
                                            lightSensors.get(ocs).setLastUpdateWeekDay(data[17] & 0xFF);
                                            lightSensors.get(ocs).setLastUpdateDay(data[18] & 0xFF);
                                            lightSensors.get(ocs).setLastUpdateMonth(data[19] & 0xFF);
                                            lightSensors.get(ocs).setLastUpdateYear(data[20] & 0xFF);

                                            lightSensorsGettingRecentData.setCharAt(ocs, '1');
                                        }
                                    }
                                }
                                if (temperatureSensorsGettingRecentData.toString().equals(temperatureSensorsGotRecentData.toString()) && humidityTemperatureSensorsGettingRecentData.toString().equals(humidityTemperatureSensorsGotRecentData.toString()) && openCloseSensorsGettingRecentData.toString().equals(openCloseSensorsGotRecentData.toString()) && leakDetectorsGettingRecentData.toString().equals(leakDetectorsGotRecentData.toString()) && lightSensorsGettingRecentData.toString().equals(lightSensorsGotRecentData.toString())) {
                                    inputStream.close();
                                    response.close();
                                    call.cancel();
                                    if (thermostats.size() > 0) getTargetThermostatTemperature();
                                    stateListener.onResponse(apiFiles, rooms, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
                                    return;
                                }
                            } else {
                                if ((data[0] & 0xFF) == 255) {
                                    inputStream.close();
                                    response.close();
                                    call.cancel();
                                    if (thermostats.size() > 0) getTargetThermostatTemperature();
                                    stateListener.onResponse(apiFiles, rooms, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
                                    return;
                                }
                            }
                        }
                    } else {
                        response.close();
                        call.cancel();
                        stateListener.onFailure("Unsuccessful response in getRecentLog");
                    }
                } catch (Exception e) {
                    call.cancel();
                    stateListener.onFailure("Exception in getRecentLog");
                }
            }
        });
    }

    private void getTargetThermostatTemperature() {
        Request request;
        Call call = null;
        Response response = null;
        try {
            for (Thermostat thermostat : thermostats) {
                request = new Request.Builder()
                        .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", thermostat.getId()))
                        .post(RequestBody.create(null, ""))
                        .build();
                call = client.newCall(request);
                response = call.execute();
                if (response.isSuccessful()) {
                    response.close();
                    call.cancel();
                    request = new Request.Builder()
                            .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801F00000000%s", thermostat.getId()))
                            .post(RequestBody.create(null, ""))
                            .build();
                    call = client.newCall(request);
                    response = call.execute();
                    if (response.isSuccessful()) {
                        response.close();
                        call.cancel();
                        Thread.sleep(100);
                        request = new Request.Builder()
                                .url(Settings.URL() + "rxset.htm")
                                .build();
                        call = client.newCall(request);
                        response = call.execute();
                        if (response.isSuccessful()) {
                            String hex = response.body().string();
                            if (hex.substring(22, 30).equals(thermostat.getId())) {
                                thermostat.setTargetTemperature(Integer.parseInt(hex.substring(14, 16), 16));
                            } else {
                                thermostat.setTargetTemperature(0);
                            }
                            response.close();
                            call.cancel();
                        } else {
                            response.close();
                            call.cancel();
                            stateListener.onFailure("Unsuccessful response in getTargetThermostatTemperature");
                        }
                    } else {
                        response.close();
                        call.cancel();
                        stateListener.onFailure("Unsuccessful response in getTargetThermostatTemperature");
                    }
                } else {
                    response.close();
                    call.cancel();
                    stateListener.onFailure("Unsuccessful response in getTargetThermostatTemperature");
                }
            }
        } catch (Exception e) {
            if (response != null) response.close();
            if (call != null) call.cancel();
            stateListener.onFailure("Exception in getTargetThermostatTemperature");
        }
    }

    public boolean getTemperatureLog(final int range, final TemperatureSensor temperatureSensor) {
        if (updating || graphLogListener == null) return false;
        updating = true;

        if (log == null) {
            cleanLog();
            Request request = new Request.Builder()
                    .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException exception) {
                    call.cancel();
                    updating = false;
                    graphLogListener.temperatureLog(false, 0, null);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        ArrayList<TemperatureUnit> temperatureUnits = new ArrayList<>();
                        try {
                            byte[] data = new byte[21];
                            String temp;
                            double temperature;

                            InputStream inputStream = response.body().byteStream();
                            while (inputStream.read(data) != -1) {
                                readLog(data);
                                if ((data[0] & 0xFF) == TYPE_RX) {
                                    if (temperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == COMMAND_TEMPERATURE_AND_HUMIDITY) {
                                        temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                        if (temp.charAt(0) == '0') {
                                            temperature = (double) Integer.parseInt(temp, 2) / 10;
                                        } else {
                                            temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                        }
                                        temperatureUnits.add(new TemperatureUnit(temperature, data[20], data[19], data[18], data[17], data[16], data[15], data[14]));
                                    }
                                } else {
                                    if ((data[0] & 0xFF) == 255) {
                                        inputStream.close();
                                        response.close();
                                        saveLog(logFile);
                                        updating = false;
                                        graphLogListener.temperatureLog(true, range, temperatureUnits);
                                        return;
                                    }
                                }
                            }
                            inputStream.close();
                            response.close();
                            saveLog(logFile);
                            updating = false;
                            graphLogListener.temperatureLog(true, range, temperatureUnits);
                        } catch (Exception e) {
                            cleanLog();
                            updating = false;
                            graphLogListener.temperatureLog(false, range, temperatureUnits);
                        }
                    } else {
                        updating = false;
                        graphLogListener.temperatureLog(false, 0, null);
                    }
                }
            });
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<TemperatureUnit> temperatureUnits = new ArrayList<>();
                    byte[] data = new byte[21];
                    String temp;
                    double temperature;

                    for (int b = 0; b < log.length; b += 21) {
                        if (thread.isInterrupted()) {
                            updating = false;
                            Thread.currentThread().interrupt();
                            return;
                        }
                        //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                        System.arraycopy(log, b, data, 0, 21);
                        if ((data[0] & 0xFF) == TYPE_RX) {
                            if (temperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == COMMAND_TEMPERATURE_AND_HUMIDITY) {
                                temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                if (temp.charAt(0) == '0') {
                                    temperature = (double) Integer.parseInt(temp, 2) / 10;
                                } else {
                                    temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                }
                                temperatureUnits.add(new TemperatureUnit(temperature, data[20], data[19], data[18], data[17], data[16], data[15], data[14]));
                            }
                        } else {
                            if ((data[0] & 0xFF) == 255) {
                                updating = false;
                                graphLogListener.temperatureLog(true, range, temperatureUnits);
                                return;
                            }
                        }
                    }
                    updating = false;
                    graphLogListener.temperatureLog(true, range, temperatureUnits);
                }
            });
            thread.start();
        }
        return true;
    }

    public boolean getHumidityTemperatureLog(final int range, final HumidityTemperatureSensor humidityTemperatureSensor) {
        if (updating || graphLogListener == null) return false;
        updating = true;

        if (log == null) {
            cleanLog();
            Request request = new Request.Builder()
                    .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException exception) {
                    call.cancel();
                    updating = false;
                    graphLogListener.humidityTemperatureLog(false, 0, null);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits = new ArrayList<>();
                        try {
                            byte[] data = new byte[21];
                            String temp;
                            double temperature;
                            int humidity;

                            InputStream inputStream = response.body().byteStream();
                            while (inputStream.read(data) != -1) {
                                readLog(data);
                                if ((data[0] & 0xFF) == 1) {
                                    if (humidityTemperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == COMMAND_TEMPERATURE_AND_HUMIDITY) {
                                        temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                        if (temp.charAt(0) == '0') {
                                            temperature = (double) Integer.parseInt(temp, 2) / 10;
                                        } else {
                                            temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                        }
                                        humidity = data[8];
                                        humidityTemperatureUnits.add(new HumidityTemperatureUnit(temperature, humidity, data[20], data[19], data[18], data[17], data[16], data[15], data[14]));
                                    }
                                } else {
                                    if ((data[0] & 0xFF) == 255) {
                                        inputStream.close();
                                        response.close();
                                        saveLog(logFile);
                                        updating = false;
                                        graphLogListener.humidityTemperatureLog(true, range, humidityTemperatureUnits);
                                        return;
                                    }
                                }
                            }
                            inputStream.close();
                            response.close();
                            saveLog(logFile);
                            updating = false;
                            graphLogListener.humidityTemperatureLog(true, range, humidityTemperatureUnits);
                        } catch (Exception e) {
                            cleanLog();
                            updating = false;
                            graphLogListener.humidityTemperatureLog(false, range, humidityTemperatureUnits);
                        }
                    } else {
                        updating = false;
                        graphLogListener.humidityTemperatureLog(false, 0, null);
                    }
                }
            });
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits = new ArrayList<>();
                    byte[] data = new byte[21];
                    String temp;
                    double temperature;
                    int humidity;

                    for (int b = 0; b < log.length; b += 21) {
                        if (thread.isInterrupted()) {
                            updating = false;
                            Thread.currentThread().interrupt();
                            return;
                        }
                        //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                        System.arraycopy(log, b, data, 0, 21);
                        if ((data[0] & 0xFF) == 1) {
                            if (humidityTemperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == COMMAND_TEMPERATURE_AND_HUMIDITY) {
                                temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                if (temp.charAt(0) == '0') {
                                    temperature = (double) Integer.parseInt(temp, 2) / 10;
                                } else {
                                    temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                }
                                humidity = data[8];
                                humidityTemperatureUnits.add(new HumidityTemperatureUnit(temperature, humidity, data[20], data[19], data[18], data[17], data[16], data[15], data[14]));
                            }
                        } else {
                            if ((data[0] & 0xFF) == 255) {
                                updating = false;
                                graphLogListener.humidityTemperatureLog(true, range, humidityTemperatureUnits);
                                return;
                            }
                        }
                    }
                    updating = false;
                    graphLogListener.humidityTemperatureLog(true, range, humidityTemperatureUnits);
                }
            });
            thread.start();
        }
        return true;
    }

    public boolean getTemperatureForListLog(final TemperatureSensor temperatureSensor) {
        if (updating || listLogListener == null) return false;
        updating = true;

        if (log == null) {
            cleanLog();
            Request request = new Request.Builder()
                    .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException exception) {
                    call.cancel();
                    updating = false;
                    listLogListener.logComplete(false, "Failure in getTemperatureForListLog");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            InputStream inputStream = response.body().byteStream();
                            byte[] data = new byte[21];
                            double block = 0;
                            double entry = 0;
                            String logSize = "[0kb/0kb]";
                            String temp;
                            double temperature;
                            String elapsedTime;

                            while (inputStream.read(data) != -1) {
                                readLog(data);
                                block++;
                                if ((data[0] & 0xFF) == 1) {
                                    if (temperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == 21) {
                                        entry++;

                                        temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                        if (temp.charAt(0) == '0') {
                                            temperature = (double) Integer.parseInt(temp, 2) / 10;
                                        } else {
                                            temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                        }

                                        elapsedTime = time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);

                                        logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * entry / 1024, 21 * block / 1024);

                                        listLogListener.temperatureLog(temperature, String.valueOf(elapsedTime), logSize);
                                    }
                                } else {
                                    if ((data[0] & 0xFF) == 255) {
                                        inputStream.close();
                                        response.close();
                                        saveLog(logFile);
                                        updating = false;
                                        listLogListener.logComplete(true, logSize);
                                        return;
                                    }
                                }
                            }
                            inputStream.close();
                            response.close();
                            saveLog(logFile);
                            updating = false;
                            listLogListener.logComplete(true, logSize);
                        } catch (Exception e) {
                            cleanLog();
                            updating = false;
                            listLogListener.logComplete(false, "Empty response in getTemperatureForListLog");
                        }
                    } else {
                        updating = false;
                        listLogListener.logComplete(false, "Unsuccessful response in getTemperatureForListLog");
                    }
                }
            });
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = new byte[21];
                    double block = 0;
                    double entry = 0;
                    String logSize = "[0kb/0kb]";
                    String temp;
                    double temperature;
                    String elapsedTime;

                    for (int b = 0; b < log.length; b += 21) {
                        if (thread.isInterrupted()) {
                            updating = false;
                            Thread.currentThread().interrupt();
                            return;
                        }
                        //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                        System.arraycopy(log, b, data, 0, 21);
                        //block++;
                        if ((data[0] & 0xFF) == 1) {
                            if (temperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == 21) {

                                temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                if (temp.charAt(0) == '0') {
                                    temperature = (double) Integer.parseInt(temp, 2) / 10;
                                } else {
                                    temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                }

                                elapsedTime = time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);

                                //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                                listLogListener.temperatureLog(temperature, String.valueOf(elapsedTime), logSize);
                            }
                        } else {
                            if ((data[0] & 0xFF) == 255) {
                                updating = false;
                                listLogListener.logComplete(true, logSize);
                                return;
                            }
                        }
                    }
                    updating = false;
                    listLogListener.logComplete(true, logSize);
                }
            });
            thread.start();
        }
        return true;
    }

    public boolean getHumidityTemperatureForListLog(final HumidityTemperatureSensor humidityTemperatureSensor) {
        if (updating || listLogListener == null) return false;
        updating = true;

        if (log == null) {
            cleanLog();
            Request request = new Request.Builder()
                    .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException exception) {
                    call.cancel();
                    updating = false;
                    listLogListener.logComplete(false, "Failure in getHumidityTemperatureForListLog");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            InputStream inputStream = response.body().byteStream();
                            byte[] data = new byte[21];
                            double block = 0;
                            double entry = 0;
                            String logSize = "[0kb/0kb]";
                            String temp;
                            double temperature;
                            int humidity;
                            String elapsedTime;

                            while (inputStream.read(data) != -1) {
                                readLog(data);
                                block++;
                                if ((data[0] & 0xFF) == 1) {
                                    if (humidityTemperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == 21) {
                                        readLog(data);
                                        entry++;

                                        temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                        if (temp.charAt(0) == '0') {
                                            temperature = (double) Integer.parseInt(temp, 2) / 10;
                                        } else {
                                            temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                        }

                                        humidity = data[8] & 0xFF;

                                        elapsedTime = time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);

                                        logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * entry / 1024, 21 * block / 1024);

                                        listLogListener.humidityTemperatureLog(temperature, humidity, String.valueOf(elapsedTime), logSize);
                                    }
                                } else {
                                    if ((data[0] & 0xFF) == 255) {
                                        inputStream.close();
                                        response.close();
                                        saveLog(logFile);
                                        updating = false;
                                        listLogListener.logComplete(true, logSize);
                                        return;
                                    }
                                }
                            }
                            inputStream.close();
                            response.close();
                            saveLog(logFile);
                            updating = false;
                            listLogListener.logComplete(true, logSize);
                        } catch (Exception e) {
                            cleanLog();
                            updating = false;
                            listLogListener.logComplete(false, "Empty response in getHumidityTemperatureForListLog");
                        }
                    } else {
                        updating = false;
                        listLogListener.logComplete(false, "Unsuccessful response in getHumidityTemperatureForListLog");
                    }
                }
            });
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = new byte[21];
                    double block = 0;
                    double entry = 0;
                    String logSize = "[0kb/0kb]";
                    String temp;
                    double temperature;
                    int humidity;
                    String elapsedTime;

                    for (int b = 0; b < log.length; b += 21) {
                        if (thread.isInterrupted()) {
                            updating = false;
                            Thread.currentThread().interrupt();
                            return;
                        }
                        //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                        System.arraycopy(log, b, data, 0, 21);
                        //block++;
                        if ((data[0] & 0xFF) == 1) {
                            if (humidityTemperatureSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == 21) {

                                temp = String.format("%8s%8s", Integer.toBinaryString(data[7] & 0xFF), Integer.toBinaryString(data[6] & 0xFF)).replace(' ', '0').substring(4, 16);
                                if (temp.charAt(0) == '0') {
                                    temperature = (double) Integer.parseInt(temp, 2) / 10;
                                } else {
                                    temperature = (double) (4096 - Integer.parseInt(temp, 2)) / -10;
                                }

                                humidity = data[8] & 0xFF;

                                elapsedTime = time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);

                                //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                                listLogListener.humidityTemperatureLog(temperature, humidity, String.valueOf(elapsedTime), logSize);
                            }
                        } else {
                            if ((data[0] & 0xFF) == 255) {
                                updating = false;
                                listLogListener.logComplete(true, logSize);
                                return;
                            }
                        }
                    }
                    updating = false;
                    listLogListener.logComplete(true, logSize);
                }
            });
            thread.start();
        }
        return true;
    }

    public boolean getMotionLog(final MotionSensor motionSensor) {
        if (updating || listLogListener == null) return false;
        updating = true;

        if (log == null) {
            cleanLog();
            Request request = new Request.Builder()
                    .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException exception) {
                    call.cancel();
                    updating = false;
                    listLogListener.logComplete(false, "Failure in getMotionLog");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            InputStream inputStream = response.body().byteStream();
                            byte[] data = new byte[21];
                            double block = 0;
                            double entry = 0;
                            String logSize = "[0kb/0kb]";
                            String elapsedTime;
                            long currentMoveTime;
                            long previousMoveTime = 0;

                            while (inputStream.read(data) != -1) {
                                readLog(data);
                                block++;
                                if ((data[0] & 0xFF) == TYPE_RX) {
                                    if (motionSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == COMMAND_MOTION) {
                                        entry++;

                                        elapsedTime = time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);

                                        logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * entry / 1024, 21 * block / 1024);

                                        currentMoveTime = getMillisecond(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                                        if (previousMoveTime - currentMoveTime > 5000 || previousMoveTime - currentMoveTime < 0) {
                                            listLogListener.motionLog(elapsedTime, logSize);
                                        }
                                        previousMoveTime = currentMoveTime;
                                    }
                                } else {
                                    if ((data[0] & 0xFF) == 255) {
                                        inputStream.close();
                                        response.close();
                                        saveLog(logFile);
                                        updating = false;
                                        listLogListener.logComplete(true, logSize);
                                        return;
                                    }
                                }
                            }
                            inputStream.close();
                            response.close();
                            saveLog(logFile);
                            updating = false;
                            listLogListener.logComplete(true, logSize);
                        } catch (Exception e) {
                            cleanLog();
                            updating = false;
                            listLogListener.logComplete(false, "Empty response in getMotionLog");
                        }
                    } else {
                        updating = false;
                        listLogListener.logComplete(false, "Unsuccessful response in getMotionLog");
                    }
                }
            });
        } else {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = new byte[21];
                    double block = 0;
                    double entry = 0;
                    String logSize = "[0kb/0kb]";
                    String elapsedTime;
                    long currentMoveTime;
                    long previousMoveTime = 0;

                    for (int b = 0; b < log.length; b += 21) {
                        if (thread.isInterrupted()) {
                            updating = false;
                            Thread.currentThread().interrupt();
                            return;
                        }
                        //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                        System.arraycopy(log, b, data, 0, 21);
                        //block++;
                        if ((data[0] & 0xFF) == TYPE_RX) {
                            if (motionSensor.getChannel() == (data[3] & 0xFF) && (data[4] & 0xFF) == COMMAND_MOTION) {

                                elapsedTime = time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);

                                //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                                currentMoveTime = getMillisecond(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                                if (previousMoveTime - currentMoveTime > 5000 || previousMoveTime - currentMoveTime < 0) {
                                    listLogListener.motionLog(elapsedTime, logSize);
                                }
                                previousMoveTime = currentMoveTime;
                            }
                        } else {
                            if ((data[0] & 0xFF) == 255) {
                                updating = false;
                                listLogListener.logComplete(true, logSize);
                                return;
                            }
                        }
                    }
                    updating = false;
                    listLogListener.logComplete(true, logSize);
                }
            });
            thread.start();
        }
        return true;
    }

    void temporaryOnTX(final int position, final int channel) {
        if (stateListener == null) return;
        Request request = new Request.Builder()
                .url(String.format(Locale.ROOT, Settings.URL() + "send.htm?sd=00000000%s19050100000000000000", getHexString(channel)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                stateListener.onSwitchingTX_Failure("temporaryOnTX #" + channel + " failure\n" + NooLiteF.getStackTrace(exception));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        call.cancel();
                        Thread.sleep(500);
                        Request request = new Request.Builder()
                                .url(Settings.URL().concat(String.format(Locale.ROOT, "send.htm?sd=00000000%s00000000000000000000", getHexString(channel))))
                                .post(RequestBody.create(null, ""))
                                .build();
                        client.newCall(request).execute();
                    } catch (Exception e) {
                        call.cancel();
                        stateListener.onSwitchingTX_Failure("temporaryOnTX #" + channel + " exception\n" + NooLiteF.getStackTrace(e));
                    }
                } else {
                    call.cancel();
                    stateListener.onSwitchingTX_Failure("temporaryOnTX #" + channel + " connection error " + response.code());
                }
            }
        });
    }

    void switchTX(final int position, final int channel) {
        if (stateListener == null) return;
        Request request = new Request.Builder()
                .url(String.format(Locale.ROOT, Settings.URL() + "send.htm?sd=00000000%s04000000000000000000", getHexString(channel)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                stateListener.onSwitchingTX_Failure("switchTX #" + channel + " failure\n" + NooLiteF.getStackTrace(exception));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        call.cancel();
                        stateListener.onSwitchingTX_Ok();
                    } catch (Exception e) {
                        call.cancel();
                        stateListener.onSwitchingTX_Failure("switchTX #" + channel + " exception\n" + NooLiteF.getStackTrace(e));
                    }
                } else {
                    call.cancel();
                    stateListener.onSwitchingTX_Failure("switchTX #" + channel + " connection error " + response.code());
                }
            }
        });
    }

    void switchF_TX(final int position, final int index, final String id) {
        new Thread(new Runnable() {
            public void run() {
                if (stateListener == null) return;

                Request request = new Request.Builder()
                        .url(String.format(Locale.ROOT, Settings.URL() + "send.htm?sd=0002080000040000000000%s", id))
                        .post(RequestBody.create(null, ""))
                        .build();
                Call call = client.newCall(request);

                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        call.cancel();
                        Thread.sleep(Settings.switchTimeout());
                        getPowerUnitFState(position, index);
                    } else {
                        call.cancel();
                        stateListener.onSwitchingF_TX_Failure(position, "switchF_TX #" + id + " connection error " + response.code());
                    }
                } catch (Exception e) {
                    call.cancel();
                    stateListener.onSwitchingF_TX_Failure(position, "switchF_TX #" + id + " exception\n" + NooLiteF.getStackTrace(e));
                }
            }
        }).start();
    }

    void bindF_TX(final String name, final int roomID, final String roomName) {
        updating = true;
        final Request request = new Request.Builder()
                .url(Settings.URL() + "send.htm?sd=010300000000000000000000000000")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                updating = false;
                bindListener.onFailure("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        Thread.sleep(1000);
                        checkBindF_TX(name, roomID, roomName);
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        updating = false;
                        bindListener.onFailure("Что-то пошло не так...");
                    }
                } else {
                    response.close();
                    call.cancel();
                    updating = false;
                    bindListener.onFailure("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void checkBindF_TX(final String name, final int roomID, final String room) throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "bind.htm")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            String hex = response.body().string();
            response.close();
            call.cancel();
            int tmp = Integer.parseInt(hex.substring(15, 17), 16);
            switch (tmp) {
                case 2:
                case 3:
                    String id = hex.substring(21, 29);
                    getF_TX(id, name, roomID, room);
                    break;
                case 4:
                    updating = false;
                    bindListener.onFailure("Нет свободной ячейки для привязки");
                    break;
                default:
                    updating = false;
                    bindListener.onFailure("Устройство не привязано");
            }
        } else {
            response.close();
            call.cancel();
            updating = false;
            bindListener.onFailure("Ошибка соединения при проверке привязки");
        }
    }

    private void getF_TX(final String id, final String name, final int roomID, final String room) throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "device.bin")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            byte[] bytes = response.body().bytes();
            response.close();
            call.cancel();
            for (int index = 0, b = 2054; b < 2566; index++, b += 8) {
                if (id.equals(getHexString(bytes[b] & 0xFF).concat(getHexString(bytes[b + 1] & 0xFF)).concat(getHexString(bytes[b + 2] & 0xFF)).concat(getHexString(bytes[b + 3] & 0xFF)))) {
                    addUnitInUser(new PowerUnitF(id, index, PowerUnitF.OFF, PowerUnitF.OFF, roomID, room, name, 0, false));
                    return;
                }
            }
            response.close();
            call.cancel();
            updating = false;
            bindListener.onFailure("Устройство привязано, но не сохранено в памяти контроллера");
        } else {
            response.close();
            call.cancel();
            updating = false;
            bindListener.onFailure("Ошибка соединения при сохранении устройства");
        }
    }

    void bindTX(final int channel, final int type) {
        updating = true;
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0100%s%02d0000000000000000000000", getHexString(channel), type))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                bindListener.onFailure("Нет соединения...");
                updating = false;
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        updating = false;
                        Thread.sleep(1000);
                        bindListener.onTXbind("Команда привязки отправлена");
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        updating = false;
                        bindListener.onFailure("Что-то пошло не так...");
                    }
                } else {
                    response.close();
                    call.cancel();
                    updating = false;
                    bindListener.onFailure("Ошибка соединения при привязке устройства");
                }
            }
        });
    }

    void saveTX(final String name, final int roomID, final String room, final int channel, final int type) {
        updating = true;
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010600%s00000000%s000000000000", getHexString(channel), getHexString(type)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                updating = false;
                bindListener.onFailure("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        Thread.sleep(1000);
                        addUnitInUser(new PowerUnit(type, channel, 0, roomID, room, name, false));
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        updating = false;
                        bindListener.onFailure("Что-то пошло не так...");
                    }
                } else {
                    response.close();
                    call.cancel();
                    updating = false;
                    bindListener.onFailure("Ошибка соединения при сохранении устройства");
                }
            }
        });
    }

    void bindRX(final String name, final int roomID, final String room) {
        homeActivity.writeAppLog(String.format("Bind RX: Привязка устройства ''%s''. Поиск свободного канала", name));
        byte[] bytes = apiFiles.getDevice();
        String id;
        for (int channel = 0, b = 1030; b < 1542; channel++, b += 8) {
            id = getHexString(bytes[b] & 0xFF).concat(getHexString(bytes[b + 1] & 0xFF)).concat(getHexString(bytes[b + 2] & 0xFF)).concat(getHexString(bytes[b + 3] & 0xFF));
            if (id.equals("FFFFFFFF")) {
                homeActivity.writeAppLog(String.format("Bind RX: Канал %s свободен", channel));
                saveRX(channel, name, roomID, room);
                return;
            }
        }
        homeActivity.writeAppLog("Bind RX: Свободного канала для привязки нет");
        bindListener.onFailure("Нет свободного канала для привязки");
    }

    private void saveRX(final int channel, final String name, final int roomID, final String room) {
        homeActivity.writeAppLog("Bind RX: Включение привязки");
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0101%s000000000000000000000000", NooLiteF.getHexString(channel)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                homeActivity.writeAppLog("Bind RX: Нет соединения");
                bindListener.onFailure("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    response.close();
                    call.cancel();
                    try {
                        int time = 40;
                        do {
                            Thread.sleep(1000);
                            if (checkRXBind(channel)) {
                                homeActivity.writeAppLog("Bind RX: Сохранение данных устройства в user.bin");
                                addUnitInUser(new TemperatureSensor(channel, false, roomID, room, name, .0, -1, -1, -1, -1, -1, -1, -1));
                                return;
                            }
                            time--;
                        } while (time > 0);
                        updating = false;
                        homeActivity.writeAppLog("Bind RX: Время ожидания привязки вышло");
                        bindListener.onFailure("Датчик/пульт не привязан");
                    } catch (Exception e) {
                        updating = false;
                        homeActivity.writeAppLog("Bind RX: Необработанная ошибка\n" + getStackTrace(e));
                        bindListener.onFailure("Что-то пошло не так...");
                    }
                } else {
                    response.close();
                    call.cancel();
                    updating = false;
                    homeActivity.writeAppLog("Bind RX: Ошибка соединения " + response.code());
                    bindListener.onFailure("Ошибка соединения при добавлении датчика/пульта");
                }
            }
        });
    }

    private boolean checkRXBind(final int channel) {
        Request request = new Request.Builder()
                .url(Settings.URL() + "bind.htm")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                String hex = response.body().string();
                Log.d("nooLiteF", hex);
                homeActivity.writeAppLog("Bind RX: Проверка bind.htm: " + hex);
                if (Integer.parseInt(hex.substring(0, 2), 16) == 2 && Integer.parseInt(hex.substring(2, 4), 16) == channel) {
                    String deviceType = "Инкогнито";
                    switch (Integer.parseInt(hex.substring(4, 6), 16)) {
                        case 0: // PU/PB/PG
                            deviceType = "Пульт";
                            break;
                        case 1: // PT112
                            deviceType = "Датчик температуры";
                            break;
                        case 2: // PT111
                            deviceType = "Датчик температуры и влажности";
                            break;
                        case 5: // PM112
                            deviceType = "Датчик движения";
                            break;
                        case 8: // DS-1
                            deviceType = "Датчик отрытия/закрытия";
                            break;
                        case 9: // WS-1
                            deviceType = "Датчик протечки";
                            break;
                        case 10: // PL111
                            deviceType = "Датчик освещенности";
                            break;
                    }
                    homeActivity.writeAppLog(String.format("Bind RX: %s привязан к каналу %s", deviceType, channel));
                    call.cancel();
                    return true;
                } else {
                    homeActivity.writeAppLog("Bind RX: Ожидание привязки к каналу " + channel);
                    call.cancel();
                    return false;
                }
            } else {
                homeActivity.writeAppLog("Bind RX: Проверка bind.htm: ошибка соединения " + response.code());
                call.cancel();
                return false;
            }
        } catch (Exception e) {
            homeActivity.writeAppLog("Bind RX: Проверка bind.htm: нет соединения");
            call.cancel();
            return false;
        }
    }

    private void addUnitInUser(final Object unit) throws IOException {
        byte[] user = apiFiles.getUser();

        byte[] name;

        if (unit instanceof PowerUnit) {
            PowerUnit powerUnit = (PowerUnit) unit;
            name = powerUnit.getName().getBytes("cp1251");
            for (int nb = 0; nb < 32; nb++) {
                if (nb < name.length)
                    user[6 + (34 * powerUnit.getChannel()) + nb] = name[nb];
                else
                    user[6 + (34 * powerUnit.getChannel()) + nb] = 0;
            }
            // set icon
            user[6 + (34 * powerUnit.getChannel()) + 33] = (byte) powerUnit.getRoomID();
        }

        if (unit instanceof TemperatureSensor) {
            TemperatureSensor temperatureSensor = (TemperatureSensor) unit;
            name = temperatureSensor.getName().getBytes("cp1251");
            for (int nb = 0; nb < 32; nb++) {
                if (nb < name.length)
                    user[2182 + (34 * temperatureSensor.getChannel()) + nb] = name[nb];
                else
                    user[2182 + (34 * temperatureSensor.getChannel()) + nb] = 0;
            }
            // set icon
            user[2182 + (34 * temperatureSensor.getChannel()) + 33] = (byte) temperatureSensor.getRoomID();
        }

        if (unit instanceof PowerUnitF) {
            PowerUnitF powerUnitF = (PowerUnitF) unit;
            name = powerUnitF.getName().getBytes("cp1251");
            for (int nb = 0; nb < 32; nb++) {
                if (nb < name.length)
                    user[4358 + (34 * powerUnitF.getIndex()) + nb] = name[nb];
                else
                    user[4358 + (34 * powerUnitF.getIndex()) + nb] = 0;
            }
            // set icon
            user[4358 + (34 * powerUnitF.getIndex()) + 33] = (byte) powerUnitF.getRoomID();
        }

        upload(new String(user, "cp1251"));
    }

    private void upload(String file) throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"i\"; filename=\"file.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(file)
                .concat("\r\n\r\n\r\n");
        Request request = new Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.getBytes("cp1251")))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            updating = false;
            homeActivity.writeAppLog("Устройство успешно добавлено");
            bindListener.onSuccess("Устройство успешно добавлено");
        } else {
            response.close();
            call.cancel();
            updating = false;
            homeActivity.writeAppLog("Ошибка соединения при сохранении устройства");
            bindListener.onFailure("Ошибка соединения при сохранении устройства");
        }
    }

    public void readLog(byte[] data) {
        for (int db = 0; db < data.length; db++) {
            logFile.append(data[db]).append("_");
        }
    }

    public void saveLog(StringBuilder logFile) {
        String[] logBytes = logFile.toString().split("_");
        log = new byte[logBytes.length];
        for (int lb = 0; lb < log.length; lb++) {
            log[lb] = Byte.parseByte(logBytes[lb]);
        }
    }

    public StringBuilder getFileLog() {
        return logFile;
    }

    public byte[] getLog() {
        return log;
    }

    public void setLog(byte[] log) {
        this.log = log;
    }

    public void cleanLog() {
        logFile = new StringBuilder();
        log = null;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void interruptThread() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
