package com.noolitef.settings;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.DateSetterDialog;
import com.noolitef.FragmentListener;
import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.OnDateSetListener;
import com.noolitef.OnTimeSetListener;
import com.noolitef.R;
import com.noolitef.TimeSetterDialog;
import com.noolitef.GUIBlockFragment;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsControllerDateTimeFragment extends DialogFragment implements View.OnClickListener, OnDateSetListener, OnTimeSetListener, CompoundButton.OnCheckedChangeListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private byte[] settings;

    private Button buttonBack;
    private Button buttonSave;
    private ProgressBar progressBarDateTimeUpdate;
    private TextView textDate;
    private TextView textTime;
    private SwitchCompat switchAutomatically;
    private GUIBlockFragment guiBlockFragment;

    private BroadcastReceiver minuteChangedReceiver;
    private boolean updateControllerDateTime;
    private FragmentListener fragmentListener;
    private boolean updateParentFragment;
    private Calendar calendar;

    public SettingsControllerDateTimeFragment() {
    }

    public void send(OkHttpClient client) {
        this.client = client;
    }

    public void setFragmentListener(FragmentListener listener) {
        fragmentListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View fragmentView;
        if (Settings.isNightMode()) {
            fragmentView = inflater.inflate(R.layout.fragment_settings_controller_date_time_dark, null);
        } else {
            fragmentView = inflater.inflate(R.layout.fragment_settings_controller_date_time, null);
        }
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_date_time_button_back);
        buttonBack.setOnClickListener(this);
        buttonSave = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_date_time_button_save);
        buttonSave.setOnClickListener(this);
        progressBarDateTimeUpdate = (ProgressBar) fragmentView.findViewById(R.id.fragment_settings_controller_date_time_progress_bar_update);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            progressBarDateTimeUpdate.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.white))), PorterDuff.Mode.SRC_IN);
        }
        textDate = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_date_time_text_view_date);
        textDate.setOnClickListener(this);
        textTime = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_date_time_text_view_time);
        textTime.setOnClickListener(this);
        switchAutomatically = (SwitchCompat) fragmentView.findViewById(R.id.fragment_settings_controller_date_time_switch_automatically);
        switchAutomatically.setOnCheckedChangeListener(this);

        getControllerDateTime();
        setBroadcastReceiver();

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
        switch (v.getId()) {
            case R.id.fragment_settings_controller_date_time_button_back:
                dismiss();
                break;
            case R.id.fragment_settings_controller_date_time_button_save:
                setControllerDateTime();
                break;
            case R.id.fragment_settings_controller_date_time_text_view_date:
                DateSetterDialog dateSetterDialog = (DateSetterDialog) getChildFragmentManager().findFragmentByTag("DATE_SETTER_DIALOG");
                if (dateSetterDialog == null) {
                    dateSetterDialog = new DateSetterDialog();
                }
                if (dateSetterDialog.isAdded()) return;
                String date = textDate.getText().toString();
                dateSetterDialog.setDate(2000 + Integer.parseInt(date.substring(6, 8)), Integer.parseInt(date.substring(3, 5)), Integer.parseInt(date.substring(0, 2)));
                getChildFragmentManager().beginTransaction().add(dateSetterDialog, "DATE_SETTER_DIALOG").show(dateSetterDialog).commit();
                break;
            case R.id.fragment_settings_controller_date_time_text_view_time:
                TimeSetterDialog timeSetterDialog = (TimeSetterDialog) getChildFragmentManager().findFragmentByTag("TIME_SETTER_DIALOG");
                if (timeSetterDialog == null) {
                    timeSetterDialog = new TimeSetterDialog();
                }
                if (timeSetterDialog.isAdded()) return;
                String time = textTime.getText().toString();
                timeSetterDialog.setTime(Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(3, 5)));
                getChildFragmentManager().beginTransaction().add(timeSetterDialog, "TIME_SETTER_DIALOG").show(timeSetterDialog).commit();
                break;
        }
    }

    @Override
    public void onDateSet(int year, int month, int date) {
        updateControllerDateTime = false;
        textDate.setText(String.format(Locale.ROOT, "%02d.%02d.%02d", date, month + 1, year % 100));
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        updateControllerDateTime = false;
        textTime.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (fragmentListener != null) fragmentListener.onDismiss(updateParentFragment);
        super.onDismiss(dialog);
    }

    @Override
    public void onDestroyView() {
        if (minuteChangedReceiver != null) getContext().unregisterReceiver(minuteChangedReceiver);
        if (client != null) client.dispatcher().cancelAll();

        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private void setBroadcastReceiver() {
        minuteChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                    getControllerDateTime();
                }
            }
        };
        getContext().registerReceiver(minuteChangedReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        updateControllerDateTime = true;
    }

    private void getControllerDateTime() {
        progressBarDateTimeUpdate.setVisibility(View.VISIBLE);
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
        StringBuilder date = new StringBuilder();
        date.append(String.format(Locale.ROOT, "%02d.", Integer.parseInt(hex.substring(8, 10), 16)));
        date.append(String.format(Locale.ROOT, "%02d.", Integer.parseInt(hex.substring(10, 12), 16)));
        date.append(String.format(Locale.ROOT, "%02d", Integer.parseInt(hex.substring(12, 14), 16)));
        StringBuilder time = new StringBuilder();
        time.append(String.format(Locale.ROOT, "%02d:", Integer.parseInt(hex.substring(4, 6), 16)));
        time.append(String.format(Locale.ROOT, "%02d", Integer.parseInt(hex.substring(2, 4), 16)));
        showDateTime(date.toString(), time.toString());
    }

    private void getDateTimeSettings() {
        progressBarDateTimeUpdate.setVisibility(View.VISIBLE);
        Request request = new Request.Builder()
                .url(Settings.URL() + "settings.bin")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!isAdded()) return;
                showToast(homeActivity.getString(R.string.no_connection));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        parseDateTimeSettings(call, response);
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

    private void parseDateTimeSettings(Call call, Response response) throws IOException {
        settings = response.body().bytes();
        response.close();
        call.cancel();
        if (settings.length != 4102) throw new IOException("File ''settings.bin'' is not full");

        StringBuilder timeSetting = new StringBuilder();
        timeSetting.append(String.format("%8s", Integer.toBinaryString(settings[6] & 0xFF)).replace(' ', '0'));
        if (timeSetting.charAt(0) == '1') {
            setSwitchAutomaticallyChecked(true);
        } else {
            setSwitchAutomaticallyChecked(false);
        }
    }

    private void setControllerDateTime() {
        blockUI();
        if (switchAutomatically.isChecked()) {
            calendar = Calendar.getInstance();
            textDate.setText(String.format(Locale.ROOT, "%02d.%02d.%02d", calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR) % 100));
            textTime.setText(String.format(Locale.ROOT, "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
        }
        String date = textDate.getText().toString();
        String time = textTime.getText().toString();
        long unixTime = NooLiteF.getUNIXtime(Integer.parseInt(date.substring(6, 8)), Integer.parseInt(date.substring(3, 5)), Integer.parseInt(date.substring(0, 2)), Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(3, 5)));
        String hexTime = (String.format("%8s", Long.toHexString(unixTime)).replace(' ', '0')).toUpperCase();
        Request request = new Request.Builder()
                .url(Settings.URL() + String.format("send.htm?sd=010B%s000000000000000000", hexTime))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                showToast(homeActivity.getString(R.string.no_connection));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        changeSettings();
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                    }
                } else {
                    response.close();
                    call.cancel();
                    showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
                }
            }
        });
    }

    private void changeSettings() throws IOException {
        StringBuilder timeSetting = new StringBuilder();
        timeSetting.append(String.format("%8s", Integer.toBinaryString(settings[6] & 0xFF)).replace(' ', '0'));
        if (switchAutomatically.isChecked()) {
            timeSetting.replace(0, 1, "1");
        } else {
            timeSetting.replace(0, 1, "0");
        }
        timeSetting.replace(1, 2, "0"); // выкл переход на летнее время
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        int hoursOffsetConstant = (int) TimeUnit.HOURS.convert(timeZone.getRawOffset(), TimeUnit.MILLISECONDS) + 11;
        String hoursOffsetSixBits = String.format("%6s", Integer.toBinaryString(hoursOffsetConstant)).replace(' ', '0');
        timeSetting.replace(2, 8, hoursOffsetSixBits);
        settings[6] = (byte) Integer.parseInt(timeSetting.toString(), 2);
        uploadSettings();
    }

    private void uploadSettings() throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"settings\"; filename=\"settings.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(new String(settings, "cp1251"))
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
            updateParentFragment = true;
            dismiss();
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void showDateTime(final String date, final String time) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarDateTimeUpdate.setVisibility(View.INVISIBLE);
                if (updateControllerDateTime) {
                    textDate.setText(date);
                    textTime.setText(time);
                    getDateTimeSettings();
                }
            }
        });
    }

    private void setSwitchAutomaticallyChecked(final boolean checked) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switchAutomatically.setChecked(checked);
                progressBarDateTimeUpdate.setVisibility(View.INVISIBLE);
            }
        });
    }


    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unblockUI();
                progressBarDateTimeUpdate.setVisibility(View.INVISIBLE);
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
