package com.noolitef.timers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.ftx.PowerSocketF;
import com.noolitef.settings.Settings;
import com.noolitef.tx.PowerUnit;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.R;
import com.noolitef.Room;

import java.util.ArrayList;

class TimerDevicesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView name;

        RoomViewHolder(View item) {
            super(item);
            name = (TextView) item.findViewById(R.id.card_view_room_name);
        }

        void setName(String name) {
            this.name.setText(name);
        }
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout card;
        private CheckBox selector;
        private TextView name;

        DeviceViewHolder(View item) {
            super(item);
            card = (LinearLayout) item.findViewById(R.id.card_view_timer_device);
            selector = (CheckBox) item.findViewById(R.id.card_view_timer_device_selector);
            name = (TextView) item.findViewById(R.id.card_view_timer_device_name);
        }

        boolean isCheck() {
            return selector.isChecked();
        }

        void setCheck(boolean checked) {
            selector.setChecked(checked);
        }

        void setClick(View.OnClickListener listener) {
            card.setOnClickListener(listener);
        }

        void setName(String name) {
            this.name.setText(name);
        }
    }

    static final int ROOM = 0;
    private static final int POWER_UNIT = 1;
    private static final int POWER_UNIT_F = 2;
    private static final int POWER_SOCKET_F = 3;

    private HomeActivity homeActivity;
    private Timer timer;
    private ArrayList<Object> units;
    private int selectedDevices;

    private Toast toast;

    TimerDevicesRecyclerAdapter(HomeActivity homeActivity, Timer timer, ArrayList<Object> units) {
        this.homeActivity = homeActivity;
        this.timer = timer;
        this.units = units;

        for (Object device : units) {
            if (device instanceof PowerUnit)
                if (((PowerUnit) device).isPreset()) selectedDevices++;
            if (device instanceof PowerUnitF)
                if (((PowerUnitF) device).isPreset()) selectedDevices++;
            if (device instanceof PowerSocketF)
                if (((PowerSocketF) device).isPreset()) selectedDevices++;
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
        if (units.get(position) instanceof PowerUnit) return POWER_UNIT;
        if (units.get(position) instanceof PowerUnitF) return POWER_UNIT_F;
        if (units.get(position) instanceof PowerSocketF) return POWER_SOCKET_F;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view;
        if (Settings.isNightMode()) {
            switch (type) {
                case ROOM:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_room, parent, false);
                    RoomViewHolder roomViewHolder = new RoomViewHolder(view);
                    return roomViewHolder;
                case POWER_UNIT:
                case POWER_UNIT_F:
                case POWER_SOCKET_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_timer_device_dark, parent, false);
                    DeviceViewHolder deviceViewHolder = new DeviceViewHolder(view);
                    return deviceViewHolder;
            }
        } else {
            switch (type) {
                case ROOM:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_room, parent, false);
                    RoomViewHolder roomViewHolder = new RoomViewHolder(view);
                    return roomViewHolder;
                case POWER_UNIT:
                case POWER_UNIT_F:
                case POWER_SOCKET_F:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_timer_device, parent, false);
                    DeviceViewHolder deviceViewHolder = new DeviceViewHolder(view);
                    return deviceViewHolder;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ROOM:
                bindRoom(holder, position);
                break;
            case POWER_UNIT:
                bindPowerUnit(holder, position);
                break;
            case POWER_UNIT_F:
                bindPowerUnitF(holder, position);
                break;
            case POWER_SOCKET_F:
                bindPowerSocketF(holder, position);
                break;
        }
    }

    private void bindRoom(RecyclerView.ViewHolder holder, int position) {
        RoomViewHolder roomViewHolder = (RoomViewHolder) holder;
        roomViewHolder.setName(String.valueOf(((Room) units.get(position)).getName()));
    }

    private void bindPowerUnit(final RecyclerView.ViewHolder holder, final int position) {
        final DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        final PowerUnit powerUnit = (PowerUnit) units.get(position);
        deviceViewHolder.setCheck(powerUnit.isPreset());
        deviceViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (powerUnit.isPreset()) {
                    powerUnit.setPreset(false);
                    deviceViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 8) {
                        powerUnit.setPreset(true);
                        deviceViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        showToast("Можно добавить только 8 устройств");
                    }
                }
            }
        });
        deviceViewHolder.setName(powerUnit.getName());
    }

    private void bindPowerUnitF(final RecyclerView.ViewHolder holder, final int position) {
        final DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        final PowerUnitF powerUnitF = (PowerUnitF) units.get(position);
        deviceViewHolder.setCheck(powerUnitF.isPreset());
        deviceViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (powerUnitF.isPreset()) {
                    powerUnitF.setPreset(false);
                    deviceViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 8) {
                        powerUnitF.setPreset(true);
                        deviceViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        showToast("Можно добавить только 8 устройств");
                    }
                }
            }
        });
        deviceViewHolder.setName(powerUnitF.getName());
    }

    private void bindPowerSocketF(final RecyclerView.ViewHolder holder, final int position) {
        final DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        final PowerSocketF powerSocketF = (PowerSocketF) units.get(position);
        deviceViewHolder.setCheck(powerSocketF.isPreset());
        deviceViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (powerSocketF.isPreset()) {
                    powerSocketF.setPreset(false);
                    deviceViewHolder.setCheck(false);
                    selectedDevices--;
                } else {
                    if (selectedDevices < 8) {
                        powerSocketF.setPreset(true);
                        deviceViewHolder.setCheck(true);
                        selectedDevices++;
                    } else {
                        showToast("Можно добавить только 8 устройств");
                    }
                }
            }
        });
        deviceViewHolder.setName(powerSocketF.getName());
    }

    private boolean isPreset(PowerUnit powerUnit) {
        for (int i = 0; i < 8; i++) {
            if (timer.getCommand(i)[0] == 0 && timer.getCommand(i)[3] == powerUnit.getChannel()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreset(PowerUnitF powerUnitF) {
        for (int i = 0; i < 8; i++) {
            if ((NooLiteF.getHexString(timer.getCommand(i)[10] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(i)[11] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(i)[12] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(i)[13] & 0xFF))))).equals(powerUnitF.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreset(PowerSocketF powerSocketF) {
        for (int i = 0; i < 8; i++) {
            if ((NooLiteF.getHexString(timer.getCommand(i)[10] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(i)[11] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(i)[12] & 0xFF).concat(NooLiteF.getHexString(timer.getCommand(i)[13] & 0xFF))))).equals(powerSocketF.getId())) {
                return true;
            }
        }
        return false;
    }

    private void showToast(final String message) {
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
