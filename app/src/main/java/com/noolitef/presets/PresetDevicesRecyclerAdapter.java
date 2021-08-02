package com.noolitef.presets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.noolitef.BrightnessSetListener;
import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.PowerUnitDialog;
import com.noolitef.R;
import com.noolitef.Room;
import com.noolitef.TemperatureListener;
import com.noolitef.Thermostat;
import com.noolitef.ThermostatDialog;
import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.PowerUnitFA;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.settings.Settings;
import com.noolitef.tx.PowerUnit;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class PresetDevicesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView name;

        RoomViewHolder(View item) {
            super(item);
            name = item.findViewById(R.id.card_view_room_name);
        }

        void setName(String name) {
            this.name.setText(name);
        }
    }

    class RelayViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private CheckBox selector;
        private TextView name;
        private TextView time;
        private SwitchCompat switcher;

        RelayViewHolder(View item) {
            super(item);
            layout = item.findViewById(R.id.card_view_preset_device_layout);
            selector = item.findViewById(R.id.card_view_preset_device_selector);
            name = item.findViewById(R.id.card_view_preset_device_name);
            time = item.findViewById(R.id.card_view_preset_device_time);
            switcher = item.findViewById(R.id.card_view_preset_device_switcher);

            switcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        void setCheck(boolean checked) {
            selector.setChecked(checked);
            setSwitchVisible(checked);
        }

        void setCheckClick(View.OnClickListener listener) {
            layout.setOnClickListener(listener);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setTimeVisibility(boolean visible) {
            if (visible) {
                if (selector.isChecked())
                    time.setVisibility(View.VISIBLE);
            } else {
                time.setVisibility(View.GONE);
            }
        }

        void setTime(int minutes) {
            if (minutes > 0) {
                time.setText(String.format(Locale.ROOT, "%dмин", minutes));
            } else {
                time.setText("∞");
            }
        }

        void setTimeClick(View.OnClickListener listener) {
            time.setOnClickListener(listener);
        }

        void setSwitchVisible(boolean visible) {
            if (visible) {
                switcher.setVisibility(View.VISIBLE);
                if (switcher.isChecked()) {
                    setTimeVisibility(true);
                }
            } else {
                switcher.setVisibility(View.GONE);
                setTimeVisibility(false);
            }
        }

        void setSwitch(int state) {
            switch (state) {
                case Preset.OFF:
                    switcher.setChecked(false);
                    setTimeVisibility(false);
                    break;
                case Preset.ON:
                    switcher.setChecked(true);
                    setTimeVisibility(true);
                    break;
                case Preset.SET_BRIGHTNESS:
                    switcher.setChecked(true);
                    setTimeVisibility(true);
                    break;
                case Preset.TEMPORARY_ON:
                    switcher.setChecked(true);
                    setTimeVisibility(true);
                    break;
            }
        }

        void setSwitchCheckedChange(CompoundButton.OnCheckedChangeListener listener) {
            switcher.setOnCheckedChangeListener(listener);
        }
    }

    class DimmerViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private CheckBox selector;
        private TextView name;
        private TextView time;
        private TextView brightness;
        private SwitchCompat switcher;

        DimmerViewHolder(View item) {
            super(item);
            layout = item.findViewById(R.id.card_view_preset_device_layout);
            selector = item.findViewById(R.id.card_view_preset_device_selector);
            name = item.findViewById(R.id.card_view_preset_device_name);
            time = item.findViewById(R.id.card_view_preset_device_time);
            brightness = item.findViewById(R.id.card_view_preset_device_brightness);
            switcher = item.findViewById(R.id.card_view_preset_device_switcher);
        }

        void setCheck(boolean checked) {
            selector.setChecked(checked);
            setSwitchVisibility(checked);
        }

        void setCheckClick(View.OnClickListener listener) {
            layout.setOnClickListener(listener);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setTimeVisibility(boolean visible) {
            if (visible) {
                if (selector.isChecked())
                    time.setVisibility(View.VISIBLE);
            } else {
                time.setVisibility(View.GONE);
            }
        }

        void setBrightnessVisibility(boolean visible) {
            if (visible) {
                if (selector.isChecked())
                    brightness.setVisibility(View.VISIBLE);
            } else {
                brightness.setVisibility(View.GONE);
            }
        }

        void setTime(int minutes) {
            if (minutes > 0) {
                time.setText(String.format(Locale.ROOT, "%dмин", minutes));
            } else {
                time.setText("∞");
            }
        }

        void setBrightness(int percent) {
            brightness.setText(String.format(Locale.ROOT, "%d%%", percent));
        }

        void setBrightnessColor(int color) {
            brightness.setTextColor(color);
        }

        void setTimeClick(View.OnClickListener listener) {
            time.setOnClickListener(listener);
        }

        void setBrightnessClick(View.OnClickListener listener) {
            brightness.setOnClickListener(listener);
        }

        void setSwitchVisibility(boolean visible) {
            if (visible) {
                switcher.setVisibility(View.VISIBLE);
                if (switcher.isChecked()) {
                    setTimeVisibility(true);
                    setBrightnessVisibility(true);
                }
            } else {
                switcher.setVisibility(View.GONE);
                setTimeVisibility(false);
                setBrightnessVisibility(false);
            }
        }

        void setSwitch(int state) {
            switch (state) {
                case Preset.OFF:
                    switcher.setChecked(false);
                    setTimeVisibility(false);
                    setBrightnessVisibility(false);
                    break;
                case Preset.ON:
                    switcher.setChecked(true);
                    setTimeVisibility(true);
                    setBrightnessVisibility(true);
                    break;
                case Preset.SET_BRIGHTNESS:
                    switcher.setChecked(true);
                    setTimeVisibility(true);
                    setBrightnessVisibility(true);
                    break;
                case Preset.TEMPORARY_ON:
                    switcher.setChecked(true);
                    setTimeVisibility(true);
                    setBrightnessVisibility(true);
                    break;
            }
        }

        void setSwitchCheckedChange(CompoundButton.OnCheckedChangeListener listener) {
            switcher.setOnCheckedChangeListener(listener);
        }
    }

    class ThermostatViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private CheckBox selector;
        private TextView name;
        private TextView temperature;
        private SwitchCompat switcher;

        ThermostatViewHolder(View item) {
            super(item);
            layout = item.findViewById(R.id.card_view_preset_device_layout);
            selector = item.findViewById(R.id.card_view_preset_device_selector);
            name = item.findViewById(R.id.card_view_preset_device_name);
            temperature = item.findViewById(R.id.card_view_preset_device_temperature);
            switcher = item.findViewById(R.id.card_view_preset_device_switcher);
        }

        void setCheck(boolean checked) {
            selector.setChecked(checked);
            setSwitchVisibility(checked);
        }

        void setCheckClick(View.OnClickListener listener) {
            layout.setOnClickListener(listener);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setTemperature(int degrees) {
            temperature.setText(String.format(Locale.ROOT, "%d°C", degrees));
        }

        void setTemperatureClick(View.OnClickListener listener) {
            temperature.setOnClickListener(listener);
        }

        void setTemperatureVisibility(boolean visible) {
            if (visible) {
                if (selector.isChecked())
                    temperature.setVisibility(View.VISIBLE);
            } else {
                temperature.setVisibility(View.GONE);
            }
        }

        void setSwitch(int state) {
            switch (state) {
                case 0:
                    switcher.setChecked(false);
                    setTemperatureVisibility(false);
                    break;
                case 6:
                    switcher.setChecked(true);
                    setTemperatureVisibility(true);
                    break;
            }
        }

        void setSwitchCheckedChange(CompoundButton.OnCheckedChangeListener listener) {
            switcher.setOnCheckedChangeListener(listener);
        }

        void setSwitchVisibility(boolean visible) {
            if (visible) {
                switcher.setVisibility(View.VISIBLE);
                if (switcher.isChecked())
                    setTemperatureVisibility(true);
            } else {
                switcher.setVisibility(View.GONE);
                setTemperatureVisibility(false);
            }
        }
    }

    class RolletViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        private CheckBox selector;
        private TextView name;
        private Button button;

        RolletViewHolder(View item) {
            super(item);
            layout = item.findViewById(R.id.card_view_preset_device_layout);
            selector = item.findViewById(R.id.card_view_preset_device_selector);
            name = item.findViewById(R.id.card_view_preset_device_name);
            button = item.findViewById(R.id.card_view_preset_device_state);
        }

        void setCheck(boolean checked) {
            selector.setChecked(checked);
            setButtonVisibility(checked);
        }

        void setCheckClick(View.OnClickListener listener) {
            layout.setOnClickListener(listener);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setButton(int state) {
            switch (state) {
                case 0:
                    button.setText("Закрыть");
                    break;
                case 2:
                    button.setText("Открыть");
                    break;
            }
        }

        void setButtonClick(View.OnClickListener listener) {
            button.setOnClickListener(listener);
        }

        void setButtonVisibility(boolean visible) {
            if (visible) {
                button.setVisibility(View.VISIBLE);
            } else {
                button.setVisibility(View.GONE);
            }
        }
    }

    static final int ROOM = 0;
    private static final int POWER_UNIT_RELAY = 1;
    private static final int POWER_UNIT_DIMMER = 2;
    private static final int POWER_UNIT_F_RELAY = 3;
    private static final int POWER_UNIT_F_DIMMER = 4;
    private static final int POWER_SOCKET_F = 5;
    private static final int THERMOSTAT = 6;
    private static final int ROLLET_UNIT_F = 7;

    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private HomeActivity homeActivity;
    private Fragment fragment;
    private ArrayList<Object> units;
    private Preset preset;
    private int selectedDevices;

    private Toast toast;

    PresetDevicesRecyclerAdapter(OkHttpClient client, NooLiteF nooLiteF, HomeActivity homeActivity, Fragment fragment, ArrayList<Object> units, Preset preset) {
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.homeActivity = homeActivity;
        this.fragment = fragment;
        this.units = units;
        this.preset = preset;
        if (preset != null) {
            for (Object unit : units) {
                if (unit instanceof PowerUnit) {
                    PowerUnit powerUnit = (PowerUnit) unit;
                    powerUnit.setPreset(isPreset(powerUnit));
                    if (powerUnit.isPreset()) selectedDevices++;
                }
                if (unit instanceof PowerUnitF) {
                    PowerUnitF powerUnitF = (PowerUnitF) unit;
                    powerUnitF.setPreset(isPreset(powerUnitF));
                    if (powerUnitF.isPreset()) selectedDevices++;
                }
                if (unit instanceof PowerSocketF) {
                    PowerSocketF powerSocketF = (PowerSocketF) unit;
                    powerSocketF.setPreset(isPreset(powerSocketF));
                    if (powerSocketF.isPreset()) selectedDevices++;
                }
                if (unit instanceof Thermostat) {
                    Thermostat thermostat = (Thermostat) unit;
                    thermostat.setPreset(isPreset(thermostat));
                    if (thermostat.isPreset()) selectedDevices++;
                }
                if (unit instanceof RolletUnitF) {
                    RolletUnitF rolletUnitF = (RolletUnitF) unit;
                    rolletUnitF.setPreset(isPreset(rolletUnitF));
                    if (rolletUnitF.isPreset()) selectedDevices++;
                }
            }
        }
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (units.get(position) instanceof Room) return ROOM;
        if (units.get(position) instanceof PowerUnit) {
            PowerUnit powerUnit = (PowerUnit) units.get(position);
            switch (powerUnit.getType()) {
                case PowerUnit.RELAY:
                    return POWER_UNIT_RELAY;
                case PowerUnit.DIMMER:
                    return POWER_UNIT_DIMMER;
                case PowerUnit.RGB_CONTROLLER:
                    return POWER_UNIT_DIMMER;
                // POWER_UNIT_PULSE_RELAY
                // POWER_UNIT_ROLLET
            }
        }
        if (units.get(position) instanceof PowerUnitF) {
            PowerUnitF powerUnitF = (PowerUnitF) units.get(position);
            if (powerUnitF.isDimmer())
                return POWER_UNIT_F_DIMMER;
            else
                return POWER_UNIT_F_RELAY;
        }
        if (units.get(position) instanceof PowerSocketF) return POWER_SOCKET_F;
        if (units.get(position) instanceof Thermostat) return THERMOSTAT;
        if (units.get(position) instanceof RolletUnitF) return ROLLET_UNIT_F;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view;
        switch (type) {
            case ROOM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_room, parent, false);
                return new RoomViewHolder(view);
            case POWER_UNIT_RELAY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_relay, parent, false);
                return new RelayViewHolder(view);
            case POWER_UNIT_DIMMER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_dimmer, parent, false);
                return new DimmerViewHolder(view);
            case POWER_UNIT_F_RELAY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_relay, parent, false);
                return new RelayViewHolder(view);
            case POWER_UNIT_F_DIMMER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_dimmer, parent, false);
                return new DimmerViewHolder(view);
            case POWER_SOCKET_F:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_relay, parent, false);
                return new RelayViewHolder(view);
            case THERMOSTAT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_thermostat, parent, false);
                return new ThermostatViewHolder(view);
            case ROLLET_UNIT_F:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_device_rollet, parent, false);
                return new RolletViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ROOM:
                bindRoom(holder, position);
                break;
            case POWER_UNIT_RELAY:
                bindPowerUnitRelay(holder, position);
                break;
            case POWER_UNIT_DIMMER:
                bindPowerUnitDimmer(holder, position);
                break;
            case POWER_UNIT_F_RELAY:
                bindPowerUnitFRelay(holder, position);
                break;
            case POWER_UNIT_F_DIMMER:
                bindPowerUnitFDimmer(holder, position);
                break;
            case POWER_SOCKET_F:
                bindPowerSocketF(holder, position);
                break;
            case THERMOSTAT:
                bindThermostat(holder, position);
                break;
            case ROLLET_UNIT_F:
                bindRolletUnitF(holder, position);
                break;
        }
    }

    private void bindRoom(RecyclerView.ViewHolder holder, int position) {
        RoomViewHolder roomViewHolder = (RoomViewHolder) holder;
        roomViewHolder.setName(String.valueOf(((Room) units.get(position)).getName()));
    }

    private void bindPowerUnitRelay(final RecyclerView.ViewHolder holder, final int position) {
        final RelayViewHolder relayViewHolder = (RelayViewHolder) holder;
        final PowerUnit powerUnit = (PowerUnit) units.get(position);
        relayViewHolder.setCheck(powerUnit.isPreset());
        relayViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (powerUnit.isPreset()) {
                    powerUnit.setPreset(false);
                    relayViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        powerUnit.setPreset(true);
                        relayViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        relayViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        relayViewHolder.setName(powerUnit.getName());
        relayViewHolder.setTime(powerUnit.getTime());
        relayViewHolder.setTimeClick(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.applyTimePickerMode();
                            powerUnitDialog.send(null, null, null, null, null, -1, powerUnit);
                            powerUnitDialog.timeSetListener(
                                    new TimeSetListener() {
                                        @Override
                                        public void setTime(int minutes) {
                                            powerUnit.setPresetState(PowerUnit.TEMPORARY_ON);
                                            powerUnit.setTime(minutes);
                                            relayViewHolder.setTime(minutes);
                                            if (minutes < 1) {
                                                powerUnit.setPresetState(PowerUnit.ON);
                                            }
                                        }
                                    }
                            );
                        }
                        if (powerUnitDialog.isAdded()) return;
                        fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }
                }
        );
        relayViewHolder.setSwitch(powerUnit.getPresetState());
        relayViewHolder.setSwitchCheckedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (buttonView.isPressed()) {
                    if (checked) {
                        powerUnit.setPresetState(PowerUnit.ON);
                    } else {
                        powerUnit.setPresetState(PowerUnit.OFF);
                    }
                    relayViewHolder.setTime(0);
                    relayViewHolder.setTimeVisibility(checked);
                    sendCommand(powerUnit);
                }
            }
        });
    }

    private void bindPowerUnitDimmer(final RecyclerView.ViewHolder holder, final int position) {
        final DimmerViewHolder dimmerViewHolder = (DimmerViewHolder) holder;
        final PowerUnit powerUnit = (PowerUnit) units.get(position);
        dimmerViewHolder.setCheck(powerUnit.isPreset());
        dimmerViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (powerUnit.isPreset()) {
                    powerUnit.setPreset(false);
                    dimmerViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        powerUnit.setPreset(true);
                        dimmerViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        dimmerViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        dimmerViewHolder.setName(powerUnit.getName());
        dimmerViewHolder.setTime(powerUnit.getTime());
        if (powerUnit.getTime() > 0) {
            dimmerViewHolder.setBrightnessColor(0xFF808080);
        } else {
            dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
        }
        dimmerViewHolder.setTimeClick(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.applyTimePickerMode();
                            powerUnitDialog.send(null, null, null, null, null, -1, powerUnit);
                            powerUnitDialog.timeSetListener(
                                    new TimeSetListener() {
                                        @Override
                                        public void setTime(int minutes) {
                                            powerUnit.setPresetState(PowerUnit.TEMPORARY_ON);
                                            powerUnit.setTime(minutes);
                                            dimmerViewHolder.setTime(minutes);
                                            dimmerViewHolder.setBrightness(100);
                                            dimmerViewHolder.setBrightnessColor(0xFF808080);
                                            if (minutes < 1) {
                                                powerUnit.setPresetState(PowerUnit.SET_BRIGHTNESS);
                                                powerUnit.setBrightness(100);
                                                dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
                                            }
                                        }
                                    }
                            );
                        }
                        if (powerUnitDialog.isAdded()) return;
                        fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }
                }
        );
        dimmerViewHolder.setBrightness(powerUnit.getBrightness());
        dimmerViewHolder.setBrightnessClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                if (powerUnitDialog == null) {
                    powerUnitDialog = new PowerUnitDialog();
                    powerUnitDialog.send(client, nooLiteF, null, null, null, -1, powerUnit);
                    powerUnitDialog.brightnessSetListener(new BrightnessSetListener() {
                        @Override
                        public void setBrightness(int percent) {
                            dimmerViewHolder.setTime(0);
                            powerUnit.setPresetState(PowerUnit.SET_BRIGHTNESS);
                            powerUnit.setBrightness(percent);
                            dimmerViewHolder.setBrightness(percent);
                            dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
                        }
                    });
                }
                if (powerUnitDialog.isAdded()) return;
                fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
            }
        });
        dimmerViewHolder.setSwitch(powerUnit.getPresetState());
        dimmerViewHolder.setSwitchCheckedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (buttonView.isPressed()) {
                    if (checked) {
                        powerUnit.setPresetState(PowerUnit.SET_BRIGHTNESS);
                        powerUnit.setBrightness(100);
                        dimmerViewHolder.setBrightness(100);
                        dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
                    } else {
                        powerUnit.setPresetState(PowerUnit.OFF);
                        powerUnit.setBrightness(0);
                        dimmerViewHolder.setBrightness(0);
                    }
                    dimmerViewHolder.setTime(0);
                    dimmerViewHolder.setTimeVisibility(checked);
                    dimmerViewHolder.setBrightnessVisibility(checked);
                    sendCommand(powerUnit);
                }
            }
        });
    }

    private void bindPowerUnitFRelay(final RecyclerView.ViewHolder holder, final int position) {
        final RelayViewHolder relayViewHolder = (RelayViewHolder) holder;
        final PowerUnitF powerUnitF = (PowerUnitF) units.get(position);
        relayViewHolder.setCheck(powerUnitF.isPreset());
        relayViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (powerUnitF.isPreset()) {
                    powerUnitF.setPreset(false);
                    relayViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        powerUnitF.setPreset(true);
                        relayViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        relayViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        relayViewHolder.setName(powerUnitF.getName());
        relayViewHolder.setTime(powerUnitF.getTime());
        relayViewHolder.setTimeClick(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.applyTimePickerMode();
                            powerUnitDialog.send(null, null, null, null, null, -1, powerUnitF);
                            powerUnitDialog.timeSetListener(
                                    new TimeSetListener() {
                                        @Override
                                        public void setTime(int minutes) {
                                            powerUnitF.setPresetState(PowerUnitF.TEMPORARY_ON);
                                            powerUnitF.setTime(minutes);
                                            relayViewHolder.setTime(minutes);
                                            if (minutes < 1) {
                                                powerUnitF.setPresetState(PowerUnit.ON);
                                            }
                                        }
                                    }
                            );
                        }
                        if (powerUnitDialog.isAdded()) return;
                        fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }
                }
        );
        relayViewHolder.setSwitch(powerUnitF.getPresetState());
        relayViewHolder.setSwitchCheckedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (buttonView.isPressed()) {
                    if (checked) {
                        powerUnitF.setPresetState(PowerUnit.ON);
                    } else {
                        powerUnitF.setPresetState(PowerUnit.OFF);
                    }
                    relayViewHolder.setTime(0);
                    relayViewHolder.setTimeVisibility(checked);
                    sendCommand(powerUnitF);
                }
            }
        });
    }

    private void bindPowerUnitFDimmer(final RecyclerView.ViewHolder holder, final int position) {
        final DimmerViewHolder dimmerViewHolder = (DimmerViewHolder) holder;
        final PowerUnitF powerUnitF = (PowerUnitF) units.get(position);
        dimmerViewHolder.setCheck(powerUnitF.isPreset());
        dimmerViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (powerUnitF.isPreset()) {
                    powerUnitF.setPreset(false);
                    dimmerViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        powerUnitF.setPreset(true);
                        dimmerViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        dimmerViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        dimmerViewHolder.setName(powerUnitF.getName());
        dimmerViewHolder.setTime(powerUnitF.getTime());
        if (powerUnitF.getTime() > 0) {
            dimmerViewHolder.setBrightnessColor(0xFF808080);
        } else {
            dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
        }
        dimmerViewHolder.setTimeClick(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.applyTimePickerMode();
                            powerUnitDialog.send(null, null, null, null, null, -1, powerUnitF);
                            powerUnitDialog.timeSetListener(
                                    new TimeSetListener() {
                                        @Override
                                        public void setTime(int minutes) {
                                            powerUnitF.setPresetState(PowerUnit.TEMPORARY_ON);
                                            powerUnitF.setTime(minutes);
                                            dimmerViewHolder.setTime(minutes);
                                            dimmerViewHolder.setBrightness(100);
                                            dimmerViewHolder.setBrightnessColor(0xFF808080);
                                            if (minutes < 1) {
                                                powerUnitF.setPresetState(PowerUnit.SET_BRIGHTNESS);
                                                powerUnitF.setBrightness(100);
                                                dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
                                            }
                                        }
                                    }
                            );
                        }
                        if (powerUnitDialog.isAdded()) return;
                        fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }
                }
        );
        dimmerViewHolder.setBrightness(powerUnitF.getPresetBrightness());
        dimmerViewHolder.setBrightnessClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                if (powerUnitDialog == null) {
                    powerUnitDialog = new PowerUnitDialog();
                    powerUnitDialog.send(client, nooLiteF, null, null, null, powerUnitF.getAdapterPosition(), powerUnitF);
                    powerUnitDialog.brightnessSetListener(new BrightnessSetListener() {
                        @Override
                        public void setBrightness(int percent) {
                            dimmerViewHolder.setTime(0);
                            powerUnitF.setPresetState(PowerUnit.SET_BRIGHTNESS);
                            powerUnitF.setPresetBrightness(percent);
                            dimmerViewHolder.setBrightness(percent);
                            dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
                        }
                    });
                }
                if (powerUnitDialog.isAdded()) return;
                fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
            }
        });
        dimmerViewHolder.setSwitch(powerUnitF.getPresetState());
        dimmerViewHolder.setSwitchCheckedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (buttonView.isPressed()) {
                    if (checked) {
                        powerUnitF.setPresetState(PowerUnit.SET_BRIGHTNESS);
                        powerUnitF.setPresetBrightness(100);
                        dimmerViewHolder.setBrightness(100);
                        dimmerViewHolder.setBrightnessColor(0xFF2C58C0);
                    } else {
                        powerUnitF.setPresetState(PowerUnit.OFF);
                        powerUnitF.setPresetBrightness(0);
                        dimmerViewHolder.setBrightness(0);
                    }
                    dimmerViewHolder.setTime(0);
                    dimmerViewHolder.setTimeVisibility(checked);
                    dimmerViewHolder.setBrightnessVisibility(checked);
                    sendCommand(powerUnitF);
                }
            }
        });
    }

    private void bindPowerSocketF(final RecyclerView.ViewHolder holder, final int position) {
        final RelayViewHolder relayViewHolder = (RelayViewHolder) holder;
        final PowerSocketF powerSocketF = (PowerSocketF) units.get(position);
        relayViewHolder.setCheck(powerSocketF.isPreset());
        relayViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (powerSocketF.isPreset()) {
                    powerSocketF.setPreset(false);
                    relayViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        powerSocketF.setPreset(true);
                        relayViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        relayViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        relayViewHolder.setName(powerSocketF.getName());
        relayViewHolder.setTime(powerSocketF.getTime());
        relayViewHolder.setTimeClick(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) fragment.getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.applyTimePickerMode();
                            powerUnitDialog.send(null, null, null, null, null, -1, powerSocketF);
                            powerUnitDialog.timeSetListener(
                                    new TimeSetListener() {
                                        @Override
                                        public void setTime(int minutes) {
                                            powerSocketF.setPresetState(PowerUnitF.TEMPORARY_ON);
                                            powerSocketF.setTime(minutes);
                                            relayViewHolder.setTime(minutes);
                                            if (minutes < 1) {
                                                powerSocketF.setPresetState(PowerUnit.ON);
                                            }
                                        }
                                    }
                            );
                        }
                        if (powerUnitDialog.isAdded()) return;
                        fragment.getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }
                }
        );
        relayViewHolder.setSwitch(powerSocketF.getPresetState());
        relayViewHolder.setSwitchCheckedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (buttonView.isPressed()) {
                    if (checked) {
                        powerSocketF.setPresetState(PowerUnit.ON);
                    } else {
                        powerSocketF.setPresetState(PowerUnit.OFF);
                    }
                    relayViewHolder.setTime(0);
                    relayViewHolder.setTimeVisibility(checked);
                    sendCommand(powerSocketF);
                }
            }
        });
    }

    private void bindThermostat(final RecyclerView.ViewHolder holder, final int position) {
        final ThermostatViewHolder thermostatViewHolder = (ThermostatViewHolder) holder;
        final Thermostat thermostat = (Thermostat) units.get(position);
        thermostatViewHolder.setCheck(thermostat.isPreset());
        thermostatViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thermostat.isPreset()) {
                    thermostat.setPreset(false);
                    thermostatViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        thermostat.setPreset(true);
                        thermostatViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        thermostatViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        thermostatViewHolder.setName(thermostat.getName());
        thermostatViewHolder.setTemperature(thermostat.getPresetTemperature());
        thermostatViewHolder.setTemperatureClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThermostatDialog thermostatDialog = (ThermostatDialog) fragment.getChildFragmentManager().findFragmentByTag("THERMOSTAT_DIALOG");
                if (thermostatDialog == null) {
                    thermostatDialog = new ThermostatDialog();
                    thermostatDialog.send(null, null, null, null, null, thermostat);
                    thermostatDialog.setTemperatureListener(new TemperatureListener() {
                        @Override
                        public void setTemperature(int degrees) {
                            thermostat.setPresetTemperature(degrees);
                            thermostatViewHolder.setTemperature(degrees);
                        }
                    });
                }
                if (thermostatDialog.isAdded()) return;
                fragment.getChildFragmentManager().beginTransaction().add(thermostatDialog, "THERMOSTAT_DIALOG").show(thermostatDialog).commit();
            }
        });
        thermostatViewHolder.setSwitch(thermostat.getPresetState());
        thermostatViewHolder.setSwitchCheckedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (buttonView.isPressed()) {
                    if (checked) {
                        thermostat.setPresetState(Thermostat.SET_TEMPERATURE);
                    } else {
                        thermostat.setPresetState(Thermostat.OFF);
                    }
                    thermostatViewHolder.setTemperatureVisibility(checked);
                }
            }
        });
    }

    private void bindRolletUnitF(final RecyclerView.ViewHolder holder, final int position) {
        final RolletViewHolder rolletViewHolder = (RolletViewHolder) holder;
        final RolletUnitF rolletUnitF = (RolletUnitF) units.get(position);
        rolletViewHolder.setCheck(rolletUnitF.isPreset());
        rolletViewHolder.setCheckClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rolletUnitF.isPreset()) {
                    rolletUnitF.setPreset(false);
                    rolletViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 72) {
                        rolletUnitF.setPreset(true);
                        rolletViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        rolletViewHolder.setCheck(false);
                        showToast("Можно добавить только 72 устройства");
                    }
                }
            }
        });
        rolletViewHolder.setName(rolletUnitF.getName());
        switch (rolletUnitF.getPresetState()) {
            case RolletUnitF.OPEN:
                if (rolletUnitF.isInversion()) {
                    rolletViewHolder.setButton(RolletUnitF.CLOSE);
                } else {
                    rolletViewHolder.setButton(RolletUnitF.OPEN);
                }
                break;
            case RolletUnitF.CLOSE:
                if (rolletUnitF.isInversion()) {
                    rolletViewHolder.setButton(RolletUnitF.OPEN);
                } else {
                    rolletViewHolder.setButton(RolletUnitF.CLOSE);
                }
                break;
        }
        rolletViewHolder.setButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (rolletUnitF.getPresetState()) {
                    case RolletUnitF.OPEN:
                        rolletUnitF.setPresetState(RolletUnitF.CLOSE);
                        if (rolletUnitF.isInversion()) {
                            rolletViewHolder.setButton(RolletUnitF.OPEN);
                        } else {
                            rolletViewHolder.setButton(RolletUnitF.CLOSE);
                        }
                        break;
                    case RolletUnitF.CLOSE:
                        rolletUnitF.setPresetState(RolletUnitF.OPEN);
                        if (rolletUnitF.isInversion()) {
                            rolletViewHolder.setButton(RolletUnitF.CLOSE);
                        } else {
                            rolletViewHolder.setButton(RolletUnitF.OPEN);
                        }
                        break;
                }
            }
        });
    }

    private boolean isPreset(PowerUnit powerUnit) {
        for (int i = 0; i < 73; i++) {
            if (preset.getCommand(i)[1] == 0 && preset.getCommand(i)[3] == powerUnit.getChannel()) {
                powerUnit.setPresetState(preset.getCommand(i)[4]);
                if (powerUnit.getType() == PowerUnit.RGB_CONTROLLER)
                    powerUnit.setBrightness((int) ((double) ((preset.getCommand(i)[6] & 0xFF) - 28) / 128 * 100 + .5));
                else {
                    powerUnit.setBrightness((int) ((double) ((preset.getCommand(i)[6] & 0xFF) - 43) / 109 * 100 + .5));
                }
                switch (preset.getCommand(i)[4]) {
                    case Preset.ON:
                        switch (powerUnit.getType()) {
                            case PowerUnit.DIMMER:
                                powerUnit.setPresetState(PowerUnit.SET_BRIGHTNESS);
                                powerUnit.setBrightness(100);
                                break;
                            case PowerUnit.RGB_CONTROLLER:
                                powerUnit.setPresetState(PowerUnit.SET_BRIGHTNESS);
                                powerUnit.setBrightness(100);
                                break;
                        }
                        break;
                    case Preset.SET_BRIGHTNESS:
                        switch (powerUnit.getType()) {
                            case PowerUnit.RELAY:
                                powerUnit.setPresetState(PowerUnit.ON);
                                break;
                        }
                        break;
                }
                return true;
            }
        }
        powerUnit.setPresetState(PowerUnit.OFF);
        powerUnit.setBrightness(0);
        return false;
    }

    private boolean isPreset(PowerUnitF powerUnitF) {
        for (int i = 0; i < 73; i++) {
            if ((NooLiteF.getHexString(preset.getCommand(i)[10] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[11] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[12] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[13] & 0xFF))))).equals(powerUnitF.getId())) {
                powerUnitF.setPresetState(preset.getCommand(i)[4]);
                if (powerUnitF instanceof PowerUnitFA) {
                    powerUnitF.setPresetBrightness(preset.getCommand(i)[6] & 0xFF);
                } else {
                    powerUnitF.setPresetBrightness((int) ((double) (preset.getCommand(i)[6] & 0xFF) / 255 * 100 + .5));
                }
                return true;
            }
        }
        powerUnitF.setPresetState(PowerUnitF.OFF);
        return false;
    }

    private boolean isPreset(PowerSocketF powerSocketF) {
        for (int i = 0; i < 73; i++) {
            if ((NooLiteF.getHexString(preset.getCommand(i)[10] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[11] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[12] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[13] & 0xFF))))).equals(powerSocketF.getId())) {
                powerSocketF.setPresetState(preset.getCommand(i)[4]);
                powerSocketF.setTime((preset.getCommand(i)[6] & 0xFF) * 5 / 60);
                return true;
            }
        }
        powerSocketF.setPresetState(PowerSocketF.OFF);
        return false;
    }

    private boolean isPreset(Thermostat thermostat) {
        for (int i = 0; i < 73; i++) {
            if ((NooLiteF.getHexString(preset.getCommand(i)[10] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[11] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[12] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[13] & 0xFF))))).equals(thermostat.getId())) {
                thermostat.setPresetState(preset.getCommand(i)[4]);
                thermostat.setPresetTemperature(preset.getCommand(i)[6]);
                return true;
            }
        }
        thermostat.setPresetState(0);
        thermostat.setPresetTemperature(20);
        return false;
    }

    private boolean isPreset(RolletUnitF rolletUnitF) {
        for (int i = 0; i < 73; i++) {
            if ((NooLiteF.getHexString(preset.getCommand(i)[10] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[11] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[12] & 0xFF).concat(NooLiteF.getHexString(preset.getCommand(i)[13] & 0xFF))))).equals(rolletUnitF.getId())) {
                rolletUnitF.setPresetState(preset.getCommand(i)[4]);
                return true;
            }
        }
        rolletUnitF.setPresetState(RolletUnitF.CLOSE);
        return false;
    }

    private void sendCommand(final Object device) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PowerUnit powerUnit;
                PowerUnitF powerUnitF = null;
                PowerSocketF powerSocketF = null;

                byte[] bytes;
                StringBuilder command = new StringBuilder("send.htm?sd=00");

                if (device instanceof PowerUnit) {
                    powerUnit = (PowerUnit) device;
                    bytes = powerUnit.getCommand();
                    for (byte b : bytes) {
                        command.append(NooLiteF.getHexString(b & 0xFF));
                    }
                }
                if (device instanceof PowerUnitF) {
                    powerUnitF = (PowerUnitF) device;
                    bytes = powerUnitF.getCommand();
                    for (byte b : bytes) {
                        command.append(NooLiteF.getHexString(b & 0xFF));
                    }
                }
                if (device instanceof PowerSocketF) {
                    powerSocketF = (PowerSocketF) device;
                    bytes = powerSocketF.getCommand();
                    for (byte b : bytes) {
                        command.append(NooLiteF.getHexString(b & 0xFF));
                    }
                }
                Request request = new Request.Builder()
                        .url(Settings.URL() + command)
                        .post(RequestBody.create(null, ""))
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        call.cancel();

                        if (powerUnitF != null) {
                            Thread.sleep(Settings.switchTimeout());
                            nooLiteF.getPowerUnitFState(powerUnitF.getAdapterPosition(), powerUnitF.getIndex());
                        }
                        if (powerSocketF != null) {
                            Thread.sleep(Settings.switchTimeout());
                            nooLiteF.getPowerUnitFState(powerSocketF.getAdapterPosition(), powerSocketF.getIndex());
                        }
                    } else {
                        response.close();
                        showToast(String.format(Locale.ROOT, "%s %d", homeActivity.getString(R.string.connection_error), response.code()));
                    }
                } catch (ConnectException ce) {
                    call.cancel();
                    showToast(homeActivity.getString(R.string.no_connection));
                } catch (IOException ioe) {
                    call.cancel();
                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                } catch (InterruptedException e) {
                    call.cancel();
                    showToast(homeActivity.getString(R.string.some_thing_went_wrong));
                }
            }
        }).start();
    }

    private void showToast(final String message) {
        if (fragment == null) return;
        else if (!fragment.isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) toast.cancel();
                toast = Toast.makeText(homeActivity, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
