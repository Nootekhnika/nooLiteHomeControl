package com.noolitef;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ConfirmDialog extends DialogFragment implements View.OnClickListener {
    private String title;
    private String message;

    private TextView textTitle;
    private TextView textMessage;
    private Button buttonAccept;
    private Button buttonDecline;

    private ConfirmDialogListener confirmDialogListener;

    public ConfirmDialog() {
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setConfirmDialogListener(ConfirmDialogListener listener) {
        this.confirmDialogListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.transparent);
        View dialogView = inflater.inflate(R.layout.dialog_confirm, null);
        textTitle = (TextView) dialogView.findViewById(R.id.dialog_confirm_title);
        textTitle.setText(title);
        textMessage = (TextView) dialogView.findViewById(R.id.dialog_confirm_message);
        textMessage.setText(message);
        buttonAccept = (Button) dialogView.findViewById(R.id.dialog_confirm_button_accept);
        buttonAccept.setOnClickListener(this);
        buttonDecline = (Button) dialogView.findViewById(R.id.dialog_confirm_button_decline);
        buttonDecline.setOnClickListener(this);
        return dialogView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_confirm_button_accept:
                if (confirmDialogListener != null) confirmDialogListener.onAccept();
                dismiss();
                break;
            case R.id.dialog_confirm_button_decline:
                if (confirmDialogListener != null) confirmDialogListener.onDecline();
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
