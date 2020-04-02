package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.noolitef.APIFiles;
import com.noolitef.ConfirmDialog;
import com.noolitef.ConfirmDialogListener;
import com.noolitef.Home;
import com.noolitef.HomeActivity;
import com.noolitef.R;
import com.noolitef.GUIBlockFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsHomeFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private APIFiles apiFiles;
    private Call call;
    private byte[] user;
    private ArrayList<Home> homes;
    private int homeIndex;
    private Home home;
    private String homeName;

    private Button buttonBack;
    private Button buttonSave;
    private EditText editHome;
    private ProgressBar progressBarHomeUpdate;
    private Button buttonIPaddress;
    private Button buttonDeleteHome;
    private Button buttonAuthorization;
    private GUIBlockFragment guiBlockFragment;

    private SettingsHomeFragmentListener settingsHomeFragmentListener;

    public SettingsHomeFragment() {
    }

    public void send(OkHttpClient client, APIFiles apiFiles) {
        this.client = client;
        this.apiFiles = apiFiles;
    }

    public void setSettingsHomeFragmentListener(SettingsHomeFragmentListener listener) {
        settingsHomeFragmentListener = listener;
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
        View fragmentView;
        if (Settings.isNightMode()) {
            fragmentView = inflater.inflate(R.layout.fragment_settings_home_dark, null);
        } else {
            fragmentView = inflater.inflate(R.layout.fragment_settings_home, null);
        }
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_settings_home_button_back);
        buttonBack.setOnClickListener(this);
        buttonSave = (Button) fragmentView.findViewById(R.id.fragment_settings_home_button_save);
        buttonSave.setOnClickListener(this);
        editHome = (EditText) fragmentView.findViewById(R.id.fragment_settings_home_edit_name);
        progressBarHomeUpdate = (ProgressBar) fragmentView.findViewById(R.id.fragment_settings_home_progress_bar_update);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            progressBarHomeUpdate.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.white))), PorterDuff.Mode.SRC_IN);
