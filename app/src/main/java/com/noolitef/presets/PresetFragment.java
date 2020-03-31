package com.noolitef.presets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.APIFiles;
import com.noolitef.ConfirmDialog;
import com.noolitef.ConfirmDialogListener;
import com.noolitef.GUIBlockFragment;
import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.R;
import com.noolitef.Room;
import com.noolitef.Thermostat;
import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.settings.Settings;
import com.noolitef.tx.PowerUnit;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PresetFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private APIFiles apiFiles;
    private Preset preset;
    private ArrayList<Object> devices;

    private boolean updatePresets;

    private Button cancelButton;
    private LinearLayout layoutDelete;
    private Button deleteButton;
    private TextView titleText;
    private Button saveButton;
    private EditText nameEdit;
    private RecyclerView presetDevicesRecyclerView;
    private LinearLayoutManager presetsDevicesLayoutManager;
    private PresetDevicesRecyclerAdapter presetDevicesRecyclerAdapter;
    private GUIBlockFragment guiBlockFragment;

    private PresetFragmentListener presetFragmentListener;

    public PresetFragment() {
    }

    public void setPresetFragmentListener(PresetFragmentListener listener) {
        presetFragmentListener = listener;
    }

    public void send(OkHttpClient client, NooLiteF nooLiteF, APIFiles apiFiles, Preset preset, ArrayList<Object> devices) {
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.apiFiles = apiFiles;
        this.preset = preset;
        this.devices = nooLiteTXFilter(devices);
        this.updatePresets = false;

        for (Object device : this.devices) {
            if (device instanceof PowerUnit)
                ((PowerUnit) device).setPreset(false);
            if (device instanceof PowerUnitF)
                ((PowerUnitF) device).setPreset(false);
            if (device instanceof PowerSocketF)
                ((PowerSocketF) device).setPreset(false);
            if (device instanceof Thermostat)
                ((Thermostat) device).setPreset(false);
            if (device instanceof RolletUnitF)
                ((RolletUnitF) device).setPreset(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View fragment;
        if (Settings.isNightMode()) {
            fragment = inflater.inflate(R.layout.fragment_preset_dark, null);
        } else {
            fragment = inflater.inflate(R.layout.fragment_preset, null);
        }

        cancelButton = (Button) fragment.findViewById(R.id.fragment_preset_button_cancel);
        cancelButton.setOnClickListener(this);
        layoutDelete = (LinearLayout) fragment.findViewById(R.id.fragment_preset_layout_delete);
        deleteButton = (Button) fragment.findViewById(R.id.fragment_preset_button_delete);
        deleteButton.setOnClickListener(this);
        titleText = (TextView) fragment.findViewById(R.id.fragment_preset_title);
        saveButton = (Button) fragment.findViewById(R.id.fragment_preset_button_save);
        saveButton.setOnClickListener(this);
        nameEdit = (EditText) fragment.findViewById(R.id.fragment_preset_edit_name);
        presetDevicesRecyclerView = (RecyclerView) fragment.findViewById(R.id.fragment_preset_recycler_view_rooms_devices);
        presetsDevicesLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        presetDevicesRecyclerView.setLayoutManager(presetsDevicesLayoutManager);
        presetDevicesRecyclerAdapter = new PresetDevicesRecyclerAdapter(client, nooLiteF, (HomeActivity) getActivity(), this, devices, preset);
        presetDevicesRecyclerView.setAdapter(presetDevicesRecyclerAdapter);

        if (preset != null) {
            layoutDelete.setVisibility(View.VISIBLE);
            titleText.setText("Редактирование");
            nameEdit.setText(preset.getName());
        }

        getDialog().setCanceledOnTouchOutside(true);
        return fragment;
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
            case R.id.fragment_preset_button_cancel:
                dismiss();
                break;
            case R.id.fragment_preset_button_delete:
                ConfirmDialog confirmDialog = (ConfirmDialog) getChildFragmentManager().findFragmentByTag("CONFIRM_DIALOG");
                if (confirmDialog == null) {
                    confirmDialog = new ConfirmDialog();
                    confirmDialog.setTitle("Удаление сценария");
                    confirmDialog.setMessage("Удалить сценарий ''".concat(preset.getName()).concat("''?"));
                    confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                        @Override
                        public void onAccept() {
                            blockUI();
                            delPreset(preset.getIndex());
                        }

                        @Override
                        public void onDecline() {
                        }
                    });
                }
                if (confirmDialog.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit();
                break;
            case R.id.fragment_preset_button_save:
                if (nameEdit.getText().length() == 0) {
                    showToast("Назовите сценарий");
                    return;
                }
                blockUI();
                if (preset == null) {
                    addPreset(devices);
                } else {
                    changePreset(preset);
                }
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        presetFragmentListener.onDismiss(updatePresets);
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

    private ArrayList<Object> nooLiteTXFilter(ArrayList<Object> units) {
        ArrayList<Object> presetUnits = new ArrayList<>();
        for (Object presetUnit : units) {
            if (presetUnit instanceof Room) {
                presetUnits.add(presetUnit);
            }
            if (presetUnit instanceof PowerUnit) {
                switch (((PowerUnit) presetUnit).getType()) {
                    case PowerUnit.RELAY:
                    case PowerUnit.DIMMER:
                    case PowerUnit.RGB_CONTROLLER:
                        presetUnits.add(presetUnit);
                }
            }
            if (presetUnit instanceof PowerUnitF) {
                presetUnits.add(presetUnit);
            }
            if (presetUnit instanceof PowerSocketF) {
                presetUnits.add(presetUnit);
            }
            if (presetUnit instanceof Thermostat) {
                presetUnits.add(presetUnit);
            }
            if (presetUnit instanceof RolletUnitF) {
                presetUnits.add(presetUnit);
            }
        }
        return presetUnits;
    }

    private void addPreset(final ArrayList<Object> devices) {
        new Thread(new Runnable() {
            public void run() {
                byte[] preset = apiFiles.getPreset();
                for (int b = 6, i = 0; b < 32774; i++, b += 1024) {
                    if ((preset[b] & 0xFF) == 255) {
                        int wb = b;
                        int c = 0;
                        for (Object device : devices) {
                            if (c < 73) {
                                if (device instanceof PowerUnit) {
                                    PowerUnit powerUnit = (PowerUnit) device;
                                    if (powerUnit.isPreset()) {
                                        for (int rb = 0; rb < 14; rb++) {
                                            preset[wb + (14 * c) + rb] = powerUnit.getCommand()[rb];
                                        }
                                        c++;
                                    }
                                }
                                if (device instanceof PowerUnitF) {
                                    PowerUnitF powerUnitF = (PowerUnitF) device;
                                    if (powerUnitF.isPreset()) {
                                        for (int rb = 0; rb < 14; rb++) {
                                            preset[wb + (14 * c) + rb] = powerUnitF.getCommand()[rb];
                                        }
                                        c++;
                                    }
                                }
                                if (device instanceof PowerSocketF) {
                                    PowerSocketF powerSocketF = (PowerSocketF) device;
                                    if (powerSocketF.isPreset()) {
                                        for (int rb = 0; rb < 14; rb++) {
                                            preset[wb + (14 * c) + rb] = powerSocketF.getCommand()[rb];
                                        }
                                        c++;
                                    }
                                }
                                if (device instanceof Thermostat) {
                                    Thermostat thermostat = (Thermostat) device;
                                    if (thermostat.isPreset()) {
                                        for (int rb = 0; rb < 14; rb++) {
                                            preset[wb + (14 * c) + rb] = thermostat.getPresetCommand()[rb];
                                        }
                                        c++;
                                    }
                                }
                                if (device instanceof RolletUnitF) {
                                    RolletUnitF rolletUnitF = (RolletUnitF) device;
                                    if (rolletUnitF.isPreset()) {
                                        for (int rb = 0; rb < 14; rb++) {
                                            preset[wb + (14 * c) + rb] = rolletUnitF.getPresetCommand()[rb];
                                        }
                                        c++;
                                    }
                                }
                            } else {
                                showToast("В сценарий можно добавить только 73 устройства");
                                return;
                            }
                        }
                        if (c == 0) {
                            showToast("Добавьте устройства в сценарий");
                            return;
                        }
                        savePreset(i, preset);
                        return;
                    }
                }
                showToast("Можно сохранить только 32 сценария");
            }
        }).start();
    }

    private void changePreset(final Preset preset) {
        new Thread(new Runnable() {
            public void run() {
                byte[] file = apiFiles.getPreset();
                int wb = 6 + (preset.getIndex() * 1024);
                int c = 0;
                for (Object device : devices) {
                    if (device instanceof PowerUnit) {
                        PowerUnit powerUnit = (PowerUnit) device;
                        if (powerUnit.isPreset()) {
                            for (int rb = 0; rb < 14; rb++) {
                                file[wb + (14 * c) + rb] = powerUnit.getCommand()[rb];
                            }
                            c++;
                        }
                    }
                    if (device instanceof PowerUnitF) {
                        PowerUnitF powerUnitF = (PowerUnitF) device;
                        if (powerUnitF.isPreset()) {
                            for (int rb = 0; rb < 14; rb++) {
                                file[wb + (14 * c) + rb] = powerUnitF.getCommand()[rb];
                            }
                            c++;
                        }
                    }
                    if (device instanceof PowerSocketF) {
                        PowerSocketF powerSocketF = (PowerSocketF) device;
                        if (powerSocketF.isPreset()) {
                            for (int rb = 0; rb < 14; rb++) {
                                file[wb + (14 * c) + rb] = powerSocketF.getCommand()[rb];
                            }
                            c++;
                        }
                    }
                    if (device instanceof Thermostat) {
                        Thermostat thermostat = (Thermostat) device;
                        if (thermostat.isPreset()) {
                            for (int rb = 0; rb < 14; rb++) {
                                file[wb + (14 * c) + rb] = thermostat.getPresetCommand()[rb];
                            }
                            c++;
                        }
                    }
                    if (device instanceof RolletUnitF) {
                        RolletUnitF rolletUnitF = (RolletUnitF) device;
                        if (rolletUnitF.isPreset()) {
                            for (int rb = 0; rb < 14; rb++) {
                                file[wb + (14 * c) + rb] = rolletUnitF.getPresetCommand()[rb];
                            }
                            c++;
                        }
                    }
                }
                if (c == 0) {
                    showToast("Добавьте устройства в сценарий");
                    return;
                }
                for (; c < 73; c++) {
                    for (int rb = 0; rb < 14; rb++) {
                        file[wb + (14 * c) + rb] = -1;
                    }
                }
                savePreset(preset.getIndex(), file);
            }
        }).start();
    }


    private void savePreset(int index, byte[] file) {
        Call call;
        try {
            updatePresets = true;

            String body = "\r\n\r\nContent-Disposition: form-data; name=\"preset\"; filename=\"preset.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                    .concat(new String(file, "cp1251"))
                    .concat("\r\n\r\n\r\n");
            Request request = new Request.Builder()
                    .url(Settings.URL() + "sett_eic.htm")
                    .post(RequestBody.create(null, body.getBytes("cp1251")))
                    .build();
            call = client.newCall(request);
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                call.cancel();
                setName(index);
            } else {
                call.cancel();
                homeActivity.writeAppLog("PresetFragment.java : savePreset() : response\nResponse code: " + response.code() + "\n");
                if (preset != null) {
                    homeActivity.writeAppLog(preset.toString());
                }
                showToast("Ошибка соединения " + response.code());
            }
        } catch (SocketTimeoutException okioTimeoutExceptionOne) { //crutch for Samsung Galaxy S9 API 26/27
            try {
                homeActivity.writeAppLog("API level: " + Build.VERSION.SDK_INT + "\n");
                homeActivity.writeAppLog("PresetFragment.java : savePreset()\nException\n");
                if (preset != null) {
                    homeActivity.writeAppLog(preset.toString());
                }
                homeActivity.writeAppLog(NooLiteF.getStackTrace(okioTimeoutExceptionOne));
                Thread.sleep(6000);
                setName(index);
            } catch (SocketTimeoutException okioTimeoutExceptionTwo) {
                homeActivity.writeAppLog("API level: " + Build.VERSION.SDK_INT + "\n");
                homeActivity.writeAppLog("PresetFragment.java : savePreset()\nException\n");
                if (preset != null) {
                    homeActivity.writeAppLog(preset.toString());
                }
                homeActivity.writeAppLog(NooLiteF.getStackTrace(okioTimeoutExceptionTwo));
                dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException ioExceptionTwo) {
                homeActivity.writeAppLog("PresetFragment.java : savePreset()\nException\n");
                if (preset != null) {
                    homeActivity.writeAppLog(preset.toString());
                }
                homeActivity.writeAppLog(NooLiteF.getStackTrace(ioExceptionTwo));
                showToast("Ошибка при сохранении сценария");
            }
        } catch (IOException ioExceptionOne) {
            homeActivity.writeAppLog("PresetFragment.java : savePreset()\nException\n");
            if (preset != null) {
                homeActivity.writeAppLog(preset.toString());
            }
            homeActivity.writeAppLog(NooLiteF.getStackTrace(ioExceptionOne));
            showToast("Ошибка при сохранении сценария");
        }
    }

    private void setName(int index) throws IOException {
        byte[] user = apiFiles.getUser();
        byte[] name = nameEdit.getText().toString().getBytes("cp1251");
        for (int b = 0; b < 32; b++) {
            if (b < name.length)
                user[10470 + (33 * index) + b] = name[b];
            else
                user[10470 + (33 * index) + b] = 0;
        }
        saveName(user);
    }

    private void saveName(byte[] file) throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"preset\"; filename=\"preset.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(new String(file, "cp1251"))
                .concat("\r\n\r\n\r\n");
        Request request = new Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.getBytes("cp1251")))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            showToast("Сценарий сохранён");
            dismissAllowingStateLoss();
        } else {
            homeActivity.writeAppLog("PresetFragment.java : saveName() : response\nResponse code: " + response.code() + "\n" + preset.toString());
            showToast("Ошибка соединения " + response.code());
        }

        call.cancel();
    }

    private void delPreset(final int index) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    updatePresets = true;
                    byte[] preset = apiFiles.getPreset();
                    for (int b = 6 + (1024 * index); b < 6 + (1024 * index) + 1024; b++) {
                        preset[b] = -1;
                    }

                    String body = "\r\n\r\nContent-Disposition: form-data; name=\"preset\"; filename=\"preset.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                            .concat(new String(preset, "cp1251"))
                            .concat("\r\n\r\n\r\n");
                    Request request = new Request.Builder()
                            .url(Settings.URL() + "sett_eic.htm")
                            .post(RequestBody.create(null, body.getBytes("cp1251")))
                            .build();
                    Call call = client.newCall(request);
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        response.close();
                        call.cancel();
                        showToast("Сценарий удалён");
                        dismiss();
                    } else {
                        response.close();
                        call.cancel();
                        homeActivity.writeAppLog("PresetFragment.java : delPreset() : response\nResponse code: " + response.code() + "\n" + preset.toString());
                        showToast("Ошибка соединения " + response.code());
                    }
                } catch (IOException e) {
                    homeActivity.writeAppLog("PresetFragment.java : delPreset()\nException\n" + preset.toString() + NooLiteF.getStackTrace(e));
                    showToast("Ошибка при удалении сценария");
                }
            }
        }).start();
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
            guiBlockFragment.dismissAllowingStateLoss();
        }
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unblockUI();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
