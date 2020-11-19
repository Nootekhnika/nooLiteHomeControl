package com.noolitef;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ThermostatFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private ThermostatFragment thermostatFragment;
    private OkHttpClient client;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private Thermostat thermostat;
    private ScheduleTableDrawable scheduleTableDrawable;

    private ArrayList<ArrayList<ThermostatActivityInterval>> thermostatWeekActivityIntervals;
    private ArrayList<ThermostatActivityInterval> thermostatDayActivityIntervals;

    private Button bCancel;
    private TextView tvTemperature;
    private TextView tvRoom;
    private TextView tvName;
    private SwitchCompat swPowerSwitch;
    private ImageButton ibSettingsButton;
    private TableLayout tlSchedule;

    public ThermostatFragment() {
    }

    void send(OkHttpClient client, byte[] device, byte[] user, ArrayList<Room> rooms, Thermostat thermostat) {  // newInstanse()
        this.client = client;
        this.device = device;
        this.user = user;
        this.rooms = rooms;
        this.thermostat = thermostat;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(true);

        thermostatFragment = this;
        homeActivity = (HomeActivity) getActivity();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        homeActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int tableWidth = displayMetrics.widthPixels - (2 * getResources().getDimensionPixelSize(R.dimen.dp_16));
        scheduleTableDrawable = new ScheduleTableDrawable(getContext(), displayMetrics.density, tableWidth);  // to onStart()

        thermostatWeekActivityIntervals = new ArrayList<>();
        for (int wd = 0; wd < 7; wd++) {
            thermostatWeekActivityIntervals.add(new ArrayList<ThermostatActivityInterval>());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_thermostat, null);
        getDialog().setCanceledOnTouchOutside(true);

        bCancel = fragmentView.findViewById(R.id.fragment_thermostat_button_cancel);
        tvTemperature = fragmentView.findViewById(R.id.fragment_thermostat_text_temperature);
        tvRoom = fragmentView.findViewById(R.id.fragment_thermostat_text_room);
        tvName = fragmentView.findViewById(R.id.fragment_thermostat_text_name);
        swPowerSwitch = fragmentView.findViewById(R.id.fragment_thermostat_switch_state);
        ibSettingsButton = fragmentView.findViewById(R.id.fragment_thermostat_button_settings);
        tlSchedule = fragmentView.findViewById(R.id.fragment_thermostat_schedule_week);

        bCancel.setOnClickListener(this);
        ibSettingsButton.setOnClickListener(this);

        tvRoom.setText(thermostat.getRoom());
        tvName.setText(thermostat.getName());

        getState();
        getThermostatSchedule();

        return fragmentView;
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
        tlSchedule.setVisibility(View.GONE);
        switch (v.getId()) {
            case R.id.fragment_thermostat_button_cancel:
                dismiss();
                break;
            case R.id.fragment_thermostat_button_settings:
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
                                            tvRoom.setText(room);
                                            tvName.setText(name);
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
                unitSettingsFragment.send(client, device, user, rooms, thermostat);
                getChildFragmentManager().beginTransaction().add(unitSettingsFragment, "UNIT_SETTINGS_DIALOG").show(unitSettingsFragment).commit();
                break;

            case R.id.fragment_thermostat_settings_schedule_table_row_0:
                // show day schedule dialog
                break;
            case R.id.fragment_thermostat_settings_schedule_table_row_1:
                // show day schedule dialog
                break;
            case R.id.fragment_thermostat_settings_schedule_table_row_2:
                // show day schedule dialog
                break;
            case R.id.fragment_thermostat_settings_schedule_table_row_3:
                // show day schedule dialog
                break;
            case R.id.fragment_thermostat_settings_schedule_table_row_4:
                // show day schedule dialog
                break;
            case R.id.fragment_thermostat_settings_schedule_table_row_5:
                // show day schedule dialog
                break;
            case R.id.fragment_thermostat_settings_schedule_table_row_6:
                // show day schedule dialog
                break;
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private void getState() {
        // + READ_STATE: send.htm?sd=0002080000800000000000idididid
        Request request = new Request.Builder()
                .url(Settings.URL() + "state.htm")
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
                        parseState(response);
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showToast(String.format(Locale.ROOT, "Ошибка при инициализации термостата %s...", thermostat.getName()));
                    }
                } else {
                    response.close();
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void parseState(Response response) throws IOException {
        String hex = response.body().string();
        int b = 14 + (3 * thermostat.getIndex());
        int currentTemperature = Byte.parseByte(hex.substring(b + 1, b + 3), 16);
        setTemperature(currentTemperature);
    }

    private void setTemperature(final int temperature) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (-51 < temperature && temperature < 151) {
                    tvTemperature.setText(String.valueOf(temperature).concat("°C"));
                } else {
                    tvTemperature.setText("--°C");
                }
            }
        });
    }

    private void getThermostatSchedule() {
        Request request = new Request.Builder()
                .url(Settings.URL() + "settings.bin")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                initSchedule(thermostatWeekActivityIntervals);
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        parseThermostatSchedule(response);
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        initSchedule(thermostatWeekActivityIntervals);
                        showToast(String.format(Locale.ROOT, "Ошибка при инициализации настроек термостата %s...", thermostat.getName()));
                    }
                } else {
                    response.close();
                    call.cancel();
                    initSchedule(thermostatWeekActivityIntervals);
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void parseThermostatSchedule(Response response) throws IOException {
        byte[] settings = response.body().bytes();

        for (int b = 100; b < 2830; b += 273) {
            //if (b == 8197) b++; for auto.bin
            if (settings[b] == NooLiteF.AUTO_TYPE_THERMOSTAT) {
                String id = NooLiteF.getHexString(settings[b + 3] & 0xFF).concat(NooLiteF.getHexString(settings[b + 4] & 0xFF)).concat(NooLiteF.getHexString(settings[b + 5] & 0xFF)).concat(NooLiteF.getHexString(settings[b + 6] & 0xFF));
                if (id.equals(thermostat.getId())) {
                    if (settings[b + 1] == NooLiteF.AUTO_STATUS_ON) {
                        setAutoOn(true);
                    } else {
                        setAutoOn(false);
                    }

                    int dayOffset;
                    int intervalOffset;
                    for (int wd = 0; wd < 7; wd++) {
                        thermostatDayActivityIntervals = new ArrayList<>(6);
                        dayOffset = 7 + (wd * 36);
                        for (int i = 0; i < 6; i++) {
                            intervalOffset = i * 6;
                            if (settings[b + dayOffset + intervalOffset] == -1) continue;
                            thermostatDayActivityIntervals.add(new ThermostatActivityInterval(settings[b + dayOffset + intervalOffset], settings[b + dayOffset + intervalOffset + 1], settings[b + dayOffset + intervalOffset + 2], settings[b + dayOffset + intervalOffset + 3], settings[b + dayOffset + intervalOffset + 5]));
                        }
                        thermostatWeekActivityIntervals.set(wd, thermostatDayActivityIntervals);
                    }
                }
            }
        }

        initSchedule(thermostatWeekActivityIntervals);
    }

    private void setAutoOn(final boolean state) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swPowerSwitch.setChecked(state);
            }
        });
    }

    private void initSchedule(final ArrayList<ArrayList<ThermostatActivityInterval>> thermostatWeekActivityIntervals) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tlSchedule.removeAllViews();
                for (int row = 0; row < 8; row++) {
                    TableRow tableRow = new TableRow(getContext());
                    tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    ImageView imageView = new ImageView(getContext());
                    switch (row) {
                        case 0:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_0);
                            imageView.setImageBitmap(scheduleTableDrawable.drawTopTableRow(thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_first_background);
                            break;
                        case 1:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_1);
                            imageView.setImageBitmap(scheduleTableDrawable.drawMiddleTableRow(row, thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_middle_background);
                            break;
                        case 2:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_2);
                            imageView.setImageBitmap(scheduleTableDrawable.drawMiddleTableRow(row, thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_middle_background);
                            break;
                        case 3:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_3);
                            imageView.setImageBitmap(scheduleTableDrawable.drawMiddleTableRow(row, thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_middle_background);
                            break;
                        case 4:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_4);
                            imageView.setImageBitmap(scheduleTableDrawable.drawMiddleTableRow(row, thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_middle_background);
                            break;
                        case 5:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_5);
                            imageView.setImageBitmap(scheduleTableDrawable.drawMiddleTableRow(row, thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_middle_background);
                            break;
                        case 6:
                            tableRow.setId(R.id.fragment_thermostat_settings_schedule_table_row_6);
                            imageView.setImageBitmap(scheduleTableDrawable.drawBottomTableRow(thermostatWeekActivityIntervals.get(row)));
                            tableRow.setBackgroundResource(R.drawable.table_row_last_background);
                            break;
                        case 7:
                            imageView.setImageBitmap(scheduleTableDrawable.drawTimeLine());
                            break;
                    }
                    tableRow.addView(imageView);
                    tableRow.setOnClickListener(thermostatFragment);
                    tlSchedule.addView(tableRow, row);
                }
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