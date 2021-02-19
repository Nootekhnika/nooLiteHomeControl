package com.noolitef;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

interface OnTemperatureSetListener {
    void onTemperatureSet(int temperature);
}

public class TemperatureSetterDialog extends DialogFragment implements View.OnClickListener {

    private OnTemperatureSetListener onTemperatureSetListener;

    private int temperature;

    private NumberPicker npTemperaturePicker;
    private Button bAccept;
    private Button bCancel;

    public void setTemperature(int temperature) {
        this.temperature = temperature;

    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            onTemperatureSetListener = (OnTemperatureSetListener) fragment;
        } catch (ClassCastException e) {
            Toast toast = new Toast(getContext());
            toast.setText(fragment.toString() + " must implement onTemperatureSetListener");
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialog = inflater.inflate(R.layout.dialog_temperature, null);
        npTemperaturePicker = (NumberPicker) dialog.findViewById(R.id.dialog_temperature_picker);
        npTemperaturePicker.setMinValue(5);
        npTemperaturePicker.setMaxValue(50);
        npTemperaturePicker.setValue(temperature);
        bAccept = (Button) dialog.findViewById(R.id.dialog_temperature_button_accept);
        bAccept.setOnClickListener(this);
        bCancel = (Button) dialog.findViewById(R.id.dialog_temperature_button_cancel);
        bCancel.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_temperature_button_accept:
                onTemperatureSetListener.onTemperatureSet(npTemperaturePicker.getValue());
                dismiss();
                break;
            case R.id.dialog_temperature_button_cancel:
                getDialog().cancel();
                break;
        }
    }
}
