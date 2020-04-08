package com.noolitef.timers;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.noolitef.ConfirmDialog;
import com.noolitef.ConfirmDialogListener;
import com.noolitef.FragmentListener;
import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.ftx.PowerSocketF;
import com.noolitef.settings.SettingsControllerDateTimeFragment;
import com.noolitef.tx.PowerUnit;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.presets.Preset;
import com.noolitef.R;
import com.noolitef.settings.Settings;
import com.noolitef.GUIBlockFragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface TimerFragmentListener {
    void onDismiss();
}

public class TimerFragment extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, OnTimeSetListener {
    private final int TIMER_ACTION_ON = 1;
    private final int TIMER_ACTION_OFF = 2;
    private final int TIMER_ACTION_ON_OFF = 3;
    private final int TIMER_ACTION_PRESET = 4;

    private HomeActivity homeActivity;
    private OkHttpClient client;
    private byte[] file;
    private Timer timer;
    private ArrayList<Object> devices;
    private ArrayList<Preset> presets;
    private Preset preset;
    private TextView timeSetView;
    private GUIBlockFragment guiBlockFragment;

    private Button cancelButton;
    private LinearLayout layoutDelete;
    private Button deleteButton;
    private TextView titleText;
    private Button saveButton;
    private RelativeLayout layoutControllerTime;
    private ProgressBar progressBarControllerTime;
    private TextView textControllerTime;
    private EditText nameEdit;
    private Spinner timerActionSpinner;
    private LinearLayout timerOnLayout;
    private TextView timerOnLabel;
    private LinearLayout timerOffLayout;
    private TextView timerOffLabel;
    private ArrayAdapter timerActionAdapter;
    private LinearLayout week;
    private ToggleButton monday;
    private ToggleButton tuesday;
    private ToggleButton wednesday;
    private ToggleButton thursday;
    private ToggleButton friday;
    private ToggleButton saturday;
    private ToggleButton sunday;
    private LinearLayout devicesLayout;
    private LinearLayout presetLayout;
    private TextView devicesTitle;
    private Spinner presetSpinner;
    private ArrayAdapter presetAdapter;
    private ListView devicesList;
    private ArrayAdapter devicesAdapter;

    private BroadcastReceiver minuteChangedReceiver;
    private boolean updateTimers;
    private TimerFragmentListener timerFragmentListener;

    public TimerFragment() {
    }

    void setTimerFragmentListener(TimerFragmentListener listener) {
        timerFragmentListener = listener;
    }

