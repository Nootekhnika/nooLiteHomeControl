package com.noolitef;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.customview.ThermostatSeekBar;
import com.noolitef.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ThermostatDialog extends DialogFragment implements View.OnClickListener, View.OnTouchListener {
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private HomeActivity homeActivity;
    private Thermostat thermostat;

    private String id;

    private TextView textCurrentTemperature;
    private TextView textTargetTemperature;
    private ProgressBar progressTargetTemperature;
    private TextView textRoom;
    private TextView textName;
    private ThermostatSeekBar seekTemperature;
    private Button buttonOn;
    private Button buttonOff;
    private ImageButton buttonSettings;
    private Toast toast;

    private float startP;
    private float startX;

    private TemperatureListener temperatureListener;

    public ThermostatDialog() {
    }

    public void send(OkHttpClient client, NooLiteF nooLiteF, byte[] device, byte[] user, ArrayList<Room> rooms, Thermostat thermostat) {
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.device = device;
        this.user = user;
        this.rooms = rooms;
        this.thermostat = thermostat;
    }

    public void setTemperatureListener(TemperatureListener listener) {
        temperatureListener = listener;
    }

    void setCurrentTemperature(int degree) {
        if (-51 < degree && degree < 101) {
            textCurrentTemperature.setText(String.format(Locale.ROOT, "%d°C", degree));
        } else {
            textCurrentTemperature.setText("--°C");
        }
    }

    void setTargetTemperature(final int degree) {
        progressTargetTemperature.setVisibility(View.GONE);
        if (4 < degree && degree < 51) {
            textTargetTemperature.setText(String.format(Locale.ROOT, "%d°C", degree));
        } else {
            textTargetTemperature.setText("--°C");
            seekTemperature.setProgress(0);
        }
        seekTemperature.setEnabled(true);
    }

    void setOutputState(int outputState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            switch (outputState) {
                case Thermostat.OUTPUT_OFF:
                    if (Settings.isNightMode()) {
                        textTargetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out_off_light);
                        textTargetTemperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(dialog.getContext(), R.color.grey))));
                    } else {
                        textTargetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out_off);
                        textTargetTemperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(dialog.getContext(), R.color.black_light))));
                    }
                    break;
                case Thermostat.OUTPUT_ON:
                    textTargetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out_on);
                    textTargetTemperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(dialog.getContext(), R.color.white))));
                    break;
            }
        }
    }

    void updateState() {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurrentTemperature(thermostat.getCurrentTemperature());
                setTargetTemperature(thermostat.getTargetTemperature());
                setOutputState(thermostat.getOutputState());
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        homeActivity = (HomeActivity) getActivity();

        toast = new Toast(getContext());

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView;
        if (Settings.isNightMode()) {
            dialogView = inflater.inflate(R.layout.dialog_thermostat_dark, null);
        } else {
            dialogView = inflater.inflate(R.layout.dialog_thermostat, null);
        }

        id = thermostat.getId();
        textCurrentTemperature = dialogView.findViewById(R.id.dialog_thermostat_current_temperature);
        textTargetTemperature = dialogView.findViewById(R.id.dialog_thermostat_target_temperature);
        progressTargetTemperature = dialogView.findViewById(R.id.dialog_thermostat_target_temperature_updating);
        textRoom = dialogView.findViewById(R.id.dialog_thermostat_room);
        textRoom.setText(thermostat.getRoom());
        textName = dialogView.findViewById(R.id.dialog_thermostat_name);
        textName.setText(thermostat.getName());
        seekTemperature = dialogView.findViewById(R.id.dialog_thermostat_seek_bar_temperature);
        seekTemperature.setProgress(thermostat.getTargetTemperature() - 5);
        if (Settings.isNightMode()) {
            seekTemperature.setTextColor(R.color.grey);
        }
        seekTemperature.setOnTouchListener(this);
        buttonOn = dialogView.findViewById(R.id.dialog_thermostat_button_on);
        buttonOn.setOnClickListener(this);
        buttonOff = dialogView.findViewById(R.id.dialog_thermostat_button_off);
        buttonOff.setOnClickListener(this);
        buttonSettings = dialogView.findViewById(R.id.dialog_thermostat_settings);
        buttonSettings.setOnClickListener(this);

        if (client == null) {
            setCurrentTemperature(thermostat.getCurrentTemperature());
            textTargetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out);
            setTargetTemperature(thermostat.getPresetTemperature());
            seekTemperature.setProgress(thermostat.getPresetTemperature() - 5);
            buttonSettings.setVisibility(View.GONE);
            buttonOn.setVisibility(View.GONE);
            buttonOff.setVisibility(View.GONE);
        } else {
            updateState();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    homeActivity.getThermostatState(thermostat);
                }
            }).start();
        }

        getDialog().setCanceledOnTouchOutside(true);
        return dialogView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window fragmentWindow = getDialog().getWindow();
        fragmentWindow.setBackgroundDrawableResource(android.R.color.transparent);
    }

//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//    }
//
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        thermostat.setTargetTemperature(seekBar.getProgress() + 5);
//        homeActivity.setThermostatTargetTemperature(thermostat);
//    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startP = seekTemperature.getProgress();
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                seekTemperature.setProgress((int) (startP + (55.0f / seekTemperature.getWidth() * (event.getX() - startX))));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (temperatureListener == null) {
                    seekTemperature.setEnabled(false);
                    textTargetTemperature.setText("");
                    progressTargetTemperature.setVisibility(View.VISIBLE);
                    thermostat.setTargetTemperature(seekTemperature.getProgress() + 5);
                    homeActivity.setThermostatTargetTemperature(thermostat);
                } else {
                    temperatureListener.setTemperature(seekTemperature.getProgress() + 5);
                    setTargetTemperature(seekTemperature.getProgress() + 5);
                }
                break;
        }

        view.performClick();
        return true;
    }

    @Override
    public void onClick(View view) {
        if (!seekTemperature.isEnabled()) return;

        switch (view.getId()) {
            case R.id.dialog_thermostat_settings:
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
                unitSettingsFragment.send(client, device, user, rooms, thermostat);
                getChildFragmentManager().beginTransaction().add(unitSettingsFragment, "UNIT_SETTINGS_DIALOG").show(unitSettingsFragment).commit();
                break;
            case R.id.dialog_thermostat_button_on:
                sendCommand(String.format(Locale.ROOT, "0002080000020000000000%s", id));
                break;
            case R.id.dialog_thermostat_button_off:
                sendCommand(String.format(Locale.ROOT, "0002080000000000000000%s", id));
                break;
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        toast.cancel();
        super.onDestroyView();
    }

    private void sendCommand(final String command) {
        final Request request = new Request.Builder()
                .url(Settings.URL().concat("send.htm?sd=").concat(command))
                .post(RequestBody.create(null, ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                showToast("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        call.cancel();
                        Thread.sleep(Settings.switchTimeout());
                        homeActivity.getThermostatState(thermostat);

                        Thread.sleep(1000);

                        call = client.newCall(request);
                        response = call.execute();
                        if (response.isSuccessful()) {
                            call.cancel();
                            Thread.sleep(Settings.switchTimeout());
                            homeActivity.getThermostatState(thermostat);
                            return;
                        }
                    } catch (Exception e) {
                        call.cancel();
                        showToast("Что-то пошло не так...");
                        return;
                    }
                }
                call.cancel();
                showToast("Ошибка соединения " + response.code());
            }
        });
    }

    public void showToast(final String message) {
        if (!isAdded()) return;
        if (homeActivity == null) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(homeActivity, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
