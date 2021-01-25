package com.noolitef;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.PowerUnitFA;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.presets.Preset;
import com.noolitef.rx.GraphLogFragment;
import com.noolitef.rx.HumidityTemperatureSensor;
import com.noolitef.rx.LeakDetector;
import com.noolitef.rx.LightSensor;
import com.noolitef.rx.ListLogDialog;
import com.noolitef.rx.MotionSensor;
import com.noolitef.rx.OpenCloseSensor;
import com.noolitef.rx.RemoteController;
import com.noolitef.rx.TemperatureSensor;
import com.noolitef.settings.Settings;
import com.noolitef.settings.SettingsRoomFragment;
import com.noolitef.settings.SettingsRoomFragmentListener;
import com.noolitef.tx.PowerUnit;
import com.noolitef.tx.RGBControllerFragment;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class RoomFragment extends Fragment {
    private FragmentManager fragmentManager;
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private APIFiles apiFiles;
    private ArrayList<PowerUnit> powerUnits;
    private ArrayList<TemperatureSensor> temperatureSensors;
    private ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors;
    private ArrayList<MotionSensor> motionSensors;
    private ArrayList<OpenCloseSensor> openCloseSensors;
    private ArrayList<LeakDetector> leakDetectors;
    private ArrayList<PowerUnitF> powerUnitsF;
    private ArrayList<PowerSocketF> powerSocketsF;
    private ArrayList<Thermostat> thermostats;
    private ArrayList<RolletUnitF> rolletUnitsF;
    private ArrayList<Object> units;
    private int roomIndex;
    private int roomID;
    private ArrayList<Room> rooms;
    private int horizontalPadding;

    private HomeActivity homeActivity;
    private View roomView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView unitsRecyclerView;
    private RecyclerView.LayoutManager unitsRecyclerLayoutManager;
    private UnitsRecyclerAdapter unitsRecyclerAdapter;

    public RoomFragment() {
    }

    void send(FragmentManager fragmentManager, OkHttpClient client, NooLiteF nooLiteF, APIFiles apiFiles, ArrayList<Room> rooms) {
        this.fragmentManager = fragmentManager;
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.rooms = rooms;
        this.apiFiles = apiFiles;
    }

    void setUnits(int roomIndex, int roomID, ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        this.roomIndex = roomIndex;
        this.roomID = roomID;
        this.powerUnits = powerUnits;
        this.temperatureSensors = temperatureSensors;
        this.humidityTemperatureSensors = humidityTemperatureSensors;
        this.motionSensors = motionSensors;
        this.openCloseSensors = openCloseSensors;
        this.leakDetectors = leakDetectors;
        this.powerUnitsF = powerUnitsF;
        this.powerSocketsF = powerSocketsF;
        this.thermostats = thermostats;
        this.rolletUnitsF = rolletUnitsF;

        units = sortUnits(powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int unitsRecyclerPadding = 2 * getResources().getDimensionPixelOffset(R.dimen.dp_16);
        int cardDimension = getResources().getDimensionPixelOffset(R.dimen.card_view_dimensions);
        int spanCount = (displayWidth - unitsRecyclerPadding) / cardDimension;
        int cardMargin = (displayWidth - unitsRecyclerPadding - (cardDimension * spanCount)) / (2 * spanCount);
        int lineMargin = 2 * cardMargin;

        // special for 480x800
        if (cardMargin < 4) {
            unitsRecyclerPadding = unitsRecyclerPadding / 2;
            cardMargin = (displayWidth - unitsRecyclerPadding - (cardDimension * spanCount)) / (2 * spanCount);
            lineMargin = 2 * cardMargin;
        }

        final int SPAN_COUNT = spanCount;
        final int LINE_MARGIN = lineMargin;

        horizontalPadding = cardMargin;

        homeActivity = (HomeActivity) getActivity();
        roomView = inflater.inflate(R.layout.fragment_room, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) roomView.findViewById(R.id.swipe_refresh_layout_room);
        unitsRecyclerView = (RecyclerView) roomView.findViewById(R.id.recycler_view_room_units);
        unitsRecyclerView.setPadding(unitsRecyclerPadding / 2, 0, unitsRecyclerPadding / 2, 0);
        unitsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        swipeRefreshLayout.setProgressViewEndTarget(true, getResources().getDimensionPixelOffset(R.dimen.swipe_progressbar_toolbar_offset) + getResources().getDimensionPixelOffset(R.dimen.dp_16));
        //swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.black_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                int d = 0;
//                for (Object device : units) {
//                    if (device instanceof PowerUnitF || device instanceof PowerSocketF || device instanceof Thermostat) {
//                        d++;
//                    }
//                }
//                boolean refresh = nooLiteF.getState(d);
//                if (!refresh) {
//                    swipeRefreshLayout.setRefreshing(false);
//                }

                if (!HomeActivity.isUpdating()) {
                    homeActivity.updatePRF64();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        unitsRecyclerLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        ((GridLayoutManager) unitsRecyclerLayoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (unitsRecyclerAdapter.getItemViewType(position)) {
                    case UnitsRecyclerAdapter.EMPTY_HEADER:
                    case UnitsRecyclerAdapter.HEADER:
                        return SPAN_COUNT;
                    default:
                        return 1;
                }
            }
        });
        unitsRecyclerView.setLayoutManager(unitsRecyclerLayoutManager);
        unitsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = LINE_MARGIN;
            }
        });
        unitsRecyclerView.setHasFixedSize(true);

        if (units.size() > 0) {
            updateRecyclerView();
        }

        return roomView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setStateListener();
    }

    private ArrayList<Object> sortUnits(ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        units = new ArrayList<>();
        units.add(new EmptyHeader());
        if ((powerUnits != null && powerUnits.size() > 0) || (powerUnitsF != null && powerUnitsF.size() > 0) || (powerSocketsF != null && powerSocketsF.size() > 0) || (thermostats != null && thermostats.size() > 0) || (rolletUnitsF != null && rolletUnitsF.size() > 0) || (remoteControllers != null && remoteControllers.size() > 0)) {
            units.add(new Header("Устройства"));
            for (int pu = 0; pu < powerUnits.size(); pu++) {
                if (powerUnits.get(pu).getRoomID() == roomID) units.add(powerUnits.get(pu));
            }
            for (int puf = 0; puf < powerUnitsF.size(); puf++) {
                if (powerUnitsF.get(puf).getRoomID() == roomID) units.add(powerUnitsF.get(puf));
            }
            for (int psf = 0; psf < powerSocketsF.size(); psf++) {
                if (powerSocketsF.get(psf).getRoomID() == roomID)
                    units.add(powerSocketsF.get(psf));
            }
            for (int ts = 0; ts < thermostats.size(); ts++) {
                if (thermostats.get(ts).getRoomID() == roomID) units.add(thermostats.get(ts));
            }
            for (int ruf = 0; ruf < rolletUnitsF.size(); ruf++) {
                if (rolletUnitsF.get(ruf).getRoomID() == roomID) units.add(rolletUnitsF.get(ruf));
            }
            for (int rc = 0; rc < remoteControllers.size(); rc++) {
                if (remoteControllers.get(rc).getRoomID() == roomID)
                    units.add(remoteControllers.get(rc));
            }
        }
        if ((temperatureSensors != null && temperatureSensors.size() > 0) || (humidityTemperatureSensors != null && humidityTemperatureSensors.size() > 0) || (motionSensors != null && motionSensors.size() > 0) || (openCloseSensors != null && openCloseSensors.size() > 0) || (leakDetectors != null && leakDetectors.size() > 0) || (lightSensors != null && lightSensors.size() > 0)) {
            units.add(new Header("Датчики"));
            for (int ts = 0; ts < temperatureSensors.size(); ts++) {
                if (temperatureSensors.get(ts).getRoomID() == roomID)
                    units.add(temperatureSensors.get(ts));
            }
            for (int hts = 0; hts < humidityTemperatureSensors.size(); hts++) {
                if (humidityTemperatureSensors.get(hts).getRoomID() == roomID)
                    units.add(humidityTemperatureSensors.get(hts));
            }
            for (int ms = 0; ms < motionSensors.size(); ms++) {
                if (motionSensors.get(ms).getRoomID() == roomID)
                    units.add(motionSensors.get(ms));
            }
            for (int ocs = 0; ocs < openCloseSensors.size(); ocs++) {
                if (openCloseSensors.get(ocs).getRoomID() == roomID)
                    units.add(openCloseSensors.get(ocs));
            }
            for (int ld = 0; ld < leakDetectors.size(); ld++) {
                if (leakDetectors.get(ld).getRoomID() == roomID)
                    units.add(leakDetectors.get(ld));
            }
            for (int ld = 0; ld < lightSensors.size(); ld++) {
                if (lightSensors.get(ld).getRoomID() == roomID)
                    units.add(lightSensors.get(ld));
            }
        }

        if (units.size() > 2 && units.get(1) instanceof Header && units.get(2) instanceof Header)
            units.remove(1);
        if (units.size() > 0 && units.get(units.size() - 1) instanceof Header)
            units.remove(units.size() - 1);

        units.add(new EmptyHeader());
        return units;
    }

    public void updateRecyclerView() {
        if (homeActivity == null) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                long time = System.currentTimeMillis();

                unitsRecyclerAdapter = new UnitsRecyclerAdapter(homeActivity, fragmentManager, getContext(), nooLiteF, client, horizontalPadding, units);
                unitsRecyclerAdapter.setOnUnitClickListener(new OnUnitClickListener() {
                    @Override
                    public void openPowerUnit(PowerUnit powerUnit) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.send(client, nooLiteF, apiFiles.getDevice(), apiFiles.getUser(), rooms, -1, powerUnit);
                        }
                        if (powerUnitDialog.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }

                    @Override
                    public void openRGBController(PowerUnit rgbController) {
                        RGBControllerFragment rgbControllerFragment = (RGBControllerFragment) getChildFragmentManager().findFragmentByTag("RGB_CONTROLLER_FRAGMENT");
                        if (rgbControllerFragment == null) {
                            rgbControllerFragment = RGBControllerFragment.newInstance(rgbController);
                        }
                        if (rgbControllerFragment.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(rgbControllerFragment, "RGB_CONTROLLER_FRAGMENT").show(rgbControllerFragment).commit();
                    }

                    @Override
                    public void openGraphLog(Object sensor) {
                        GraphLogFragment graphLogFragment = (GraphLogFragment) getChildFragmentManager().findFragmentByTag("GRAPH_LOG");
                        if (graphLogFragment == null) {
                            graphLogFragment = new GraphLogFragment();
                            graphLogFragment.send(client, nooLiteF, apiFiles.getDevice(), apiFiles.getUser(), rooms, sensor);
                        }
                        if (graphLogFragment.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(graphLogFragment, "GRAPH_LOG").show(graphLogFragment).commit();
                    }

                    @Override
                    public void openListLog(Object sensor) {
                        ListLogDialog listLogDialog = (ListLogDialog) getChildFragmentManager().findFragmentByTag("LIST_LOG");
                        if (listLogDialog == null) {
                            listLogDialog = new ListLogDialog();
                            listLogDialog.send(nooLiteF, client, apiFiles.getDevice(), apiFiles.getUser(), rooms, sensor);
                        }
                        if (listLogDialog.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(listLogDialog, "LIST_LOG").show(listLogDialog).commit();
                    }

                    @Override
                    public void openPowerUnitF(int position, Object unit) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.send(client, nooLiteF, apiFiles.getDevice(), apiFiles.getUser(), rooms, position, unit);
                        }
                        if (powerUnitDialog.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }

                    @Override
                    public void openPowerSocketF(int position, PowerSocketF powerSocketF) {
                        PowerUnitDialog powerUnitDialog = (PowerUnitDialog) getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                        if (powerUnitDialog == null) {
                            powerUnitDialog = new PowerUnitDialog();
                            powerUnitDialog.send(client, nooLiteF, apiFiles.getDevice(), apiFiles.getUser(), rooms, position, powerSocketF);
                        }
                        if (powerUnitDialog.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(powerUnitDialog, "POWER_UNIT_DIALOG").show(powerUnitDialog).commit();
                    }

                    @Override
                    public void openThermostat(int position, Thermostat thermostat) {
                        ThermostatDialog thermostatDialog = (ThermostatDialog) getChildFragmentManager().findFragmentByTag("THERMOSTAT_DIALOG");
                        if (thermostatDialog == null) {
                            thermostatDialog = new ThermostatDialog();
                            thermostatDialog.send(client, nooLiteF, apiFiles.getDevice(), apiFiles.getUser(), rooms, thermostat);
                        }
                        if (thermostatDialog.isAdded()) return;
                        getChildFragmentManager().beginTransaction().add(thermostatDialog, "THERMOSTAT_DIALOG").show(thermostatDialog).commit();
                    }

                    @Override
                    public void openThermostatSchedule(int position, Thermostat thermostat) {
                        ThermostatFragment thermostatFragment = (ThermostatFragment) fragmentManager.findFragmentByTag("THERMOSTAT_FRAGMENT");
                        if (thermostatFragment == null) {
                            thermostatFragment = new ThermostatFragment();
                            thermostatFragment.send(client, apiFiles.getDevice(), apiFiles.getUser(), rooms, thermostat);
                        }
                        if (thermostatFragment.isAdded()) return;
                        fragmentManager.beginTransaction().add(thermostatFragment, "THERMOSTAT_FRAGMENT").show(thermostatFragment).commit();
                    }
                });
                unitsRecyclerView.setAdapter(unitsRecyclerAdapter);
                swipeRefreshLayout.setRefreshing(false);

                Log.d("nooLiteF", "updateRoomRecyclerView() - " + (System.currentTimeMillis() - time) + " ms");
                homeActivity.writeAppLog("updateRoomRecyclerView() - " + (System.currentTimeMillis() - time) + " ms");

            }
        });
    }

    void showPopupMenu(View view) {
        PopupMenu popupMenu;
        if (Settings.isNightMode()) {
            Context context = new ContextThemeWrapper(getActivity(), R.style.PopupMenuDark);
            popupMenu = new PopupMenu(context, view);
        } else {
            popupMenu = new PopupMenu(getActivity(), view);
        }
        popupMenu.inflate(R.menu.room_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_refresh:
                        swipeRefreshLayout.setRefreshing(true);
                        homeActivity.hardUpdatePRF64();
                        return true;
                    case R.id.menu_item_bind:
                        if (apiFiles.getDevice() != null) {
                            if (rooms.size() != 0) {
                                BindFragment_temporary bindFragmentTemporary = (BindFragment_temporary) getChildFragmentManager().findFragmentByTag("BIND_DIALOG");
                                if (bindFragmentTemporary == null) {
                                    bindFragmentTemporary = new BindFragment_temporary();
                                    ArrayList<Object> units = new ArrayList<>();
                                    units.addAll(powerUnits);
                                    bindFragmentTemporary.send(client, nooLiteF, units, roomIndex, rooms);
                                    bindFragmentTemporary.setBindFragmentListener(new BindFragmentListener() {
                                        @Override
                                        public void onDismiss(boolean update) {
                                            if (update) {
                                                swipeRefreshLayout.setRefreshing(true);
                                                homeActivity.hardUpdatePRF64();
                                            }
                                        }
                                    });
                                }
                                if (bindFragmentTemporary.isAdded()) return false;
                                getChildFragmentManager().beginTransaction().add(bindFragmentTemporary, "BIND_DIALOG").show(bindFragmentTemporary).commit();
                            } else {
                                homeActivity.showSnackBar("Сначала создайте комнату...", 0, Snackbar.LENGTH_SHORT);
                            }
                        } else {
                            homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                        }
                        return true;
                    case R.id.menu_item_settings:
                        if (apiFiles == null) {
                            homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                            return false;
                        } else {
                            if (apiFiles.getUser() == null) {
                                homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                                return false;
                            } else {
                                if (rooms == null) {
                                    homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                                    return false;
                                }
                            }
                        }
                        if (rooms.size() != 0) {
                            SettingsRoomFragment settingsRoomFragment = (SettingsRoomFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_ROOM_FRAGMENT");
                            if (settingsRoomFragment == null) {
                                settingsRoomFragment = SettingsRoomFragment.newInstance(apiFiles.getUser(), roomID, rooms.get(roomIndex).getName());
                                settingsRoomFragment.setSettingsRoomFragmentListener(new SettingsRoomFragmentListener() {
                                    @Override
                                    public void onDismiss(boolean update) {
                                        if (update) {
                                            setRefreshing(true);
                                            homeActivity.hardUpdatePRF64();
                                        }
                                    }
                                });
                            }
                            if (settingsRoomFragment.isAdded()) return false;
                            getChildFragmentManager().beginTransaction().add(settingsRoomFragment, "SETTINGS_ROOM_FRAGMENT").show(settingsRoomFragment).commit();
                        } else {
                            homeActivity.showSnackBar("Сначала создайте комнату...", 0, Snackbar.LENGTH_SHORT);
                            return false;
                        }
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void setStateListener() {
        nooLiteF.setStateListener(new StateListener() {
            @Override
            public void onFailure(final String message) {
                homeActivity.writeAppLog(message);
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        homeActivity.showSnackBar("Ошибка соединения...", 0, Snackbar.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(APIFiles files, ArrayList<Room> rooms, ArrayList<Preset> presets, ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
                apiFiles = files;
                homeActivity.setRooms(rooms);
                homeActivity.setPresets(presets);
                homeActivity.setRoomUnits(rooms, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
                units = sortUnits(powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);

                updateRecyclerView();
            }

            @Override
            public void onSwitchingTX_Failure(String message) {
                homeActivity.writeAppLog(message);
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        homeActivity.showSnackBar("Ошибка соединения...", 0, Snackbar.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onSwitchingTX_Ok() {
            }

            @Override
            public void onSwitchingF_TX_Failure(final int position, String message) {
                homeActivity.writeAppLog(message);
                if (units.get(position) instanceof PowerUnitF) {
                    ((PowerUnitF) units.get(position)).setState(PowerUnitF.NOT_CONNECTED);
                }
                if (units.get(position) instanceof PowerSocketF) {
                    ((PowerSocketF) units.get(position)).setState(PowerSocketF.NOT_CONNECTED);
                }
                if (units.get(position) instanceof Thermostat) {
                    ((Thermostat) units.get(position)).setState(Thermostat.NOT_CONNECTED);
                }
                if (units.get(position) instanceof RolletUnitF) {
                    ((RolletUnitF) units.get(position)).setState(RolletUnitF.NOT_CONNECTED);
                }
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        unitsRecyclerAdapter.notifyItemChanged(position);
                        homeActivity.showSnackBar("Ошибка соединения...", 0, Snackbar.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onSwitchingF_TX_Ok(final int position, final int state, final int out, final int brightness, final int temperature) {
                if (units.get(position) instanceof PowerUnitF) {
                    ((PowerUnitF) units.get(position)).setState(state);
                    ((PowerUnitF) units.get(position)).setBrightness((int) (brightness * 100.0 / 255.0 + .5));
                }
                if (units.get(position) instanceof PowerUnitFA) {
                    ((PowerUnitF) units.get(position)).setState(state);
                    ((PowerUnitF) units.get(position)).setPresetBrightness(brightness);
                }
                if (units.get(position) instanceof PowerSocketF) {
                    ((PowerSocketF) units.get(position)).setState(state);
                }
                if (units.get(position) instanceof Thermostat) {
                    ((Thermostat) units.get(position)).setState(state);
                    ((Thermostat) units.get(position)).setOutputState(out);
                    ((Thermostat) units.get(position)).setCurrentTemperature(temperature);
                }
                if (units.get(position) instanceof RolletUnitF) {
                    ((RolletUnitF) units.get(position)).setState(state);
                }
                final PowerUnitDialog powerUnitDialog = (PowerUnitDialog) getChildFragmentManager().findFragmentByTag("POWER_UNIT_DIALOG");
                final ThermostatDialog thermostatDialog = (ThermostatDialog) getChildFragmentManager().findFragmentByTag("THERMOSTAT_DIALOG");
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        unitsRecyclerAdapter.notifyItemChanged(position);
                        if (powerUnitDialog != null && powerUnitDialog.isAdded()) {
                            powerUnitDialog.setBrightness(brightness);
                            powerUnitDialog.setRawData(temperature);
                        }
                        if (thermostatDialog != null && thermostatDialog.isAdded())
                            thermostatDialog.setCurrentTemperature(temperature);
                    }
                });
            }

            @Override
            public void onDebugging(final String message) {
                Toast.makeText(homeActivity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setRefreshing(final boolean refreshing) {
        if (homeActivity == null) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(refreshing);
                if (homeActivity != null) homeActivity.headerProgressBarSetVisible(refreshing);
            }
        });
    }

    public void updateUnitsAdapterItem(final int position) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unitsRecyclerAdapter.notifyItemChanged(position);
            }
        });
    }

    public void updateUnitsAdapter() {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unitsRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }
}
