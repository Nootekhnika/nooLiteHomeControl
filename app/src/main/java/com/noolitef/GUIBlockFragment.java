package com.noolitef;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import com.noolitef.settings.Settings;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GUIBlockFragment extends DialogFragment {
    private ProgressBar progressBar;
    private Button buttonCancel;

    private boolean cancelable;
    private OkHttpClient client;

    public GUIBlockFragment() {
    }

    void setCancelable(OkHttpClient client) {
        cancelable = true;
        this.client = client;
    }

    void setNoneCancelable() {
        cancelable = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        setRetainInstance(true);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_gui_block, null);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.fragment_gui_block_progress_bar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.white))), PorterDuff.Mode.SRC_IN);
        }
        if (cancelable) {
            buttonCancel = (Button) fragmentView.findViewById(R.id.fragment_gui_block_button_cancel);
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelBind();
                    dismiss();
                }
            });
            buttonCancel.setVisibility(View.VISIBLE);
        }
        getDialog().setCanceledOnTouchOutside(false);
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window fragmentWindow = getDialog().getWindow();
        DisplayMetrics display = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(display);
        int displayWidth = display.widthPixels;
        int displayHeight = display.heightPixels;
        if (displayWidth < displayHeight) {
            fragmentWindow.setLayout(displayWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            fragmentWindow.setLayout(displayHeight, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void cancelBind() {
        final Request request = new Request.Builder()
                .url(Settings.URL().concat("send.htm?sd=000004000000000000000000000000"))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    call.cancel();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) call.cancel();
                    else call.cancel();
                } catch (IOException ex) {
                    call.cancel();
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                call.cancel();
            }
        });
    }
}