//        }
        buttonIPaddress = fragmentView.findViewById(R.id.fragment_settings_home_button_ip_address);
        buttonIPaddress.setOnClickListener(this);
        buttonDeleteHome = fragmentView.findViewById(R.id.fragment_settings_home_button_delete);
        buttonDeleteHome.setOnClickListener(this);
        buttonAuthorization = fragmentView.findViewById(R.id.fragment_settings_home_button_authorization);
        buttonAuthorization.setOnClickListener(this);
        getHome();
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
            case R.id.fragment_settings_home_button_back:
                homeName = null;
                dismiss();
                break;
            case R.id.fragment_settings_home_button_save:
                if (user != null) {
                    blockUI();
                    changeHome();
                }
                break;
            case R.id.fragment_settings_home_button_ip_address:
                SettingsControllerIPFragment settingsControllerIPFragment = (SettingsControllerIPFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_IP_FRAGMENT");
                if (settingsControllerIPFragment == null) {
                    settingsControllerIPFragment = new SettingsControllerIPFragment();
                    settingsControllerIPFragment.send(client);
                }
                if (settingsControllerIPFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(settingsControllerIPFragment, "SETTINGS_CONTROLLER_IP_FRAGMENT").show(settingsControllerIPFragment).commit();
                break;
            case R.id.fragment_settings_home_button_authorization:
                SettingsControllerAuthorizationFragment settingsControllerAuthorizationFragment = (SettingsControllerAuthorizationFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT");
                if (settingsControllerAuthorizationFragment == null) {
                    settingsControllerAuthorizationFragment = new SettingsControllerAuthorizationFragment();
                    settingsControllerAuthorizationFragment.send(client);
                }
                if (settingsControllerAuthorizationFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(settingsControllerAuthorizationFragment, "SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT").show(settingsControllerAuthorizationFragment).commit();
                break;
            case R.id.fragment_settings_home_button_delete:
                ConfirmDialog confirmDialog = (ConfirmDialog) getChildFragmentManager().findFragmentByTag("CONFIRM_DIALOG");
                if (confirmDialog == null) {
                    confirmDialog = new ConfirmDialog();
                    confirmDialog.setTitle("Удаление дома");
                    confirmDialog.setMessage("Удалить дом ''".concat(homeName).concat("''?\nВсе настройки хранятся на контроллере и будут восстановлены при сонхронизации по URL-адресу дома."));
                    confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                        @Override
                        public void onAccept() {
                            blockUI();
                            deleteHome();
                        }

                        @Override
                        public void onDecline() {
                        }
                    });
                }
                if (confirmDialog.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (settingsHomeFragmentListener != null)
            settingsHomeFragmentListener.onDismiss(homeName);
        super.onDismiss(dialog);
    }

    @Override
    public void onStop() {
        if (call != null) call.cancel();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private void deleteHome() {
        homes.remove(homeIndex);
        saveHomeSet();
        dismiss();
    }

    private void addHome() {
        homes.add(home);
        saveHomeSet();
    }

    private void saveHomeSet() {
        Set<String> homeSet = new HashSet<>();
        for (Home home : homes) {
            homeSet.add(home.getString());
        }
        SharedPreferences sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("Homes", homeSet);
        editor.apply();
    }

    private void getHome() {
        if (apiFiles != null && apiFiles.getUser() != null && apiFiles.getUser().length == 12294) {
            user = apiFiles.getUser();
            try {
                parseHome(user);
            } catch (IOException e) {
                showToast(homeActivity.getString(R.string.some_thing_went_wrong));
            }
            return;
        }

        progressBarHomeUpdate.setVisibility(View.VISIBLE);
        Request request = new Request.Builder()
                .url(Settings.URL() + "user.bin")
                .build();
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                if (!isAdded()) return;
                showToast(homeActivity.getString(R.string.no_connection));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        parseHome(call, response);
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        if (!isAdded()) return;
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                    }
                } else {
                    response.close();
                    call.cancel();
                    if (!isAdded()) return;
                    showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
                }
            }
        });
    }

    private void parseHome(Call call, Response response) throws IOException {
        byte[] bytes = response.body().bytes();
        call.cancel();
        if (bytes.length != 12294) {
            throw new IOException("File ''user.bin'' not full");
        }

        user = bytes;

        byte[] homeBytes = new byte[64];
        for (int b = 8710, hb = 0; hb < 64; hb++, b++) {
            if (bytes[b] != 0) {
                homeBytes[hb] = bytes[b];
            } else {
                homeBytes[hb] = 20;
            }
        }
        homeName = new String(homeBytes, "cp1251").trim();

        if (homeName.length() > 1) {
            if (homeName.substring(0, 2).equalsIgnoreCase("яя")) homeName = "";
        }

        setHomeName(homeName);
    }

    private void parseHome(byte[] user) throws IOException {
        byte[] homeBytes = new byte[64];
        for (int b = 8710, hb = 0; hb < 64; hb++, b++) {
            if (user[b] != 0) {
                homeBytes[hb] = user[b];
            } else {
                homeBytes[hb] = 20;
            }
        }
        homeName = new String(homeBytes, "cp1251").trim();

        if (homeName.length() > 1) {
            if (homeName.substring(0, 2).equalsIgnoreCase("яя")) homeName = "";
        }

        setHomeName(homeName);
    }

    private void changeHome() {
        if (user.length != 12294) {
            showToast("Загрузка настроек...");
            getHome();
            return;
        }

        try {
            homeName = editHome.getText().toString();
            final byte[] name = editHome.getText().toString().getBytes("cp1251");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int nb = 0; nb < 64; nb++) {
                            if (nb < name.length)
                                user[8710 + nb] = name[nb];
                            else
                                user[8710 + nb] = 0;
                        }
                        if (apiFiles != null) apiFiles.setUser(user);
                        upload(new String(user, "cp1251"));
                    } catch (Exception e) {
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                    }
                }
            }).start();
        } catch (Exception e) {
            showToast(homeActivity.getString(R.string.some_thing_went_wrong));
        }
    }

    private void upload(String file) throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"user\"; filename=\"user.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(file)
                .concat("\r\n\r\n\r\n");
        Request request = new Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.getBytes("cp1251")))
                .build();
        call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            showToast("Сохранено");
            dismiss();
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void setHomeName(final String homeName) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editHome.setText(homeName);
                progressBarHomeUpdate.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unblockUI();
                progressBarHomeUpdate.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
}
