package com.noolitef;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.presets.OnPresetLongClickListener;
import com.noolitef.presets.Preset;
import com.noolitef.presets.PresetFragment;
import com.noolitef.presets.PresetFragmentListener;
import com.noolitef.presets.PresetsBlock;
import com.noolitef.presets.PresetsRecyclerAdapter;
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
import com.noolitef.settings.SettingsControllerFragment;
import com.noolitef.tx.PowerUnit;
import com.noolitef.tx.RGBControllerFragment;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class HomeFragment extends Fragment {
    private FragmentManager fragmentManager;
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private APIFiles apiFiles;
    private ArrayList<Object> devices;
    private ArrayList<PresetsBlock> presetsBlocks;
    private ArrayList<Object> units;
    private ArrayList<Room> rooms;
    private Object sensor;

    private int presetsRecyclerPadding;
    private int horizontalPadding;
    private int verticalPadding;

    private HomeActivity homeActivity;
    private View homeView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout presetsLayout;
    private TextView presetsTitle;
    private HorizontalScrollView presetsScrollView;
    private RecyclerView presetsRecyclerView;
    private RecyclerView unitsRecyclerView;
    private RecyclerView.LayoutManager presetsRecyclerLayoutManager;
    private RecyclerView.LayoutManager unitsRecyclerLayoutManager;
    private PresetsRecyclerAdapter presetsRecyclerAdapter;
    private UnitsRecyclerAdapter unitsRecyclerAdapter;

    public HomeFragment() {
    }

    void send(FragmentManager fragmentManager, OkHttpClient client, NooLiteF nooLiteF, APIFiles apiFiles) {
        this.fragmentManager = fragmentManager;
        this.client = client;
        this.nooLiteF = nooLiteF;
        this.apiFiles = apiFiles;
    }

    void setUnits(ArrayList<Room> rooms, ArrayList<Preset> presets, ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        this.rooms = rooms;
        presetsBlocks = combinePresets(presets);
        devices = sortDevicesByRooms(this.rooms, combineDevices(powerUnits, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF));
        homeActivity.setDevices(devices);
        units = combineUnits(powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);

        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecyclerView();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (apiFiles == null) apiFiles = new APIFiles();
        if (presetsBlocks == null) presetsBlocks = new ArrayList<>();
        if (devices == null) devices = new ArrayList<>();
        if (units == null) units = new ArrayList<>();
        if (rooms == null) rooms = new ArrayList<>();

        setRetainInstance(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int unitsRecyclerPadding = 2 * getResources().getDimensionPixelOffset(R.dimen.dp_16);
        int cardDimension = getResources().getDimensionPixelOffset(R.dimen.card_view_dimensions);
        int spanCount = (displayWidth - unitsRecyclerPadding) / cardDimension;
        int cardMargin = (displayWidth - unitsRecyclerPadding - (cardDimension * spanCount)) / (2 * spanCount);
        int lineMargin = 2 * cardMargin;
        presetsRecyclerPadding = unitsRecyclerPadding / 2;

        // special for 480x800
        if (cardMargin < 4) {
            unitsRecyclerPadding = unitsRecyclerPadding / 2;
            cardMargin = (displayWidth - unitsRecyclerPadding - (cardDimension * spanCount)) / (2 * spanCount);
            lineMargin = 2 * cardMargin;
            presetsRecyclerPadding = unitsRecyclerPadding / 2;
        }

        final int SPAN_COUNT = spanCount;
        final int LINE_MARGIN = lineMargin;

        horizontalPadding = cardMargin;
        verticalPadding = lineMargin;

        homeActivity = (HomeActivity) getActivity();
        if (Settings.isNightMode()) {
            homeView = inflater.inflate(R.layout.fragment_home_dark, container, false);
        } else {
            homeView = inflater.inflate(R.layout.fragment_home, container, false);
        }
        swipeRefreshLayout = homeView.findViewById(R.id.swipe_refresh_layout);
        presetsLayout = homeView.findViewById(R.id.recycler_view_home_presets_layout);
        presetsTitle = homeView.findViewById(R.id.recycler_view_home_presets_title);
        presetsTitle.setPadding(presetsRecyclerPadding + horizontalPadding, getResources().getDimensionPixelOffset(R.dimen.dp_56) + verticalPadding, 0, 0);
        presetsScrollView = homeView.findViewById(R.id.horizontal_scroll_view_home_presets);
        presetsRecyclerView = homeView.findViewById(R.id.recycler_view_home_presets);
        presetsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        unitsRecyclerView = homeView.findViewById(R.id.recycler_view_home_units);
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
        presetsScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        swipeRefreshLayout.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        swipeRefreshLayout.setEnabled(true);
                        // @SuppressLint("ClickableViewAccessibility")
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            view.performClick();
                        }
                        break;
                }

                return false;
            }
        });

        if (units.size() == 0) {
            swipeRefreshLayout.setRefreshing(true);
        }

        presetsRecyclerLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        presetsRecyclerView.setLayoutManager(presetsRecyclerLayoutManager);
        presetsRecyclerView.setHasFixedSize(true);
        presetsRecyclerView.setNestedScrollingEnabled(false);

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

        //Log.d("nooLiteF", "onCreateView");
        updateRecyclerView();

        return homeView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setStateListener();
    }

    private ArrayList<Object> combineDevices(ArrayList<PowerUnit> powerUnits, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        int d = 0;
        final ArrayList<Object> devices = new ArrayList<>();

        if (powerUnits != null && powerUnits.size() > 0) {
            for (int pu = 0; pu < powerUnits.size(); d++, pu++) {
                devices.add(d, powerUnits.get(pu));
            }
        }
        if (powerUnitsF != null && powerUnitsF.size() > 0) {
            for (int puf = 0; puf < powerUnitsF.size(); d++, puf++) {
                devices.add(d, powerUnitsF.get(puf));
            }
        }
        if (powerSocketsF != null && powerSocketsF.size() > 0) {
            for (int psf = 0; psf < powerSocketsF.size(); d++, psf++) {
                devices.add(d, powerSocketsF.get(psf));
            }
        }
        if (thermostats != null && thermostats.size() > 0) {
            for (int t = 0; t < thermostats.size(); d++, t++) {
                devices.add(d, thermostats.get(t));
            }
        }
        if (rolletUnitsF != null && rolletUnitsF.size() > 0) {
            for (int ruf = 0; ruf < rolletUnitsF.size(); d++, ruf++) {
                devices.add(d, rolletUnitsF.get(ruf));
            }
        }

        return devices;
    }

    private ArrayList<Object> sortDevicesByRooms(ArrayList<Room> rooms, ArrayList<Object> devices) {
        ArrayList<Object> mixedDevices = new ArrayList<>(devices);
        ArrayList<Object> sortedDevices = new ArrayList<>();
        int r;
        for (r = 0; r < rooms.size(); r++) {
            sortedDevices.add(new Room(r, rooms.get(r).getName()));
            for (int d = 0; d < mixedDevices.size(); d++) {
                if (mixedDevices.get(d) instanceof PowerUnit) {
                    if (((PowerUnit) mixedDevices.get(d)).getRoomID() == r) {
                        sortedDevices.add(mixedDevices.get(d));
                        mixedDevices.remove(d);
                        d--;
                        continue;
                    }
                }
                if (mixedDevices.get(d) instanceof PowerUnitF) {
                    if (((PowerUnitF) mixedDevices.get(d)).getRoomID() == r) {
                        sortedDevices.add(mixedDevices.get(d));
                        mixedDevices.remove(d);
                        d--;
                        continue;
                    }
                }
                if (mixedDevices.get(d) instanceof PowerSocketF) {
                    if (((PowerSocketF) mixedDevices.get(d)).getRoomID() == r) {
                        sortedDevices.add(mixedDevices.get(d));
                        mixedDevices.remove(d);
                        d--;
                        continue;
                    }
                }
                if (mixedDevices.get(d) instanceof Thermostat) {
                    if (((Thermostat) mixedDevices.get(d)).getRoomID() == r) {
                        sortedDevices.add(mixedDevices.get(d));
                        mixedDevices.remove(d);
                        d--;
                    }
                }
                if (mixedDevices.get(d) instanceof RolletUnitF) {
                    if (((RolletUnitF) mixedDevices.get(d)).getRoomID() == r) {
                        sortedDevices.add(mixedDevices.get(d));
                        mixedDevices.remove(d);
                        d--;
                    }
                }
            }
            if (sortedDevices.get(sortedDevices.size() - 1) instanceof Room) {
                sortedDevices.remove(sortedDevices.size() - 1);
            }
        }
        if (mixedDevices.size() > 0) {
            sortedDevices.add(new Room(r, "Нераспределенные"));
            for (int d = 0; d < mixedDevices.size(); d++) {
                sortedDevices.add(mixedDevices.get(d));
                mixedDevices.remove(d);
                d--;
            }
        }
        return sortedDevices;
    }

    private ArrayList<PresetsBlock> combinePresets(ArrayList<Preset> presets) {
        ArrayList<PresetsBlock> presetsBlocks = new ArrayList<>();
        for (int p = 0, pb = 0; p < presets.size(); p++) {
            if (p % 2 == 0) {
                presetsBlocks.add(new PresetsBlock(presets.get(p), null));
            } else {
                presetsBlocks.get(pb++).setBottomPreset(presets.get(p));
            }
        }
        return presetsBlocks;
    }

    private ArrayList<Object> combineUnits(ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        int d = 0;
        final ArrayList<Object> units = new ArrayList<>();

        if ((powerUnits != null && powerUnits.size() > 0) || (powerUnitsF != null && powerUnitsF.size() > 0) || (powerSocketsF != null && powerSocketsF.size() > 0) || (thermostats != null && thermostats.size() > 0) || (rolletUnitsF != null && rolletUnitsF.size() > 0) || (remoteControllers != null && remoteControllers.size() > 0)) {
            units.add(d++, new Header("Устройства"));
            for (int pu = 0; pu < powerUnits.size(); d++, pu++) {
                units.add(d, powerUnits.get(pu));
            }
            for (int puf = 0; puf < powerUnitsF.size(); d++, puf++) {
                units.add(d, powerUnitsF.get(puf));
            }
            for (int psf = 0; psf < powerSocketsF.size(); d++, psf++) {
                units.add(d, powerSocketsF.get(psf));
            }
            for (int ts = 0; ts < thermostats.size(); d++, ts++) {
                units.add(d, thermostats.get(ts));
            }
            for (int ruf = 0; ruf < rolletUnitsF.size(); d++, ruf++) {
                units.add(d, rolletUnitsF.get(ruf));
            }
            for (int rc = 0; rc < remoteControllers.size(); d++, rc++) {
                units.add(d, remoteControllers.get(rc));
            }
        }
        if ((temperatureSensors != null && temperatureSensors.size() > 0) || (humidityTemperatureSensors != null && humidityTemperatureSensors.size() > 0) || (motionSensors != null && motionSensors.size() > 0) || (openCloseSensors != null && openCloseSensors.size() > 0) || (leakDetectors != null && leakDetectors.size() > 0) || (lightSensors != null && lightSensors.size() > 0)) {
            units.add(d++, new Header("Датчики"));
            for (int ts = 0; ts < temperatureSensors.size(); d++, ts++) {
                units.add(d, temperatureSensors.get(ts));
            }
            for (int hts = 0; hts < humidityTemperatureSensors.size(); d++, hts++) {
                units.add(d, humidityTemperatureSensors.get(hts));
            }
            for (int ms = 0; ms < motionSensors.size(); d++, ms++) {
                units.add(d, motionSensors.get(ms));
            }
            for (int ocs = 0; ocs < openCloseSensors.size(); d++, ocs++) {
                units.add(d, openCloseSensors.get(ocs));
            }
            for (int ld = 0; ld < leakDetectors.size(); d++, ld++) {
                units.add(d, leakDetectors.get(ld));
            }
            for (int ld = 0; ld < lightSensors.size(); d++, ld++) {
                units.add(d, lightSensors.get(ld));
            }
        }
        units.add(d++, new EmptyHeader());
        return units;
    }

    private void updateRecyclerView() {
        long time = System.currentTimeMillis();

        presetsRecyclerAdapter = new PresetsRecyclerAdapter(client, (HomeActivity) getActivity(), presetsRecyclerPadding, horizontalPadding, verticalPadding, presetsBlocks);
        presetsRecyclerAdapter.setOnPresetLongClickListener(new OnPresetLongClickListener() {
            @Override
            public void openPresetFragment(Preset preset) {
                if (apiFiles.getDevice() != null) {
                    PresetFragment presetFragment = (PresetFragment) getChildFragmentManager().findFragmentByTag("PRESET_FRAGMENT");
                    if (presetFragment == null) {
                        presetFragment = new PresetFragment();
                        presetFragment.setPresetFragmentListener(new PresetFragmentListener() {
                            @Override
                            public void onDismiss(boolean update) {
                                if (update) {
                                    swipeRefreshLayout.setRefreshing(true);
                                    homeActivity.hardUpdatePRF64();
                                }
                            }
                        });
                    }
                    if (presetFragment.isAdded()) return;
                    presetFragment.send(client, nooLiteF, apiFiles, preset, devices);
                    getChildFragmentManager().beginTransaction().add(presetFragment, "PRESET_FRAGMENT").show(presetFragment).commit();
                } else {
                    homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                }
            }
        });
        presetsRecyclerView.setAdapter(presetsRecyclerAdapter);

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
                ThermostatDialog thermostatDialog = (ThermostatDialog) fragmentManager.findFragmentByTag("THERMOSTAT_DIALOG");
                if (thermostatDialog == null) {
                    thermostatDialog = new ThermostatDialog();
                    thermostatDialog.send(client, nooLiteF, apiFiles.getDevice(), apiFiles.getUser(), rooms, thermostat);
                }
                if (thermostatDialog.isAdded()) return;
                fragmentManager.beginTransaction().add(thermostatDialog, "THERMOSTAT_DIALOG").show(thermostatDialog).commit();
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

        if (units.size() > 0) {
            homeActivity.enableUI();
            presetsLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            if (!nooLiteF.isUpdating() && !HomeActivity.isUpdating()) {
                homeActivity.enableUI();
                presetsLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        }

        Log.d("nooLiteF", "updateHomeRecyclerView() - " + (System.currentTimeMillis() - time) + " ms");
        homeActivity.writeAppLog("updateHomeRecyclerView() - " + (System.currentTimeMillis() - time) + " ms");
    }

    void showPopupMenu(View view) {
        PopupMenu popupMenu;
        if (Settings.isNightMode()) {
            Context context = new ContextThemeWrapper(getActivity(), R.style.PopupMenuDark);
            popupMenu = new PopupMenu(context, view);
        } else {
            popupMenu = new PopupMenu(getActivity(), view);
        }
        popupMenu.inflate(R.menu.home_popup_menu);
        popupMenu.getMenu().findItem(R.id.menu_item_night_mode).setChecked(Settings.isNightMode());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_refresh:
                        swipeRefreshLayout.setRefreshing(true);
                        homeActivity.hardUpdatePRF64();
                        return true;
                    case R.id.menu_item_preset:
                        if (apiFiles.getDevice() != null) {
                            PresetFragment presetFragment = (PresetFragment) getChildFragmentManager().findFragmentByTag("PRESET_FRAGMENT");
                            if (presetFragment == null) {
                                presetFragment = new PresetFragment();
                                presetFragment.setPresetFragmentListener(new PresetFragmentListener() {
                                    @Override
                                    public void onDismiss(boolean update) {
                                        if (update) {
                                            swipeRefreshLayout.setRefreshing(true);
                                            homeActivity.hardUpdatePRF64();
                                        }
                                    }
                                });
                            }
                            if (presetFragment.isAdded()) return false;
                            presetFragment.send(client, nooLiteF, apiFiles, null, devices);
                            getChildFragmentManager().beginTransaction().add(presetFragment, "PRESET_FRAGMENT").show(presetFragment).commit();
                        } else {
                            homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                        }
                        return true;
                    case R.id.menu_item_bind:
                        if (apiFiles.getDevice() != null) {
                            BindFragment_temporary bindFragmentTemporary = (BindFragment_temporary) getChildFragmentManager().findFragmentByTag("BIND_DIALOG");
                            if (bindFragmentTemporary == null) {
                                bindFragmentTemporary = new BindFragment_temporary();
                                bindFragmentTemporary.send(client, nooLiteF, units, -1, rooms);
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
                            homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                        }
                        return true;
                    case R.id.menu_item_settings:
                        SettingsControllerFragment settingsControllerFragment = (SettingsControllerFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_FRAGMENT");
                        if (settingsControllerFragment == null) {
                            settingsControllerFragment = new SettingsControllerFragment();
                            settingsControllerFragment.send(client);
                        }
                        if (settingsControllerFragment.isAdded()) return false;
                        getChildFragmentManager().beginTransaction().add(settingsControllerFragment, "SETTINGS_CONTROLLER_FRAGMENT").show(settingsControllerFragment).commit();
                        return true;
                    case R.id.menu_item_night_mode:
                        menuItem.setChecked(!menuItem.isChecked());
                        Settings.setNightMode(menuItem.isChecked());

                        SharedPreferences.Editor sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE).edit();
                        sharedPreferences.putBoolean("NightMode", Settings.isNightMode());
                        sharedPreferences.apply();

                        homeActivity.recreate();
                        return true;
//                    case R.id.menu_item_connect:
//                        homeActivity.connectToHub();
//                        return true;
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
                        String str = message;
                        homeActivity.enableUI();
                        presetsLayout.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        homeActivity.showSnackBar("Ошибка соединения...", 0, Snackbar.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(APIFiles files, ArrayList<Room> chambers, ArrayList<Preset> presets, ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
                rooms = chambers;
                homeActivity.setApiFiles(apiFiles);
                homeActivity.setRooms(rooms);
                homeActivity.setPresets(presets);
                homeActivity.setUnits(powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);

                apiFiles = files;
                devices = sortDevicesByRooms(rooms, combineDevices(powerUnits, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF));
                presetsBlocks = combinePresets(presets);
                units = combineUnits(powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);

                homeActivity.setDevices(devices);
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("nooLiteF", "onResponse");
                        updateRecyclerView();
                    }
                });
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
                    ((PowerUnitF) units.get(position)).setBrightness(brightness);
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
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(homeActivity, message, Toast.LENGTH_SHORT).show();
                    }
                });
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
