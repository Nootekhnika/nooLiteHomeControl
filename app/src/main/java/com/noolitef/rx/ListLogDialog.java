package com.noolitef.rx;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.R;
import com.noolitef.Room;
import com.noolitef.UnitSettingsFragment;
import com.noolitef.UnitSettingsFragmentListener;
import com.noolitef.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListLogDialog extends DialogFragment {
    private HomeActivity homeActivity;
    private NooLiteF nooLiteF;
    private OkHttpClient client;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private Object sensor;
    private RemoteController remoteController;
    private TemperatureSensor temperatureSensor;
    private HumidityTemperatureSensor humidityTemperatureSensor;
    private MotionSensor motionSensor;
    private OpenCloseSensor openCloseSensor;
    private LeakDetector leakDetector;
    private LightSensor lightSensor;
    private Thread thread;

    private TextView textRoom;
    private TextView textName;
    private TextView logSizeLabel;
    private ProgressBar progressBar;
    private ImageButton settingsButton;
    private ListView logListView;

    private ArrayList<LogListViewItem> logItems;
    private SimpleAdapter adapter;
    private String logSize;

    public ListLogDialog() {
    }

    public void send(NooLiteF nooLiteF, OkHttpClient client, byte[] device, byte[] user, ArrayList<Room> rooms, Object sensor) {
        this.client = client;
        this.device = device;
        this.user = user;
        this.rooms = rooms;
        this.nooLiteF = nooLiteF;
        this.sensor = sensor;
        if (sensor instanceof RemoteController) {
            remoteController = (RemoteController) sensor;
        }
        if (sensor instanceof TemperatureSensor) {
            temperatureSensor = (TemperatureSensor) sensor;
        }
        if (sensor instanceof HumidityTemperatureSensor) {
            humidityTemperatureSensor = (HumidityTemperatureSensor) sensor;
        }
        if (sensor instanceof MotionSensor) {
            motionSensor = (MotionSensor) sensor;
        }
        if (sensor instanceof OpenCloseSensor) {
            openCloseSensor = (OpenCloseSensor) sensor;
        }
        if (sensor instanceof LeakDetector) {
            leakDetector = (LeakDetector) sensor;
        }
        if (sensor instanceof LightSensor) {
            lightSensor = (LightSensor) sensor;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        homeActivity = (HomeActivity) getActivity();

        setListLogListener();

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialog;
        if (Settings.isNightMode()) {
            dialog = inflater.inflate(R.layout.dialog_log_dark, null);
        } else {
            dialog = inflater.inflate(R.layout.dialog_log, null);
        }
        textRoom = (TextView) dialog.findViewById(R.id.dialog_log_room);
        textName = (TextView) dialog.findViewById(R.id.dialog_log_name);
        logSizeLabel = (TextView) dialog.findViewById(R.id.dialog_log_size);
        progressBar = (ProgressBar) dialog.findViewById(R.id.dialog_log_progress_bar);
        //progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
        settingsButton = (ImageButton) dialog.findViewById(R.id.dialog_log_button_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UnitSettingsFragment unitSettingsFragment = (UnitSettingsFragment) getChildFragmentManager().findFragmentByTag("UNIT_SETTINGS_DIALOG");
                if (unitSettingsFragment == null) {
                    unitSettingsFragment = new UnitSettingsFragment();
                    unitSettingsFragment.setUnitSettingsFragmentListener(new UnitSettingsFragmentListener() {
                        @Override
                        public void onDismiss(final boolean unbind, final boolean update, final String room, final String name) {
                            if (!unbind) {
                                if (update) {
                                    homeActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textRoom.setText(room);
                                            textName.setText(name);
                                            homeActivity.showProgressBar();
                                            homeActivity.hardUpdatePRF64();
                                        }
                                    });
                                }
                            } else {
                                homeActivity.showProgressBar();
                                homeActivity.hardUpdatePRF64();
                                dismiss();
                            }
                        }
                    });
                }
                if (unitSettingsFragment.isAdded()) return;
                unitSettingsFragment.send(client, device, user, rooms, sensor);
                getChildFragmentManager().beginTransaction().add(unitSettingsFragment, "UNIT_SETTINGS_DIALOG").show(unitSettingsFragment).commit();
            }
        });
        logListView = (ListView) dialog.findViewById(R.id.dialog_log_list_view);

        progressBar.setVisibility(View.VISIBLE);

        if (savedInstanceState == null) {
            logItems = new ArrayList<>();
        } else {
            logSizeLabel.setText(logSize);
            progressBar.setVisibility(View.INVISIBLE);
        }

        int listItemResource;
        if (remoteController != null) {
            textRoom.setText(remoteController.getRoom());
            textName.setText(remoteController.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_remote_controller_event_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_remote_controller_event;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.DATA1, LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_command, R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            if (nooLiteF.getLog() == null) {
                getLog(remoteController);
            } else {
                readRemoteControllerLog(remoteController);
                disableProgressBar();
            }
        }
        if (temperatureSensor != null) {
            textRoom.setText(temperatureSensor.getRoom());
            textName.setText(temperatureSensor.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_temperature_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_temperature;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.DATA1, LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_temperature, R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            nooLiteF.getTemperatureForListLog(temperatureSensor);
        }
        if (humidityTemperatureSensor != null) {
            textRoom.setText(humidityTemperatureSensor.getRoom());
            textName.setText(humidityTemperatureSensor.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_humidity_temperature_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_humidity_temperature;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.DATA1, LogListViewItem.DATA2, LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_temperature, R.id.log_list_view_item_humidity, R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            nooLiteF.getHumidityTemperatureForListLog(humidityTemperatureSensor);
        }
        if (motionSensor != null) {
            textRoom.setText(motionSensor.getRoom());
            textName.setText(motionSensor.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_motion_event_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_motion_event;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            nooLiteF.getMotionLog(motionSensor);
        }
        if (openCloseSensor != null) {
            textRoom.setText(openCloseSensor.getRoom());
            textName.setText(openCloseSensor.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_open_close_event_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_open_close_event;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.DATA1, LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_state, R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            if (nooLiteF.getLog() == null) {
                getLog(openCloseSensor);
            } else {
                readOpenCloseLog(openCloseSensor);
                disableProgressBar();
            }
        }
        if (leakDetector != null) {
            textRoom.setText(leakDetector.getRoom());
            textName.setText(leakDetector.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_leak_event_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_leak_event;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.DATA1, LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_state, R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            if (nooLiteF.getLog() == null) {
                getLog(leakDetector);
            } else {
                readLeakageLog(leakDetector);
                disableProgressBar();
            }
        }
        if (lightSensor != null) {
            textRoom.setText(lightSensor.getRoom());
            textName.setText(lightSensor.getName());
            if (Settings.isNightMode()) {
                listItemResource = R.layout.log_list_view_item_light_event_grey;
            } else {
                listItemResource = R.layout.log_list_view_item_light_event;
            }
            adapter = new SimpleAdapter(getContext(), logItems, listItemResource, new String[]{LogListViewItem.DATA1, LogListViewItem.TIME}, new int[]{R.id.log_list_view_item_state, R.id.log_list_view_item_time});
            logListView.setAdapter(adapter);
            if (nooLiteF.getLog() == null) {
                getLog(lightSensor);
            } else {
                readLightLog(lightSensor);
                disableProgressBar();
            }
        }

        getDialog().setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (nooLiteF != null) {
            nooLiteF.interruptThread();
        }
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        super.onDismiss(dialog);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private void setListLogListener() {
        if (nooLiteF == null) { // костыль для корректного сворачивания
            dismiss();
            return;
        }
        nooLiteF.setListLogListener(new ListLogListener() {
            @Override
            public void temperatureLog(final double temperature, final String elapsedTime, final String logSize) {
                if (isAdded()) {
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //logSizeLabel.setText(logSize);
                            logItems.add(new LogListViewItem(String.format(Locale.ROOT, "%.1f°C", temperature), "", elapsedTime));
                            ((SimpleAdapter) logListView.getAdapter()).notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void humidityTemperatureLog(final double temperature, final int humidity, final String elapsedTime, final String logSize) {
                if (isAdded()) {
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //logSizeLabel.setText(logSize);
                            logItems.add(new LogListViewItem(String.format(Locale.ROOT, "%.1f°C", temperature), String.format(Locale.ROOT, "%d%%", humidity), elapsedTime));
                            ((SimpleAdapter) logListView.getAdapter()).notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void motionLog(final String elapsedTime, final String logSize) {
                if (isAdded()) {
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //logSizeLabel.setText(logSize);
                            logItems.add(new LogListViewItem("", "", elapsedTime));
                            ((SimpleAdapter) logListView.getAdapter()).notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void logComplete(final boolean successfully, final String message) {
                if (isAdded()) {
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //logSize = message;
                            //logSizeLabel.setText(logSize);
                            progressBar.setVisibility(View.INVISIBLE);
                            if (successfully) {
                                //Toast.makeText(getDialog().getContext(), "Готово", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getDialog().getContext(), "Ошибка при загрузке лога...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void getLog(final Object sensor) {
        Request request = new Request.Builder()
                .url(String.format(Locale.ROOT, Settings.URL() + "log.bin"))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                disableProgressBar();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        if (remoteController != null) {
                            parseRemoteControllerLog(response, remoteController);
                            response.close();
                            call.cancel();
                            disableProgressBar();
                        }
                        if (openCloseSensor != null) {
                            parseOpenCloseLog(response, openCloseSensor);
                            response.close();
                            call.cancel();
                            disableProgressBar();
                        }
                        if (leakDetector != null) {
                            parseLeakageLog(response, leakDetector);
                            response.close();
                            call.cancel();
                            disableProgressBar();
                        }
                        if (lightSensor != null) {
                            parseLightLog(response, lightSensor);
                            response.close();
                            call.cancel();
                            disableProgressBar();
                        }
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        nooLiteF.cleanLog();
                        disableProgressBar();
                        homeActivity.writeAppLog("Ошибка при обработке данных..." + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
                        showToast("Ошибка при обработке данных...");
                    }
                } else {
                    response.close();
                    call.cancel();
                    disableProgressBar();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void parseRemoteControllerLog(final Response response, final RemoteController remoteController) throws IOException {
        InputStream inputStream = response.body().byteStream();
        byte[] data = new byte[21];
        double block = 0;
        double entry = 0;
        String logSize = "";
        String eventTime;
        String command;

        while (inputStream.read(data) != -1) {
            nooLiteF.readLog(data);
            //block++;
            if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                if (remoteController.getChannel() == (data[3] & 0xFF)) {
                    //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                    command = Integer.toString(data[4] & 0xFF);
                    eventTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                    showLogItem(logSize, command, null, eventTime);
                }
            } else {
                if ((data[0] & 0xFF) == 255) {
                    inputStream.close();
                    nooLiteF.saveLog(nooLiteF.getFileLog());
                    return;
                }
            }
        }
        inputStream.close();
        nooLiteF.saveLog(nooLiteF.getFileLog());
    }

    private void readRemoteControllerLog(final RemoteController remoteController) {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] log = nooLiteF.getLog();
                byte[] data = new byte[21];
                double block = 0;
                double entry = 0;
                final String logSize = "";
                String eventTime;
                String command;

                for (int b = 0; b < log.length; b += 21) {
                    if (thread.isInterrupted()) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                    System.arraycopy(log, b, data, 0, 21);
                    //block++;
                    if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                        if (remoteController.getChannel() == (data[3] & 0xFF)) {
                            //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                            command = Integer.toString(data[4] & 0xFF);
                            eventTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                            showLogItem(logSize, command, null, eventTime);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private void parseOpenCloseLog(final Response response, final OpenCloseSensor openCloseSensor) throws IOException {
        InputStream inputStream = response.body().byteStream();
        byte[] data = new byte[21];
        double block = 0;
        double entry = 0;
        String logSize = "";
        String currentTime;
        String nextTime = "";
        int currentState;
        int nextState = -1;
        String[] state = {"Закрыто", "", "        Открыто"};

        while (inputStream.read(data) != -1) {
            nooLiteF.readLog(data);
            //block++;
            if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                if (openCloseSensor.getChannel() == (data[3] & 0xFF) && ((data[4] & 0xFF) == 0 || (data[4] & 0xFF) == 2)) {
                    //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                    currentState = data[4] & 0xFF;
                    currentTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                    if (currentState != nextState && nextState != -1) {
                        showLogItem(logSize, state[nextState], null, nextTime);
                    }
                    nextState = currentState;
                    nextTime = currentTime;
                }
            } else {
                if ((data[0] & 0xFF) == 255) {
                    if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
                    inputStream.close();
                    nooLiteF.saveLog(nooLiteF.getFileLog());
                    return;
                }
            }
        }
        if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
        inputStream.close();
        nooLiteF.saveLog(nooLiteF.getFileLog());
    }

    private void readOpenCloseLog(final OpenCloseSensor openCloseSensor) {

        thread = new Thread(new Runnable() {
            byte[] log = nooLiteF.getLog();
            byte[] data = new byte[21];
            double block = 0;
            double entry = 0;
            String logSize = "";
            String currentTime;
            String nextTime = "";
            int currentState;
            int nextState = -1;
            String[] state = {"Закрыто", "", "        Открыто"};

            @Override
            public void run() {
                for (int b = 0; b < log.length; b += 21) {
                    if (thread.isInterrupted()) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                    System.arraycopy(log, b, data, 0, 21);
                    //block++;
                    if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                        if (openCloseSensor.getChannel() == (data[3] & 0xFF) && ((data[4] & 0xFF) == 0 || (data[4] & 0xFF) == 2)) {
                            //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                            currentState = data[4] & 0xFF;
                            currentTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                            if (currentState != nextState && nextState != -1) {
                                showLogItem(logSize, state[nextState], null, nextTime);
                            }
                            nextState = currentState;
                            nextTime = currentTime;
                        }
                    } else {
                        if ((data[0] & 0xFF) == 255) {
                            if (nextState != -1)
                                showLogItem(logSize, state[nextState], null, nextTime);
                            //nooLiteF.saveLog(nooLiteF.getFileLog());
                            return;
                        }
                    }
                }
                if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
            }
        });
        thread.start();
    }

    private void parseLeakageLog(final Response response, final LeakDetector leakDetector) throws IOException {
        InputStream inputStream = response.body().byteStream();
        byte[] data = new byte[21];
        double block = 0;
        double entry = 0;
        String logSize = "";
        String currentTime;
        String nextTime = "";
        int currentState;
        int nextState = -1;
        String[] state = {"Сухо", "", "        Протечка!"};

        while (inputStream.read(data) != -1) {
            nooLiteF.readLog(data);
            //block++;
            if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                if (leakDetector.getChannel() == (data[3] & 0xFF) && ((data[4] & 0xFF) == 0 || (data[4] & 0xFF) == 2)) {
                    //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                    currentState = (data[4] & 0xFF);
                    currentTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                    if (currentState != nextState && nextState != -1) {
                        showLogItem(logSize, state[nextState], null, nextTime);
                    }
                    nextState = currentState;
                    nextTime = currentTime;
                }
            } else {
                if ((data[0] & 0xFF) == 255) {
                    if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
                    inputStream.close();
                    nooLiteF.saveLog(nooLiteF.getFileLog());
                    return;
                }
            }
        }
        if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
        inputStream.close();
        nooLiteF.saveLog(nooLiteF.getFileLog());
    }

    private void readLeakageLog(final LeakDetector leakDetector) {

        thread = new Thread(new Runnable() {
            byte[] log = nooLiteF.getLog();
            byte[] data = new byte[21];
            double block = 0;
            double entry = 0;
            String logSize = "";
            String currentTime;
            String nextTime = "";
            int currentState;
            int nextState = -1;
            String[] state = {"Сухо", "", "        Протечка!"};

            @Override
            public void run() {
                for (int b = 0; b < log.length; b += 21) {
                    if (thread.isInterrupted()) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                    System.arraycopy(log, b, data, 0, 21);
                    //block++;
                    if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                        if (leakDetector.getChannel() == (data[3] & 0xFF) && ((data[4] & 0xFF) == 0 || (data[4] & 0xFF) == 2)) {
                            //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                            currentState = (data[4] & 0xFF);
                            currentTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                            if (currentState != nextState && nextState != -1) {
                                showLogItem(logSize, state[nextState], null, nextTime);
                            }
                            nextState = currentState;
                            nextTime = currentTime;
                        }
                    } else {
                        if ((data[0] & 0xFF) == 255) {
                            if (nextState != -1)
                                showLogItem(logSize, state[nextState], null, nextTime);
                            //nooLiteF.saveLog(nooLiteF.getFileLog());
                            return;
                        }
                    }
                }
                if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
            }
        });
        thread.start();
    }

    private void parseLightLog(final Response response, final LightSensor lightSensor) throws IOException {
        InputStream inputStream = response.body().byteStream();
        byte[] data = new byte[21];
        double block = 0;
        double entry = 0;
        String logSize = "";
        String currentTime;
        String nextTime = "";
        int currentState;
        int nextState = -1;
        String[] state = {"Светло", "", "        Темно"};

        while (inputStream.read(data) != -1) {
            nooLiteF.readLog(data);
            //block++;
            if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                if (lightSensor.getChannel() == (data[3] & 0xFF) && ((data[4] & 0xFF) == 0 || (data[4] & 0xFF) == 2)) {
                    //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                    currentState = data[4] & 0xFF;
                    currentTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                    if (currentState != nextState && nextState != -1) {
                        showLogItem(logSize, state[nextState], null, nextTime);
                    }
                    nextState = currentState;
                    nextTime = currentTime;
                }
            } else {
                if ((data[0] & 0xFF) == 255) {
                    if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
                    inputStream.close();
                    nooLiteF.saveLog(nooLiteF.getFileLog());
                    return;
                }
            }
        }
        if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
        inputStream.close();
        nooLiteF.saveLog(nooLiteF.getFileLog());
    }

    private void readLightLog(final LightSensor lightSensor) {

        thread = new Thread(new Runnable() {
            byte[] log = nooLiteF.getLog();
            byte[] data = new byte[21];
            double block = 0;
            double entry = 0;
            String logSize = "";
            String currentTime;
            String nextTime = "";
            int currentState;
            int nextState = -1;
            String[] state = {"Светло", "", "        Темно"};

            @Override
            public void run() {
                for (int b = 0; b < log.length; b += 21) {
                    if (thread.isInterrupted()) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //for (int db = 0; db < 21; db++) data[db] = log[b + db];
                    System.arraycopy(log, b, data, 0, 21);
                    //block++;
                    if ((data[0] & 0xFF) == NooLiteF.TYPE_RX) {
                        if (lightSensor.getChannel() == (data[3] & 0xFF) && ((data[4] & 0xFF) == 0 || (data[4] & 0xFF) == 2)) {
                            //logSize = String.format(Locale.ROOT, "[%.3fkb/%.3fkb]", 21 * ++entry / 1024, 21 * block / 1024);

                            currentState = data[4] & 0xFF;
                            currentTime = NooLiteF.time(data[20] & 0xFF, data[19] & 0xFF, data[18] & 0xFF, data[17] & 0xFF, data[16] & 0xFF, data[15] & 0xFF, data[14] & 0xFF);
                            if (currentState != nextState && nextState != -1) {
                                showLogItem(logSize, state[nextState], null, nextTime);
                            }
                            nextState = currentState;
                            nextTime = currentTime;
                        }
                    } else {
                        if ((data[0] & 0xFF) == 255) {
                            if (nextState != -1)
                                showLogItem(logSize, state[nextState], null, nextTime);
                            //nooLiteF.saveLog(nooLiteF.getFileLog());
                            return;
                        }
                    }
                }
                if (nextState != -1) showLogItem(logSize, state[nextState], null, nextTime);
            }
        });
        thread.start();
    }

    private void showLogItem(final String logSize, final String data1, final String data2, final String time) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logSizeLabel.setText(logSize);
                logItems.add(new LogListViewItem(data1, data2, time));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void disableProgressBar() {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
