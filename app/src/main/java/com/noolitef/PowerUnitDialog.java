package com.noolitef;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.customview.PercentageSeekBar;
import com.noolitef.ftx.FTXUnitSettingsFragment;
import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.PowerUnitFA;
import com.noolitef.ftx.RolletUnitF;
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

public class PowerUnitDialog extends DialogFragment implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener, SeekBar.OnSeekBarChangeListener {
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private HomeActivity homeActivity;
    private SharedPreferences sharedPreferences;
    private boolean invertButtons;
    private Object unit;
    private PowerUnit powerUnit;
    private PowerUnitF powerUnitF;
    private PowerSocketF powerSocketF;
    private RolletUnitF rolletUnitF;

    private int position;
    // Add val: MODE, CTR, CMD
    private int channel;
    private String id;

    private int displayHeight;

    private ImageView icon;
    private TextView textRoom;
    private TextView textName;
    private LinearLayout seekLayout;
    private LinearLayout layoutRelay;
    private LinearLayout layoutPulseRelay;
    private LinearLayout layoutRollet;
    private PercentageSeekBar seekBrightness;
    private Button buttonOn;
    private Button buttonOff;
    private Button temporaryOn;
    private Button buttonOpen;
    private Button buttonStop;
    private Button buttonClose;
    private ImageButton buttonSettings;
    private Toast toast;

    private boolean buttonPressed;

    private BrightnessSetListener brightnessSetListener;

    public PowerUnitDialog() {
    }

    public void brightnessSetListener(BrightnessSetListener listener) {
        brightnessSetListener = listener;
    }

    public void send(OkHttpClient client, NooLiteF nooLiteF, byte[] device, byte[] user, ArrayList<Room> rooms, int position, Object unit) {
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.device = device;
        this.user = user;
        this.rooms = rooms;
        this.position = position;
        this.unit = unit;
        if (unit instanceof PowerUnit) {
            powerUnit = (PowerUnit) unit;
        }
        if (unit instanceof PowerUnitF) {
            powerUnitF = (PowerUnitF) unit;
        }
        if (unit instanceof PowerSocketF) {
            powerSocketF = (PowerSocketF) unit;
        }
        if (unit instanceof RolletUnitF) {
            rolletUnitF = (RolletUnitF) unit;
        }
    }

    public void setBrightness(int percent) {
        if (seekLayout.getVisibility() == View.VISIBLE) {
            if (!(powerUnitF instanceof PowerUnitFA)) {
                seekBrightness.setProgress(percent);
            }
        }
    }

