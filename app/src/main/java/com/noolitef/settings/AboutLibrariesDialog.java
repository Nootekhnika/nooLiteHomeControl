package com.noolitef.settings;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.noolitef.R;

public class AboutLibrariesDialog extends DialogFragment implements View.OnClickListener {
    private ImageButton closeButton;

    public AboutLibrariesDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawableResource(R.color.transparent);
        getDialog().setCanceledOnTouchOutside(true);
        View dialog = inflater.inflate(R.layout.dialog_about_libraries, null);
        closeButton = dialog.findViewById(R.id.dialog_libraries_button_close);
        closeButton.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_libraries_button_close:
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
}
