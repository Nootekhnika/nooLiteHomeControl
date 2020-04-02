package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.ConfirmDialog;
import com.noolitef.ConfirmDialogListener;
import com.noolitef.HomeActivity;
import com.noolitef.R;
import com.noolitef.GUIBlockFragment;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsRoomFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private byte[] user;
    private int roomID;
    private String roomName;

    private Button buttonCancel;
    private TextView textTitle;
    private Button buttonSave;
    private EditText editRoom;
    private Button buttonDelete;
    private GUIBlockFragment guiBlockFragment;

    private SettingsRoomFragmentListener settingsRoomFragmentListener;
    private boolean update;

    public SettingsRoomFragment() {
    }

    public static SettingsRoomFragment newInstance(byte[] user, int roomID, String roomName) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("user", user);
        bundle.putInt("roomID", roomID);
        bundle.putString("roomName", roomName);

        SettingsRoomFragment settingsRoomFragment = new SettingsRoomFragment();
        settingsRoomFragment.setArguments(bundle);

        return settingsRoomFragment;
    }

    public void setSettingsRoomFragmentListener(SettingsRoomFragmentListener listener) {
        settingsRoomFragmentListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();

        this.user = getArguments().getByteArray("user");
        this.roomID = getArguments().getInt("roomID");
        this.roomName = getArguments().getString("roomName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View fragmentView;
        if (Settings.isNightMode()) {
            fragmentView = inflater.inflate(R.layout.fragment_settings_room_dark, null);
        } else {
            fragmentView = inflater.inflate(R.layout.fragment_settings_room, null);
        }
        buttonCancel = (Button) fragmentView.findViewById(R.id.fragment_settings_room_button_cancel);
        buttonCancel.setOnClickListener(this);
        textTitle = (TextView) fragmentView.findViewById(R.id.fragment_settings_room_title);
        buttonSave = (Button) fragmentView.findViewById(R.id.fragment_settings_room_button_save);
        buttonSave.setOnClickListener(this);
        editRoom = (EditText) fragmentView.findViewById(R.id.fragment_settings_room_edit_name);
        buttonDelete = (Button) fragmentView.findViewById(R.id.fragment_settings_room_button_delete);
        buttonDelete.setOnClickListener(this);
        if (roomID != -1) {
            textTitle.setText("Изменение");
            editRoom.setText(roomName);
            buttonDelete.setVisibility(View.VISIBLE);
        } else {
            textTitle.setText("Добавление");
        }
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        client = homeActivity.getHttpClient();
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
            case R.id.fragment_settings_room_button_cancel:
                update = false;
                dismiss();
                break;
            case R.id.fragment_settings_room_button_save:
                if (editRoom.getText().length() == 0) {
                    showToast("Назовите комнату");
                    return;
                }
                blockUI();
                setRoom();
                break;
            case R.id.fragment_settings_room_button_delete:
                ConfirmDialog confirmDialog = (ConfirmDialog) getChildFragmentManager().findFragmentByTag("CONFIRM_DIALOG");
                if (confirmDialog == null) {
                    confirmDialog = new ConfirmDialog();
                    confirmDialog.setTitle("Удаление комнаты");
                    confirmDialog.setMessage("Удалить комнату ''".concat(roomName).concat("''?\nВсе устройства из этой комнаты останутся во вкладке ''Дом''."));
                    confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                        @Override
                        public void onAccept() {
                            blockUI();
                            deleteRoom();
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
        if (settingsRoomFragmentListener != null)
            settingsRoomFragmentListener.onDismiss(update);
        super.onDismiss(dialog);
    }

    @Override
    public void onStop() {
        client.dispatcher().cancelAll();
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

    private void setRoom() {
        if (roomID == -1) {
            newRoom();
        } else {
            changeRoom();
        }
    }

    private void newRoom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int r = 0, b = 8774; b < 10470; b += 53, r++) {
                        if (user[b + 31] == -1) {
                            roomID = r;
                            byte[] name = editRoom.getText().toString().getBytes("cp1251");
                            for (int nb = 0; nb < 32; nb++) {
                                if (nb < name.length)
                                    user[8774 + (roomID * 53) + nb] = name[nb];
                                else
                                    user[8774 + (roomID * 53) + nb] = 0;
                            }
                            upload(new String(user, "cp1251"), "Комната добавлена");
                            return;
                        }
                    }
                    showToast("Можно создать только 32 комнаты");
                } catch (Exception e) {
                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                }
            }
        }).start();
    }

    private void changeRoom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] name = editRoom.getText().toString().getBytes("cp1251");
                    for (int nb = 0; nb < 32; nb++) {
                        if (nb < name.length)
                            user[8774 + (roomID * 53) + nb] = name[nb];
                        else
                            user[8774 + (roomID * 53) + nb] = 0;
                    }
                    upload(new String(user, "cp1251"), "Комната изменена");
                } catch (Exception e) {
                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                }
            }
        }).start();
    }

    private void deleteRoom() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int b = 39; b < 8710; b += 34) {
                        if (user[b] == roomID) user[b] = -1;
                    }
                    for (int b = 0; b < 53; b++) {
                        user[8774 + (roomID * 53) + b] = -1;
                    }
                    upload(new String(user, "cp1251"), "Комната удалена");
                } catch (Exception e) {
                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                }
            }
        }).start();
    }

    private void upload(String file, String message) throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"user\"; filename=\"user.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(file)
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
            showToast(message);
            update = true;
            dismiss();
        } else {
            response.close();
            call.cancel();
            showToast(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
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
