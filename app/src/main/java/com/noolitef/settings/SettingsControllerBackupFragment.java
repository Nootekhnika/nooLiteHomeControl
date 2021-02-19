package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.ConfirmDialog;
import com.noolitef.ConfirmDialogListener;
import com.noolitef.HomeActivity;
import com.noolitef.R;
import com.noolitef.GUIBlockFragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsControllerBackupFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private Call call;

    private boolean backupAvialable;

    private byte[] device;
    private byte[] user;
    private byte[] preset;
    private byte[] timer;
    private byte[] auto;

    private Button buttonBack;
    private Button buttonCopy;
    private TextView textDateTime;
    private Button buttonUpload;
    private GUIBlockFragment guiBlockFragment;

    public SettingsControllerBackupFragment() {
    }

    public void send(OkHttpClient client) {
        this.client = client;
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
        View fragmentView = inflater.inflate(R.layout.fragment_settings_controller_backup, null);
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_backup_button_back);
        buttonBack.setOnClickListener(this);
        buttonCopy = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_backup_button_copy);
        buttonCopy.setOnClickListener(this);
        textDateTime = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_backup_text_date_time);
        buttonUpload = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_backup_button_upload);
        buttonUpload.setOnClickListener(this);
        getBackupFiles();
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
        ConfirmDialog confirmDialog;
        switch (v.getId()) {
            case R.id.fragment_settings_controller_backup_button_back:
                dismiss();
                break;
            case R.id.fragment_settings_controller_backup_button_copy:
                confirmDialog = (ConfirmDialog) getChildFragmentManager().findFragmentByTag("CONFIRM_DIALOG");
                if (confirmDialog == null) {
                    confirmDialog = new ConfirmDialog();
                    confirmDialog.setTitle("Резервное копирование настроек контроллера");
                    confirmDialog.setMessage("Данная операция сохранит текущую конфигурацию контроллера в память приложения.\nРанее сохраненные данные будут перезаписаны.\nПродолжить?");
                    confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                        @Override
                        public void onAccept() {
                            blockUI();
                            getDevice();
                        }

                        @Override
                        public void onDecline() {
                        }
                    });
                }
                if (confirmDialog.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit();
                break;
            case R.id.fragment_settings_controller_backup_button_upload:
                if (backupAvialable) {
                    confirmDialog = (ConfirmDialog) getChildFragmentManager().findFragmentByTag("CONFIRM_DIALOG");
                    if (confirmDialog == null) {
                        confirmDialog = new ConfirmDialog();
                        confirmDialog.setTitle("Восстановление настроек контроллера");
                        confirmDialog.setMessage("Данная операция загрузит в контроллер конфигурацию, сохраненную в памяти приложения.\nТекущие настройки контроллера будут перезаписаны. Настройки автоматизации будут удалены.\nПродолжить?");
                        confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                            @Override
                            public void onAccept() {
                                blockUI();
                                uploadFiles();
                            }

                            @Override
                            public void onDecline() {
                            }
                        });
                    }
                    if (confirmDialog.isAdded()) return;
                    getChildFragmentManager().beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit();
                } else {
                    showToast("Резервной копии нет");
                }
                break;
        }
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

    private void getBackupFiles() {
        FileInputStream fileInputStream;

        try {
            byte[] timestamp = new byte[5];
            fileInputStream = homeActivity.openFileInput("timestamp.bin");
            fileInputStream.read(timestamp);
            fileInputStream.close();
            setTimestamp(String.format(Locale.ROOT, "%02d.%02d.%02d %02d:%02d", timestamp[0], timestamp[1], timestamp[2], timestamp[3], timestamp[4]));
        } catch (Exception e) {
            backupAvialable = false;
            return;
        }

        try {
            device = new byte[4102];
            fileInputStream = homeActivity.openFileInput("device.bin");
            fileInputStream.read(device);
            fileInputStream.close();

            user = new byte[12294];
            fileInputStream = homeActivity.openFileInput("user.bin");
            fileInputStream.read(user);
            fileInputStream.close();

            preset = new byte[32774];
            fileInputStream = homeActivity.openFileInput("preset.bin");
            fileInputStream.read(preset);
            fileInputStream.close();

            timer = new byte[8198];
            fileInputStream = homeActivity.openFileInput("timer.bin");
            fileInputStream.read(timer);
            fileInputStream.close();

            auto = new byte[12294];
            fileInputStream = homeActivity.openFileInput("auto.bin");
            fileInputStream.read(auto);
            fileInputStream.close();

            backupAvialable = true;
        } catch (Exception e) {
            backupAvialable = false;
            showToast("Ошибка при загрузке сохраненных файлов конфигурации контроллера");
        }

//        SharedPreferences sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
//        textDateTime.setText(sharedPreferences.getString("ControllerBackupDateTime", "00.00.00 00:00"));
    }

    private void getDevice() {
        Request request = new Request.Builder()
                .url(Settings.URL() + "device.bin")
                .build();
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                showToast(homeActivity.getString(R.string.no_connection));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        device = response.body().bytes();
                        response.close();
                        call.cancel();
                        if (device.length == 4102) {
                            getUser();
                        } else {
                            throw new IOException("device.bin is damaged");
                        }
                    } catch (Exception e) {
                        response.close();
                        call.cancel();
                        showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                    }
                } else {
                    response.close();
                    call.cancel();
                    showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
                }
            }
        });
    }

    private void getUser() throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "user.bin")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            user = response.body().bytes();
            response.close();
            call.cancel();
            if (user.length == 12294) {
                getPreset();
            } else {
                throw new IOException("user.bin is damaged");
            }
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void getPreset() throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "preset.bin")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            preset = response.body().bytes();
            response.close();
            call.cancel();
            if (preset.length == 32774) {
                getTimer();
            } else {
                throw new IOException("preset.bin is damaged");
            }
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void getTimer() throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "timer.bin")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            timer = response.body().bytes();
            response.close();
            call.cancel();
            if (timer.length == 8198) {
                getAuto();
            } else {
                throw new IOException("timer.bin is damaged");
            }
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void getAuto() throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "auto.bin")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            auto = response.body().bytes();
            response.close();
            call.cancel();
            if (auto.length == 12294) {
                saveFiles();
            } else {
                throw new IOException("auto.bin is damaged");
            }
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
        }
    }

    private void saveFiles() {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = homeActivity.openFileOutput("device.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(device);
            fileOutputStream.flush();
            fileOutputStream.close();

            fileOutputStream = homeActivity.openFileOutput("user.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(user);
            fileOutputStream.flush();
            fileOutputStream.close();

            fileOutputStream = homeActivity.openFileOutput("preset.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(preset);
            fileOutputStream.flush();
            fileOutputStream.close();

            fileOutputStream = homeActivity.openFileOutput("timer.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(timer);
            fileOutputStream.flush();
            fileOutputStream.close();

            fileOutputStream = homeActivity.openFileOutput("auto.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(auto);
            fileOutputStream.flush();
            fileOutputStream.close();

            byte[] timestamp = new byte[5];
            Calendar calendar = Calendar.getInstance();
            timestamp[0] = (byte) calendar.get(Calendar.DATE);
            timestamp[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
            timestamp[2] = (byte) (calendar.get(Calendar.YEAR) % 100);
            timestamp[3] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            timestamp[4] = (byte) calendar.get(Calendar.MINUTE);
            fileOutputStream = homeActivity.openFileOutput("timestamp.bin", Context.MODE_PRIVATE);
            fileOutputStream.write(timestamp);
            fileOutputStream.flush();
            fileOutputStream.close();
            setTimestamp(String.format(Locale.ROOT, "%02d.%02d.%02d %02d:%02d", timestamp[0], timestamp[1], timestamp[2], timestamp[3], timestamp[4]));

//        SharedPreferences sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("BACKUP_TIMESTAMP", timestamp);
//        editor.apply();

            backupAvialable = true;
            showToast("Резервное копирование выполнено");
        } catch (Exception e) {
            showToast("Ошикба при сохранении файлов");
        }
    }

    private void uploadFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadFile(device);
                    uploadFile(user);
                    uploadFile(preset);
                    uploadFile(timer);
                    uploadFile(auto);
                    showToast("Восстановление конфигурации выполнено");
                } catch (Exception e) {
                    showToast("Ошибка при загрузке файлов");
                }
            }
        }).start();
    }

    private void uploadFile(byte[] file) throws IOException, InterruptedException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"file\"; filename=\"file.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(new String(file, "cp1251"))
                .concat("\r\n\r\n\r\n");
        Request request = new Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.getBytes("cp1251")))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            Thread.sleep(250);
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
            throw new IOException("connection error");
        }
    }

    private void setTimestamp(final String dateTime) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textDateTime.setText(dateTime);
                unblockUI();
            }
        });
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
