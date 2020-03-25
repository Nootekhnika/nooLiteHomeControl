package com.noolitef;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.Toast;

public class DateSetterDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private OnDateSetListener onDateSetListener;

    private int year;
    private int month;
    private int date;

    public void setDate(int year, int month, int date) {
        this.year = year;
        this.month = month;
        this.date = date;
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            onDateSetListener = (OnDateSetListener) fragment;
        } catch (ClassCastException e) {
            Toast toast = Toast.makeText(getContext(), fragment.toString() + " must implement OnDateSetListener", Toast.LENGTH_LONG);
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog, this, year, month - 1, date);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        }
        //datePickerDialog.setTitle("Выберите дату");
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int date) {
        onDateSetListener.onDateSet(year, month, date);
    }
}
