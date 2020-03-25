package com.noolitef.timers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.noolitef.HomeActivity;
import com.noolitef.R;

import java.util.ArrayList;

interface ChoiceDevicesDialogListener {
    void onDismiss();
}

public class ChoiceDevicesDialog extends DialogFragment {
    private HomeActivity homeActivity;
    private Timer timer;
    private ArrayList<Object> devices;

    private Button buttonOk;
    private RecyclerView timerDevicesRecyclerView;
    private RecyclerView.LayoutManager timerDevicesLayoutManager;
    private TimerDevicesRecyclerAdapter timerDevicesRecyclerAdapter;

    private ChoiceDevicesDialogListener choiceDevicesDialogListener;

    public ChoiceDevicesDialog() {
    }

    void send(HomeActivity homeActivity, Timer timer, ArrayList<Object> devices) {
        this.homeActivity = homeActivity;
        this.timer = timer;
        this.devices = devices;
    }

    void setOnDismissListener(ChoiceDevicesDialogListener listener) {
        choiceDevicesDialogListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View dialog = inflater.inflate(R.layout.dialog_rooms_devices, null);

        buttonOk = (Button) dialog.findViewById(R.id.dialog_rooms_devices_button_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        timerDevicesRecyclerView = (RecyclerView) dialog.findViewById(R.id.dialog_rooms_devices_recycler_view);
        timerDevicesLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        timerDevicesRecyclerView.setLayoutManager(timerDevicesLayoutManager);
        timerDevicesRecyclerAdapter = new TimerDevicesRecyclerAdapter(homeActivity, timer, devices);
        timerDevicesRecyclerView.setAdapter(timerDevicesRecyclerAdapter);

        getDialog().setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        choiceDevicesDialogListener.onDismiss();

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
}
