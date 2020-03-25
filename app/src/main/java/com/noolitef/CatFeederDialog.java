package com.noolitef;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.noolitef.ftx.RolletUnitF;
import com.noolitef.settings.Settings;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CatFeederDialog extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private RolletUnitF rolletUnitF;

    private ImageButton imageButton;
    private Toast toast;

    public CatFeederDialog() {
    }

    public static CatFeederDialog newInstance(FragmentManager fragmentManager, RolletUnitF rolletUnitF) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("rolletUnitF", rolletUnitF);

        CatFeederDialog catFeederDialog = (CatFeederDialog) fragmentManager.findFragmentByTag(CatFeederDialog.class.getSimpleName());
        if (catFeederDialog == null) {
            catFeederDialog = new CatFeederDialog();
        }
        catFeederDialog.setArguments(bundle);

        return catFeederDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
        rolletUnitF = (RolletUnitF) getArguments().getSerializable("rolletUnitF");
        toast = new Toast(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_cat_feeder, null);
        imageButton = dialogView.findViewById(R.id.button_feed_the_cat);
        imageButton.setOnClickListener(this);

        return dialogView;
    }

    @Override
    public void onViewCreated(@NotNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Window fragmentWindow = getDialog().getWindow();
        fragmentWindow.setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        client = homeActivity.getHttpClient();
    }


    @Override
    public void onStart() {
        super.onStart();

        //Window fragmentWindow = getDialog().getWindow();
        //fragmentWindow.setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_feed_the_cat:
                feedCat(rolletUnitF);
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        toast.cancel();
        super.onDestroyView();
    }

    private void feedCat(final RolletUnitF rolletUnitF) {
        if (client.dispatcher().runningCallsCount() > 0) return;

        buttonActivated(true);

        String command;

        if (rolletUnitF.isInversion())
            command = "0002080000000000000000".concat(rolletUnitF.getId());
        else
            command = "0002080000020000000000".concat(rolletUnitF.getId());

        Request openRequest = new Request.Builder()
                .url(Settings.URL().concat("send.htm?sd=").concat(command))
                .post(RequestBody.create(null, ""))
                .build();
        client.newCall(openRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();

                showToast("Нет соединения");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        showToast("Ошибка соединения");
                    }
                    call.cancel();

                    String cmd;

                    if (rolletUnitF.isInversion())
                        cmd = "0002080000020000000000".concat(rolletUnitF.getId());
                    else
                        cmd = "0002080000000000000000".concat(rolletUnitF.getId());

                    Thread.sleep(1000);

                    Request closeRequest = new Request.Builder()
                            .url(Settings.URL().concat("send.htm?sd=").concat(cmd))
                            .post(RequestBody.create(null, ""))
                            .build();
                    call = client.newCall(closeRequest);
                    response = call.execute();
                    if (!response.isSuccessful()) {
                        showToast("Ошибка соединения");
                    }
                    call.cancel();

                    cmd = "00020800000A0000000000".concat(rolletUnitF.getId());

                    Thread.sleep(1500);

                    Request stopRequest = new Request.Builder()
                            .url(Settings.URL().concat("send.htm?sd=").concat(cmd))
                            .post(RequestBody.create(null, ""))
                            .build();
                    call = client.newCall(stopRequest);
                    response = call.execute();
                    if (!response.isSuccessful()) {
                        showToast("Ошибка соединения");
                    }
                    call.cancel();

                } catch (Exception e) {
                    call.cancel();

                    showToast("Что-то пошло не так...");
                } finally {
                    buttonActivated(false);
                }
            }
        });
    }

    private void buttonActivated(final boolean activate) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageButton.setActivated(activate);
            }
        });
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                    toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
