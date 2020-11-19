package com.noolitef.tx;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.PRF64;
import com.noolitef.R;
import com.noolitef.Room;
import com.noolitef.UnitSettingsFragment;
import com.noolitef.UnitSettingsFragmentListener;
import com.noolitef.customview.PercentageSeekBar;
import com.noolitef.settings.Settings;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RGBControllerFragment extends DialogFragment implements View.OnClickListener, View.OnLongClickListener, SeekBar.OnSeekBarChangeListener {
    private OkHttpClient client;
    private HomeActivity homeActivity;
    private PRF64 nooLitePRF64;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private PowerUnit rgbController;
    private int red;
    private int green;
    private int blue;
    private boolean rainbow;
    private int rainbowSpeed;

    private Button buttonBack;
    private View viewResponseState;
    private TextView textRoom;
    private TextView textName;
    private ImageButton buttonSettings;
    private Button buttonSwitchColor;
    private Button buttonChooseColor;
    private PercentageSeekBar dimmer;
    private Button buttonSwitchMode;
    private Button buttonRainbowSpeed;
    private Button buttonOn;
    private Button buttonOff;
    private Toast toast;

    public RGBControllerFragment() {
    }

    public static RGBControllerFragment newInstance(PowerUnit rgbController) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("rgbController", rgbController);

        RGBControllerFragment rgbControllerFragment = new RGBControllerFragment();
        rgbControllerFragment.setArguments(bundle);

        return rgbControllerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
        rgbController = (PowerUnit) getArguments().getSerializable("rgbController");
        red = 0;
        green = 0;
        blue = 0;
        rainbowSpeed = 1;
        toast = new Toast(homeActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView;
        if (Settings.isNightMode()) {
            fragmentView = inflater.inflate(R.layout.fragment_rgb_controller_dark, null);
        } else {
            fragmentView = inflater.inflate(R.layout.fragment_rgb_controller, null);
        }

        buttonBack = fragmentView.findViewById(R.id.fragment_rgb_controller_button_back);
        buttonBack.setOnClickListener(this);
        viewResponseState = fragmentView.findViewById(R.id.fragment_rgb_controller_view_response_state);
        textRoom = fragmentView.findViewById(R.id.fragment_rgb_controller_text_room);
        textName = fragmentView.findViewById(R.id.fragment_rgb_controller_text_name);
        buttonSettings = fragmentView.findViewById(R.id.fragment_rgb_controller_button_settings);
        buttonSettings.setOnClickListener(this);
        buttonSwitchColor = fragmentView.findViewById(R.id.fragment_rgb_controller_button_switch_color);
        buttonSwitchColor.setOnClickListener(this);
        buttonChooseColor = fragmentView.findViewById(R.id.fragment_rgb_controller_button_choose_color);
        buttonChooseColor.setOnClickListener(this);
        buttonChooseColor.setOnLongClickListener(this);
        dimmer = fragmentView.findViewById(R.id.fragment_rgb_controller_dimmer);
        dimmer.setTextColor(R.color.black_light);
        dimmer.setOnSeekBarChangeListener(this);
        buttonSwitchMode = fragmentView.findViewById(R.id.fragment_rgb_controller_button_switch_mode);
        buttonSwitchMode.setOnClickListener(this);
        buttonRainbowSpeed = fragmentView.findViewById(R.id.fragment_rgb_controller_button_rainbow_speed);
        buttonRainbowSpeed.setOnClickListener(this);
        buttonOn = fragmentView.findViewById(R.id.fragment_rgb_controller_button_on);
        buttonOn.setOnClickListener(this);
        buttonOff = fragmentView.findViewById(R.id.fragment_rgb_controller_button_off);
        buttonOff.setOnClickListener(this);

        textRoom.setText(rgbController.getRoom());
        textName.setText(rgbController.getName());

        getDialog().setCanceledOnTouchOutside(true);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        client = homeActivity.getHttpClient();
        nooLitePRF64 = homeActivity.getPRF64();

        device = nooLitePRF64.getDevice();
        user = nooLitePRF64.getUser();
        rooms = nooLitePRF64.getHome().getRooms();
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
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        toast.cancel();

        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_rgb_controller_button_back:
                dismiss();
                break;
            case R.id.fragment_rgb_controller_button_settings:
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
                                            homeActivity.updatePRF64();
                                            if (rgbController.getType() != PowerUnit.RGB_CONTROLLER) {
                                                dismiss();
                                            }
                                        }
                                    });
                                }
                            } else {
                                homeActivity.showProgressBar();
                                homeActivity.updatePRF64();
                                dismiss();
                            }
                        }
                    });
                }
                if (unitSettingsFragment.isAdded()) return;
                unitSettingsFragment.send(client, device, user, rooms, rgbController);
                getChildFragmentManager().beginTransaction().add(unitSettingsFragment, "UNIT_SETTINGS_DIALOG").show(unitSettingsFragment).commit();
                break;
            case R.id.fragment_rgb_controller_button_switch_color:
                sendCommand(String.format(Locale.ROOT, "00000000%s11000000000000000000", NooLiteF.getHexString(rgbController.getChannel())));
                rainbow = false;
                setRainbow(false);
                break;
            case R.id.fragment_rgb_controller_button_choose_color:
                showMaterialColorPickerDialog();
                break;
            case R.id.fragment_rgb_controller_button_switch_mode:
                switchMode();
                break;
            case R.id.fragment_rgb_controller_button_rainbow_speed:
                switchRainbowSpeed();
                break;
            case R.id.fragment_rgb_controller_button_on:
                sendCommand(String.format(Locale.ROOT, "00000000%s02000000000000000000", NooLiteF.getHexString(rgbController.getChannel())));
                break;
            case R.id.fragment_rgb_controller_button_off:
                sendCommand(String.format(Locale.ROOT, "00000000%s00000000000000000000", NooLiteF.getHexString(rgbController.getChannel())));
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_rgb_controller_button_choose_color:
                //
                return true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        sendCommand(String.format(Locale.ROOT, "00000000%s0601%s00000000000000", NooLiteF.getHexString(rgbController.getChannel()), NooLiteF.getHexString((int) (seekBar.getProgress() * 1.28 + 28.5))));
    }

    public void sendColor(int red, int green, int blue) {
        sendCommand(String.format(Locale.ROOT, "00000000%s0603%s%s%s0000000000", NooLiteF.getHexString(rgbController.getChannel()), NooLiteF.getHexString(red), NooLiteF.getHexString(green), NooLiteF.getHexString(blue)));
        setRainbow(false);
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
                showResponseState(PowerUnit.RESPONSE_FAIL);
                showToast(homeActivity.getString(R.string.no_connection));
                hideResponseState();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        showResponseState(PowerUnit.RESPONSE_OK);
                        hideResponseState();
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showResponseState(PowerUnit.RESPONSE_FAIL);
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                        hideResponseState();
                    }
                } else {
                    response.close();
                    call.cancel();
                    showResponseState(PowerUnit.RESPONSE_FAIL);
                    showToast(homeActivity.getString(R.string.connection_error) + response.code());
                    hideResponseState();
                }
            }
        });
    }

    private void switchMode() {
        Request request = new Request.Builder()
                .url(Settings.URL().concat(String.format(Locale.ROOT, "send.htm?sd=00000000%s12000000000000000000", NooLiteF.getHexString(rgbController.getChannel()))))
                .post(RequestBody.create(null, ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                showResponseState(PowerUnit.RESPONSE_FAIL);
                showToast(homeActivity.getString(R.string.no_connection));
                hideResponseState();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        showResponseState(PowerUnit.RESPONSE_OK);
                        rainbow = !rainbow;
                        setRainbow(rainbow);
                        hideResponseState();
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showResponseState(PowerUnit.RESPONSE_FAIL);
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                        hideResponseState();
                    }
                } else {
                    response.close();
                    call.cancel();
                    showResponseState(PowerUnit.RESPONSE_FAIL);
                    showToast(homeActivity.getString(R.string.connection_error) + response.code());
                    hideResponseState();
                }
            }
        });
    }

    private void setRainbow(final boolean rainbow) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rainbow) {
                    buttonSwitchMode.setActivated(true);
                    buttonSwitchMode.setTextColor(getResources().getColor(R.color.white));
                } else {
                    if (Settings.isNightMode()) {
                        buttonSwitchMode.setTextColor(getResources().getColor(R.color.grey));
                    } else {
                        buttonSwitchMode.setTextColor(getResources().getColor(R.color.black_light));
                    }
                    buttonSwitchMode.setActivated(false);
                }
            }
        });
    }

    private void switchRainbowSpeed() {
        Request request = new Request.Builder()
                .url(Settings.URL().concat(String.format(Locale.ROOT, "send.htm?sd=00000000%s13000000000000000000", NooLiteF.getHexString(rgbController.getChannel()))))
                .post(RequestBody.create(null, ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                showResponseState(PowerUnit.RESPONSE_FAIL);
                showToast(homeActivity.getString(R.string.no_connection));
                hideResponseState();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        showResponseState(PowerUnit.RESPONSE_OK);
                        setRainbowSpeed(++rainbowSpeed);
                        if (rainbowSpeed > 2) rainbowSpeed = 0;
                        hideResponseState();
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showResponseState(PowerUnit.RESPONSE_FAIL);
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                        hideResponseState();
                    }
                } else {
                    response.close();
                    call.cancel();
                    showResponseState(PowerUnit.RESPONSE_FAIL);
                    showToast(homeActivity.getString(R.string.connection_error) + response.code());
                    hideResponseState();
                }
            }
        });
    }

    private void setRainbowSpeed(final int speed) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonRainbowSpeed.setText(String.format(Locale.ROOT, "Скорость: %d", speed));
            }
        });
    }

    private void showMaterialColorPickerDialog() {
        if (!isAdded()) return;
        ColorPicker colorPickerDialog = new ColorPicker(homeActivity, red, green, blue);
        colorPickerDialog.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(int color) {
                red = Color.red(color);
                green = Color.green(color);
                blue = Color.blue(color);
                sendColor(red, green, blue);
            }
        });
        colorPickerDialog.enableAutoClose();
        colorPickerDialog.show();
    }

    private void showResponseState(final int state) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state) {
                    case PowerUnit.RESPONSE_OK:
                        viewResponseState.setBackgroundResource(R.drawable.view_response_state_ok);
                        break;
                    case PowerUnit.RESPONSE_FAIL:
                        viewResponseState.setBackgroundResource(R.drawable.view_response_state_fail);
                        break;
                }
                viewResponseState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideResponseState() {
        if (!isAdded()) return;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewResponseState.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
                toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