    public void setRawData(int percent) {
        if (seekLayout.getVisibility() == View.VISIBLE) {
            if (powerUnitF instanceof PowerUnitFA) {
                seekBrightness.setProgress(percent);
                powerUnitF.setBrightness(percent);
            }
        }
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
        DisplayMetrics display = new DisplayMetrics();
        homeActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        displayHeight = display.heightPixels;

        View dialogView;
        if (Settings.isNightMode()) {
            dialogView = inflater.inflate(R.layout.dialog_power_unit_dark, null);
        } else {
            dialogView = inflater.inflate(R.layout.dialog_power_unit, null);
        }

        icon = (ImageView) dialogView.findViewById((R.id.dialog_power_unit_icon));
        textRoom = (TextView) dialogView.findViewById(R.id.dialog_power_unit_room);
        textName = (TextView) dialogView.findViewById(R.id.dialog_power_unit_name);
        seekLayout = (LinearLayout) dialogView.findViewById(R.id.dialog_power_unit_seek_bar_layout);
        layoutRelay = dialogView.findViewById(R.id.dialog_power_unit_layout_relay);
        layoutPulseRelay = dialogView.findViewById(R.id.dialog_power_unit_layout_pulse_relay);
        layoutRollet = dialogView.findViewById(R.id.dialog_power_unit_layout_rollet);
        seekBrightness = dialogView.findViewById(R.id.dialog_power_unit_seek_bar_brightness);
        if (Settings.isNightMode()) {
            seekBrightness.setTextColor(R.color.grey);
        }
        seekBrightness.setOnSeekBarChangeListener(this);

        if (powerUnit != null) {
            setupPowerUnit();
        }
        if (powerUnitF != null) {
            id = powerUnitF.getId();
            textRoom.setText(powerUnitF.getRoom());
            textName.setText(powerUnitF.getName());
            if (powerUnitF.isDimmer()) {
                seekBrightness.setProgress(powerUnitF.getBrightness());
                seekLayout.setVisibility(View.VISIBLE);
            }
            layoutRelay.setVisibility(View.VISIBLE);
        }
        if (powerSocketF != null) {
            id = powerSocketF.getId();
            if (Settings.isNightMode()) {
                icon.setImageResource(R.drawable.ic_power_socket_grey);
            } else {
                icon.setImageResource(R.drawable.ic_power_socket);
            }
            textRoom.setText(powerSocketF.getRoom());
            textName.setText(powerSocketF.getName());
            layoutRelay.setVisibility(View.VISIBLE);
        }
        if (rolletUnitF != null) {
            id = rolletUnitF.getId();
            if (Settings.isNightMode()) {
                icon.setImageResource(R.drawable.ic_rollet_grey);
            } else {
                icon.setImageResource(R.drawable.ic_rollet);
            }
            textRoom.setText(rolletUnitF.getRoom());
            textName.setText(rolletUnitF.getName());
            layoutRollet.setVisibility(View.VISIBLE);
        }

        buttonOn = (Button) dialogView.findViewById(R.id.dialog_power_unit_button_on);
        buttonOn.setOnClickListener(this);
        buttonOff = (Button) dialogView.findViewById(R.id.dialog_power_unit_button_off);
        buttonOff.setOnClickListener(this);
        temporaryOn = (Button) dialogView.findViewById(R.id.dialog_power_unit_button_temporary_on);
        temporaryOn.setOnClickListener(this);
        buttonOpen = (Button) dialogView.findViewById(R.id.dialog_power_unit_button_open);
        buttonOpen.setOnTouchListener(this);
        buttonOpen.setOnClickListener(this);
        buttonOpen.setOnLongClickListener(this);
        buttonStop = (Button) dialogView.findViewById(R.id.dialog_power_unit_button_stop);
        buttonStop.setOnClickListener(this);
        buttonClose = (Button) dialogView.findViewById(R.id.dialog_power_unit_button_close);
        buttonClose.setOnTouchListener(this);
        buttonClose.setOnClickListener(this);
        buttonClose.setOnLongClickListener(this);
        buttonSettings = (ImageButton) dialogView.findViewById(R.id.dialog_power_unit_settings);
        buttonSettings.setOnClickListener(this);

        // setup for preset
        if (device == null) {
            if (powerUnitF != null && powerUnitF.isDimmer()) {
                buttonSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FTXUnitSettingsFragment ftxUnitSettingsFragment = (FTXUnitSettingsFragment) getChildFragmentManager().findFragmentByTag("FTX_OTHER_SETTINGS_FRAGMENT");
                        if (ftxUnitSettingsFragment == null) {
                            ftxUnitSettingsFragment = new FTXUnitSettingsFragment();
                        }
                        if (ftxUnitSettingsFragment.isAdded()) return;
                        ftxUnitSettingsFragment.instance(client, powerUnitF);
                        getChildFragmentManager().beginTransaction().add(ftxUnitSettingsFragment, "FTX_OTHER_SETTINGS_FRAGMENT").show(ftxUnitSettingsFragment).commit();
                    }
                });
                buttonSettings.setVisibility(View.VISIBLE);
                seekBrightness.setProgress(powerUnitF.getPresetBrightness());
            } else {
                buttonSettings.setVisibility(View.INVISIBLE);
            }
            layoutRelay.setVisibility(View.GONE);
            layoutPulseRelay.setVisibility(View.GONE);
            layoutRollet.setVisibility(View.GONE);
        }

        invertOpenClose();

        getDialog().setCanceledOnTouchOutside(true);
        return dialogView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window fragmentWindow = getDialog().getWindow();
        fragmentWindow.setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (brightnessSetListener != null) {
            brightnessSetListener.setBrightness(seekBar.getProgress());
        }
        if (powerUnit != null) {
            if (powerUnit.getType() == PowerUnit.RGB_CONTROLLER)
                sendCommand(String.format(Locale.ROOT, "00000000%s0601%s00000000000000", NooLiteF.getHexString(channel), NooLiteF.getHexString((int) (seekBar.getProgress() * 1.28 + 28.5))));
            else
                sendCommand(String.format(Locale.ROOT, "00000000%s0601%s00000000000000", NooLiteF.getHexString(channel), NooLiteF.getHexString((int) (seekBar.getProgress() * 1.09 + 43.5))));
        }
        if (powerUnitF != null) {
            String format;
            int brightness;
            if (powerUnitF instanceof PowerUnitFA) {
                format = "01";
                brightness = seekBar.getProgress();
            } else {
                format = "00";
                brightness = (int) (seekBar.getProgress() * 2.55 + .5);
            }
            if (seekBar.getProgress() != 0)
                sendCommand(String.format(Locale.ROOT, "000208000006%s%s000000%s", format, NooLiteF.getHexString(brightness), powerUnitF.getId()));
            else
                sendCommand(String.format(Locale.ROOT, "000208000006%s01000000%s", format, powerUnitF.getId())); //zero percent to one
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (view.getId()) {
            case R.id.dialog_power_unit_button_open:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        buttonOpen.setPressed(false);
                        view.performClick();
                        return true;
                }
                break;
            case R.id.dialog_power_unit_button_close:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        buttonClose.setPressed(false);
                        view.performClick();
                        return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_power_unit_settings:
                UnitSettingsFragment unitSettingsFragment = (UnitSettingsFragment) getChildFragmentManager().findFragmentByTag("UNIT_SETTINGS_DIALOG");
                if (unitSettingsFragment == null) {
                    unitSettingsFragment = new UnitSettingsFragment();
                    unitSettingsFragment.setUnitSettingsFragmentListener(new UnitSettingsFragmentListener() {
                        @Override
                        public void onDismiss(final boolean unbind, final boolean update, final String room, final String name) {
                            if ((powerUnit != null && powerUnit.getType() == PowerUnit.ROLLET) || rolletUnitF != null)
                                invertOpenClose();
                            if (!unbind) {
                                if (update) {
                                    homeActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (powerUnit != null) setupPowerUnit();
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
                unitSettingsFragment.send(client, device, user, rooms, unit);
                getChildFragmentManager().beginTransaction().add(unitSettingsFragment, "UNIT_SETTINGS_DIALOG").show(unitSettingsFragment).commit();
                break;
            case R.id.dialog_power_unit_button_on:
                if (powerUnit != null) {
                    sendCommand(String.format(Locale.ROOT, "00000000%s02000000000000000000", NooLiteF.getHexString(channel)));
                }
                if (powerUnitF != null || powerSocketF != null) {
                    sendCommand(String.format(Locale.ROOT, "0002080000020000000000%s", id));
                }
                break;
            case R.id.dialog_power_unit_button_off:
                if (powerUnit != null) {
                    sendCommand(String.format(Locale.ROOT, "00000000%s00000000000000000000", NooLiteF.getHexString(channel)));
                }
                if (powerUnitF != null || powerSocketF != null) {
                    sendCommand(String.format(Locale.ROOT, "0002080000000000000000%s", id));
                }
                break;
            case R.id.dialog_power_unit_button_temporary_on:
                sendCommand(String.format(Locale.ROOT, "00000000%s19050100000000000000", NooLiteF.getHexString(channel)));
                break;
            case R.id.dialog_power_unit_button_open:
                if (powerUnit != null) {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "00000000%s02000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                    else
                        sendCommand(String.format(Locale.ROOT, "00000000%s00000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                } else {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "0002080000000000000000%s", id));
                    else
                        sendCommand(String.format(Locale.ROOT, "0002080000020000000000%s", id));
                }
                break;
            case R.id.dialog_power_unit_button_stop:
                if (powerUnit != null) {
                    sendCommand(String.format(Locale.ROOT, "00000000%s0A000000000000000000", NooLiteF.getHexString(channel)));
                } else {
                    sendCommand(String.format(Locale.ROOT, "00020800000A0000000000%s", id));
                }
                break;
            case R.id.dialog_power_unit_button_close:
                if (powerUnit != null) {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "00000000%s00000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                    else
                        sendCommand(String.format(Locale.ROOT, "00000000%s02000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                } else {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "0002080000020000000000%s", id));
                    else
                        sendCommand(String.format(Locale.ROOT, "0002080000000000000000%s", id));
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_power_unit_button_open:
                if (powerUnit != null) {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "00000000%s02000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                    else
                        sendCommand(String.format(Locale.ROOT, "00000000%s00000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                } else {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "0002080000000000000000%s", id));
                    else
                        sendCommand(String.format(Locale.ROOT, "0002080000020000000000%s", id));
                }
                break;
            case R.id.dialog_power_unit_button_close:
                if (powerUnit != null) {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "00000000%s00000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                    else
                        sendCommand(String.format(Locale.ROOT, "00000000%s02000000000000000000", NooLiteF.getHexString(channel)));  // must stay inverted
                } else {
                    if (invertButtons)
                        sendCommand(String.format(Locale.ROOT, "0002080000020000000000%s", id));
                    else
                        sendCommand(String.format(Locale.ROOT, "0002080000000000000000%s", id));
                }
                break;
        }
        return false;
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

    private void setupPowerUnit() {
        channel = powerUnit.getChannel();
        id = "00000000";
        textRoom.setText(powerUnit.getRoom());
        textName.setText(powerUnit.getName());
        if (device == null)  // for Preset
            seekBrightness.setProgress(powerUnit.getBrightness());
        switch (powerUnit.getType()) {
            case PowerUnit.DIMMER:
                if (Settings.isNightMode()) {
                    icon.setImageResource(R.drawable.ic_bulb_grey);
                } else {
                    icon.setImageResource(R.drawable.ic_bulb);
                }
                seekLayout.setVisibility(View.VISIBLE);
                layoutRelay.setVisibility(View.VISIBLE);
                layoutPulseRelay.setVisibility(View.GONE);
                layoutRollet.setVisibility(View.GONE);
                break;
            case PowerUnit.RGB_CONTROLLER:
                if (device != null) dismiss();  // for Preset
                if (Settings.isNightMode()) {
                    icon.setImageResource(R.drawable.ic_rgb_controller_grey);
                } else {
                    icon.setImageResource(R.drawable.ic_rgb_controller);
                }
                seekLayout.setVisibility(View.VISIBLE);
                layoutRelay.setVisibility(View.GONE);
                layoutPulseRelay.setVisibility(View.GONE);
                layoutRollet.setVisibility(View.GONE);
                break;
            case PowerUnit.RELAY:
                if (Settings.isNightMode()) {
                    icon.setImageResource(R.drawable.ic_bulb_grey);
                } else {
                    icon.setImageResource(R.drawable.ic_bulb);
                }
                seekLayout.setVisibility(View.GONE);
                layoutRelay.setVisibility(View.VISIBLE);
                layoutPulseRelay.setVisibility(View.GONE);
                layoutRollet.setVisibility(View.GONE);
                break;
            case PowerUnit.PULSE_RELAY:
                if (Settings.isNightMode()) {
                    icon.setImageResource(R.drawable.ic_gate_grey);
                } else {
                    icon.setImageResource(R.drawable.ic_gate);
                }
                seekLayout.setVisibility(View.GONE);
                layoutRelay.setVisibility(View.GONE);
                layoutPulseRelay.setVisibility(View.VISIBLE);
                layoutRollet.setVisibility(View.GONE);
                break;
            case PowerUnit.ROLLET:
                if (Settings.isNightMode()) {
                    icon.setImageResource(R.drawable.ic_rollet_grey);
                } else {
                    icon.setImageResource(R.drawable.ic_rollet);
                }
                seekLayout.setVisibility(View.GONE);
                layoutRelay.setVisibility(View.GONE);
                layoutPulseRelay.setVisibility(View.GONE);
                layoutRollet.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void invertOpenClose() {
        sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
        if (powerUnit != null && powerUnit.getType() == PowerUnit.ROLLET) {
            invertButtons = sharedPreferences.getBoolean(Integer.toString(channel), false);
        }
        if (rolletUnitF != null) {
            invertButtons = sharedPreferences.getBoolean(id, false);
        }
    }

    private void sendCommand(final String command) {
        Request request = new Request.Builder()
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
                try {
                    if (response.isSuccessful()) {
                        response.close();
                        call.cancel();
                        if (powerUnitF != null) {
                            Thread.sleep(Settings.switchTimeout());
                            nooLiteF.getPowerUnitFState(position, powerUnitF.getIndex());
                        }
                        if (powerSocketF != null) {
                            Thread.sleep(Settings.switchTimeout());
                            nooLiteF.getPowerUnitFState(position, powerSocketF.getIndex());
                        }
                        if (rolletUnitF != null) {
                            Thread.sleep(Settings.switchTimeout());
                            nooLiteF.getPowerUnitFState(position, rolletUnitF.getIndex());
                        }
                    } else {
                        response.close();
                        call.cancel();
                        showToast("Ошибка соединения " + response.code());
                    }
                    if (powerUnit != null) {
                        if (command.substring(10, 12).equals("19")) { // 19(hex) --> 25(int) CMD TemporaryOn
                            Thread.sleep(500);
                            Request request = new Request.Builder()
                                    .url(Settings.URL().concat(String.format(Locale.ROOT, "send.htm?sd=00000000%s00000000000000000000", NooLiteF.getHexString(channel))))
                                    .post(RequestBody.create(null, ""))
                                    .build();
                            client.newCall(request).execute();
                        }
                    }
                } catch (Exception e) {
                    response.close();
                    call.cancel();
                    showToast("Что-то пошло не так...");
                }
            }
        });
    }

    private void setPowerUnitFID(final String id) {
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", id))
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
                    try {
                        call.cancel();
                        Thread.sleep(100);
                        readPowerUnitFState(id);
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showToast(String.format(Locale.ROOT, "Ошибка при считывании состояния блока %s...", textName.getText()));
                    }
                } else {
                    response.close();
                    call.cancel();
                    showToast("Ошибка соединения " + response.code());
                }
            }
        });
    }

    private void readPowerUnitFState(String id) throws IOException, InterruptedException {
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801000000000%s", id))
                .post(RequestBody.create(null, ""))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            Thread.sleep(100);
            rxset();
        } else {
            response.close();
            call.cancel();
            showToast("Ошибка соединения " + response.code());
        }
    }

    private void rxset() throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "rxset.htm")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            rxsetParse(response.body().string());
            response.close();
            call.cancel();
        } else {
            response.close();
            call.cancel();
            showToast("Ошибка соединения " + response.code());
        }
    }

    private void rxsetParse(final String hex) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // rxset.setText(hex);
            }
        });
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                    toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, displayHeight / 5);
                    toast.show();
                }
            }
        });
    }
}
