package com.noolitef;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.rx.HumidityTemperatureSensor;
import com.noolitef.rx.LeakDetector;
import com.noolitef.rx.LightSensor;
import com.noolitef.rx.MotionSensor;
import com.noolitef.rx.OpenCloseSensor;
import com.noolitef.rx.RemoteController;
import com.noolitef.rx.TemperatureSensor;
import com.noolitef.settings.Settings;
import com.noolitef.tx.PowerUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface OnUnitClickListener {
    void openPowerUnit(PowerUnit powerUnit);

    void openRGBController(PowerUnit rgbController);

    void openGraphLog(Object sensor);

    void openListLog(Object sensor);

    void openPowerUnitF(int position, Object object);

    void openPowerSocketF(int position, PowerSocketF powerSocketF);

    void openThermostat(int position, Thermostat thermostat);

    void openThermostatSchedule(int position, Thermostat thermostat);
}

class UnitsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class EmptyHeaderViewHolder extends RecyclerView.ViewHolder {
        EmptyHeaderViewHolder(View item) {
            super(item);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView name;

        HeaderViewHolder(View item) {
            super(item);
            name = item.findViewById(R.id.recycler_view_header);
            name.setPadding(headerPadding, 0, 0, 0);
        }

        void setName(String name) {
            this.name.setText(name);
        }
    }

    class PowerUnitViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private ImageView icon;
        private TextView room;
        private TextView name;
        private TextView index;

        PowerUnitViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_power_unit);
            icon = item.findViewById(R.id.card_view_power_unit_icon);
            room = item.findViewById(R.id.card_view_power_unit_room);
            name = item.findViewById(R.id.card_view_power_unit_name);
            index = item.findViewById(R.id.card_view_power_unit_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void setLongClick(View.OnLongClickListener onLongClickListener) {
            this.card.setOnLongClickListener(onLongClickListener);
        }

        void setIcon(int iconID) {
            this.icon.setImageResource(iconID);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[TX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class RemoteControllerViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private TextView index;

        RemoteControllerViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_remote_controller);
            batteryLowIndicator = item.findViewById(R.id.card_view_remote_controller_battery_low_indicator);
            room = item.findViewById(R.id.card_view_remote_controller_room);
            name = item.findViewById(R.id.card_view_remote_controller_name);
            index = item.findViewById(R.id.card_view_remote_controller_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void batteryLowIndicator(boolean visible) {
            if (visible) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class TemperatureSensorViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private TextView temperature;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private ProgressBar progressBar;
        private TextView index;

        TemperatureSensorViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_temperature_sensor);
            temperature = item.findViewById(R.id.card_view_temperature_sensor_temperature);
            room = item.findViewById(R.id.card_view_temperature_sensor_room);
            name = item.findViewById(R.id.card_view_temperature_sensor_name);
            batteryLowIndicator = item.findViewById(R.id.card_view_temperature_sensor_battery_low_indicator);
            progressBar = item.findViewById(R.id.card_view_temperature_sensor_update_indicator);
            progressBar.setVisibility(View.GONE);
            index = item.findViewById(R.id.card_view_temperature_sensor_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void setLongClick(View.OnLongClickListener onLongClickListener) {
            this.card.setOnLongClickListener(onLongClickListener);
        }

        void batteryLowIndicator(boolean Ok) {
            if (!Ok) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setUpdating(boolean update) {
            if (!update) this.progressBar.setVisibility(View.GONE);
        }

        void setTemperature(String degrees) {
            this.temperature.setText(degrees);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setRelevance(boolean relevant) {
            if (relevant) {
                if (Settings.isNightMode()) {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                } else {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                }
            } else {
                if (Settings.isNightMode()) {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
                } else {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
                }
            }
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class HumidityTemperatureSensorViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private TextView temperature;
        private TextView humidity;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private ProgressBar progressBar;
        private TextView index;

        HumidityTemperatureSensorViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_humidity_temperature_sensor);
            temperature = item.findViewById(R.id.card_view_humidity_temperature_sensor_temperature);
            humidity = item.findViewById(R.id.card_view_humidity_temperature_sensor_humidity);
            room = item.findViewById(R.id.card_view_humidity_temperature_sensor_room);
            name = item.findViewById(R.id.card_view_humidity_temperature_sensor_name);
            batteryLowIndicator = item.findViewById(R.id.card_view_humidity_temperature_sensor_battery_low_indicator);
            progressBar = item.findViewById(R.id.card_view_humidity_temperature_sensor_update_indicator);
            progressBar.setVisibility(View.GONE);
            index = item.findViewById(R.id.card_view_humidity_temperature_sensor_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void setLongClick(View.OnLongClickListener onLongClickListener) {
            this.card.setOnLongClickListener(onLongClickListener);
        }

        void batteryLowIndicator(boolean Ok) {
            if (!Ok) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setUpdating(boolean update) {
            if (!update) this.progressBar.setVisibility(View.GONE);
        }

        void setTemperature(String degrees) {
            this.temperature.setText(degrees);
        }

        void setHumidity(String percentages) {
            this.humidity.setText(percentages);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setRelevance(boolean relevant) {
            if (relevant) {
                if (Settings.isNightMode()) {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    humidity.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                } else {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    humidity.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                }
            } else {
                if (Settings.isNightMode()) {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
                    humidity.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
                } else {
                    temperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
                    humidity.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
                }
            }
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class MotionSensorViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private ProgressBar progressBar;
        private TextView index;

        MotionSensorViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_motion_sensor);
            batteryLowIndicator = item.findViewById(R.id.card_view_motion_sensor_battery_low_indicator);
            room = item.findViewById(R.id.card_view_motion_sensor_room);
            name = item.findViewById(R.id.card_view_motion_sensor_name);
            progressBar = item.findViewById(R.id.card_view_motion_sensor_update_indicator);
            progressBar.setVisibility(View.GONE);
            index = item.findViewById(R.id.card_view_motion_sensor_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void batteryLowIndicator(boolean Ok) {
            if (!Ok) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setUpdating(boolean update) {
            if (!update) this.progressBar.setVisibility(View.GONE);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class OpenCloseSensorViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private ImageView state;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private ProgressBar progressBar;
        private TextView index;

        OpenCloseSensorViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_open_close_sensor);
            batteryLowIndicator = item.findViewById(R.id.card_view_open_close_sensor_battery_low_indicator);
            state = item.findViewById(R.id.card_view_open_close_sensor_state);
            room = item.findViewById(R.id.card_view_open_close_sensor_room);
            name = item.findViewById(R.id.card_view_open_close_sensor_name);
            progressBar = item.findViewById(R.id.card_view_open_close_sensor_update_indicator);
            progressBar.setVisibility(View.GONE);
            index = item.findViewById(R.id.card_view_open_close_sensor_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void batteryLowIndicator(boolean Ok) {
            if (!Ok) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setUpdating(boolean update) {
            if (!update) this.progressBar.setVisibility(View.GONE);
        }

        void setState(int drawableID) {
            this.state.setImageResource(drawableID);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class LeakDetectorViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private ImageView state;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private ProgressBar progressBar;
        private TextView index;

        LeakDetectorViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_leak_detector);
            batteryLowIndicator = item.findViewById(R.id.card_view_leak_detector_battery_low_indicator);
            state = item.findViewById(R.id.card_view_leak_detector_state);
            room = item.findViewById(R.id.card_view_leak_detector_room);
            name = item.findViewById(R.id.card_view_leak_detector_name);
            progressBar = item.findViewById(R.id.card_view_leak_detector_update_indicator);
            progressBar.setVisibility(View.GONE);
            index = item.findViewById(R.id.card_view_leak_detector_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void batteryLowIndicator(boolean visible) {
            if (visible) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setUpdating(boolean update) {
            if (!update) this.progressBar.setVisibility(View.GONE);
        }

        void setState(int drawableID) {
            this.state.setImageResource(drawableID);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class LightSensorViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private ImageView state;
        private TextView room;
        private TextView name;
        private ImageView batteryLowIndicator;
        private ProgressBar progressBar;
        private TextView index;

        LightSensorViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_light_sensor);
            batteryLowIndicator = item.findViewById(R.id.card_view_light_sensor_battery_low_indicator);
            state = item.findViewById(R.id.card_view_light_sensor_state);
            room = item.findViewById(R.id.card_view_light_sensor_room);
            name = item.findViewById(R.id.card_view_light_sensor_name);
            progressBar = item.findViewById(R.id.card_view_light_sensor_update_indicator);
            progressBar.setVisibility(View.GONE);
            index = item.findViewById(R.id.card_view_light_sensor_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void batteryLowIndicator(boolean visible) {
            if (visible) {
                batteryLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        void setUpdating(boolean update) {
            if (!update) this.progressBar.setVisibility(View.GONE);
        }

        void setState(int drawableID) {
            this.state.setImageResource(drawableID);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[RX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class PowerUnitFViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private View state;
        private ProgressBar progressBar;
        private ImageView icon;
        private TextView room;
        private TextView name;
        private TextView index;

        PowerUnitFViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_power_unit_f);
            state = item.findViewById(R.id.card_view_power_unit_f_state);
            progressBar = item.findViewById(R.id.card_view_power_unit_f_progressbar);
            icon = item.findViewById(R.id.card_view_power_unit_f_icon);
            room = item.findViewById(R.id.card_view_power_unit_f_room);
            name = item.findViewById(R.id.card_view_power_unit_f_name);
            index = item.findViewById(R.id.card_view_power_unit_f_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void setLongClick(View.OnLongClickListener onLongClickListener) {
            this.card.setOnLongClickListener(onLongClickListener);
        }

        void setState(int state) {
            switch (state) {
                case PowerUnitF.NOT_CONNECTED:
                    this.state.setBackgroundResource(R.drawable.card_view_state_not_connected);
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.state.setVisibility(View.VISIBLE);
                    this.card.setEnabled(true);
                    break;
                case PowerUnitF.ON:
                    this.state.setBackgroundResource(R.drawable.card_view_state_on);
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.state.setVisibility(View.VISIBLE);
                    this.card.setEnabled(true);
                    break;
                case PowerUnitF.UPDATING:
                    this.card.setEnabled(false);
                    this.state.setVisibility(View.INVISIBLE);
                    this.progressBar.setVisibility(View.VISIBLE);
                    break;
                case PowerUnitF.OFF:
                    if (Settings.isNightMode()) {
                        this.state.setBackgroundResource(R.drawable.card_view_state_off_light);
                    } else {
                        this.state.setBackgroundResource(R.drawable.card_view_state_off);
                    }
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.state.setVisibility(View.VISIBLE);
                    this.card.setEnabled(true);
                    break;
            }
        }

        void setIcon(int imageResourceId) {
            this.icon.setImageResource(imageResourceId);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[FTX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class PowerSocketFViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private View state;
        private ProgressBar progressBar;
        private TextView room;
        private TextView name;
        private TextView index;

        PowerSocketFViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_power_socket_f);
            state = item.findViewById(R.id.card_view_power_socket_f_state);
            progressBar = item.findViewById(R.id.card_view_power_socket_f_progressbar);
            room = item.findViewById(R.id.card_view_power_socket_f_room);
            name = item.findViewById(R.id.card_view_power_socket_f_name);
            index = item.findViewById(R.id.card_view_power_socket_f_index);
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void setLongClick(View.OnLongClickListener onLongClickListener) {
            this.card.setOnLongClickListener(onLongClickListener);
        }

        void setState(int state) {
            switch (state) {
                case PowerSocketF.NOT_CONNECTED:
                    this.state.setBackgroundResource(R.drawable.card_view_state_not_connected);
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.state.setVisibility(View.VISIBLE);
                    this.card.setEnabled(true);
                    break;
                case PowerSocketF.ON:
                    this.state.setBackgroundResource(R.drawable.card_view_state_on);
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.state.setVisibility(View.VISIBLE);
                    this.card.setEnabled(true);
                    break;
                case PowerSocketF.UPDATING:
                    this.card.setEnabled(false);
                    this.state.setVisibility(View.INVISIBLE);
                    this.progressBar.setVisibility(View.VISIBLE);
                    break;
                case PowerSocketF.OFF:
                    if (Settings.isNightMode()) {
                        this.state.setBackgroundResource(R.drawable.card_view_state_off_light);
                    } else {
                        this.state.setBackgroundResource(R.drawable.card_view_state_off);
                    }
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.state.setVisibility(View.VISIBLE);
                    this.card.setEnabled(true);
                    break;
            }
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[FTX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    class ThermostatViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private View stateIcon;
        private ProgressBar progressBar;
        private TextView currentTemperature;
        private TextView targetTemperature;
        private TextView room;
        private TextView name;
        private TextView index;

        ThermostatViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_thermostat);
            stateIcon = item.findViewById(R.id.card_view_thermostat_state);
            progressBar = item.findViewById(R.id.card_view_thermostat_progressbar);
            currentTemperature = item.findViewById(R.id.card_view_thermostat_current_temperature);
            targetTemperature = item.findViewById(R.id.card_view_thermostat_target_temperature);
            room = item.findViewById(R.id.card_view_thermostat_room);
            name = item.findViewById(R.id.card_view_thermostat_name);
            index = item.findViewById(R.id.card_view_thermostat_index);
        }

        void setState(int state, int out) {
            switch (state) {
                case Thermostat.NOT_CONNECTED:
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.stateIcon.setBackgroundResource(R.drawable.card_view_state_not_connected);
                    break;
                case Thermostat.UPDATING:
                    this.card.setClickable(false);
                    this.progressBar.setVisibility(View.VISIBLE);
                    break;
                case Thermostat.OFF:
                    this.progressBar.setVisibility(View.INVISIBLE);
                    if (Settings.isNightMode()) {
                        this.stateIcon.setBackgroundResource(R.drawable.card_view_state_off_light);
                    } else {
                        this.stateIcon.setBackgroundResource(R.drawable.card_view_state_off);
                    }
                    break;
                case Thermostat.ON:
                    this.progressBar.setVisibility(View.INVISIBLE);
                    this.stateIcon.setBackgroundResource(R.drawable.card_view_state_on);
                    break;
            }
            switch (out) {
                case Thermostat.OUTPUT_OFF:
                    if (Settings.isNightMode()) {
                        targetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out_off_light);
                        targetTemperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    } else {
                        targetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out_off);
                        targetTemperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    }
                    break;
                case Thermostat.OUTPUT_ON:
                    targetTemperature.setBackgroundResource(R.drawable.card_view_thermostat_out_on);
                    targetTemperature.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.white))));
                    break;
            }
        }

        void setClick(View.OnClickListener onClickListener) {
            this.card.setOnClickListener(onClickListener);
        }

        void setLongClick(View.OnLongClickListener onLongClickListener) {
            this.card.setOnLongClickListener(onLongClickListener);
        }

        void setCurrentTemperature(String currentTemperature) {
            this.currentTemperature.setText(currentTemperature);
        }

        void setTargetTemperature(String targetTemperature) {
            this.targetTemperature.setText(targetTemperature);
        }

        void setRoom(String room) {
            this.room.setText(room);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[FTX%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }
    }

    Context context;

    OnUnitClickListener onUnitClickListener;

    static final int EMPTY_HEADER = 0;
    static final int HEADER = 1;
    private static final int POWER_UNIT = 2;
    private static final int REMOTE_CONTROLLER = 3;
    private static final int TEMPERATURE_SENSOR = 4;
    private static final int HUMIDITY_TEMPERATURE_SENSOR = 5;
    private static final int MOTION_SENSOR = 6;
    private static final int OPEN_CLOSE_SENSOR = 7;
    private static final int LEAK_DETECTOR = 8;
    private static final int LIGHT_SENSOR = 9;
    private static final int POWER_UNIT_F = 10;
    private static final int POWER_SOCKET_F = 11;
    private static final int THERMOSTAT = 12;
    private static final int ROLLET_UNIT_F = 13;

    private int headerPadding;

    private ArrayList<Object> units;
    private NooLiteF nooLiteF;

    private HomeActivity homeActivity;
    private FragmentManager fragmentManager;
    private OkHttpClient client;

    UnitsRecyclerAdapter(HomeActivity homeActivity, FragmentManager fragmentManager, Context context, NooLiteF nooLiteF, OkHttpClient client, int headerLeftPadding, ArrayList<Object> units) {
        this.homeActivity = homeActivity;
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.nooLiteF = nooLiteF;
        this.client = client;
        this.headerPadding = headerLeftPadding;
        this.units = units;
        setHasStableIds(true);
    }

    void setOnUnitClickListener(OnUnitClickListener listener) {
        onUnitClickListener = listener;
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
        if (units.get(position) instanceof EmptyHeader) return EMPTY_HEADER;
        if (units.get(position) instanceof Header) return HEADER;
        if (units.get(position) instanceof PowerUnit) return POWER_UNIT;
        if (units.get(position) instanceof RemoteController) return REMOTE_CONTROLLER;
        if (units.get(position) instanceof TemperatureSensor)
            return TEMPERATURE_SENSOR;
        if (units.get(position) instanceof HumidityTemperatureSensor)
            return HUMIDITY_TEMPERATURE_SENSOR;
        if (units.get(position) instanceof MotionSensor) return MOTION_SENSOR;
        if (units.get(position) instanceof OpenCloseSensor) return OPEN_CLOSE_SENSOR;
        if (units.get(position) instanceof LeakDetector) return LEAK_DETECTOR;
        if (units.get(position) instanceof LightSensor) return LIGHT_SENSOR;
        if (units.get(position) instanceof PowerUnitF) return POWER_UNIT_F;
        if (units.get(position) instanceof PowerSocketF) return POWER_SOCKET_F;
        if (units.get(position) instanceof Thermostat) return THERMOSTAT;
        if (units.get(position) instanceof RolletUnitF) return ROLLET_UNIT_F;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view;
        if (Settings.isNightMode()) {
            switch (type) {
                case EMPTY_HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_units_empty_header, parent, false);
                    EmptyHeaderViewHolder emptyHeaderViewHolder = new EmptyHeaderViewHolder(view);
                    return emptyHeaderViewHolder;
                case HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_header_light, parent, false);
                    HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
                    return headerViewHolder;
                case POWER_UNIT:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_unit_dark, parent, false);
                    PowerUnitViewHolder powerUnitViewHolder = new PowerUnitViewHolder(view);
                    return powerUnitViewHolder;
                case REMOTE_CONTROLLER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_remote_controller_dark, parent, false);
                    RemoteControllerViewHolder remoteControllerViewHolder = new RemoteControllerViewHolder(view);
                    return remoteControllerViewHolder;
                case TEMPERATURE_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_temperature_sensor_dark, parent, false);
                    TemperatureSensorViewHolder temperatureSensorViewHolder = new TemperatureSensorViewHolder(view);
                    return temperatureSensorViewHolder;
                case HUMIDITY_TEMPERATURE_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_humidity_temperature_sensor_dark, parent, false);
                    HumidityTemperatureSensorViewHolder humidityTemperatureSensorViewHolder = new HumidityTemperatureSensorViewHolder(view);
                    return humidityTemperatureSensorViewHolder;
                case MOTION_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_motion_sensor_dark, parent, false);
                    MotionSensorViewHolder motionSensorViewHolder = new MotionSensorViewHolder(view);
                    return motionSensorViewHolder;
                case OPEN_CLOSE_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_open_close_sensor_dark, parent, false);
                    OpenCloseSensorViewHolder openCloseSensorViewHolder = new OpenCloseSensorViewHolder(view);
                    return openCloseSensorViewHolder;
                case LEAK_DETECTOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_leak_detector_dark, parent, false);
                    return new LeakDetectorViewHolder(view);
                case LIGHT_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_light_sensor_dark, parent, false);
                    LightSensorViewHolder lightSensorViewHolder = new LightSensorViewHolder(view);
                    return lightSensorViewHolder;
                case POWER_UNIT_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_unit_f_dark, parent, false);
                    PowerUnitFViewHolder powerUnitFViewHolder = new PowerUnitFViewHolder(view);
                    return powerUnitFViewHolder;
                case POWER_SOCKET_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_socket_f_dark, parent, false);
                    PowerSocketFViewHolder powerSocketFViewHolder = new PowerSocketFViewHolder(view);
                    return powerSocketFViewHolder;
                case THERMOSTAT:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_thermostat_dark, parent, false);
                    ThermostatViewHolder thermostatViewHolder = new ThermostatViewHolder(view);
                    return thermostatViewHolder;
                case ROLLET_UNIT_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_unit_f_dark, parent, false);
                    PowerUnitFViewHolder rolletUnitFViewHolder = new PowerUnitFViewHolder(view);
                    return rolletUnitFViewHolder;
            }
        } else {
            switch (type) {
                case EMPTY_HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_units_empty_header, parent, false);
                    EmptyHeaderViewHolder emptyHeaderViewHolder = new EmptyHeaderViewHolder(view);
                    return emptyHeaderViewHolder;
                case HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_header, parent, false);
                    HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
                    return headerViewHolder;
                case POWER_UNIT:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_unit, parent, false);
                    PowerUnitViewHolder powerUnitViewHolder = new PowerUnitViewHolder(view);
                    return powerUnitViewHolder;
                case REMOTE_CONTROLLER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_remote_controller, parent, false);
                    RemoteControllerViewHolder remoteControllerViewHolder = new RemoteControllerViewHolder(view);
                    return remoteControllerViewHolder;
                case TEMPERATURE_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_temperature_sensor, parent, false);
                    TemperatureSensorViewHolder temperatureSensorViewHolder = new TemperatureSensorViewHolder(view);
                    return temperatureSensorViewHolder;
                case HUMIDITY_TEMPERATURE_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_humidity_temperature_sensor, parent, false);
                    HumidityTemperatureSensorViewHolder humidityTemperatureSensorViewHolder = new HumidityTemperatureSensorViewHolder(view);
                    return humidityTemperatureSensorViewHolder;
                case MOTION_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_motion_sensor, parent, false);
                    MotionSensorViewHolder motionSensorViewHolder = new MotionSensorViewHolder(view);
                    return motionSensorViewHolder;
                case OPEN_CLOSE_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_open_close_sensor, parent, false);
                    OpenCloseSensorViewHolder openCloseSensorViewHolder = new OpenCloseSensorViewHolder(view);
                    return openCloseSensorViewHolder;
                case LEAK_DETECTOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_leak_detector, parent, false);
                    return new LeakDetectorViewHolder(view);
                case LIGHT_SENSOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_light_sensor, parent, false);
                    LightSensorViewHolder lightSensorViewHolder = new LightSensorViewHolder(view);
                    return lightSensorViewHolder;
                case POWER_UNIT_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_unit_f, parent, false);
                    PowerUnitFViewHolder powerUnitFViewHolder = new PowerUnitFViewHolder(view);
                    return powerUnitFViewHolder;
                case POWER_SOCKET_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_socket_f, parent, false);
                    PowerSocketFViewHolder powerSocketFViewHolder = new PowerSocketFViewHolder(view);
                    return powerSocketFViewHolder;
                case THERMOSTAT:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_thermostat, parent, false);
                    ThermostatViewHolder thermostatViewHolder = new ThermostatViewHolder(view);
                    return thermostatViewHolder;
                case ROLLET_UNIT_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_power_unit_f, parent, false);
                    PowerUnitFViewHolder rolletUnitFViewHolder = new PowerUnitFViewHolder(view);
                    return rolletUnitFViewHolder;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case EMPTY_HEADER:
                bindEmptyHeader(holder, position);
                break;
            case HEADER:
                bindHeader(holder, position);
                break;
            case POWER_UNIT:
                bindPowerUnit(holder, position);
                break;
            case REMOTE_CONTROLLER:
                bindRemoteController(holder, position);
                break;
            case TEMPERATURE_SENSOR:
                bindTemperatureSensor(holder, position);
                break;
            case HUMIDITY_TEMPERATURE_SENSOR:
                bindHumidityTemperatureSensor(holder, position);
                break;
            case MOTION_SENSOR:
                bindMotionSensor(holder, position);
                break;
            case OPEN_CLOSE_SENSOR:
                bindOpenCloseSensor(holder, position);
                break;
            case LEAK_DETECTOR:
                bindLeakDetector(holder, position);
                break;
            case LIGHT_SENSOR:
                bindLightSensor(holder, position);
                break;
            case POWER_UNIT_F:
                bindPowerUnitF(holder, position);
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

    private void bindEmptyHeader(RecyclerView.ViewHolder holder, int position) {
    }

    private void bindHeader(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        if (position == 0)
            headerViewHolder.name.setPadding(headerPadding, 2 * headerPadding, 0, 0);
        headerViewHolder.setName(String.valueOf(((Header) units.get(position)).getName()));
    }

    private void bindPowerUnit(final RecyclerView.ViewHolder holder, final int position) {
        final PowerUnitViewHolder powerUnitViewHolder = (PowerUnitViewHolder) holder;
        final PowerUnit powerUnit = (PowerUnit) units.get(position);
        powerUnitViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (powerUnit.getType()) {
                    case PowerUnit.DIMMER:
                    case PowerUnit.RGB_CONTROLLER:
                    case PowerUnit.RELAY:
                        nooLiteF.switchTX(position, powerUnit.getChannel());
                        break;
                    case PowerUnit.PULSE_RELAY:
                        nooLiteF.temporaryOnTX(position, powerUnit.getChannel());
                        break;
                    case PowerUnit.ROLLET:
                        onUnitClickListener.openPowerUnit(powerUnit);
                        break;
                }
            }
        });
        powerUnitViewHolder.setLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                switch (powerUnit.getType()) {
                    case PowerUnit.DIMMER:
                    case PowerUnit.RELAY:
                    case PowerUnit.PULSE_RELAY:
                        onUnitClickListener.openPowerUnit(powerUnit);
                        break;
                    case PowerUnit.RGB_CONTROLLER:
                        onUnitClickListener.openRGBController(powerUnit);
                        break;
                    case PowerUnit.ROLLET:
                        return false;
                    default:
                        onUnitClickListener.openPowerUnit(powerUnit);
                }
                return true;
            }
        });
        if (Settings.isNightMode()) {
            switch (powerUnit.getType()) {
                case PowerUnit.DIMMER:
                case PowerUnit.RELAY:
                    powerUnitViewHolder.setIcon(R.drawable.ic_bulb_grey);
                    break;
                case PowerUnit.PULSE_RELAY:
                    powerUnitViewHolder.setIcon(R.drawable.ic_gate_grey);
                    break;
                case PowerUnit.RGB_CONTROLLER:
                    powerUnitViewHolder.setIcon(R.drawable.ic_rgb_controller_grey);
                    break;
                case PowerUnit.ROLLET:
                    powerUnitViewHolder.setIcon(R.drawable.ic_rollet_grey);
                    break;
            }
        } else {
            switch (powerUnit.getType()) {
                case PowerUnit.DIMMER:
                case PowerUnit.RELAY:
                    powerUnitViewHolder.setIcon(R.drawable.ic_bulb);
                    break;
                case PowerUnit.PULSE_RELAY:
                    powerUnitViewHolder.setIcon(R.drawable.ic_gate);
                    break;
                case PowerUnit.RGB_CONTROLLER:
                    powerUnitViewHolder.setIcon(R.drawable.ic_rgb_controller);
                    break;
                case PowerUnit.ROLLET:
                    powerUnitViewHolder.setIcon(R.drawable.ic_rollet);
                    break;
            }
        }
        powerUnitViewHolder.setRoom(powerUnit.getRoom());
        powerUnitViewHolder.setName(powerUnit.getName());
        if (Settings.isDeveloperMode()) {
            powerUnitViewHolder.setIndex(powerUnit.getChannel());
        }
    }

    private void bindRemoteController(RecyclerView.ViewHolder holder, int position) {
        RemoteControllerViewHolder remoteControllerViewHolder = (RemoteControllerViewHolder) holder;
        final RemoteController remoteController = (RemoteController) units.get(position);
        remoteControllerViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openListLog(remoteController);
            }
        });
        remoteControllerViewHolder.batteryLowIndicator(remoteController.isBatteryLow());
        remoteControllerViewHolder.setRoom(remoteController.getRoom());
        remoteControllerViewHolder.setName(remoteController.getName());
        if (Settings.isDeveloperMode()) {
            remoteControllerViewHolder.setIndex(remoteController.getChannel());
        }
    }

    private void bindTemperatureSensor(RecyclerView.ViewHolder holder, int position) {
        TemperatureSensorViewHolder temperatureSensorViewHolder = (TemperatureSensorViewHolder) holder;
        final TemperatureSensor temperatureSensor = (TemperatureSensor) units.get(position);
        temperatureSensor.setAdapterPosition(position);
        temperatureSensorViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openGraphLog(temperatureSensor);
            }
        });
        temperatureSensorViewHolder.setLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onUnitClickListener.openListLog(temperatureSensor);
                return true;
            }
        });
        temperatureSensorViewHolder.batteryLowIndicator(temperatureSensor.isBatteryOK());
        if (temperatureSensor.getUpdateYear() != -1)
            temperatureSensorViewHolder.setUpdating(false);
        temperatureSensorViewHolder.setTemperature(String.valueOf(temperatureSensor.getCurrentTemperature()).concat("°C"));
        temperatureSensorViewHolder.setRoom(temperatureSensor.getRoom());
        temperatureSensorViewHolder.setName(temperatureSensor.getName());
        if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(temperatureSensor.getUpdateYear(), temperatureSensor.getUpdateMonth(), temperatureSensor.getUpdateDay(), temperatureSensor.getUpdateHour(), temperatureSensor.getUpdateMinute(), temperatureSensor.getUpdateSecond())) > 10800000) {
            temperatureSensorViewHolder.setRelevance(false);
        } else {
            temperatureSensorViewHolder.setRelevance(true);
        }
        if (Settings.isDeveloperMode()) {
            temperatureSensorViewHolder.setIndex(temperatureSensor.getChannel());
        }
    }

    private void bindHumidityTemperatureSensor(RecyclerView.ViewHolder holder, int position) {
        HumidityTemperatureSensorViewHolder humidityTemperatureSensorViewHolder = (HumidityTemperatureSensorViewHolder) holder;
        final HumidityTemperatureSensor humidityTemperatureSensor = (HumidityTemperatureSensor) units.get(position);
        humidityTemperatureSensor.setAdapterPosition(position);
        humidityTemperatureSensorViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openGraphLog(humidityTemperatureSensor);
            }
        });
        humidityTemperatureSensorViewHolder.setLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onUnitClickListener.openListLog(humidityTemperatureSensor);
                return true;
            }
        });
        humidityTemperatureSensorViewHolder.batteryLowIndicator(humidityTemperatureSensor.isBatteryOK());
        if (humidityTemperatureSensor.getLastUpdateYear() != -1)
            humidityTemperatureSensorViewHolder.setUpdating(false);
        humidityTemperatureSensorViewHolder.setTemperature(String.valueOf(humidityTemperatureSensor.getTemperature()).concat("°C"));
        humidityTemperatureSensorViewHolder.setHumidity(String.valueOf(humidityTemperatureSensor.getHumidity()).concat("%"));
        humidityTemperatureSensorViewHolder.setRoom(humidityTemperatureSensor.getRoom());
        humidityTemperatureSensorViewHolder.setName(humidityTemperatureSensor.getName());
        if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(humidityTemperatureSensor.getLastUpdateYear(), humidityTemperatureSensor.getLastUpdateMonth(), humidityTemperatureSensor.getLastUpdateDay(), humidityTemperatureSensor.getLastUpdateHour(), humidityTemperatureSensor.getLastUpdateMinute(), humidityTemperatureSensor.getLastUpdateSecond())) > 10800000) {
            humidityTemperatureSensorViewHolder.setRelevance(false);
        } else {
            humidityTemperatureSensorViewHolder.setRelevance(true);
        }
        if (Settings.isDeveloperMode()) {
            humidityTemperatureSensorViewHolder.setIndex(humidityTemperatureSensor.getChannel());
        }
    }

    private void bindMotionSensor(RecyclerView.ViewHolder holder, int position) {
        MotionSensorViewHolder motionSensorViewHolder = (MotionSensorViewHolder) holder;
        final MotionSensor motionSensor = (MotionSensor) units.get(position);
        motionSensorViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openListLog(motionSensor);
            }
        });
        motionSensorViewHolder.batteryLowIndicator(motionSensor.isBatteryOK());
        motionSensorViewHolder.setRoom(motionSensor.getRoom());
        motionSensorViewHolder.setName(motionSensor.getName());
        // add check relevance of data
        if (Settings.isDeveloperMode()) {
            motionSensorViewHolder.setIndex(motionSensor.getChannel());
        }
    }

    private void bindOpenCloseSensor(RecyclerView.ViewHolder holder, int position) {
        OpenCloseSensorViewHolder openCloseSensorViewHolder = (OpenCloseSensorViewHolder) holder;
        final OpenCloseSensor openCloseSensor = (OpenCloseSensor) units.get(position);
        openCloseSensor.setAdapterPosition(position);
        openCloseSensorViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openListLog(openCloseSensor);
            }
        });
        openCloseSensorViewHolder.batteryLowIndicator(openCloseSensor.isBatteryOK());
        if (openCloseSensor.getLastUpdateYear() != -1)
            openCloseSensorViewHolder.setUpdating(false);
        if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(openCloseSensor.getLastUpdateYear(), openCloseSensor.getLastUpdateMonth(), openCloseSensor.getLastUpdateDay(), openCloseSensor.getLastUpdateHour(), openCloseSensor.getLastUpdateMinute(), openCloseSensor.getLastUpdateSecond())) > 43200000) {
            if (openCloseSensor.isClosed()) {
                if (Settings.isNightMode()) {
                    openCloseSensorViewHolder.setState(R.drawable.ic_closed_grey_800);
                } else {
                    openCloseSensorViewHolder.setState(R.drawable.ic_closed_grey);
                }
            } else {
                if (Settings.isNightMode()) {
                    openCloseSensorViewHolder.setState(R.drawable.ic_opened_grey_800);
                } else {
                    openCloseSensorViewHolder.setState(R.drawable.ic_opened_grey);
                }
            }
        } else {
            if (openCloseSensor.isClosed()) {
                if (Settings.isNightMode()) {
                    openCloseSensorViewHolder.setState(R.drawable.ic_closed_grey);
                } else {
                    openCloseSensorViewHolder.setState(R.drawable.ic_closed_black);
                }
            } else {
                if (Settings.isNightMode()) {
                    openCloseSensorViewHolder.setState(R.drawable.ic_opened_grey);
                } else {
                    openCloseSensorViewHolder.setState(R.drawable.ic_opened_black);
                }
            }
        }
        openCloseSensorViewHolder.setRoom(openCloseSensor.getRoom());
        openCloseSensorViewHolder.setName(openCloseSensor.getName());
        if (Settings.isDeveloperMode()) {
            openCloseSensorViewHolder.setIndex(openCloseSensor.getChannel());
        }
    }

    private void bindLeakDetector(RecyclerView.ViewHolder holder, int position) {
        LeakDetectorViewHolder leakDetectorViewHolder = (LeakDetectorViewHolder) holder;
        final LeakDetector leakDetector = (LeakDetector) units.get(position);
        leakDetector.setAdapterPosition(position);
        leakDetectorViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openListLog(leakDetector);
            }
        });
        leakDetectorViewHolder.batteryLowIndicator(leakDetector.isBatteryLow());
        if (leakDetector.getLastUpdateYear() != -1) leakDetectorViewHolder.setUpdating(false);
        if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(leakDetector.getLastUpdateYear(), leakDetector.getLastUpdateMonth(), leakDetector.getLastUpdateDay(), leakDetector.getLastUpdateHour(), leakDetector.getLastUpdateMinute(), leakDetector.getLastUpdateSecond())) > 86400000) {
            if (leakDetector.isLeakage()) {
                if (Settings.isNightMode()) {
                    leakDetectorViewHolder.setState(R.drawable.ic_wet_grey_800);
                } else {
                    leakDetectorViewHolder.setState(R.drawable.ic_wet_grey);
                }
            } else {
                if (Settings.isNightMode()) {
                    leakDetectorViewHolder.setState(R.drawable.ic_dry_grey_800);
                } else {
                    leakDetectorViewHolder.setState(R.drawable.ic_dry_grey);
                }
            }
        } else {
            if (leakDetector.isLeakage()) {
                if (Settings.isNightMode()) {
                    leakDetectorViewHolder.setState(R.drawable.ic_wet_grey);
                } else {
                    leakDetectorViewHolder.setState(R.drawable.ic_wet_black);
                }
            } else {
                if (Settings.isNightMode()) {
                    leakDetectorViewHolder.setState(R.drawable.ic_dry_grey);
                } else {
                    leakDetectorViewHolder.setState(R.drawable.ic_dry_black);
                }
            }
        }
        leakDetectorViewHolder.setRoom(leakDetector.getRoom());
        leakDetectorViewHolder.setName(leakDetector.getName());
        if (Settings.isDeveloperMode()) {
            leakDetectorViewHolder.setIndex(leakDetector.getChannel());
        }
    }

    private void bindLightSensor(RecyclerView.ViewHolder holder, int position) {
        LightSensorViewHolder lightSensorViewHolder = (LightSensorViewHolder) holder;
        final LightSensor lightSensor = (LightSensor) units.get(position);
        lightSensor.setAdapterPosition(position);
        lightSensorViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openListLog(lightSensor);
            }
        });
        lightSensorViewHolder.batteryLowIndicator(lightSensor.isBatteryLow());
        lightSensorViewHolder.setUpdating(false);
        if (lightSensor.getLastUpdateYear() != -1) lightSensorViewHolder.setUpdating(false);
        if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(lightSensor.getLastUpdateYear(), lightSensor.getLastUpdateMonth(), lightSensor.getLastUpdateDay(), lightSensor.getLastUpdateHour(), lightSensor.getLastUpdateMinute(), lightSensor.getLastUpdateSecond())) > 10800000) {
            if (lightSensor.isDark()) {
                if (Settings.isNightMode()) {
                    lightSensorViewHolder.setState(R.drawable.ic_moon_grey_800);
                } else {
                    lightSensorViewHolder.setState(R.drawable.ic_moon_grey);
                }
            } else {
                if (Settings.isNightMode()) {
                    lightSensorViewHolder.setState(R.drawable.ic_sun_grey_800);
                } else {
                    lightSensorViewHolder.setState(R.drawable.ic_sun_grey);
                }
            }
        } else {
            if (lightSensor.isDark()) {
                if (Settings.isNightMode()) {
                    lightSensorViewHolder.setState(R.drawable.ic_moon_grey);
                } else {
                    lightSensorViewHolder.setState(R.drawable.ic_moon_black);
                }
            } else {
                if (Settings.isNightMode()) {
                    lightSensorViewHolder.setState(R.drawable.ic_sun_grey);
                } else {
                    lightSensorViewHolder.setState(R.drawable.ic_sun_black);
                }
            }
        }
        lightSensorViewHolder.setRoom(lightSensor.getRoom());
        lightSensorViewHolder.setName(lightSensor.getName());
        if (Settings.isDeveloperMode()) {
            lightSensorViewHolder.setIndex(lightSensor.getChannel());
        }
    }

    private void bindPowerUnitF(final RecyclerView.ViewHolder holder, final int position) {
        final PowerUnitFViewHolder powerUnitFViewHolder = (PowerUnitFViewHolder) holder;
        final PowerUnitF powerUnitF = (PowerUnitF) units.get(position);
        powerUnitF.setAdapterPosition(position);
        powerUnitFViewHolder.setState(powerUnitF.getState());
        powerUnitFViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerUnitFViewHolder.setState(PowerUnitF.UPDATING);
                nooLiteF.switchF_TX(position, powerUnitF.getIndex(), powerUnitF.getId());
            }
        });
        powerUnitFViewHolder.setLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onUnitClickListener.openPowerUnitF(position, powerUnitF);
                return true;
            }
        });
        powerUnitFViewHolder.setRoom(powerUnitF.getRoom());
        powerUnitFViewHolder.setName(powerUnitF.getName());
        if (Settings.isDeveloperMode()) {
            powerUnitFViewHolder.setIndex(powerUnitF.getIndex());
        }
    }

    private void bindPowerSocketF(final RecyclerView.ViewHolder holder, final int position) {
        final PowerSocketFViewHolder powerSocketFViewHolder = (PowerSocketFViewHolder) holder;
        final PowerSocketF powerSocketF = (PowerSocketF) units.get(position);
        powerSocketF.setAdapterPosition(position);
        powerSocketFViewHolder.setState(powerSocketF.getState());
        powerSocketFViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerSocketFViewHolder.setState(PowerSocketF.UPDATING);
                nooLiteF.switchF_TX(position, powerSocketF.getIndex(), powerSocketF.getId());
            }
        });
        powerSocketFViewHolder.setLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onUnitClickListener.openPowerSocketF(position, powerSocketF);
                return true;
            }
        });
        powerSocketFViewHolder.setRoom(powerSocketF.getRoom());
        powerSocketFViewHolder.setName(powerSocketF.getName());
        if (Settings.isDeveloperMode()) {
            powerSocketFViewHolder.setIndex(powerSocketF.getIndex());
        }
    }

    private void bindThermostat(final RecyclerView.ViewHolder holder, final int position) {
        final ThermostatViewHolder thermostatViewHolder = (ThermostatViewHolder) holder;
        final Thermostat thermostat = (Thermostat) units.get(position);
        thermostat.setAdapterPosition(position);
        thermostatViewHolder.setState(thermostat.getState(), thermostat.getOutputState());
        thermostatViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnitClickListener.openThermostat(position, thermostat);
            }
        });
        thermostatViewHolder.setLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //onUnitClickListener.openThermostatSchedule(position, thermostat);
                return false;  //true
            }
        });
        if (-51 < thermostat.getCurrentTemperature() && thermostat.getCurrentTemperature() < 101) {
            thermostatViewHolder.setCurrentTemperature(String.valueOf(thermostat.getCurrentTemperature()).concat("°C"));
        } else {
            thermostatViewHolder.setCurrentTemperature("--°C");
        }
        if (4 < thermostat.getTargetTemperature() && thermostat.getTargetTemperature() < 51) {
            thermostatViewHolder.setTargetTemperature(String.valueOf(thermostat.getTargetTemperature()).concat("°C"));
        } else {
            thermostatViewHolder.setTargetTemperature("--°C");
        }
        thermostatViewHolder.setRoom(thermostat.getRoom());
        thermostatViewHolder.setName(thermostat.getName());
        if (Settings.isDeveloperMode()) {
            thermostatViewHolder.setIndex(thermostat.getIndex());
        }
    }

    private void bindRolletUnitF(final RecyclerView.ViewHolder holder, final int position) {
        final PowerUnitFViewHolder powerUnitFViewHolder = (PowerUnitFViewHolder) holder;
        final RolletUnitF rolletUnitF = (RolletUnitF) units.get(position);
        rolletUnitF.setAdapterPosition(position);
        powerUnitFViewHolder.setState(rolletUnitF.getState());
        if (!rolletUnitF.isCatFeeder()) {
            powerUnitFViewHolder.setClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onUnitClickListener.openPowerUnitF(position, rolletUnitF);
                }
            });
            powerUnitFViewHolder.setLongClick(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (units.get(position + 1) instanceof RolletUnitF && ((RolletUnitF) units.get(position + 1)).isCatFeeder()) {
                        units.remove(position + 1);
                        notifyItemRemoved(position + 1);
                        //notifyDataSetChanged();
                        return true;
                    } else {
                        if (!Settings.isDeveloperMode()) return false;
                        units.add(position + 1, new RolletUnitF(rolletUnitF.getId(), rolletUnitF.getIndex(), rolletUnitF.getState(), rolletUnitF.getRoomID(), rolletUnitF.getRoom(), rolletUnitF.getName()));
                        ((RolletUnitF) units.get(position + 1)).setCatFeeder(true);
                        //notifyItemInserted(position + 1);
                        notifyDataSetChanged();
                        return true;
                    }
                }
            });
            if (Settings.isNightMode()) {
                powerUnitFViewHolder.setIcon(R.drawable.ic_rollet_grey);
            } else {
                powerUnitFViewHolder.setIcon(R.drawable.ic_rollet);
            }
            powerUnitFViewHolder.setName(rolletUnitF.getName());
        } else {
            powerUnitFViewHolder.setClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    feedCat(rolletUnitF);
                }
            });
            powerUnitFViewHolder.setLongClick(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CatFeederDialog catFeederDialog = CatFeederDialog.newInstance(fragmentManager, rolletUnitF);
                    fragmentManager.beginTransaction().add(catFeederDialog, CatFeederDialog.class.getSimpleName()).show(catFeederDialog).commit();
                    return true;
                }
            });
            powerUnitFViewHolder.setIcon(R.mipmap.ic_cat_feeder);
            powerUnitFViewHolder.setName("Покормить кота");
        }
        powerUnitFViewHolder.setRoom(rolletUnitF.getRoom());
        if (Settings.isDeveloperMode()) {
            powerUnitFViewHolder.setIndex(rolletUnitF.getIndex());
        }
    }


    private void feedCat(final RolletUnitF rolletUnitF) {
        if (client.dispatcher().runningCallsCount() > 0) return;

        rolletUnitF.setState(RolletUnitF.OPEN);
        notifyItemChanged(rolletUnitF.getAdapterPosition());

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

                rolletUnitF.setState(RolletUnitF.NOT_CONNECTED);
                notifyItemChanged(rolletUnitF.getAdapterPosition());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        rolletUnitF.setState(RolletUnitF.NOT_CONNECTED);
                        notifyItemChanged(rolletUnitF.getAdapterPosition());
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
                        rolletUnitF.setState(RolletUnitF.NOT_CONNECTED);
                        notifyItemChanged(rolletUnitF.getAdapterPosition());
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
                        rolletUnitF.setState(RolletUnitF.NOT_CONNECTED);
                        notifyItemChanged(rolletUnitF.getAdapterPosition());
                    }
                    call.cancel();

                } catch (Exception e) {
                    call.cancel();

                    rolletUnitF.setState(RolletUnitF.NOT_CONNECTED);
                    notifyItemChanged(rolletUnitF.getAdapterPosition());
                } finally {
                    rolletUnitF.setState(RolletUnitF.CLOSE);
                    notifyItemChanged(rolletUnitF.getAdapterPosition());
                }
            }
        });
    }
}
