package com.noolitef;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.tx.PowerUnit;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

interface BindFragmentListener {
    void onDismiss(boolean update);
}

public class BindFragment_temporary extends DialogFragment {
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private ArrayList<Object> units;
    private ArrayList<Room> rooms;
    private int roomIndex;

    private Button buttonBack;
    private AppCompatSpinner unitTypeSpinner;
    private ArrayAdapter unitTypeAdapter;
    private TextView textName;
    private AppCompatEditText editName;
    private TextView textRoom;
    private AppCompatSpinner roomSpinner;
    private ArrayAdapter roomAdapter;
    private TextView textDeviceType;
    private AppCompatSpinner deviceTypeSpinner;
    private ArrayAdapter deviceTypeAdapter;
    private TextView textChannel;
    private AppCompatSpinner channelSpinner;
    private ArrayAdapter channelAdapter;
    private TextView textInstructions;
    private Button buttonBind;
    private TextView textStatus;
    private Button buttonSwitch;
    private TextView textSave;
    private Button buttonSave;

    private GUIBlockFragment guiBlockFragment;

    private BindFragmentListener bindFragmentListener;
    private boolean update;

    public BindFragment_temporary() {
    }

    void setBindFragmentListener(BindFragmentListener listener) {
        bindFragmentListener = listener;
    }

