package com.noolitef;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.ftx.ThermostatSensorSelectorFragment;
import com.noolitef.ftx.FTXUnitSettingsFragment;
import com.noolitef.rx.HumidityTemperatureSensor;
import com.noolitef.rx.LeakDetector;
import com.noolitef.rx.LightSensor;
import com.noolitef.rx.MotionSensor;
import com.noolitef.rx.OpenCloseSensor;
import com.noolitef.rx.RemoteController;
import com.noolitef.rx.TemperatureSensor;
import com.noolitef.settings.Settings;
import com.noolitef.tx.PowerUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UnitSettingsFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private HomeActivity homeActivity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private OkHttpClient client;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private PowerUnit powerUnit;
    private RemoteController remoteController;
    private TemperatureSensor temperatureSensor;
    private HumidityTemperatureSensor humidityTemperatureSensor;
    private MotionSensor motionSensor;
    private OpenCloseSensor openCloseSensor;
    private LeakDetector leakDetector;
    private LightSensor lightSensor;
    private PowerUnitF powerUnitF;
    private PowerSocketF powerSocketF;
    private Thermostat thermostat;
    private RolletUnitF rolletUnitF;

    private Button buttonBack;
    private Button buttonSave;
    private TextView textType;
    private EditText editName;
    private Spinner spinnerRoom;
    private LinearLayout layoutDeviceType;
    private Spinner spinnerDeviceType;
    private LinearLayout layoutAddress;
    private TextView textAddress;
    private TextView editAddress;
    private LinearLayout layoutInvert;
    private CheckBox checkBoxInvert;
    private LinearLayout layoutOtherSettings;
    private Button buttonDimmingSettings;
    private LinearLayout layoutSensorSelection;
    private Button buttonSensorSelection;
    private Button buttonBindingMode;
    private Button buttonUnbindingMode;
    private Button buttonServiceModeOn;
    private Button buttonServiceModeOff;
    private Button buttonBootMode;
    private Button buttonRemove;
    private LinearLayout layoutUnbind;
    private TextView textInstructions;
    private Button buttonUnbind;
    private LinearLayout layoutTX;
    private Button buttonSwitch;
    private Button buttonDelete;
    private GUIBlockFragment guiBlockFragment;

    private ArrayAdapter deviceTypeAdapter;

    private String name;
    private int roomID;
    private int deviceType;
    private boolean changeUnitType; // костыль для изменения типа устройства в device.bin
    private int channel;
    private String id;
    private String addressType;
    private String address;

    private int serviceMode;

    private UnitSettingsFragmentListener unitSettingsFragmentListener;
    private boolean unbind;
    private boolean update;

    public UnitSettingsFragment() {
    }

    public void send(OkHttpClient client, byte[] device, byte[] user, ArrayList<Room> rooms, Object unit) {
        this.client = client;
        this.device = device;
        this.user = user;
        this.rooms = rooms;
        this.deviceType = 0;
        this.channel = 0;
        this.id = "00000000";
        if (unit instanceof PowerUnit) {
            powerUnit = (PowerUnit) unit;
            name = powerUnit.getName();
            roomID = powerUnit.getRoomID();
            deviceType = powerUnit.getType();
            addressType = "Номер передающего канала";
            channel = powerUnit.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof RemoteController) {
            remoteController = (RemoteController) unit;
            name = remoteController.getName();
            roomID = remoteController.getRoomID();
            addressType = "Номер принимающего канала";
            channel = remoteController.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof TemperatureSensor) {
            temperatureSensor = (TemperatureSensor) unit;
            name = temperatureSensor.getName();
            roomID = temperatureSensor.getRoomID();
            addressType = "Номер принимающего канала";
            channel = temperatureSensor.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof HumidityTemperatureSensor) {
            humidityTemperatureSensor = (HumidityTemperatureSensor) unit;
            name = humidityTemperatureSensor.getName();
            roomID = humidityTemperatureSensor.getRoomID();
            addressType = "Номер принимающего канала";
            channel = humidityTemperatureSensor.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof MotionSensor) {
            motionSensor = (MotionSensor) unit;
            name = motionSensor.getName();
            roomID = motionSensor.getRoomID();
            addressType = "Номер принимающего канала";
            channel = motionSensor.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof OpenCloseSensor) {
            openCloseSensor = (OpenCloseSensor) unit;
            name = openCloseSensor.getName();
            roomID = openCloseSensor.getRoomID();
            addressType = "Номер принимающего канала";
            channel = openCloseSensor.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof LeakDetector) {
            leakDetector = (LeakDetector) unit;
            name = leakDetector.getName();
            roomID = leakDetector.getRoomID();
            addressType = "Номер принимающего канала";
            channel = leakDetector.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof LightSensor) {
            lightSensor = (LightSensor) unit;
            name = lightSensor.getName();
            roomID = lightSensor.getRoomID();
            addressType = "Номер принимающего канала";
            channel = lightSensor.getChannel();
            address = String.valueOf(channel + 1);
        }
        if (unit instanceof PowerUnitF) {
            powerUnitF = (PowerUnitF) unit;
            name = powerUnitF.getName();
            roomID = powerUnitF.getRoomID();
            addressType = "ID устройства";
            id = powerUnitF.getId();
            address = id;
        }
        if (unit instanceof PowerSocketF) {
            powerSocketF = (PowerSocketF) unit;
            name = powerSocketF.getName();
            roomID = powerSocketF.getRoomID();
            addressType = "ID устройства";
            id = powerSocketF.getId();
            address = id;
        }
        if (unit instanceof Thermostat) {
            thermostat = (Thermostat) unit;
            name = thermostat.getName();
            roomID = thermostat.getRoomID();
            addressType = "ID устройства";
            id = thermostat.getId();
            address = id;
        }
        if (unit instanceof RolletUnitF) {
            rolletUnitF = (RolletUnitF) unit;
            name = rolletUnitF.getName();
            roomID = rolletUnitF.getRoomID();
            addressType = "ID устройства";
            id = rolletUnitF.getId();
            address = id;
        }
    }

    public void setUnitSettingsFragmentListener(UnitSettingsFragmentListener listener) {
        unitSettingsFragmentListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(true);
        setRetainInstance(true);

        homeActivity = (HomeActivity) getActivity();
        sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View dialogView = inflater.inflate(R.layout.fragment_settings_unit, null);

        buttonBack = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_back);
        buttonBack.setOnClickListener(this);
        buttonSave = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_save);
        buttonSave.setOnClickListener(this);
        textType = (TextView) dialogView.findViewById(R.id.fragment_settings_unit_text_type);
        editName = (EditText) dialogView.findViewById(R.id.fragment_settings_unit_edit_name);
        editName.setText(name);
        spinnerRoom = (Spinner) dialogView.findViewById(R.id.fragment_settings_unit_spinner_room);
        if (rooms == null) rooms = new ArrayList<>();
        if (rooms.size() > 0) {
            if (rooms.get(rooms.size() - 1).getId() != -1) {
                rooms.add(new Room(-1, ""));
            }
        } else {
            rooms.add(new Room(-1, ""));
        }
        String[] roomsNames = new String[rooms.size()];
        for (int r = 0; r < rooms.size(); r++) {
            roomsNames[r] = rooms.get(r).getName();
        }
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roomsNames);
        spinnerRoom.setAdapter(roomAdapter);
        if (roomID != 255) {
            for (int r = 0; r < rooms.size(); r++) {
                if (rooms.get(r).getId() == roomID) {
                    spinnerRoom.setSelection(r);
                }
            }
        } else {
            spinnerRoom.setSelection(rooms.size() - 1);
        }
        layoutDeviceType = (LinearLayout) dialogView.findViewById(R.id.fragment_settings_unit_layout_device_type);
        spinnerDeviceType = (Spinner) dialogView.findViewById(R.id.fragment_settings_unit_spinner_device_type);
        if (powerUnit != null) {
            layoutDeviceType.setVisibility(View.VISIBLE);
            String[] stringArray = homeActivity.getResources().getStringArray(R.array.deviceType);
            String[] types = new String[5];
            for (int d = 0; d < types.length; d++) types[d] = stringArray[d + 1];
            deviceTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types);
            spinnerDeviceType.setAdapter(deviceTypeAdapter);
            switch (powerUnit.getType()) {
                case PowerUnit.DIMMER:
                    spinnerDeviceType.setSelection(0);
                    break;
                case PowerUnit.RELAY:
                    spinnerDeviceType.setSelection(1);
                    break;
                case PowerUnit.PULSE_RELAY:
                    spinnerDeviceType.setSelection(2);
                    break;
                case PowerUnit.RGB_CONTROLLER:
                    spinnerDeviceType.setSelection(3);
                    break;
                case PowerUnit.ROLLET:
                    spinnerDeviceType.setSelection(4);
                    break;
            }
            spinnerDeviceType.setOnItemSelectedListener(this);
        }
        layoutAddress = dialogView.findViewById(R.id.fragment_settings_unit_layout_address);
        textAddress = dialogView.findViewById(R.id.fragment_settings_unit_text_address);
        textAddress.setText(addressType);
        editAddress = dialogView.findViewById(R.id.fragment_settings_unit_edit_address);
        editAddress.setText(address);
        layoutInvert = dialogView.findViewById(R.id.fragment_settings_unit_layout_invert);
        checkBoxInvert = dialogView.findViewById(R.id.fragment_settings_unit_checkbox_invert);
        if (powerUnit != null && powerUnit.getType() == PowerUnit.ROLLET) {
            if (sharedPreferences.getBoolean(Integer.toString(channel), false)) {
                checkBoxInvert.setChecked(true);
            }
        }
        if (rolletUnitF != null) {
            if (sharedPreferences.getBoolean(rolletUnitF.getId(), false)) {
                checkBoxInvert.setChecked(true);
            }
        }
        layoutOtherSettings = dialogView.findViewById(R.id.fragment_settings_unit_layout_others);
        buttonDimmingSettings = dialogView.findViewById(R.id.fragment_settings_unit_button_others);
        buttonDimmingSettings.setOnClickListener(this);
        layoutSensorSelection = dialogView.findViewById(R.id.fragment_settings_unit_layout_sensor);
        buttonSensorSelection = dialogView.findViewById(R.id.fragment_settings_unit_button_sensor);
        buttonSensorSelection.setOnClickListener(this);
        buttonBindingMode = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_binding_mode);
        buttonBindingMode.setOnClickListener(this);
        buttonUnbindingMode = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_unbinding_mode);
        buttonUnbindingMode.setOnClickListener(this);
        buttonServiceModeOn = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_service_mode_on);
        buttonServiceModeOn.setOnClickListener(this);
        buttonServiceModeOff = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_service_mode_off);
        buttonServiceModeOff.setOnClickListener(this);
        buttonBootMode = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_boot_mode);
        buttonBootMode.setOnClickListener(this);
        buttonRemove = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_remove);
        buttonRemove.setOnClickListener(this);
        layoutUnbind = (LinearLayout) dialogView.findViewById(R.id.fragment_settings_unit_layout_unbind);
        textInstructions = (TextView) dialogView.findViewById(R.id.fragment_settings_unit_text_instructions);
        buttonUnbind = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_unbind);
        buttonUnbind.setOnClickListener(this);
        layoutTX = (LinearLayout) dialogView.findViewById(R.id.fragment_settings_unit_layout_tx);
        buttonSwitch = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_switch);
        buttonSwitch.setOnClickListener(this);
        buttonDelete = (Button) dialogView.findViewById(R.id.fragment_settings_unit_button_delete);
        buttonDelete.setOnClickListener(this);

        if (powerUnit != null) {
            textType.setText("блока nooLite");
            if (powerUnit.getType() == PowerUnit.ROLLET) layoutInvert.setVisibility(View.VISIBLE);
            buttonBindingMode.setVisibility(View.VISIBLE);
            buttonUnbindingMode.setVisibility(View.VISIBLE);
            textInstructions.setText("Для отвязки блока от контроллера нажмите ''ОТВЯЗАТЬ'', затем сервисную кнопку на блоке");
        }
        if (remoteController != null) {
            textType.setText("пульта");
            textInstructions.setText("Для отвязки пульта от контроллера нажмите ''ОТВЯЗАТЬ''");
        }
        if (temperatureSensor != null || humidityTemperatureSensor != null || motionSensor != null || openCloseSensor != null || leakDetector != null || lightSensor != null) {
            textType.setText("датчика");
            textInstructions.setText("Для отвязки датчика от контроллера нажмите ''ОТВЯЗАТЬ''");
        }
        if (powerUnitF != null || powerSocketF != null || thermostat != null || rolletUnitF != null) {
            textType.setText("блока nooLite-F");
            if (powerUnitF != null && powerUnitF.isDimmer())
                layoutOtherSettings.setVisibility(View.VISIBLE);
            if (thermostat != null)
                layoutSensorSelection.setVisibility(View.VISIBLE);
            if (rolletUnitF != null)
                layoutInvert.setVisibility(View.VISIBLE);
            buttonServiceModeOn.setVisibility(View.VISIBLE);
            buttonServiceModeOff.setVisibility(View.VISIBLE);
            //buttonBootMode.setVisibility(View.VISIBLE);
            textInstructions.setText("Для отвязки устройства от контроллера переведите блок в режим отвязки, затем нажмите ''ОТВЯЗАТЬ''");
        }

        return dialogView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window fragmentWindow = getDialog().getWindow();
        WindowManager.LayoutParams dialogParams = fragmentWindow.getAttributes();
        dialogParams.dimAmount = 0.75f;
        fragmentWindow.setAttributes(dialogParams);
        fragmentWindow.setBackgroundDrawableResource(R.color.transparent);

        DisplayMetrics display = new DisplayMetrics();
        homeActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        int displayWidth = display.widthPixels;
        int displayHeight = display.heightPixels;
        if (displayWidth < displayHeight) {
            fragmentWindow.setLayout(displayWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            fragmentWindow.setLayout(displayHeight, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 4) layoutInvert.setVisibility(View.VISIBLE);
        else layoutInvert.setVisibility(View.GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_settings_unit_button_back:
                dismiss();
                break;
            case R.id.fragment_settings_unit_button_save:
                if (powerUnit != null) {
                    int type = 0;
                    switch (spinnerDeviceType.getSelectedItemPosition()) {
                        case 0:
                            type = PowerUnit.DIMMER;
                            break;
                        case 1:
                            type = PowerUnit.RELAY;
                            break;
                        case 2:
                            type = PowerUnit.PULSE_RELAY;
                            break;
                        case 3:
                            type = PowerUnit.RGB_CONTROLLER;
                            break;
                        case 4:
                            type = PowerUnit.ROLLET;
                            break;
                    }
                    if (deviceType != type) {
                        powerUnit.setType(type);
                        changeUnitType = true;
                    }
                    if (powerUnit.getType() == PowerUnit.ROLLET) {
                        if (checkBoxInvert.isChecked()) {
                            editor.putBoolean(Integer.toString(channel), true);
                        } else {
                            editor.putBoolean(Integer.toString(channel), false);
                        }
                        editor.apply();
                    }
                }
                if (rolletUnitF != null) {
                    editor.putBoolean(rolletUnitF.getId(), checkBoxInvert.isChecked());
                    editor.apply();
                    rolletUnitF.setInversion(checkBoxInvert.isChecked());
                }
                if (!name.equals(editName.getText().toString()) || roomID != rooms.get(spinnerRoom.getSelectedItemPosition()).getId()) {
                    blockUI();
                    changeUnit();
                } else {
                    if (changeUnitType) {
                        blockUI();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    changeType(powerUnit, true);
                                } catch (IOException e) {
                                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                                }
                            }
                        }).start();
                    } else {
                        dismiss();
                    }
                }
                break;
            case R.id.fragment_settings_unit_button_others:
                FTXUnitSettingsFragment ftxUnitSettingsFragment = (FTXUnitSettingsFragment) getChildFragmentManager().findFragmentByTag("FTX_OTHER_SETTINGS_FRAGMENT");
                if (ftxUnitSettingsFragment == null) {
                    ftxUnitSettingsFragment = new FTXUnitSettingsFragment();
                }
                if (ftxUnitSettingsFragment.isAdded()) return;
                ftxUnitSettingsFragment.instance(client, powerUnitF);
                getChildFragmentManager().beginTransaction().add(ftxUnitSettingsFragment, "FTX_OTHER_SETTINGS_FRAGMENT").show(ftxUnitSettingsFragment).commit();
                break;
            case R.id.fragment_settings_unit_button_sensor:
                ThermostatSensorSelectorFragment thermostatSensorSelectorFragment = (ThermostatSensorSelectorFragment) getChildFragmentManager().findFragmentByTag("THERMOSTAT_SENSOR_SELECTOR_FRAGMENT");
                if (thermostatSensorSelectorFragment == null) {
                    thermostatSensorSelectorFragment = new ThermostatSensorSelectorFragment();
                }
                if (thermostatSensorSelectorFragment.isAdded()) return;
                thermostatSensorSelectorFragment.send(client, thermostat);
                getChildFragmentManager().beginTransaction().add(thermostatSensorSelectorFragment, "THERMOSTAT_SENSOR_SELECTOR_FRAGMENT").show(thermostatSensorSelectorFragment).commit();
                break;
            case R.id.fragment_settings_unit_button_binding_mode:
                bindingMode();
                break;
            case R.id.fragment_settings_unit_button_unbinding_mode:
                unbindingMode();
                break;
            case R.id.fragment_settings_unit_button_service_mode_on:
                serviceModeOn();
                break;
            case R.id.fragment_settings_unit_button_service_mode_off:
                serviceModeOff();
                break;
            case R.id.fragment_settings_unit_button_boot_mode:
                bootMode();
                break;
            case R.id.fragment_settings_unit_button_remove:
                buttonRemove.setVisibility(View.GONE);
                layoutUnbind.setVisibility(View.VISIBLE);
