package com.noolitef;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimeSetterDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private OnTimeSetListener onTimeSetListener;

    private int hour;
    private int minute;

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            onTimeSetListener = (OnTimeSetListener) fragment;
        } catch (ClassCastException e) {
            Toast toast = Toast.makeText(getContext(), fragment.toString() + " must implement OnTimeSetListener", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog, this, hour, minute, true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            timePickerDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        }
        //timePickerDialog.setTitle("Выберите время");
        return timePickerDialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        onTimeSetListener.onTimeSet(hour, minute);
    }
}