    void send(OkHttpClient client, byte[] file, Timer timer, ArrayList<Object> devices, ArrayList<Preset> presets) {
        this.client = client;
        this.file = file;
        this.timer = timer;
        this.devices = new ArrayList<>();
        if (devices != null) this.devices.addAll(devices);
        this.presets = presets;
        if (this.presets == null) this.presets = new ArrayList<>();

        for (Object device : this.devices) {
            if (device instanceof PowerUnit)
                ((PowerUnit) device).setPreset(false);
            if (device instanceof PowerUnitF)
                ((PowerUnitF) device).setPreset(false);
            if (device instanceof PowerSocketF)
                ((PowerSocketF) device).setPreset(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragment;
        if (Settings.isNightMode()) {
            fragment = inflater.inflate(R.layout.fragment_timer_dark, null);
        } else {
            fragment = inflater.inflate(R.layout.fragment_timer, null);
        }

        cancelButton = (Button) fragment.findViewById(R.id.fragment_timer_button_cancel);
        cancelButton.setOnClickListener(this);
        layoutDelete = (LinearLayout) fragment.findViewById(R.id.fragment_timer_layout_delete);
        deleteButton = (Button) fragment.findViewById(R.id.fragment_timer_delete);
        deleteButton.setOnClickListener(this);
        if (timer != null) {
            layoutDelete.setVisibility(View.VISIBLE);
        }
        titleText = (TextView) fragment.findViewById(R.id.fragment_timer_title);
        saveButton = (Button) fragment.findViewById(R.id.fragment_timer_button_save);
        saveButton.setOnClickListener(this);
        layoutControllerTime = fragment.findViewById(R.id.fragment_timer_layout_controller_time);
        layoutControllerTime.setOnClickListener(this);
        progressBarControllerTime = fragment.findViewById(R.id.fragment_timer_progressbar_controller_time);
        textControllerTime = fragment.findViewById(R.id.fragment_timer_text_controller_time);
        nameEdit = (EditText) fragment.findViewById(R.id.fragment_timer_edit_name);
        timerActionSpinner = (Spinner) fragment.findViewById(R.id.fragment_timer_spinner_action);
        initTimerActionSpinner();
        timerOnLayout = (LinearLayout) fragment.findViewById(R.id.fragment_timer_on_layout);
        timerOnLayout.setOnClickListener(this);
        timerOnLabel = (TextView) fragment.findViewById(R.id.fragment_timer_on_label);
        timerOffLayout = (LinearLayout) fragment.findViewById(R.id.fragment_timer_off_layout);
        timerOffLayout.setOnClickListener(this);
        timerOffLabel = (TextView) fragment.findViewById(R.id.fragment_timer_off_label);
        week = (LinearLayout) fragment.findViewById(R.id.fragment_timer_week);
        monday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_monday);
        monday.setOnCheckedChangeListener(this);
        tuesday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_tuesday);
        tuesday.setOnCheckedChangeListener(this);
        wednesday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_wednesday);
        wednesday.setOnCheckedChangeListener(this);
        thursday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_thursday);
        thursday.setOnCheckedChangeListener(this);
        friday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_friday);
        friday.setOnCheckedChangeListener(this);
        saturday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_saturday);
        saturday.setOnCheckedChangeListener(this);
        sunday = (ToggleButton) fragment.findViewById(R.id.fragment_timer_toggle_sunday);
        sunday.setOnCheckedChangeListener(this);
        devicesLayout = (LinearLayout) fragment.findViewById(R.id.fragment_timer_type_devices);
        devicesTitle = (TextView) fragment.findViewById(R.id.fragment_timer_title_devices);
        devicesTitle.setOnClickListener(this);
        presetLayout = (LinearLayout) fragment.findViewById(R.id.fragment_timer_type_preset);
        presetSpinner = (Spinner) fragment.findViewById(R.id.fragment_timer_spinner_preset);
        initPresetSpinner();
        devicesList = (ListView) fragment.findViewById(R.id.fragment_timer_list_devices);
        DisplayMetrics display = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(display);
        devicesList.setDividerHeight((int) display.density);

        initTimer();

        getControllerDateTime();

        setTimeReceiver();

        getDialog().setCanceledOnTouchOutside(false);
        return fragment;
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
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.fragment_timer_button_cancel:
                    updateTimers = false;
                    dismiss();
                    break;
                case R.id.fragment_timer_delete:
                    ConfirmDialog confirmDialog = (ConfirmDialog) getChildFragmentManager().findFragmentByTag("CONFIRM_DIALOG");
                    if (confirmDialog == null) {
                        confirmDialog = new ConfirmDialog();
                        confirmDialog.setTitle("Удаление таймера");
                        confirmDialog.setMessage("Удалить таймер ''".concat(timer.getName()).concat("''?"));
                        confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                            @Override
                            public void onAccept() {
                                blockUI();
                                deleteTimer();
                            }

                            @Override
                            public void onDecline() {
                            }
                        });
                    }
                    if (confirmDialog.isAdded()) return;
                    getChildFragmentManager().beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit();
                    break;
                case R.id.fragment_timer_button_save:
                    if (nameEdit.getText().length() == 0) {
                        showToast("Назовите таймер");
                        return;
                    } else {
                        if (timerActionSpinner.getSelectedItemPosition() == 0) {
                            showToast("Выберите действие для таймера");
                            return;
                        } else {
                            if (timerActionSpinner.getSelectedItemPosition() == 6 && preset == null) {
                                showToast("Выберите сценарий для таймера");
                                return;
                            } else {
                                if ((0 < timerActionSpinner.getSelectedItemPosition() && timerActionSpinner.getSelectedItemPosition() < 6) && !isSomeDevicesChecked(devices)) {
                                    showToast("Добавьте хотя бы одно устройство в таймер");
                                    return;
                                }
                            }
                        }
                    }
                    blockUI();
                    if (timer == null) {
                        switch (timerActionSpinner.getSelectedItemPosition()) {
                            case 1:
                                setCommandOn(devices);
                                addTimer(devices);
                                break;
                            case 2:
                                setCommandSunrise(devices);
                                addTimer(devices);
                                break;
                            case 3:
                                setCommandOff(devices);
                                addTimer(devices);
                                break;
                            case 4:
                                setCommandSunset(devices);
                                addTimer(devices);
                                break;
                            case 5:
                                setCommandOn(devices);
                                addTimer(devices);
                                break;
                            case 6:
                                addTimer(preset);
                                break;
                        }
                    } else {
                        switch (timerActionSpinner.getSelectedItemPosition()) {
                            case 1:
                                setCommandOn(devices);
                                changeTimer(devices);
                                break;
                            case 2:
                                setCommandSunrise(devices);
                                changeTimer(devices);
                                break;
                            case 3:
                                setCommandOff(devices);
                                changeTimer(devices);
                                break;
                            case 4:
                                setCommandSunset(devices);
                                changeTimer(devices);
                                break;
                            case 5:
                                setCommandOn(devices);
                                changeTimer(devices);
                                break;
                            case 6:
                                changeTimer(preset);
                                break;
                        }
                    }
                    break;
                case R.id.fragment_timer_layout_controller_time:
                    SettingsControllerDateTimeFragment settingsControllerDateTimeFragment = (SettingsControllerDateTimeFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_DATE_TIME_FRAGMENT");
                    if (settingsControllerDateTimeFragment == null) {
                        settingsControllerDateTimeFragment = new SettingsControllerDateTimeFragment();
                        settingsControllerDateTimeFragment.send(client);
                        settingsControllerDateTimeFragment.setFragmentListener(new FragmentListener() {
                            @Override
                            public void onDismiss(boolean update) {
                                if (update) getControllerDateTime();
                            }
                        });
                    }
                    if (settingsControllerDateTimeFragment.isAdded()) return;
                    getChildFragmentManager().beginTransaction().add(settingsControllerDateTimeFragment, "SETTINGS_CONTROLLER_DATE_TIME_FRAGMENT").show(settingsControllerDateTimeFragment).commit();
                    break;
                case R.id.fragment_timer_on_layout:
                    showTimePicker(timerOnLabel, Integer.parseInt(timerOnLabel.getText().toString().substring(0, 2)), Integer.parseInt(timerOnLabel.getText().toString().substring(3, 5)));
                    break;
                case R.id.fragment_timer_off_layout:
                    showTimePicker(timerOffLabel, Integer.parseInt(timerOffLabel.getText().toString().substring(0, 2)), Integer.parseInt(timerOffLabel.getText().toString().substring(3, 5)));
                    break;
                case R.id.fragment_timer_title_devices:
                    ChoiceDevicesDialog choiceDevicesDialog = (ChoiceDevicesDialog) getChildFragmentManager().findFragmentByTag("ROOMS_DEVICES_DIALOG");
                    if (choiceDevicesDialog == null) {
                        choiceDevicesDialog = new ChoiceDevicesDialog();
                        choiceDevicesDialog.setOnDismissListener(new ChoiceDevicesDialogListener() {
                            @Override
                            public void onDismiss() {
                                initDevicesList();
                            }
                        });
                    }
                    if (choiceDevicesDialog.isAdded()) return;
                    choiceDevicesDialog.send(homeActivity, timer, devices);
                    getChildFragmentManager().beginTransaction().add(choiceDevicesDialog, "ROOMS_DEVICES_DIALOG").show(choiceDevicesDialog).commit();
                    break;

            }
        } catch (Exception e) {
            showToast("Что-то пошло не так...");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
        switch (buttonView.getId()) {
            case R.id.fragment_timer_toggle_monday:
                if (checked)
                    monday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    monday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
            case R.id.fragment_timer_toggle_tuesday:
                if (checked)
                    tuesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    tuesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
            case R.id.fragment_timer_toggle_wednesday:
                if (checked)
                    wednesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    wednesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
            case R.id.fragment_timer_toggle_thursday:
                if (checked)
                    thursday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    thursday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
            case R.id.fragment_timer_toggle_friday:
                if (checked)
                    friday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    friday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
            case R.id.fragment_timer_toggle_saturday:
                if (checked)
                    saturday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    saturday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
            case R.id.fragment_timer_toggle_sunday:
                if (checked)
                    sunday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                else
                    sunday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (updateTimers) timerFragmentListener.onDismiss();
        super.onDismiss(dialog);
    }

    @Override
    public void onDestroyView() {
        if (minuteChangedReceiver != null) getContext().unregisterReceiver(minuteChangedReceiver);

        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private void getControllerDateTime() {
        Request request = new Request.Builder()
                .url(Settings.URL() + "time.htm")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                if (!isAdded()) return;
                showToast(homeActivity.getString(R.string.no_connection));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        parseDateTime(call, response);
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        if (!isAdded()) return;
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                    }
                } else {
                    response.close();
                    call.cancel();
                    if (!isAdded()) return;
                    showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
                }
            }
        });
    }

    private void parseDateTime(Call call, Response response) throws IOException {
        String hex = response.body().string();
        response.close();
        call.cancel();
        StringBuilder time = new StringBuilder();
        time.append(String.format(Locale.ROOT, "%s  ", NooLiteF.getWeekDayShort(Integer.parseInt(hex.substring(6, 8), 16))));
        time.append(String.format(Locale.ROOT, "%02d:", Integer.parseInt(hex.substring(4, 6), 16)));
        time.append(String.format(Locale.ROOT, "%02d", Integer.parseInt(hex.substring(2, 4), 16)));
        setTime(time.toString());
    }

    private void setTimeReceiver() {
        minuteChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                    getControllerDateTime();
                }
            }
        };
        getContext().registerReceiver(minuteChangedReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void initTimerActionSpinner() {
        timerActionAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, homeActivity.getResources().getStringArray(R.array.timerType)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if (position == 0) {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                } else {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                }
                return view;
            }
        };
        timerActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timerOnLayout.setVisibility(View.GONE);
                timerOffLayout.setVisibility(View.GONE);
                week.setVisibility(View.GONE);
                devicesLayout.setVisibility(View.GONE);
                presetLayout.setVisibility(View.GONE);
                switch (position) {
                    case 1:
                    case 2:
                        timerOnLayout.setVisibility(View.VISIBLE);
                        week.setVisibility(View.VISIBLE);
                        devicesLayout.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                    case 4:
                        timerOffLayout.setVisibility(View.VISIBLE);
                        week.setVisibility(View.VISIBLE);
                        devicesLayout.setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        timerOnLayout.setVisibility(View.VISIBLE);
                        timerOffLayout.setVisibility(View.VISIBLE);
                        week.setVisibility(View.VISIBLE);
                        devicesLayout.setVisibility(View.VISIBLE);
                        break;
                    case 6:
                        timerOnLayout.setVisibility(View.VISIBLE);
                        week.setVisibility(View.VISIBLE);
                        presetLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        timerActionSpinner.setAdapter(timerActionAdapter);
    }

    private void initDevicesList() {
        ArrayList<String> deviceRoomName = new ArrayList<>();
        for (Object device : devices) {
            if (device instanceof PowerUnit) {
                PowerUnit powerUnit = (PowerUnit) device;
                if (powerUnit.isPreset()) {
                    deviceRoomName.add(powerUnit.getRoom().concat(" - ").concat(powerUnit.getName()));
                }
            }
            if (device instanceof PowerUnitF) {
                PowerUnitF powerUnitF = (PowerUnitF) device;
                if (powerUnitF.isPreset()) {
                    deviceRoomName.add(powerUnitF.getRoom().concat(" - ").concat(powerUnitF.getName()));
                }
            }
            if (device instanceof PowerSocketF) {
                PowerSocketF powerSocketF = (PowerSocketF) device;
                if (powerSocketF.isPreset()) {
                    deviceRoomName.add(powerSocketF.getRoom().concat(" - ").concat(powerSocketF.getName()));
                }
            }
        }
        if (deviceRoomName.size() == 0) {
            deviceRoomName.add("добавьте устройства...");
        }
        devicesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, deviceRoomName);
        devicesList.setAdapter(devicesAdapter);
    }

    private void initPresetSpinner() {
        ArrayList<String> presetName = new ArrayList<>();
        if (presets.size() == 0) {
            presetName.add("...");
        } else {
            presetName.add("выберите сценарий...");
            for (Preset preset : presets) {
                presetName.add(preset.getName());
            }
        }

        presetAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, presetName) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if (position == 0) {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                } else {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                }
                return view;
            }
        };
        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) preset = presets.get(position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        presetSpinner.setAdapter(presetAdapter);
    }

    private void initTimer() {
        if (timer != null) {
            nameEdit.setText(timer.getName());

            switch (timer.getType()) {
                case 0:
                    switch (timer.getCommand(0)[0] & 0xFF) {
                        case 0:
                        case 2:
                            switch (timer.getCommand(0)[4] & 0xFF) {
                                case 0:
                                    timerActionSpinner.setSelection(3);
                                    timerOffLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOnHour(), timer.getOnMinute()));
                                    break;
                                case 2:
                                    timerActionSpinner.setSelection(1);
                                    timerOnLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOnHour(), timer.getOnMinute()));
                                    break;
                                case 13:
                                    switch (timer.getCommand(0)[6] & 0xFF) {
                                        case 1:
                                            timerActionSpinner.setSelection(2);
                                            timerOnLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOnHour(), timer.getOnMinute()));
                                            break;
                                        case 255:
                                            timerActionSpinner.setSelection(4);
                                            timerOffLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOnHour(), timer.getOnMinute()));
                                            break;
                                    }
                                    break;
                            }
                            for (Object device : devices) {
                                if (device instanceof PowerUnit) {
                                    PowerUnit powerUnit = (PowerUnit) device;
                                    for (int c = 0; c < 8; c++) {
                                        if (timer.getCommand(c)[0] == 0 && timer.getCommand(c)[3] == powerUnit.getChannel()) {
                                            powerUnit.setPreset(true);
                                        }
                                    }
                                }
                                if (device instanceof PowerUnitF) {
                                    PowerUnitF powerUnitF = (PowerUnitF) device;
                                    for (int c = 0; c < 8; c++) {
                                        if (timer.getCommand(c)[0] == 2 && NooLiteF.getHexString(timer.getCommand(c)[10] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[11] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[12] & 0xFF)).concat(NooLiteF.getHexString(timer.getCommand(c)[13] & 0xFF))).equals(powerUnitF.getId())) {
                                            powerUnitF.setPreset(true);
                                        }
                                    }
                                }
                                if (device instanceof PowerSocketF) {
                                    PowerSocketF powerSocketF = (PowerSocketF) device;
                                    for (int c = 0; c < 8; c++) {
                                        if (timer.getCommand(c)[0] == 2 && NooLiteF.getHexString(timer.getCommand(c)[10] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[11] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[12] & 0xFF)).concat(NooLiteF.getHexString(timer.getCommand(c)[13] & 0xFF))).equals(powerSocketF.getId())) {
                                            powerSocketF.setPreset(true);
                                        }
                                    }
                                }
                            }
                            initDevicesList();
                            break;
                        case 254:
                            timerActionSpinner.setSelection(6);
                            timerOnLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOnHour(), timer.getOnMinute()));
                            if (timer.getCommand(0)[1] + 1 < presetSpinner.getCount())
                                presetSpinner.setSelection(timer.getCommand(0)[1] + 1);
                            break;
                    }
                    break;
                case 1:
                    timerActionSpinner.setSelection(5);
                    timerOnLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOnHour(), timer.getOnMinute()));
                    timerOffLabel.setText(String.format(Locale.ROOT, "%02d:%02d", timer.getOffHour(), timer.getOffMinute()));
                    for (Object device : devices) {
                        if (device instanceof PowerUnit) {
                            PowerUnit powerUnit = (PowerUnit) device;
                            for (int c = 0; c < 8; c++) {
                                if (timer.getCommand(c)[0] == 0 && timer.getCommand(c)[3] == powerUnit.getChannel()) {
                                    powerUnit.setPreset(true);
                                }
                            }
                        }
                        if (device instanceof PowerUnitF) {
                            PowerUnitF powerUnitF = (PowerUnitF) device;
                            for (int c = 0; c < 8; c++) {
                                if (timer.getCommand(c)[0] == 2 && NooLiteF.getHexString(timer.getCommand(c)[10] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[11] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[12] & 0xFF)).concat(NooLiteF.getHexString(timer.getCommand(c)[13] & 0xFF))).equals(powerUnitF.getId())) {
                                    powerUnitF.setPreset(true);
                                }
                            }
                        }
                        if (device instanceof PowerSocketF) {
                            PowerSocketF powerSocketF = (PowerSocketF) device;
                            for (int c = 0; c < 8; c++) {
                                if (timer.getCommand(c)[0] == 2 && NooLiteF.getHexString(timer.getCommand(c)[10] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[11] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(c)[12] & 0xFF)).concat(NooLiteF.getHexString(timer.getCommand(c)[13] & 0xFF))).equals(powerSocketF.getId())) {
                                    powerSocketF.setPreset(true);
                                }
                            }
                        }
                    }
                    initDevicesList();
                    break;
            }

            String weekDay = String.format("%8s", Integer.toBinaryString(timer.getWorkDays() & 0xFF)).replace(' ', '0');
            if (weekDay.substring(6, 7).equals("1")) {
                monday.setChecked(true);
            } else {
                monday.setChecked(false);
            }
            if (weekDay.substring(5, 6).equals("1")) {
                tuesday.setChecked(true);
            } else {
                tuesday.setChecked(false);
            }
            if (weekDay.substring(4, 5).equals("1")) {
                wednesday.setChecked(true);
            } else {
                wednesday.setChecked(false);
            }
            if (weekDay.substring(3, 4).equals("1")) {
                thursday.setChecked(true);
            } else {
                thursday.setChecked(false);
            }
            if (weekDay.substring(2, 3).equals("1")) {
                friday.setChecked(true);
            } else {
                friday.setChecked(false);
            }
            if (weekDay.substring(1, 2).equals("1")) {
                saturday.setChecked(true);
            } else {
                saturday.setChecked(false);
            }
            if (weekDay.substring(0, 1).equals("1")) {
                sunday.setChecked(true);
            } else {
                sunday.setChecked(false);
            }
        }
    }

    private void showTimePicker(TextView textView, int hour, int minute) {
        timeSetView = textView;
        TimeSetterDialog timeSetterDialog = (TimeSetterDialog) getChildFragmentManager().findFragmentByTag("TIME_PICKER_DIALOG");
        if (timeSetterDialog == null) {
            timeSetterDialog = new TimeSetterDialog();
            timeSetterDialog.setTime(hour, minute);
        }
        if (timeSetterDialog.isAdded()) return;
        getChildFragmentManager().beginTransaction().add(timeSetterDialog, "TIME_PICKER_DIALOG").show(timeSetterDialog).commit();
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        timeSetView.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute));
    }

    private boolean isSomeDevicesChecked(ArrayList<Object> devices) {
        for (Object device : devices) {
            if (device instanceof PowerUnit)
                if (((PowerUnit) device).isPreset())
                    return true;
            if (device instanceof PowerUnitF)
                if (((PowerUnitF) device).isPreset())
                    return true;
            if (device instanceof PowerSocketF)
                if (((PowerSocketF) device).isPreset())
                    return true;
        }
        return false;
    }

    private void setCommandOn(ArrayList<Object> devices) {
        if (0 < timerActionSpinner.getSelectedItemPosition() && timerActionSpinner.getSelectedItemPosition() < 6) {
            for (Object device : devices) {
                if (device instanceof PowerUnit)
                    ((PowerUnit) device).setPresetState(PowerUnit.ON);
                if (device instanceof PowerUnitF)
                    ((PowerUnitF) device).setPresetState(PowerUnitF.ON);
                if (device instanceof PowerSocketF)
                    ((PowerSocketF) device).setPresetState(PowerSocketF.ON);
            }
        }
    }

    private void setCommandSunrise(ArrayList<Object> devices) {
        for (Object device : devices) {
            if (device instanceof PowerUnit) {
                PowerUnit powerUnit = (PowerUnit) device;
                switch (powerUnit.getType()) {
                    case PowerUnit.RGB_CONTROLLER:
                        powerUnit.setPresetState(PowerUnitF.ON);
                        break;
                    default:
                        powerUnit.setCommandSunrise();
                }
            }
            if (device instanceof PowerUnitF)
                ((PowerUnitF) device).setPresetState(PowerUnitF.ON);
            if (device instanceof PowerSocketF)
                ((PowerSocketF) device).setPresetState(PowerSocketF.ON);
        }
    }

    private void setCommandOff(ArrayList<Object> devices) {
        for (Object device : devices) {
            if (device instanceof PowerUnit)
                ((PowerUnit) device).setPresetState(PowerUnit.OFF);
            if (device instanceof PowerUnitF)
                ((PowerUnitF) device).setPresetState(PowerUnitF.OFF);
            if (device instanceof PowerSocketF)
                ((PowerSocketF) device).setPresetState(PowerSocketF.OFF);
        }
    }

    private void setCommandSunset(ArrayList<Object> devices) {
        for (Object device : devices) {
            if (device instanceof PowerUnit) {
                PowerUnit powerUnit = (PowerUnit) device;
                switch (powerUnit.getType()) {
                    case PowerUnit.RGB_CONTROLLER:
                        powerUnit.setPresetState(PowerUnitF.OFF);
                        break;
                    default:
                        powerUnit.setCommandSunset();
                }
            }
            if (device instanceof PowerUnitF)
                ((PowerUnitF) device).setPresetState(PowerUnitF.OFF);
            if (device instanceof PowerSocketF)
                ((PowerSocketF) device).setPresetState(PowerSocketF.OFF);
        }
    }

    private void addTimer(final ArrayList<Object> devices) {
        new Thread(new Runnable() {
            public void run() {
                for (int b = 4102, i = 0; b < 7910; i++, b += 119) {
                    if (file[b] == -1) {
                        switch (timerActionSpinner.getSelectedItemPosition()) {
                            case 0:
                                return;
                            case 1:
                            case 2:
                            case 6:
                                file[b] = 0;
                                file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                                file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                                file[b + 4] = 0;
                                file[b + 5] = 0;
                                break;
                            case 3:
                            case 4:
                                file[b] = 0;
                                file[b + 2] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                                file[b + 3] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                                file[b + 4] = 0;
                                file[b + 5] = 0;
                                break;
                            case 5:
                                file[b] = 1;
                                file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                                file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                                file[b + 4] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                                file[b + 5] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                                break;
                        }

                        file[b + 1] = 0;

                        StringBuilder weekDays = new StringBuilder();
                        if (sunday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (saturday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (friday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (thursday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (wednesday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (tuesday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (monday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        weekDays.append("0");
                        file[b + 6] = (byte) Integer.parseInt(weekDays.toString(), 2);

                        int ci = 0;
                        for (Object device : devices) {
                            if (device instanceof PowerUnit) {
                                PowerUnit powerUnit = (PowerUnit) device;
                                if (powerUnit.isPreset()) {
                                    for (int fb = b + 7 + (ci * 14), cb = 0; fb < b + 7 + (ci * 14) + 14; cb++, fb++) {
                                        file[fb] = powerUnit.getCommand()[cb];
                                    }
                                    ci++;
                                }
                            }
                            if (device instanceof PowerUnitF) {
                                PowerUnitF powerUnitF = (PowerUnitF) device;
                                if (powerUnitF.isPreset()) {
                                    for (int fb = b + 7 + (ci * 14), cb = 0; fb < b + 7 + (ci * 14) + 14; cb++, fb++) {
                                        file[fb] = powerUnitF.getCommand()[cb];
                                    }
                                    ci++;
                                }
                            }
                            if (device instanceof PowerSocketF) {
                                PowerSocketF powerSocketF = (PowerSocketF) device;
                                if (powerSocketF.isPreset()) {
                                    for (int fb = b + 7 + (ci * 14), cb = 0; fb < b + 7 + (ci * 14) + 14; cb++, fb++) {
                                        file[fb] = powerSocketF.getCommand()[cb];
                                    }
                                    ci++;
                                }
                            }
                            if (ci == 8) break;
                        }

                        byte[] name;
                        try {
                            name = nameEdit.getText().toString().getBytes("cp1251");
                        } catch (UnsupportedEncodingException e) {
                            showToast("Попробуйте ввести другое имя для таймера...");
                            return;
                        }
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                file[6 + (32 * i) + nb] = name[nb];
                            else
                                file[6 + (32 * i) + nb] = 0;
                        }

                        uploadFile("Таймер сохранён", "Ошибка при сохранении таймера");
                        return;
                    }
                }
                showToast("Можно сохранить только 32 таймера");
            }
        }).start();
    }

    private void addTimer(final Preset preset) {
        new Thread(new Runnable() {
            public void run() {
                for (int b = 4102, i = 0; b < 7910; i++, b += 119) {
                    if (file[b] == -1) {
                        switch (timerActionSpinner.getSelectedItemPosition()) {
                            case 0:
                                return;
                            case 1:
                            case 2:
                            case 6:
                                file[b] = 0;
                                file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                                file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                                file[b + 4] = 0;
                                file[b + 5] = 0;
                                break;
                            case 3:
                            case 4:
                                file[b] = 0;
                                file[b + 2] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                                file[b + 3] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                                file[b + 4] = 0;
                                file[b + 5] = 0;
                                break;
                            case 5:
                                file[b] = 1;
                                file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                                file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                                file[b + 4] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                                file[b + 5] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                                break;
                        }

                        file[b + 1] = 0;

                        StringBuilder weekDays = new StringBuilder();
                        if (sunday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (saturday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (friday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (thursday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (wednesday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (tuesday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        if (monday.isChecked()) weekDays.append("1");
                        else weekDays.append("0");
                        weekDays.append("0");
                        file[b + 6] = (byte) Integer.parseInt(weekDays.toString(), 2);

                        file[b + 7] = (byte) 254;
                        file[b + 8] = (byte) preset.getIndex();
                        file[b + 9] = file[b + 10] = file[b + 11] = file[b + 12] = file[b + 13] = file[b + 14] = file[b + 15] = file[b + 16] = file[b + 17] = file[b + 18] = file[b + 19] = file[b + 20] = 0;

                        byte[] name;
                        try {
                            name = nameEdit.getText().toString().getBytes("cp1251");
                        } catch (UnsupportedEncodingException e) {
                            showToast("Попробуйте ввести другое имя для таймера...");
                            return;
                        }
                        for (int nb = 0; nb < 32; nb++) {
                            if (nb < name.length)
                                file[6 + (32 * i) + nb] = name[nb];
                            else
                                file[6 + (32 * i) + nb] = 0;
                        }

                        uploadFile("Таймер сохранён", "Ошибка при сохранении таймера");
                        return;
                    }
                }
                showToast("Можно сохранить только 32 таймера");
            }
        }).start();
    }

    private void changeTimer(final ArrayList<Object> devices) {
        new Thread(new Runnable() {
            public void run() {
                int b = 4102 + (timer.getIndex() * 119);

                switch (timerActionSpinner.getSelectedItemPosition()) {
                    case 0:
                        return;
                    case 1:
                    case 2:
                    case 6:
                        file[b] = 0;
                        file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                        file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                        file[b + 4] = 0;
                        file[b + 5] = 0;
                        break;
                    case 3:
                    case 4:
                        file[b] = 0;
                        file[b + 2] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                        file[b + 3] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                        file[b + 4] = 0;
                        file[b + 5] = 0;
                        break;
                    case 5:
                        file[b] = 1;
                        file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                        file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                        file[b + 4] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                        file[b + 5] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                        break;
                }

                file[b + 1] = 0;

                StringBuilder weekDays = new StringBuilder();
                if (sunday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (saturday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (friday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (thursday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (wednesday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (tuesday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (monday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                weekDays.append("0");
                file[b + 6] = (byte) Integer.parseInt(weekDays.toString(), 2);

                int ci = 0;
                for (Object device : devices) {
                    if (device instanceof PowerUnit) {
                        PowerUnit powerUnit = (PowerUnit) device;
                        if (powerUnit.isPreset()) {
                            for (int fb = b + 7 + (ci * 14), cb = 0; fb < b + 7 + (ci * 14) + 14; cb++, fb++) {
                                file[fb] = powerUnit.getCommand()[cb];
                            }
                            ci++;
                        }
                    }
                    if (device instanceof PowerUnitF) {
                        PowerUnitF powerUnitF = (PowerUnitF) device;
                        if (powerUnitF.isPreset()) {
                            for (int fb = b + 7 + (ci * 14), cb = 0; fb < b + 7 + (ci * 14) + 14; cb++, fb++) {
                                file[fb] = powerUnitF.getCommand()[cb];
                            }
                            ci++;
                        }
                    }
                    if (device instanceof PowerSocketF) {
                        PowerSocketF powerSocketF = (PowerSocketF) device;
                        if (powerSocketF.isPreset()) {
                            for (int fb = b + 7 + (ci * 14), cb = 0; fb < b + 7 + (ci * 14) + 14; cb++, fb++) {
                                file[fb] = powerSocketF.getCommand()[cb];
                            }
                            ci++;
                        }
                    }
                    if (ci == 8) break;
                }

                for (int cb = b + 7 + (ci * 14) + 14; cb < b + 119; cb++) {
                    file[cb] = -1;
                }

                byte[] name;
                try {
                    name = nameEdit.getText().toString().getBytes("cp1251");
                } catch (UnsupportedEncodingException e) {
                    showToast("Попробуйте ввести другое имя для таймера...");
                    return;
                }
                for (int nb = 0; nb < 32; nb++) {
                    if (nb < name.length)
                        file[6 + (32 * timer.getIndex()) + nb] = name[nb];
                    else
                        file[6 + (32 * timer.getIndex()) + nb] = 0;
                }

                uploadFile("Таймер изменён", "Ошибка при изменении таймера");
            }
        }).start();
    }

    private void changeTimer(final Preset preset) {
        new Thread(new Runnable() {
            public void run() {
                int b = 4102 + (timer.getIndex() * 119);

                switch (timerActionSpinner.getSelectedItemPosition()) {
                    case 0:
                        return;
                    case 1:
                    case 2:
                    case 6:
                        file[b] = 0;
                        file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                        file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                        file[b + 4] = 0;
                        file[b + 5] = 0;
                        break;
                    case 3:
                    case 4:
                        file[b] = 0;
                        file[b + 2] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                        file[b + 3] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                        file[b + 4] = 0;
                        file[b + 5] = 0;
                        break;
                    case 5:
                        file[b] = 1;
                        file[b + 2] = Byte.parseByte(timerOnLabel.getText().toString().substring(0, 2));
                        file[b + 3] = Byte.parseByte(timerOnLabel.getText().toString().substring(3, 5));
                        file[b + 4] = Byte.parseByte(timerOffLabel.getText().toString().substring(0, 2));
                        file[b + 5] = Byte.parseByte(timerOffLabel.getText().toString().substring(3, 5));
                        break;
                }

                file[b + 1] = 0;

                StringBuilder weekDays = new StringBuilder();
                if (sunday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (saturday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (friday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (thursday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (wednesday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (tuesday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                if (monday.isChecked()) weekDays.append("1");
                else weekDays.append("0");
                weekDays.append("0");
                file[b + 6] = (byte) Integer.parseInt(weekDays.toString(), 2);

                file[b + 7] = (byte) 254;
                file[b + 8] = (byte) preset.getIndex();
                file[b + 9] = file[b + 10] = file[b + 11] = file[b + 12] = file[b + 13] = file[b + 14] = file[b + 15] = file[b + 16] = file[b + 17] = file[b + 18] = file[b + 19] = file[b + 20] = 0;

                for (int cb = b + 21; cb < b + 119; cb++) {
                    file[cb] = -1;
                }

                byte[] name;
                try {
                    name = nameEdit.getText().toString().getBytes("cp1251");
                } catch (UnsupportedEncodingException e) {
                    showToast("Попробуйте ввести другое имя для таймера...");
                    return;
                }
                for (int nb = 0; nb < 32; nb++) {
                    if (nb < name.length)
                        file[6 + (32 * timer.getIndex()) + nb] = name[nb];
                    else
                        file[6 + (32 * timer.getIndex()) + nb] = 0;
                }

                uploadFile("Таймер изменён", "Ошибка при изменении таймера");
            }
        }).start();
    }

    private void deleteTimer() {
        new Thread(new Runnable() {
            public void run() {
                for (int b = 4102 + (timer.getIndex() * 119); b < 4102 + (timer.getIndex() * 119) + 119; b++) {
                    file[b] = -1;
                }

                for (int b = 6 + (timer.getIndex() * 32); b < 6 + (timer.getIndex() * 32) + 32; b++) {
                    file[b] = -1;
                }

                uploadFile("Таймер удалён", "Ошибка при удалении таймера");
            }
        }).start();
    }

    private void uploadFile(String successMessage, String errorMessage) {
        try {
            String body = "\r\n\r\nContent-Disposition: form-data; name=\"timer\"; filename=\"timer.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                    .concat(new String(file, "cp1251"))
                    .concat("\r\n\r\n\r\n");
            Request request = new Request.Builder()
                    .url(Settings.URL() + "sett_eic.htm")
                    .post(RequestBody.create(null, body.getBytes("cp1251")))
                    .build();
            Call call = client.newCall(request);
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                response.close();
                call.cancel();
                showToast(successMessage);
                updateTimers = true;
                dismiss();
            } else {
                response.close();
                call.cancel();
                showToast("Ошибка соединения " + response.code());
            }
        } catch (IOException e) {
            showToast(errorMessage);
        }
    }

    private void setTime(final String time) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarControllerTime.setVisibility(View.INVISIBLE);
                textControllerTime.setText(time);
                textControllerTime.setVisibility(View.VISIBLE);
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
}