    void send(OkHttpClient client, NooLiteF nooLiteF, ArrayList<Object> units, int roomIndex, ArrayList<Room> rooms) {
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.units = units;
        this.rooms = rooms;
        this.roomIndex = roomIndex;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View fragmentView = inflater.inflate(R.layout.fragment_bind_temporary, null);

        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_bind_button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update = false;
                dismiss();
            }
        });

        unitTypeSpinner = (AppCompatSpinner) fragmentView.findViewById(R.id.fragment_bind_spinner_unit_type);
        unitTypeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, homeActivity.getResources().getStringArray(R.array.unitType)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if (position == 0) {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                } else {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                }
                return view;
            }
        };
        unitTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        textName.setVisibility(View.VISIBLE);
                        editName.setVisibility(View.VISIBLE);
                        textRoom.setVisibility(View.VISIBLE);
                        roomSpinner.setVisibility(View.VISIBLE);
                        textDeviceType.setVisibility(View.GONE);
                        deviceTypeSpinner.setVisibility(View.GONE);
                        textChannel.setVisibility(View.GONE);
                        channelSpinner.setVisibility(View.GONE);
                        textInstructions.setText("Переведите блок в режим привязки, затем нажмите ''ПРИВЯЗАТЬ''");
                        textInstructions.setVisibility(View.VISIBLE);
                        buttonBind.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                blockUI(false);
                                int roomID = roomSpinner.getSelectedItemPosition() - 1;
                                if (roomID > -1) {
                                    roomID = rooms.get(roomSpinner.getSelectedItemPosition() - 1).getId();
                                }
                                nooLiteF.bindF_TX(editName.getText().toString(), roomID, roomSpinner.getSelectedItem().toString());
                            }
                        });
                        buttonBind.setVisibility(View.VISIBLE);
                        textStatus.setVisibility(View.GONE);
                        buttonSwitch.setVisibility(View.GONE);
                        textSave.setVisibility(View.GONE);
                        buttonSave.setVisibility(View.GONE);
                        break;
                    case 2:
                        textName.setVisibility(View.VISIBLE);
                        editName.setVisibility(View.VISIBLE);
                        textRoom.setVisibility(View.VISIBLE);
                        roomSpinner.setVisibility(View.VISIBLE);
                        textDeviceType.setVisibility(View.VISIBLE);
                        deviceTypeSpinner.setVisibility(View.VISIBLE);
                        textChannel.setVisibility(View.VISIBLE);
                        channelSpinner.setVisibility(View.VISIBLE);
                        textInstructions.setText("1. Переведите блок в режим привязки, затем нажмите ''ПРИВЯЗАТЬ''.");
                        textInstructions.setVisibility(View.VISIBLE);
                        buttonBind.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (deviceTypeSpinner.getSelectedItemPosition() == 0) {
                                    showToast("Выберите тип блока");
                                    return;
                                }
                                if (channelSpinner.getSelectedItemPosition() == 0) {
                                    showToast("Выберите канал");
                                    return;
                                }
                                blockUI(false);
                                nooLiteF.bindTX(channelSpinner.getSelectedItemPosition() - 1, 0);
                            }
                        });
                        buttonBind.setVisibility(View.VISIBLE);
                        textStatus.setText("2. Нажмите сервисную кнопку на блоке ещё 2 раза.\n3. Проверьте работу блока...");
                        textStatus.setVisibility(View.GONE);
                        buttonSwitch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                nooLiteF.switchTX(0, channelSpinner.getSelectedItemPosition() - 1);
                            }
                        });
                        buttonSwitch.setVisibility(View.GONE);
                        textSave.setText("4. Сохраните блок в памяти контроллера.");
                        textSave.setVisibility(View.GONE);
                        buttonSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                blockUI(false);
                                int roomID = roomSpinner.getSelectedItemPosition() - 1;
                                if (roomID > -1) {
                                    roomID = rooms.get(roomSpinner.getSelectedItemPosition() - 1).getId();
                                }
                                int deviceType = 0;
                                switch (deviceTypeSpinner.getSelectedItemPosition()) {
                                    case 1:
                                        deviceType = PowerUnit.DIMMER;
                                        break;
                                    case 2:
                                        deviceType = PowerUnit.RELAY;
                                        break;
                                    case 3:
                                        deviceType = PowerUnit.PULSE_RELAY;
                                        break;
                                    case 4:
                                        deviceType = PowerUnit.RGB_CONTROLLER;
                                        break;
                                    case 5:
                                        deviceType = PowerUnit.ROLLET;
                                        break;
                                }
                                nooLiteF.saveTX(editName.getText().toString(), roomID, roomSpinner.getSelectedItem().toString(), channelSpinner.getSelectedItemPosition() - 1, deviceType);
                            }
                        });
                        buttonSave.setVisibility(View.GONE);
                        break;
                    case 3:
                        textName.setVisibility(View.VISIBLE);
                        editName.setVisibility(View.VISIBLE);
                        textRoom.setVisibility(View.VISIBLE);
                        roomSpinner.setVisibility(View.VISIBLE);
                        textDeviceType.setVisibility(View.GONE);
                        deviceTypeSpinner.setVisibility(View.GONE);
                        textChannel.setVisibility(View.GONE);
                        channelSpinner.setVisibility(View.GONE);
                        textInstructions.setText("Датчик:\nНажмите ''ПРИВЯЗАТЬ'', затем сервисную кнопку на датчике.\nПульт:\nНажмите ''ПРИВЯЗАТЬ'', затем сервисную кнопку на пульте, после чего управляющую кнопку пульта.");
                        textInstructions.setVisibility(View.VISIBLE);
                        buttonBind.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                blockUI(true);
                                int roomID = roomSpinner.getSelectedItemPosition() - 1;
                                if (roomID > -1) {
                                    roomID = rooms.get(roomSpinner.getSelectedItemPosition() - 1).getId();
                                }
                                nooLiteF.bindRX(editName.getText().toString(), roomID, roomSpinner.getSelectedItem().toString());
                            }
                        });
                        buttonBind.setVisibility(View.VISIBLE);
                        textStatus.setText("Ожидайте завершения привязки...");
                        textStatus.setVisibility(View.VISIBLE);
                        buttonSwitch.setVisibility(View.GONE);
                        textSave.setVisibility(View.GONE);
                        buttonSave.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        unitTypeSpinner.setAdapter(unitTypeAdapter);

        textName = (TextView) fragmentView.findViewById(R.id.fragment_bind_text_name);
        editName = (AppCompatEditText) fragmentView.findViewById(R.id.fragment_bind_edit_name);

        textRoom = (TextView) fragmentView.findViewById(R.id.fragment_bind_text_room);
        roomSpinner = (AppCompatSpinner) fragmentView.findViewById(R.id.fragment_bind_spinner_room);
        String[] roomsNames;
        if (rooms != null) {
            roomsNames = new String[rooms.size() + 1];  // null pointer
            roomsNames[0] = "выберите комнату...";
            for (int rn = 1, r = 0; r < rooms.size(); r++, rn++) {
                roomsNames[rn] = rooms.get(r).getName();
            }
        } else {
            if (nooLiteF == null) {
                dismiss();
            }
            roomsNames = new String[1];
            roomsNames[0] = "комнат нет";
        }
        roomAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, roomsNames) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if (position == 0) {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                } else {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                }
                return view;
            }
        };
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        roomSpinner.setAdapter(roomAdapter);
        roomSpinner.setSelection(roomIndex + 1);

        textDeviceType = (TextView) fragmentView.findViewById(R.id.fragment_bind_text_device_type);
        deviceTypeSpinner = (AppCompatSpinner) fragmentView.findViewById(R.id.fragment_bind_spinner_device_type);
        deviceTypeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, homeActivity.getResources().getStringArray(R.array.deviceType)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if (position == 0) {
                    item.setTextColor(getResources().getColor(R.color.grey));
                } else {
                    item.setTextColor(getResources().getColor(R.color.black_light));
                }
                return view;
            }
        };
        deviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        deviceTypeSpinner.setAdapter(deviceTypeAdapter);

        textChannel = (TextView) fragmentView.findViewById(R.id.fragment_bind_text_channel);
        channelSpinner = (AppCompatSpinner) fragmentView.findViewById(R.id.fragment_bind_spinner_channel);
        String[] channels = new String[65];
        channels[0] = "выберите канал...";
        String unitName = null;
        if (units != null) {
            for (int ch = 1; ch < channels.length; ch++) {
                for (Object unit : units) {  // null pointer
                    if (unit instanceof PowerUnit) {
                        PowerUnit powerUnit = (PowerUnit) unit;
                        if (powerUnit.getChannel() == ch - 1) {
                            unitName = powerUnit.getName();
                            break;
                        }
                    }
                }
                if (unitName != null) {
                    channels[ch] = String.valueOf(ch).concat(" - ").concat(unitName);
                    unitName = null;
                } else {
                    channels[ch] = String.valueOf(ch);
                }
            }
        } else {
            if (nooLiteF == null) {
                dismiss();
            }
        }
        channelAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, channels) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if (position == 0) {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                } else {
                    item.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.black_light))));
                }
                return view;
            }
        };
        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        channelSpinner.setAdapter(channelAdapter);

        textInstructions = (TextView) fragmentView.findViewById(R.id.fragment_bind_instructions);
        buttonBind = (Button) fragmentView.findViewById(R.id.fragment_bind_button);

        textStatus = (TextView) fragmentView.findViewById(R.id.fragment_bind_status);
        buttonSwitch = (Button) fragmentView.findViewById(R.id.fragment_bind_button_switch);

        textSave = (TextView) fragmentView.findViewById(R.id.fragment_bind_save);
        buttonSave = (Button) fragmentView.findViewById(R.id.fragment_bind_button_save);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        nooLiteF = homeActivity.getNooLiteF();
        if (nooLiteF != null) {
            nooLiteF.setBindListener(new BindListener() {  // null pointer
                @Override
                public void onSuccess(String message) {
                    showToast(message);
                    update = true;
                    dismiss();
                }

                @Override
                public void onTXbind(final String message) {
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            unitTypeSpinner.setEnabled(false);
                            channelSpinner.setEnabled(false);
                            //buttonBind.setEnabled(false);
                            textStatus.setVisibility(View.VISIBLE);
                            buttonSwitch.setVisibility(View.VISIBLE);
                            textSave.setVisibility(View.VISIBLE);
                            buttonSave.setVisibility(View.VISIBLE);
                        }
                    });
                    showToast(message);
                }

                @Override
                public void onFailure(String message) {
                    showToast(message);
                }
            });
        } else {
            dismiss();
        }
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
    public void onDismiss(DialogInterface dialog) {
        bindFragmentListener.onDismiss(update);
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

    private void blockUI(boolean cancelable) {
        if (!isAdded()) return;
        guiBlockFragment = (GUIBlockFragment) getChildFragmentManager().findFragmentByTag("GUI_BLOCK_FRAGMENT");
        if (guiBlockFragment == null) {
            guiBlockFragment = new GUIBlockFragment();
        }
        if (guiBlockFragment.isAdded()) return;
        if (cancelable) guiBlockFragment.setCancelable(client);
        else guiBlockFragment.setNoneCancelable();
        getChildFragmentManager().beginTransaction().add(guiBlockFragment, "GUI_BLOCK_FRAGMENT").show(guiBlockFragment).commit();
    }

    private void unblockUI() {
        if (!isAdded()) return;
        guiBlockFragment = (GUIBlockFragment) getChildFragmentManager().findFragmentByTag("GUI_BLOCK_FRAGMENT");
        if (guiBlockFragment != null && guiBlockFragment.isAdded()) {
            guiBlockFragment.dismissAllowingStateLoss();
        }
    }
}
