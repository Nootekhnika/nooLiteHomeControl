package com.noolitef.settings;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.R;

import java.util.concurrent.CopyOnWriteArrayList;

public class AppLogFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private CopyOnWriteArrayList errorLog;

    private Button buttonBack;
    private TextView editLog;

    public AppLogFragment() {
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
        View fragmentView = inflater.inflate(R.layout.fragment_app_error_log, null);
        buttonBack = fragmentView.findViewById(R.id.fragment_app_log_button_back);
        buttonBack.setOnClickListener(this);
        editLog = fragmentView.findViewById(R.id.fragment_app_error_log_edit);
        showErrorLog();
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
            case R.id.fragment_app_log_button_back:
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

    private void showErrorLog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                errorLog = homeActivity.getAppLog();
                if (errorLog == null) showToast("no records");
                else if (errorLog.size() == 0) showToast("no records");
                for (int i = 0; i < errorLog.size(); i++) {
                    addLogItem(errorLog.get(i).toString());
                }
            }
        }).start();
    }

    private void addLogItem(final String logItem) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editLog.append(logItem);
                editLog.append("\n\n");
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