//                if (powerUnit != null) {
//                    layoutTX.setVisibility(View.VISIBLE);
//                }
                break;
            case R.id.fragment_settings_unit_button_unbind:
                blockUI();
                if (powerUnit != null) {
                    unbindTX(powerUnit.getChannel());
                }
                if (remoteController != null) {
                    unbindRX(remoteController.getChannel());
                }
                if (temperatureSensor != null) {
                    unbindRX(temperatureSensor.getChannel());
                }
                if (humidityTemperatureSensor != null) {
                    unbindRX(humidityTemperatureSensor.getChannel());
                }
                if (motionSensor != null) {
                    unbindRX(motionSensor.getChannel());
                }
                if (openCloseSensor != null) {
                    unbindRX(openCloseSensor.getChannel());
                }
                if (leakDetector != null) {
                    unbindRX(leakDetector.getChannel());
                }
                if (lightSensor != null) {
                    unbindRX(lightSensor.getChannel());
                }
                if (powerUnitF != null) {
                    unbindFTX(powerUnitF.getId());
                }
                if (powerSocketF != null) {
                    unbindFTX(powerSocketF.getId());
                }
                if (thermostat != null) {
                    unbindFTX(thermostat.getId());
                }
                if (rolletUnitF != null) {
                    unbindFTX(rolletUnitF.getId());
                }
                break;
            case R.id.fragment_settings_unit_button_switch:
                switchTX(channel);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (unitSettingsFragmentListener != null) {
            unitSettingsFragmentListener.onDismiss(unbind, update, spinnerRoom.getSelectedItem().toString(), editName.getText().toString());
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

    private void bindingMode() {
        int mode = 0;
        int ctr = 0;
        if (powerUnitF != null || powerSocketF != null || thermostat != null || rolletUnitF != null) {
            mode = 2;
            ctr = 8;
        }

        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00%02d%02d00%s0F0000000000%s", mode, ctr, NooLiteF.getHexString(channel), id))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    response.close();
                    call.cancel();
                    showToast("Команда привязки отправлена...");
                } else {
                    response.close();
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void unbindingMode() {
        int mode = 0;
        int ctr = 0;
        if (powerUnitF != null || powerSocketF != null || thermostat != null || rolletUnitF != null) {
            mode = 2;
            ctr = 8;
        }

        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00%02d%02d00%s090000000000%s", mode, ctr, NooLiteF.getHexString(channel), id))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    response.close();
                    call.cancel();
                    showToast("Команда отвязки отправлена...");
                } else {
                    response.close();
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void serviceModeOn() {
        int mode = 0;
        int ctr = 0;

        if (powerUnitF != null || powerSocketF != null || thermostat != null || rolletUnitF != null) {
            mode = 2;
            ctr = 8;
            serviceMode = 1; //on
        }

        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00%02d%02d00%s8300%02d000000%s", mode, ctr, NooLiteF.getHexString(channel), serviceMode, id))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    showToast("Сервисный режим включен");
                } else {
                    showToast("Ошибка соединения " + response.code());
                }
                response.close();
                call.cancel();
            }
        });
    }

    private void serviceModeOff() {
        int mode = 0;
        int ctr = 0;

        if (powerUnitF != null || powerSocketF != null || thermostat != null || rolletUnitF != null) {
            mode = 2;
            ctr = 8;
            serviceMode = 0; //off
        }

        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00%02d%02d00%s8300%02d000000%s", mode, ctr, NooLiteF.getHexString(channel), serviceMode, id))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    showToast("Сервисный режим выключен");
                } else {
                    showToast("Ошибка соединения " + response.code());
                }
                response.close();
                call.cancel();
            }
        });
    }

    private void bootMode() {
        Request request1 = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008500AA55AA55%s", id))
                .build();
        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        call.cancel();
                        Thread.sleep(1000);

                        Request _request = new Request.Builder()
                                .url(Settings.URL() + "send.htm?sd=00050A000000000000000000000000")
                                .build();
                        Call _call = client.newCall(_request);
                        Response _response = _call.execute();
                        if (_response.isSuccessful()) {
                            _call.cancel();
                            Thread.sleep(1000);

                            Request __request = new Request.Builder()
                                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00AF050B0055AA55AA000000000000000000000000%s%sAC", id, "0A")) //0A=CRC
                                    .build();
                            Call __call = client.newCall(__request);
                            Response __response = __call.execute();
                            if (__response.isSuccessful()) {
                                __call.cancel();
                                Thread.sleep(1000);

                                Request ___request = new Request.Builder()
                                        .url(Settings.URL() + "recive.htm")
                                        .build();
                                Call ___call = client.newCall(___request);
                                Response ___response = ___call.execute();
                                if (___response.isSuccessful()) {
                                    String hex = ___response.body().string();
                                    ___call.cancel();

                                    showResponse(hex.substring(14, 42));

                                    return;
                                }
                            }
                        }
                    }
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                } catch (Exception e) {
                    showToast(e.toString() + "\n" + e.getMessage());
                }
            }
        });
    }

    private void showResponse(final String response) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonBootMode.setText(response);
            }
        });
    }

    private void unbindTX(int channel) {
        sendUnbind(String.format(Locale.ROOT, "00000000%s09000000000000000000", NooLiteF.getHexString(channel)));
    }

    private void unbindRX(int channel) {
        sendUnbind(String.format(Locale.ROOT, "00010500%s00000000000000000000", NooLiteF.getHexString(channel)));
    }

    private void unbindFTX(String id) {
        sendUnbind(String.format(Locale.ROOT, "0002080000090000000000%s", id));
    }

    private void sendUnbind(String command) {
        Request request = new Request.Builder()
                .url(Settings.URL() + "send.htm?sd=" + command)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        Thread.sleep(1000);
                        // getDevice();
                        // clearDevice(null, null);
                        clearDevice();
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showToast("Ошибка при отвязке устройства...");
                    }
                } else {
                    response.close();
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void switchTX(int channel) {
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00000000%s04000000000000000000", NooLiteF.getHexString(channel)))
                .post(RequestBody.create(null, ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    response.close();
                    call.cancel();
                } else {
                    response.close();
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void changeType(PowerUnit powerUnit, boolean dismiss) throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010600%s00000000%s000000000000", NooLiteF.getHexString(powerUnit.getChannel()), NooLiteF.getHexString(powerUnit.getType())))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            changeUnitType = false;
            if (dismiss) {
                update = true;
                dismiss();
            }
        } else {
            response.close();
            call.cancel();
            showToast("Ошибка соединения " + response.code());
        }
    }

    private void clearDevice() throws IOException {
        int type = -1;
        int cell = -1;

        if (powerUnit != null) {
            type = 0;
            cell = powerUnit.getChannel();
        }
        if (remoteController != null) {
            type = NooLiteF.TYPE_RX;
            cell = remoteController.getChannel();
        }
        if (temperatureSensor != null) {
            type = 1;
            cell = temperatureSensor.getChannel();
        }
        if (humidityTemperatureSensor != null) {
            type = 1;
            cell = humidityTemperatureSensor.getChannel();
        }
        if (motionSensor != null) {
            type = 1;
            cell = motionSensor.getChannel();
        }
        if (openCloseSensor != null) {
            type = 1;
            cell = openCloseSensor.getChannel();
        }
        if (leakDetector != null) {
            type = 1;
            cell = leakDetector.getChannel();
        }
        if (lightSensor != null) {
            type = 1;
            cell = lightSensor.getChannel();
        }
        if (powerUnitF != null) {
            type = 2;
            cell = powerUnitF.getIndex();
        }
        if (powerSocketF != null) {
            type = 2;
            cell = powerSocketF.getIndex();
        }
        if (thermostat != null) {
            type = 2;
            cell = thermostat.getIndex();
        }
        if (rolletUnitF != null) {
            type = 2;
            cell = rolletUnitF.getIndex();
        }

        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0106%02d%sFFFFFFFFFFFFFFFF000000", type, NooLiteF.getHexString(cell)))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            unbind = true;
            dismiss();
        } else {
            response.close();
            call.cancel();
            showToast("Ошибка соединения " + response.code());
        }
    }

    private void changeUnit() {
        if (powerUnit != null) {
            powerUnit.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                powerUnit.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                powerUnit.setRoomID(-1);
            }
            changeUnitInUser(powerUnit);
        }
        if (remoteController != null) {
            remoteController.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                remoteController.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                remoteController.setRoomID(-1);
            }
            changeUnitInUser(remoteController);
        }
        if (temperatureSensor != null) {
            temperatureSensor.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                temperatureSensor.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                temperatureSensor.setRoomID(-1);
            }
            changeUnitInUser(temperatureSensor);
        }
        if (humidityTemperatureSensor != null) {
            humidityTemperatureSensor.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                humidityTemperatureSensor.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                humidityTemperatureSensor.setRoomID(-1);
            }
            changeUnitInUser(humidityTemperatureSensor);
        }
        if (motionSensor != null) {
            motionSensor.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                motionSensor.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                motionSensor.setRoomID(-1);
            }
            changeUnitInUser(motionSensor);
        }
        if (openCloseSensor != null) {
            openCloseSensor.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                openCloseSensor.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                openCloseSensor.setRoomID(-1);
            }
            changeUnitInUser(openCloseSensor);
        }
        if (leakDetector != null) {
            leakDetector.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                leakDetector.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                leakDetector.setRoomID(-1);
            }
            changeUnitInUser(leakDetector);
        }
        if (lightSensor != null) {
            lightSensor.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                lightSensor.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                lightSensor.setRoomID(-1);
            }
            changeUnitInUser(lightSensor);
        }
        if (powerUnitF != null) {
            powerUnitF.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                powerUnitF.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                powerUnitF.setRoomID(-1);
            }
            changeUnitInUser(powerUnitF);
        }
        if (powerSocketF != null) {
            powerSocketF.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                powerSocketF.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                powerSocketF.setRoomID(-1);
            }
            changeUnitInUser(powerSocketF);
        }
        if (thermostat != null) {
            thermostat.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                thermostat.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                thermostat.setRoomID(-1);
            }
            changeUnitInUser(thermostat);
        }
        if (rolletUnitF != null) {
            rolletUnitF.setName(editName.getText().toString());
            if (spinnerRoom.getSelectedItemPosition() != rooms.size()) {
                rolletUnitF.setRoomID(rooms.get(spinnerRoom.getSelectedItemPosition()).getId());
            } else {
                rolletUnitF.setRoomID(-1);
            }
            changeUnitInUser(rolletUnitF);
        }
    }

    private void changeUnitInUser(final Object unit) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] name;

                    if (powerUnit != null) {
                        if (changeUnitType) {
                            changeType(powerUnit, false);
                        }

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

                    if (remoteController != null) {
                        name = remoteController.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[2182 + (34 * remoteController.getChannel()) + nb] = name[nb];
                            else
                                user[2182 + (34 * remoteController.getChannel()) + nb] = 0;
                        }
                        // set icon
                        user[2182 + (34 * remoteController.getChannel()) + 33] = (byte) remoteController.getRoomID();
                    }

                    if (temperatureSensor != null) {
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

                    if (humidityTemperatureSensor != null) {
                        name = humidityTemperatureSensor.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[2182 + (34 * humidityTemperatureSensor.getChannel()) + nb] = name[nb];
                            else
                                user[2182 + (34 * humidityTemperatureSensor.getChannel()) + nb] = 0;
                        }
                        // set icon
                        user[2182 + (34 * humidityTemperatureSensor.getChannel()) + 33] = (byte) humidityTemperatureSensor.getRoomID();
                    }

                    if (motionSensor != null) {
                        name = motionSensor.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[2182 + (34 * motionSensor.getChannel()) + nb] = name[nb];
                            else
                                user[2182 + (34 * motionSensor.getChannel()) + nb] = 0;
                        }
                        // set icon
                        user[2182 + (34 * motionSensor.getChannel()) + 33] = (byte) motionSensor.getRoomID();
                    }

                    if (openCloseSensor != null) {
                        name = openCloseSensor.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[2182 + (34 * openCloseSensor.getChannel()) + nb] = name[nb];
                            else
                                user[2182 + (34 * openCloseSensor.getChannel()) + nb] = 0;
                        }
                        // set icon
                        user[2182 + (34 * openCloseSensor.getChannel()) + 33] = (byte) openCloseSensor.getRoomID();
                    }

                    if (leakDetector != null) {
                        name = leakDetector.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[2182 + (34 * leakDetector.getChannel()) + nb] = name[nb];
                            else
                                user[2182 + (34 * leakDetector.getChannel()) + nb] = 0;
                        }
                        // set icon
                        user[2182 + (34 * leakDetector.getChannel()) + 33] = (byte) leakDetector.getRoomID();
                    }

                    if (lightSensor != null) {
                        name = lightSensor.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[2182 + (34 * lightSensor.getChannel()) + nb] = name[nb];
                            else
                                user[2182 + (34 * lightSensor.getChannel()) + nb] = 0;
                        }
                        // set icon
                        user[2182 + (34 * lightSensor.getChannel()) + 33] = (byte) lightSensor.getRoomID();
                    }

                    if (powerUnitF != null) {
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

                    if (powerSocketF != null) {
                        PowerSocketF powerSocketF = (PowerSocketF) unit;
                        name = powerSocketF.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[4358 + (34 * powerSocketF.getIndex()) + nb] = name[nb];
                            else
                                user[4358 + (34 * powerSocketF.getIndex()) + nb] = 0;
                        }
                        // set icon
                        user[4358 + (34 * powerSocketF.getIndex()) + 33] = (byte) powerSocketF.getRoomID();
                    }

                    if (thermostat != null) {
                        Thermostat thermostat = (Thermostat) unit;
                        name = thermostat.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[4358 + (34 * thermostat.getIndex()) + nb] = name[nb];
                            else
                                user[4358 + (34 * thermostat.getIndex()) + nb] = 0;
                        }
                        // set icon
                        user[4358 + (34 * thermostat.getIndex()) + 33] = (byte) thermostat.getRoomID();
                    }

                    if (rolletUnitF != null) {
                        RolletUnitF rolletUnitF = (RolletUnitF) unit;
                        name = rolletUnitF.getName().getBytes("cp1251");
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                user[4358 + (34 * rolletUnitF.getIndex()) + nb] = name[nb];
                            else
                                user[4358 + (34 * rolletUnitF.getIndex()) + nb] = 0;
                        }
                        // set icon
                        user[4358 + (34 * rolletUnitF.getIndex()) + 33] = (byte) rolletUnitF.getRoomID();
                    }

                    upload(new String(user, "cp1251"));
                } catch (Exception e) {
                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                }
            }
        }).start();
    }

    private void upload(String file) throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"user\"; filename=\"user.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(file)
                .concat("\r\n\r\n\r\n");
        Request request = new Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.getBytes("cp1251")))
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            unbind = false;
            update = true;
            dismiss();
        } else {
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unblockUI();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockUI() {
        if (!isAdded()) return;
        guiBlockFragment = (GUIBlockFragment) getChildFragmentManager().findFragmentByTag("GUI_BLOCK_FRAGMENT");
        if (guiBlockFragment == null) {
            guiBlockFragment = new GUIBlockFragment();
        }
        if (guiBlockFragment.isAdded()) return;
        getChildFragmentManager().beginTransaction().add(guiBlockFragment, "GUI_BLOCK_FRAGMENT").show(guiBlockFragment).commit();
    }

    private void unblockUI() {
        if (!isAdded()) return;
        guiBlockFragment = (GUIBlockFragment) getChildFragmentManager().findFragmentByTag("GUI_BLOCK_FRAGMENT");
        if (guiBlockFragment != null) {
            guiBlockFragment.dismiss();
        }
    }
}
