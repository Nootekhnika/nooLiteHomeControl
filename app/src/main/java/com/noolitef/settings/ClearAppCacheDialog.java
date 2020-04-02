package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.noolitef.GUIBlockFragment;
import com.noolitef.HomeActivity;
import com.noolitef.R;

import java.io.FileOutputStream;
import java.util.Arrays;

import okhttp3.OkHttpClient;

public class ClearAppCacheDialog extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private Button clearButton;
    private Button cancelButton;
    private GUIBlockFragment guiBlockFragment;

    public ClearAppCacheDialog() {
    }

    public void send(OkHttpClient client) {
        this.client = client;
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
        getDialog().getWindow().setBackgroundDrawableResource(R.color.transparent);
        View dialog;
        if (Settings.isNightMode()){
            dialog = inflater.inflate(R.layout.dialog_clear_cache_dark, null);
        } else {
            dialog = inflater.inflate(R.layout.dialog_clear_cache, null);
        }
        clearButton = (Button) dialog.findViewById(R.id.dialog_clear_cache_button_confirm);
        clearButton.setOnClickListener(this);
        cancelButton = (Button) dialog.findViewById(R.id.dialog_clear_cache_button_cancel);
        cancelButton.setOnClickListener(this);
        fakeTimer();
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_clear_cache_button_confirm:
                clearCache();
                break;
            case R.id.dialog_clear_cache_button_cancel:
                dismiss();
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

    private void fakeTimer() {
        enableClearButton(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int timeLeft = 3;
                    while (timeLeft > 0) {
                        showTimeLeft(timeLeft--);
                        Thread.sleep(1000);
                    }
                    enableClearButton(true);
                } catch (InterruptedException e) {
                    enableClearButton(true);
                }
            }
        }).start();
    }

    private void clearCache() {
        //toBackgroundThread?
        clearButton.setOnClickListener(null);
        FileOutputStream fileOutputStream;
        try {
            byte[] device = new byte[4102];
            Arrays.fill(device, (byte) 255);
            device[0] = 80;
            device[1] = 82;
            device[2] = 70;
            device[3] = 54;
            device[4] = 52;
            device[5] = 68;
            fileOutputStream = homeActivity.openFileOutput("cached_device.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(device);
            fileOutputStream.flush();
            fileOutputStream.close();

            byte[] user = new byte[12294];
            Arrays.fill(user, (byte) 255);
            user[0] = 80;
            user[1] = 82;
            user[2] = 70;
            user[3] = 54;
            user[4] = 52;
            user[5] = 85;
            fileOutputStream = homeActivity.openFileOutput("cached_user.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(user);
            fileOutputStream.flush();
            fileOutputStream.close();

            byte[] preset = new byte[32774];
            Arrays.fill(preset, (byte) 255);
            preset[0] = 80;
            preset[1] = 82;
            preset[2] = 70;
            preset[3] = 54;
            preset[4] = 52;
            preset[5] = 80;
            fileOutputStream = homeActivity.openFileOutput("cached_preset.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(preset);
            fileOutputStream.flush();
            fileOutputStream.close();

            byte[] timestamp = new byte[14];
            Arrays.fill(timestamp, (byte) 255);
            fileOutputStream = homeActivity.openFileOutput("cache_timestamp.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(timestamp);
            fileOutputStream.flush();
            fileOutputStream.close();

            byte[] log = new byte[0];
            fileOutputStream = homeActivity.openFileOutput("cached_log.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(log);
            fileOutputStream.flush();
            fileOutputStream.close();

            showToast("Кэш очищен");
            dismiss();
        } catch (Exception e) {
            clearButton.setOnClickListener(this);
            showToast("Ошикба при очистке кэша");
        }
    }

    private void enableClearButton(final boolean enable) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clearButton.setEnabled(enable);
                if (enable) {
                    clearButton.setText("Очистить");
                    clearButton.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    clearButton.setTextColor(getResources().getColor(R.color.grey));
                }
            }
        });
    }

    private void showTimeLeft(final int timeLeft) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clearButton.setText("Очистить | ".concat(Integer.toString(timeLeft)));
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
