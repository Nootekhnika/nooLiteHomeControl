package com.noolitef.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.R;
import com.noolitef.GUIBlockFragment;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClearLogDialog extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private Button clearButton;
    private Button cancelButton;
    private GUIBlockFragment guiBlockFragment;

    public ClearLogDialog() {
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
        if (Settings.isNightMode()) {
            dialog = inflater.inflate(R.layout.dialog_clear_log_dark, null);
        } else {
            dialog = inflater.inflate(R.layout.dialog_clear_log, null);
        }
        clearButton = (Button) dialog.findViewById(R.id.dialog_clear_log_button_confirm);
        clearButton.setOnClickListener(this);
        cancelButton = (Button) dialog.findViewById(R.id.dialog_clear_log_button_cancel);
        cancelButton.setOnClickListener(this);
        fakeTimer();
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_clear_log_button_confirm:
                clearLog();
                break;
            case R.id.dialog_clear_log_button_cancel:
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

    private void clearLog() {
        blockUI();
        Request request = new Request.Builder()
                .url(String.format(Locale.ROOT, Settings.URL() + "send.htm?sd=0109AABBC000000000000000000000"))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                showToast(homeActivity.getString(R.string.no_connection));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        response.close();
                        call.cancel();
                        Thread.sleep(8000);
                        showToast("Память логирования очищена");
                        dismiss();
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                    }
                } else {
                    response.close();
                    call.cancel();
                    showToast(homeActivity.getString(R.string.connection_error) + response.code());
                }
            }
        });
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
