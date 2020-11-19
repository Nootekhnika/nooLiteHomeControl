package com.noolitef;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.noolitef.automatics.AutomaticsFragment;
import com.noolitef.ftx.PowerSocketF;
import com.noolitef.ftx.PowerUnitF;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.presets.Preset;
import com.noolitef.rx.HumidityTemperatureSensor;
import com.noolitef.rx.LeakDetector;
import com.noolitef.rx.LightSensor;
import com.noolitef.rx.MotionSensor;
import com.noolitef.rx.OpenCloseSensor;
import com.noolitef.rx.RemoteController;
import com.noolitef.rx.TemperatureSensor;
import com.noolitef.settings.Settings;
import com.noolitef.settings.SettingsControllerAuthorizationFragment;
import com.noolitef.settings.SettingsControllerAuthorizationFragmentListener;
import com.noolitef.settings.SettingsControllerBackupFragment;
import com.noolitef.settings.SettingsControllerIPFragment;
import com.noolitef.settings.SettingsControllerIPFragmentListener;
import com.noolitef.settings.SettingsHomeFragment;
import com.noolitef.settings.SettingsHomeFragmentListener;
import com.noolitef.settings.SettingsRoomFragment;
import com.noolitef.settings.SettingsRoomFragmentListener;
import com.noolitef.timers.TimersFragment;
import com.noolitef.tx.PowerUnit;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static boolean updating;
    private PRF64 nooLitePRF64;
    private Call call;
    private int pollStateTimeout;
    private long pollStateTime;
    //private HubConnection hubConnection;

    private SharedPreferences sharedPreferences;
    private CopyOnWriteArrayList<String> errorLog;
    private Calendar calendar;

    private static long backPressed;
    private static String home;
    private static int imageID;

    private final String ROOM = "ROOM";
    private final String TIMERS = "TIMERS";

    private Dispatcher clientDispatcher;
    private OkHttpClient client;
    private NooLiteF nooLiteF;
    private APIFiles apiFiles;
    private ArrayList<PowerUnit> powerUnits;
    private ArrayList<RemoteController> remoteControllers;
    private ArrayList<TemperatureSensor> temperatureSensors;
    private ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors;
    private ArrayList<MotionSensor> motionSensors;
    private ArrayList<OpenCloseSensor> openCloseSensors;
    private ArrayList<LeakDetector> leakDetectors;
    private ArrayList<LightSensor> lightSensors;
    private ArrayList<PowerUnitF> powerUnitsF;
    private ArrayList<PowerSocketF> powerSocketsF;
    private ArrayList<Thermostat> thermostats;
    private ArrayList<RolletUnitF> rolletUnitsF;
    private int roomIndex;
    private int roomID;
    private ArrayList<Room> rooms;
    private ArrayList<Preset> presets;
    private ArrayList<Object> devices;

    private ImageView background;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView homeSettings;
    private TextView homeName;
    private TextView homeIP;
    private Button buttonAddRoom;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private ImageButton burgerButton;
    private TextView toolbarTitle;
    private ProgressBar headerProgressBar;
    private ImageButton menuButton;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private SettingsControllerIPFragment settingsControllerIPFragment;
    private SettingsHomeFragment settingsHomeFragment;
    private HomeFragment homeFragment;
    private RoomFragment roomFragment;
    private TimersFragment timersFragment;
    private AutomaticsFragment automaticsFragment;
    private BottomNavigationView bottomNavigationView;
    private Toast toast;

    public HomeActivity() {
    }

    public void restart() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("nooLite", Context.MODE_PRIVATE);
        Settings.setNightMode(sharedPreferences.getBoolean("NightMode", Settings.isNightMode()));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black_light));
        }
        if (Settings.isNightMode()) {
            setContentView(R.layout.navigation_drawer_dark);
        } else {
            setContentView(R.layout.navigation_drawer);
        }

        background = findViewById(R.id.background);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        homeSettings = navigationView.getHeaderView(0).findViewById(R.id.home_settings);
        homeSettings.setOnClickListener(this);
        homeName = navigationView.getHeaderView(0).findViewById(R.id.home_name);
        homeIP = navigationView.getHeaderView(0).findViewById(R.id.home_ip);
        buttonAddRoom = drawerLayout.findViewById(R.id.add_room_button);
        toolbar = findViewById(R.id.appToolbar);
        headerProgressBar = navigationView.getHeaderView(0).findViewById(R.id.header_progress_bar);
        burgerButton = findViewById(R.id.burger_button);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        menuButton = findViewById(R.id.menu_button);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (sharedPreferences.getBoolean("Authorization", false)) {
            Settings.setLogin(sharedPreferences.getString("Login", Settings.login()));
            Settings.setPassword(sharedPreferences.getString("Password", Settings.password()));
        }

        Settings.setDeveloperMode(sharedPreferences.getBoolean("DeveloperMode", false));

        errorLog = new CopyOnWriteArrayList<>();

        fragmentManager = getSupportFragmentManager();

        clientDispatcher = new Dispatcher();
        clientDispatcher.setMaxRequests(1);

        client = new OkHttpClient().newBuilder()
                .dispatcher(clientDispatcher)
                //.connectTimeout(Settings.connectTimeout(), TimeUnit.MILLISECONDS)
                //.writeTimeout(Settings.connectTimeout(), TimeUnit.MILLISECONDS)
                //.readTimeout(Settings.connectTimeout(), TimeUnit.MILLISECONDS)
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) {
                        if (response.request().header("Authorization") != null) {
                            SettingsControllerAuthorizationFragment settingsControllerAuthorizationFragment = (SettingsControllerAuthorizationFragment) fragmentManager.findFragmentByTag("SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT");
                            if (settingsControllerAuthorizationFragment == null) {
                                settingsControllerAuthorizationFragment = new SettingsControllerAuthorizationFragment();
                                settingsControllerAuthorizationFragment.send(client);
                                settingsControllerAuthorizationFragment.setDismissListener(new SettingsControllerAuthorizationFragmentListener() {
                                    @Override
                                    public void onDismiss() {
                                        showProgressBar();
                                        hardUpdatePRF64();
                                    }
                                });
                            }
                            if (settingsControllerAuthorizationFragment.isAdded()) return null;
                            fragmentManager.beginTransaction().add(settingsControllerAuthorizationFragment, "SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT").show(settingsControllerAuthorizationFragment).commitAllowingStateLoss();
                            showToast("Необходима авторизация");
                            return null;
                        }
                        String credential = Credentials.basic(Settings.login(), Settings.password());
                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }
                })
                .build();

        nooLitePRF64 = new PRF64(this);
        apiFiles = new APIFiles();

        if (savedInstanceState == null) {
            roomID = 0;
            //setBackground(true);
        } else {
            //setBackground(false);
            toolbarTitle.setText(home);
        }
        nooLiteF = new NooLiteF(HomeActivity.this, nooLitePRF64, client, apiFiles);

        homeFragment = (HomeFragment) fragmentManager.findFragmentByTag("HOME");
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
            //homeFragment.send(client, nooLiteF);
        }
        homeFragment.send(fragmentManager, client, nooLiteF, apiFiles);

        roomFragment = (RoomFragment) fragmentManager.findFragmentByTag("ROOM");
        if (roomFragment == null) {
            roomFragment = new RoomFragment();
            //roomFragment.send(client, nooLiteF, apiFiles, rooms);
            //roomFragment.setUnits(roomIndex, roomID, powerUnits, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, powerUnitsF, powerSocketsF, thermostats);
        }
        roomFragment.send(fragmentManager, client, nooLiteF, apiFiles, rooms);
        roomFragment.setUnits(roomIndex, roomID, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);

        timersFragment = (TimersFragment) fragmentManager.findFragmentByTag("TIMERS");
        if (timersFragment == null) {
            timersFragment = new TimersFragment();
            //timersFragment.send(client, rooms, devices, presets);
        }
        timersFragment.send(client, nooLitePRF64, rooms, devices, presets);

        automaticsFragment = AutomaticsFragment.getInstance(fragmentManager, nooLitePRF64, client);

        addStartFragment();

        setupDrawer();
        setupToolbar();
        setupBottomNavigationView();

        nooLiteF.setHomeListener(new HomeListener() {
            @Override
            public void onResponse(String home, ArrayList<Room> rooms) {
                setRooms(rooms);
                setHome(home);
            }

            @Override
            public void onFailure(final String message) {
                String msg = message;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enableUI();
                        homeFragment.setRefreshing(false);
                        roomFragment.setRefreshing(false);
                        showSnackBar("Ошибка соединения...", 0, Snackbar.LENGTH_SHORT);
                    }
                });
            }
        });

        toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);

        if (!sharedPreferences.getBoolean("FirstLaunch", true)) {
            if (sharedPreferences.getBoolean("useDNS", false)) {
                Settings.setAddress(sharedPreferences.getString("DNS", "noolite.nootech.dns.by:80"));
            } else {
                Settings.setAddress(sharedPreferences.getString("URL", "192.168.0.170"));
            }
            //nooLiteF.getState(64);
            updatePRF64();
        } else {
            settingsControllerIPFragment = (SettingsControllerIPFragment) fragmentManager.findFragmentByTag("SETTINGS_CONTROLLER_IP_FRAGMENT");
            if (settingsControllerIPFragment == null) {
                settingsControllerIPFragment = new SettingsControllerIPFragment();
                settingsControllerIPFragment.send(client);
                settingsControllerIPFragment.setSettingsControllerIPFragmentListener(new SettingsControllerIPFragmentListener() {
                    @Override
                    public void onDismiss(boolean update) {
                        settingsHomeFragment = (SettingsHomeFragment) fragmentManager.findFragmentByTag("SETTINGS_HOME_FRAGMENT");
                        if (settingsHomeFragment == null) {
                            settingsHomeFragment = new SettingsHomeFragment();
                            settingsHomeFragment.send(client, apiFiles);
                            settingsHomeFragment.setSettingsHomeFragmentListener(new SettingsHomeFragmentListener() {
                                @Override
                                public void onDismiss(String home) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("FirstLaunch", false);
                                    editor.apply();
                                    if (sharedPreferences.getBoolean("useDNS", false)) {
                                        Settings.setAddress(sharedPreferences.getString("DNS", "noolite.nootech.dns.by:80"));
                                    } else {
                                        Settings.setAddress(sharedPreferences.getString("URL", "192.168.0.170"));
                                    }
                                    showProgressBar();
                                    //nooLiteF.getState(64);
                                    updatePRF64();
                                    ConfirmDialog confirmDialog = (ConfirmDialog) fragmentManager.findFragmentByTag("CONFIRM_DIALOG");
                                    if (confirmDialog == null) {
                                        confirmDialog = new ConfirmDialog();
                                        confirmDialog.setTitle("На всякий случай...");
                                        confirmDialog.setMessage("Мы всегда стараемся создавать качественное программное обеспечение, однако не всегда все идет по намеченному плану. Чтобы быть готовым к непредвиденным ситуациям и избежать потери данных мы рекомендуем вам периодически делать резервную копию настроек контроллера.\nЭто можно сделать в разделе: вкладка ''Дом'' - меню (три точки) - ''Настройки контроллера...'' - ''Резервирование и восстановление''.\nПерейти сейчас?");
                                        confirmDialog.setConfirmDialogListener(new ConfirmDialogListener() {
                                            @Override
                                            public void onAccept() {
                                                SettingsControllerBackupFragment settingsControllerBackupFragment = (SettingsControllerBackupFragment) fragmentManager.findFragmentByTag("BACKUP_FRAGMENT");
                                                if (settingsControllerBackupFragment == null) {
                                                    settingsControllerBackupFragment = new SettingsControllerBackupFragment();
                                                    settingsControllerBackupFragment.send(client);
                                                }
                                                if (settingsControllerBackupFragment.isAdded())
                                                    return;
                                                fragmentManager.beginTransaction().add(settingsControllerBackupFragment, "BACKUP_FRAGMENT").show(settingsControllerBackupFragment).commit();
                                            }

                                            @Override
                                            public void onDecline() {
                                            }
                                        });
                                    }
                                    fragmentManager.beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit();
                                }
                            });
                        }
                        fragmentManager.beginTransaction().add(settingsHomeFragment, "SETTINGS_HOME_FRAGMENT").show(settingsHomeFragment).commit();
                    }
                });
            }
            fragmentManager.beginTransaction().add(settingsControllerIPFragment, "SETTINGS_CONTROLLER_IP_FRAGMENT").show(settingsControllerIPFragment).commit();
        }

//        ManualFragment manualFragment = (ManualFragment) fragmentManager.findFragmentByTag("MANUAL_FRAGMENT");
//        if (manualFragment == null) {
//            manualFragment = new ManualFragment();
//        }
//        fragmentManager.beginTransaction().add(manualFragment, "MANUAL_FRAGMENT").show(manualFragment).commit();
    }

    public void writeAppLog(String message) {
        calendar = Calendar.getInstance();
        int date = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);
        errorLog.add(String.format(Locale.ROOT, "%02d/%02d/%04d %02d:%02d.%02d.%03d\n%s", date, month + 1, year, hour, minute, second, millisecond, message));
    }

    public CopyOnWriteArrayList<String> getAppLog() {
        return errorLog;
    }

    public void enableUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                background.setVisibility(View.INVISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void setUnits(ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        this.powerUnits = powerUnits;
        this.remoteControllers = remoteControllers;
        this.temperatureSensors = temperatureSensors;
        this.humidityTemperatureSensors = humidityTemperatureSensors;
        this.motionSensors = motionSensors;
        this.openCloseSensors = openCloseSensors;
        this.leakDetectors = leakDetectors;
        this.lightSensors = lightSensors;
        this.powerUnitsF = powerUnitsF;
        this.powerSocketsF = powerSocketsF;
        this.thermostats = thermostats;
        this.rolletUnitsF = rolletUnitsF;
    }

    public void setRoomUnits(ArrayList<Room> rooms, ArrayList<Preset> presets, ArrayList<PowerUnit> powerUnits, ArrayList<RemoteController> remoteControllers, ArrayList<TemperatureSensor> temperatureSensors, ArrayList<HumidityTemperatureSensor> humidityTemperatureSensors, ArrayList<MotionSensor> motionSensors, ArrayList<OpenCloseSensor> openCloseSensors, ArrayList<LeakDetector> leakDetectors, ArrayList<LightSensor> lightSensors, ArrayList<PowerUnitF> powerUnitsF, ArrayList<PowerSocketF> powerSocketsF, ArrayList<Thermostat> thermostats, ArrayList<RolletUnitF> rolletUnitsF) {
        this.powerUnits = powerUnits;
        this.remoteControllers = remoteControllers;
        this.temperatureSensors = temperatureSensors;
        this.humidityTemperatureSensors = humidityTemperatureSensors;
        this.motionSensors = motionSensors;
        this.openCloseSensors = openCloseSensors;
        this.leakDetectors = leakDetectors;
        this.lightSensors = lightSensors;
        this.powerUnitsF = powerUnitsF;
        this.powerSocketsF = powerSocketsF;
        this.thermostats = thermostats;
        this.rolletUnitsF = rolletUnitsF;

        homeFragment.setUnits(rooms, presets, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, this.rolletUnitsF);
    }

    public void setPresets(ArrayList<Preset> presets) {
        this.presets = presets;
    }

    public void setDevices(ArrayList<Object> devices) {
        this.devices = devices;
    }

//    private void setBackground(boolean setNew) {
//        if (setNew) {
//            imageID = (int) (Math.random() * 7);
//        }
//        switch (imageID) {
//            case 0:
//                background.setImageDrawable(new ColorDrawable(0xffffff));
//                break;
//            case 1:
//                background.setImageResource(R.mipmap.img_coast);
//                break;
//            case 2:
//                background.setImageResource(R.mipmap.img_evening_city);
//                break;
//            case 3:
//                background.setImageResource(R.mipmap.img_lake_forest);
//                break;
//            case 4:
//                background.setImageResource(R.mipmap.img_lake_mountains);
//                break;
//            case 5:
//                background.setImageResource(R.mipmap.img_night_city);
//                break;
//            case 6:
//                background.setImageResource(R.mipmap.img_water_island);
//                break;
//        }
//    }

    private void setupDrawer() {
        drawerLayout.setScrimColor(Color.argb(128, 0, 0, 0));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawerLayout.setDrawerShadow(R.drawable.navigation_view_shadow, Gravity.LEFT);
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        homeIP.setText(Settings.getAddress());
        homeIP.setOnClickListener(this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                roomIndex = menuItem.getItemId() - 1;
                roomID = rooms.get(menuItem.getItemId() - 1).getId();
                bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_room);
                drawerLayout.closeDrawer(Gravity.LEFT);
                return true;
            }
        });

        buttonAddRoom.setOnClickListener(this);
    }

    private void navigationViewMenuInitialization(Cursor cursor) {
        final int first = 1;
        Menu menu = navigationView.getMenu();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int itemID = first;
                do {
                    menu.add(first, itemID++, 0, cursor.getString(1));
                } while (cursor.moveToNext());
                menu.setGroupCheckable(first, true, false);
                navigationView.setCheckedItem(first);
                navigationView.invalidate();
            }
        }
    }

    public void setApiFiles(APIFiles apiFiles) {
        this.apiFiles = apiFiles;
    }

    public void setRooms(final ArrayList<Room> rooms) {
        this.rooms = rooms;
        roomFragment.send(fragmentManager, client, nooLiteF, apiFiles, rooms);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();

                final int first = 1;
                Menu menu = navigationView.getMenu();
                menu.clear();
                if (Settings.isDeveloperMode()) {
                    for (int itemId = first, room = 0; room < rooms.size(); room++, itemId++) {
                        menu.add(first, itemId, 0, String.format(Locale.ROOT, "[%02d] %s", rooms.get(room).getId(), rooms.get(room).getName()));
                    }
                } else {
                    for (int itemId = first, room = 0; room < rooms.size(); room++, itemId++) {
                        menu.add(first, itemId, 0, rooms.get(room).getName());
                    }
                }
                menu.setGroupCheckable(first, true, false);
                navigationView.invalidate();
                if (bottomNavigationView.getSelectedItemId() == R.id.bottom_navigation_item_room) {
                    if (roomIndex < rooms.size()) {
                        navigationView.getMenu().getItem(roomIndex).setChecked(true);
                        roomID = rooms.get(roomIndex).getId();
                        replaceRoom();
//                        navigationView.getMenu().getItem(roomIndex).setChecked(true);
//                        roomFragment.send(nooLiteF, apiFiles, rooms);
//                        roomFragment.setUnits(roomIndex, rooms.get(roomIndex).getId(), powerUnits, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, powerUnitsF, powerSocketsF, thermostats);
//                        fragmentTransaction.replace(R.id.fragmentContainer, roomFragment, "ROOM");
//                        if (rooms != null && rooms.size() > roomIndex)
//                            toolbarTitle.setText(rooms.get(roomIndex).getName());
                    } else {
                        if (rooms.size() > 0) {
                            roomIndex = rooms.size() - 1;
                            navigationView.getMenu().getItem(roomIndex).setChecked(true);
                            roomID = rooms.get(roomIndex).getId();
                            replaceRoom();
                        } else {
                            bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_home);
                        }
                    }
                } else {
                    if (roomIndex < rooms.size()) {
                        navigationView.getMenu().getItem(roomIndex).setChecked(true);
                    }
                }

                Log.d("nooLiteF", "setRooms() - " + (System.currentTimeMillis() - time) + " ms");
                writeAppLog("setRooms() - " + (System.currentTimeMillis() - time) + " ms");
            }
        });
    }

    private void replaceRoom() {
        roomFragment.send(fragmentManager, client, nooLiteF, apiFiles, rooms);
        roomFragment.setUnits(roomIndex, roomID, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, roomFragment, "ROOM");
        fragmentTransaction.commitAllowingStateLoss();  // Проверить по Vitals как заходит в последующих версиях
        //fragmentTransaction.commitNowAllowingStateLoss();
        if (rooms != null && rooms.size() > roomIndex)
            toolbarTitle.setText(rooms.get(roomIndex).getName());
    }

    private void setupToolbar() {
        burgerButton.setOnClickListener(this);
        setSupportActionBar(toolbar);
//        cursor = db.query("locations", null, "id = 1", null, null, null, null);
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                toolbarTitle.setText(cursor.getString(1));
//            }
//        }
        menuButton.setOnClickListener(this);
    }

    private void setHome(final String name) {
        home = name;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();

                if (bottomNavigationView.getSelectedItemId() == R.id.bottom_navigation_item_room) {
                    if (rooms == null) rooms = new ArrayList<>();
                    if (roomID < rooms.size()) {
                        toolbarTitle.setText(rooms.get(roomID).getName());
                    }
                } else {
                    if (bottomNavigationView.getSelectedItemId() != R.id.bottom_navigation_item_timers) {
                        toolbarTitle.setText(home);
                    }
                }
                homeName.setText(name);
                homeIP.setText(Settings.getAddress());

                Log.d("nooLiteF", "setHome() - " + (System.currentTimeMillis() - time) + " ms");
                writeAppLog("setHome() - " + (System.currentTimeMillis() - time) + " ms");
            }
        });
    }

    private void setupBottomNavigationView() {
        //BottomNavigationViewDesign.disableShiftMode(bottomNavigationView, getApplicationContext());
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottom_navigation_item_home:
                                replaceFragment(R.layout.fragment_home);
                                break;
                            case R.id.bottom_navigation_item_room:
                                replaceFragment(R.layout.fragment_room);
                                break;
                            case R.id.bottom_navigation_item_timers:
                                replaceFragment(R.layout.fragment_timers);
                                break;
                            case R.id.bottom_navigation_item_automatics:
                                replaceFragment(R.layout.fragment_automatics);
                                break;
                        }
                        return true;
                    }
                });
    }

    private void addStartFragment() {
        if (homeFragment != null && !homeFragment.isAdded()) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_home);
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, homeFragment, "HOME");
            fragmentTransaction.commit();
        }
    }

    private void replaceFragment(int id) {
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (id) {
            case R.layout.fragment_home:
                //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.fragmentContainer, homeFragment, "HOME");
                if (home != null) toolbarTitle.setText(home);
                else toolbarTitle.setText("Дом");
                break;
            case R.layout.fragment_room:
                roomFragment.send(fragmentManager, client, nooLiteF, apiFiles, rooms);
                roomFragment.setUnits(roomIndex, roomID, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
                fragmentTransaction.replace(R.id.fragmentContainer, roomFragment, "ROOM");
                roomFragment.updateRecyclerView();
                if (rooms != null && rooms.size() > roomIndex)
                    toolbarTitle.setText(rooms.get(roomIndex).getName());
                else toolbarTitle.setText("Комната");
                break;
            case R.layout.fragment_timers:
                timersFragment.send(client, nooLitePRF64, rooms, devices, presets);
                //fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                fragmentTransaction.replace(R.id.fragmentContainer, timersFragment, "TIMERS");
                toolbarTitle.setText(R.string.bottom_navigation_menu_item_timers_title);
                break;
            case R.layout.fragment_automatics:
                automaticsFragment = AutomaticsFragment.getInstance(fragmentManager, nooLitePRF64, client);
                fragmentTransaction.replace(R.id.fragmentContainer, automaticsFragment, AutomaticsFragment.class.getSimpleName());
                toolbarTitle.setText(R.string.bottom_navigation_menu_item_automation_title);
                break;
        }
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public Boolean wifiOn() {
        final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            switch (wifi.getWifiState()) {
                case (WifiManager.WIFI_STATE_UNKNOWN):
                case (WifiManager.WIFI_STATE_DISABLED):
                case (WifiManager.WIFI_STATE_DISABLING):
                    Snackbar.make(findViewById(R.id.homeCoordinator), "Подключитесь к сети", Snackbar.LENGTH_LONG).show();
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    public void showSnackBar(final String message, final int colorID, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.homeCoordinator), "", length);
                View snackBarView = snackbar.getView();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBarView.getLayoutParams();
                params.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.dp_56);
                snackBarView.setLayoutParams(params);
                if (colorID != 0) snackBarView.setBackgroundResource(colorID);
                snackbar.setText(message);
                snackbar.show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_ip:
                settingsControllerIPFragment = (SettingsControllerIPFragment) fragmentManager.findFragmentByTag("SETTINGS_CONTROLLER_IP_FRAGMENT");
                if (settingsControllerIPFragment == null) {
                    settingsControllerIPFragment = new SettingsControllerIPFragment();
                    settingsControllerIPFragment.send(client);
                    settingsControllerIPFragment.setSettingsControllerIPFragmentListener(new SettingsControllerIPFragmentListener() {
                        @Override
                        public void onDismiss(boolean update) {
                            if (update) {
                                homeFragment.setRefreshing(true);
                                roomFragment.setRefreshing(true);
                                updatePRF64();
                            }
                            homeIP.setText(Settings.getAddress());
                        }
                    });
                }
                fragmentManager.beginTransaction().add(settingsControllerIPFragment, "SETTINGS_CONTROLLER_IP_FRAGMENT").show(settingsControllerIPFragment).commit();
                break;
            case R.id.home_settings:
                settingsHomeFragment = (SettingsHomeFragment) fragmentManager.findFragmentByTag("SETTINGS_HOME_FRAGMENT");
                if (settingsHomeFragment == null) {
                    settingsHomeFragment = new SettingsHomeFragment();
                    settingsHomeFragment.send(client, apiFiles);
                    settingsHomeFragment.setSettingsHomeFragmentListener(new SettingsHomeFragmentListener() {
                        @Override
                        public void onDismiss(String home) {
                            if (home != null) setHome(home);
                        }
                    });
                }
                if (settingsHomeFragment.isAdded()) return;
                fragmentManager.beginTransaction().add(settingsHomeFragment, "SETTINGS_HOME_FRAGMENT").show(settingsHomeFragment).commit();
                break;
            case R.id.add_room_button:
                SettingsRoomFragment settingsRoomFragment = (SettingsRoomFragment) fragmentManager.findFragmentByTag("SETTINGS_ROOM_FRAGMENT");
                if (settingsRoomFragment == null) {
                    settingsRoomFragment = SettingsRoomFragment.newInstance(apiFiles.getUser(), -1, null);
                    settingsRoomFragment.setSettingsRoomFragmentListener(new SettingsRoomFragmentListener() {
                        @Override
                        public void onDismiss(boolean update) {
                            if (update) {
                                homeFragment.setRefreshing(true);
                                roomFragment.setRefreshing(true);
                                hardUpdatePRF64();
                            }
                        }
                    });
                }
                if (settingsRoomFragment.isAdded()) return;
                if (apiFiles != null) {
                    if (apiFiles.getUser() != null) {
                        fragmentManager.beginTransaction().add(settingsRoomFragment, "SETTINGS_ROOM_FRAGMENT").show(settingsRoomFragment).commit();
                    } else {
                        showToast("Сначала обновите...");
                    }
                } else {
                    showToast("Сначала обновите...");
                }
                break;
            case R.id.burger_button:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.menu_button:
                switch (bottomNavigationView.getSelectedItemId()) {
                    case R.id.bottom_navigation_item_home:
                        homeFragment.showPopupMenu(view);
                        break;
                    case R.id.bottom_navigation_item_room:
                        roomFragment.showPopupMenu(view);
                        break;
                    case R.id.bottom_navigation_item_timers:
                        timersFragment.showPopupMenu(view);
                        break;
                    case R.id.bottom_navigation_item_automatics:
                        automaticsFragment.showPopupMenu(view);
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        actionBarDrawerToggle.onConfigurationChanged(newConfig);

//        switch (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) {
//            case Configuration.UI_MODE_NIGHT_NO:
//                Settings.setNightMode(false);
//                break;
//            case Configuration.UI_MODE_NIGHT_YES:
//                Settings.setNightMode(true);
//                break;
//        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            if (backPressed + 2000 > System.currentTimeMillis()) {
                toast.cancel();
                super.onBackPressed();
            } else {
                toast.setText("Нажмите ещё раз, для выхода...");
                toast.show();
            }
            backPressed = System.currentTimeMillis();
        }
    }

    @Override
    protected void onResume() {
//        if (nooLiteF == null) {
//            // дублирование из onCreate() в ответ на NullPointerException при восстановлении приложения и выполнении nooLiteF.setStateListener()
//
//            nooLiteF = new NooLiteF(client, apiFiles);
//
//            fragmentManager = getSupportFragmentManager();
//            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag("HOME");
//            if (homeFragment == null) {
//                homeFragment = new HomeFragment();
//            }
//            homeFragment.send(client, nooLiteF);
//
//            roomFragment = (RoomFragment) fragmentManager.findFragmentByTag("ROOM");
//            if (roomFragment == null) {
//                roomFragment = new RoomFragment();
//            }
//            roomFragment.send(client, nooLiteF, apiFiles, rooms);
//            roomFragment.setUnits(roomIndex, roomID, powerUnits, remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF);
//
//            timersFragment = (TimersFragment) fragmentManager.findFragmentByTag("TIMERS");
//            if (timersFragment == null) {
//                timersFragment = new TimersFragment();
//            }
//            timersFragment.send(client, rooms, devices, presets);
//        }

        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (timersFragment.isAdded()) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(timersFragment);
            fragmentTransaction.commitAllowingStateLoss();
            bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_home);
        }
        if (automaticsFragment.isAdded()) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(automaticsFragment);
            fragmentTransaction.commitAllowingStateLoss();
            bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_home);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    public void showProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homeFragment.setRefreshing(true);
                roomFragment.setRefreshing(true);
            }
        });
    }

    public void getUnitsState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homeFragment.setRefreshing(true);
                roomFragment.setRefreshing(true);
                //nooLiteF.getState(64);
                updatePRF64();
            }
        });
    }

    public void headerProgressBarSetVisible(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visible) {
                    buttonAddRoom.setEnabled(false);
                    headerProgressBar.setVisibility(View.VISIBLE);
                } else {
                    buttonAddRoom.setEnabled(true);
                    headerProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //signalR
//    public void connectToHub() {
//        hubConnection = new WebSocketHubConnectionP2(Settings.URL() + "chatHub", null);
//        hubConnection.addListener(new HubConnectionListener() {
//            @Override
//            public void onConnected() {
//                showToast("WebSocketHubConnectionP2: connected");
//            }
//
//            @Override
//            public void onDisconnected() {
//                showToast("WebSocketHubConnectionP2: disconnected");
//            }
//
//            @Override
//            public void onMessage(HubMessage message) {
//                HubMessage hubMessage = message;
//                JsonElement[] jsonElements = hubMessage.getArguments();
//                for (JsonElement jsonElement : jsonElements) {
//                    JsonObject jsonObject = jsonElement.getAsJsonObject();
//                    JsonElement jsonElement1 = jsonObject.get("value");
//                    JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
//                    showToast("Message from " + jsonObject1.get("name").toString());
//                }
//            }
//
//            @Override
//            public void onError(Exception exception) {
//                showToast("WebSocketHubConnectionP2: exception" + "\n\n" + exception.toString());
//            }
//        });
//        hubConnection.subscribeToEvent("UpdateDevView", new HubEventListener() {
//            @Override
//            public void onEventMessage(HubMessage message) {
//                HubMessage hubMessage = message;
//                JsonElement[] jsonElements = hubMessage.getArguments();
//                for (JsonElement jsonElement : jsonElements) {
//                    JsonObject jsonObject = jsonElement.getAsJsonObject();
//                    JsonElement jsonElement1 = jsonObject.get("value");
//                    JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
//                    showToast("Event from " + jsonObject1.get("name").toString());
//                }
//            }
//        });
//        hubConnection.connect();
//
//        client.newCall(new Request.Builder().url(Settings.URL() + "home/devbase").build()).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                showToast("OkHttpClient: failure");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                try {
//                    JSONObject jsonData = new JSONObject(response.body().string());
//                    JSONArray jsonNames = jsonData.names();
//                    JSONObject jsonObject;
//                    ArrayList<PowerUnit> powerUnits = new ArrayList<>();
//                    for (int i = 0; i < jsonNames.length(); i++) {
//                        jsonObject = jsonData.getJSONObject(jsonNames.get(i).toString());
//                        powerUnits.add(new PowerUnit(i, -1, -1, jsonObject.getString("room"), jsonObject.getString("name"), false));
//                    }
//                    homeFragment.setUnits(powerUnits);
//                } catch (Exception e) {
//                    showToast("OkHttpClient: exception");
//                }
//            }
//        });
//    }

    private Byte[] toByteObjectArray(byte[] byteArray) {
        Byte[] byteObjectArray = new Byte[byteArray.length];

        for (int b = 0; b < byteArray.length; b++) {
            byteObjectArray[b] = byteArray[b];
        }

        return byteObjectArray;
    }

    private byte[] toByteArray(Byte[] byteObjectArray) {
        byte[] byteArray = new byte[byteObjectArray.length];

        for (int b = 0; b < byteObjectArray.length; b++) {
            byteArray[b] = byteObjectArray[b];
        }

        return byteArray;
    }

    public void updatePRF64() {
        updating = true;

        Request request = new Request.Builder()
                .url(Settings.URL() + "state.htm")
                .build();
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                updating = false;
                enableUI();
                homeFragment.setRefreshing(false);
                roomFragment.setRefreshing(false);
                showSnackBar(getString(R.string.no_connection), 0, Snackbar.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        String state = response.body() != null ? response.body().string() : "";
                        call.cancel();

                        if (state.length() != 269) {
                            call.cancel();
                            updating = false;
                            enableUI();
                            homeFragment.setRefreshing(false);
                            roomFragment.setRefreshing(false);
                            showSnackBar(getString(R.string.no_response), 0, Snackbar.LENGTH_SHORT);
                            return;
                        }

                        nooLitePRF64.setState(state);

                        if (nooLitePRF64.getLastUpdateTimestamp().equals(state.substring(255))) {
                            softUpdate();
                        } else {
                            hardUpdate();
                        }
                    } else {
                        call.cancel();
                        updating = false;
                        enableUI();
                        homeFragment.setRefreshing(false);
                        roomFragment.setRefreshing(false);
                        showSnackBar(getString(R.string.connection_error) + " " + response.code(), 0, Snackbar.LENGTH_SHORT);
                    }
                } catch (ConnectException e) {
                    client.dispatcher().cancelAll();
                    updating = false;
                    enableUI();
                    homeFragment.setRefreshing(false);
                    roomFragment.setRefreshing(false);
                    writeAppLog("HomeActivity.java : updatePRF64()" + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
                    showSnackBar(getString(R.string.connection_error), 0, Snackbar.LENGTH_SHORT);
                } catch (Exception e) {
                    client.dispatcher().cancelAll();
                    updating = false;
                    enableUI();
                    homeFragment.setRefreshing(false);
                    roomFragment.setRefreshing(false);
                    writeAppLog("HomeActivity.java : updatePRF64()" + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
                    showSnackBar(getString(R.string.some_thing_went_wrong), 0, Snackbar.LENGTH_SHORT);
                }
            }
        });
    }

    private void softUpdate() throws IOException, InterruptedException {
        updating = false;
        updateHomeActivity(nooLitePRF64);
        updateHomeFragment(nooLitePRF64);
        updateRoomFragment(nooLitePRF64);
        updateAPIFiles(nooLitePRF64);
        updateLog(nooLitePRF64);

        long time = System.currentTimeMillis();
        if (pollState()) {
            Log.d("nooLiteF", "softUpdate(): poolState() - " + (System.currentTimeMillis() - time) + " ms");
            writeAppLog("softUpdate(): poolState() - " + (System.currentTimeMillis() - time) + " ms");

            time = System.currentTimeMillis();
            if (getState()) {
                Log.d("nooLiteF", "softUpdate(): getState() - " + (System.currentTimeMillis() - time) + " ms");
                writeAppLog("softUpdate(): getState() - " + (System.currentTimeMillis() - time) + " ms");

                time = System.currentTimeMillis();
                getThermostatsTargetTemperature(nooLitePRF64.getThermostats());
                Log.d("nooLiteF", "softUpdate(): getThermostatsTargetTemperature() - " + (System.currentTimeMillis() - time) + " ms");
                writeAppLog("softUpdate(): getThermostatsTargetTemperature() - " + (System.currentTimeMillis() - time) + " ms");

                time = System.currentTimeMillis();
                nooLitePRF64.setLog(getLog(nooLitePRF64.getLog()));
                Log.d("nooLiteF", "softUpdate(): getLog() - " + (System.currentTimeMillis() - time) + " ms");
                writeAppLog("softUpdate(): getLog() - " + (System.currentTimeMillis() - time) + " ms");

                updateLog(nooLitePRF64);
            }
        }

        updating = false;
        homeFragment.setRefreshing(false);
        roomFragment.setRefreshing(false);
        enableUI();
    }

    public void hardUpdatePRF64() {
        updating = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    hardUpdate();
                } catch (ConnectException e) {
                    client.dispatcher().cancelAll();
                    updating = false;
                    enableUI();
                    homeFragment.setRefreshing(false);
                    roomFragment.setRefreshing(false);
                    writeAppLog("HomeActivity.java : hardUpdatePRF64()" + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
                    showSnackBar(getString(R.string.connection_error), 0, Snackbar.LENGTH_SHORT);
                } catch (Exception e) {
                    client.dispatcher().cancelAll();
                    updating = false;
                    enableUI();
                    homeFragment.setRefreshing(false);
                    roomFragment.setRefreshing(false);
                    writeAppLog("HomeActivity.java : hardUpdatePRF64()" + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
                    showSnackBar(getString(R.string.some_thing_went_wrong), 0, Snackbar.LENGTH_SHORT);
                }
            }
        }).start();
    }

    private void hardUpdate() throws IOException, InterruptedException {
        long time = System.currentTimeMillis();
        if (pollState()) {
            Log.d("nooLiteF", "hardUpdate(): poolState() - " + (System.currentTimeMillis() - time) + " ms");
            writeAppLog("hardUpdate(): poolState() - " + (System.currentTimeMillis() - time) + " ms");

            time = System.currentTimeMillis();
            if (getFiles()) {
                Log.d("nooLiteF", "hardUpdate(): getFiles() - " + (System.currentTimeMillis() - time) + " ms");
                writeAppLog("hardUpdate(): getFiles() - " + (System.currentTimeMillis() - time) + " ms");

                updating = false;
                updateHomeActivity(nooLitePRF64);
                updateHomeFragment(nooLitePRF64);
                updateRoomFragment(nooLitePRF64);
                updateAPIFiles(nooLitePRF64);
                updateLog(nooLitePRF64);

                time = System.currentTimeMillis();
                if (getState()) {
                    Log.d("nooLiteF", "hardUpdate(): getState() - " + (System.currentTimeMillis() - time) + " ms");
                    writeAppLog("hardUpdate(): getState() - " + (System.currentTimeMillis() - time) + " ms");

                    time = System.currentTimeMillis();
                    getThermostatsTargetTemperature(nooLitePRF64.getThermostats());
                    Log.d("nooLiteF", "hardUpdate(): getThermostatsTargetTemperature() - " + (System.currentTimeMillis() - time) + " ms");
                    writeAppLog("hardUpdate(): getThermostatsTargetTemperature() - " + (System.currentTimeMillis() - time) + " ms");

                    time = System.currentTimeMillis();
                    nooLitePRF64.setLog(getLog(nooLitePRF64.getLog()));
                    Log.d("nooLiteF", "hardUpdate(): getLog() - " + (System.currentTimeMillis() - time) + " ms");
                    writeAppLog("hardUpdate(): getLog() - " + (System.currentTimeMillis() - time) + " ms");

                    updateLog(nooLitePRF64);
                }
            }
        }

        updating = false;
        homeFragment.setRefreshing(false);
        roomFragment.setRefreshing(false);
        enableUI();
    }

    private boolean pollState() throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "send.htm?sd=000200000080000000000000000000")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            call.cancel();

            pollStateTime = System.currentTimeMillis();
            pollStateTimeout = nooLitePRF64.getFTXCount() * 100;

            return true;
        }

        call.cancel();
        showSnackBar(getString(R.string.connection_error) + " " + response.code(), 0, Snackbar.LENGTH_SHORT);
        return false;
    }

    private boolean getFiles() throws IOException {
        byte[] file;

        Request request = new Request.Builder()
                .url(Settings.URL() + "device.bin")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            file = response.body() != null ? response.body().bytes() : new byte[0];
            call.cancel();

            if (file.length == 4102) {
                nooLitePRF64.setDevice(file);
            } else {
                showSnackBar(getString(R.string.no_response), 0, Snackbar.LENGTH_SHORT);
                return false;
            }

            request = new Request.Builder()
                    .url(Settings.URL() + "preset.bin")
                    .build();
            call = client.newCall(request);
            response = call.execute();

            if (response.isSuccessful()) {
                file = response.body() != null ? response.body().bytes() : new byte[0];
                call.cancel();

                if (file.length == 32774) {
                    nooLitePRF64.setPreset(file);
                } else {
                    showSnackBar(getString(R.string.no_response), 0, Snackbar.LENGTH_SHORT);
                    return false;
                }

                request = new Request.Builder()
                        .url(Settings.URL() + "user.bin")
                        .build();
                call = client.newCall(request);
                response = call.execute();

                if (response.isSuccessful()) {
                    file = response.body() != null ? response.body().bytes() : new byte[0];
                    call.cancel();

                    if (file.length == 12294) {
                        nooLitePRF64.setUser(file);
                    } else {
                        showSnackBar(getString(R.string.no_response), 0, Snackbar.LENGTH_SHORT);
                        return false;
                    }

                    return true;
                }
            }
        }

        call.cancel();
        showSnackBar(getString(R.string.connection_error) + " " + response.code(), 0, Snackbar.LENGTH_SHORT);
        return false;
    }

    private boolean getState() throws IOException, InterruptedException {
        long timePassed = System.currentTimeMillis() - pollStateTime;
        if (timePassed < pollStateTimeout) {
            Thread.sleep(pollStateTimeout - timePassed);
        }

        Request request = new Request.Builder()
                .url(Settings.URL() + "state.htm")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            String state = response.body() != null ? response.body().string() : "";
            call.cancel();

            if (state.length() != 269) {
                showSnackBar(getString(R.string.no_response), 0, Snackbar.LENGTH_SHORT);
                return false;
            }

            nooLitePRF64.setState(state);
            nooLitePRF64.setLastUpdateTimestamp(state);

            return true;
        }

        call.cancel();
        showSnackBar(getString(R.string.connection_error) + " " + response.code(), 0, Snackbar.LENGTH_SHORT);
        return false;
    }

    private void getThermostatsTargetTemperature(ArrayList<Thermostat> thermostats) {
        if (thermostats.size() == 0) return;

        for (Thermostat thermostat : thermostats) {
            if (!getThermostatTargetTemperature(thermostat)) break;
        }
    }

    private byte[] getLog(byte[] log) throws IOException {
        Request request = new Request.Builder()
                .url(Settings.URL() + "log.bin")
                .build();
        call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            InputStream inputStream = response.body() != null ? response.body().byteStream() : null;
            if (inputStream == null) return log;

            byte[] lastLogRecord = new byte[21];
            Arrays.fill(lastLogRecord, (byte) 255);
            if (log.length > 20) {
                System.arraycopy(log, 0, lastLogRecord, 0, 21);
            }
            byte[] logRecord = new byte[21];
            ArrayList<Byte[]> recentLog = new ArrayList<>();

            boolean mergeLog = false;
            while (inputStream.read(logRecord) != -1) {
                if ((logRecord[0] & 0xFF) == 255) break;
                nooLitePRF64.parseRecentLog(logRecord);
                if (!Arrays.equals(logRecord, lastLogRecord)) {
                    recentLog.add(toByteObjectArray(logRecord));
                } else {
                    mergeLog = true;
                    break;
                }
            }

            inputStream.close();
            call.cancel();

            byte[] summaryLog;
            int logRecordIndex;
            if (mergeLog) {
                summaryLog = new byte[21 * recentLog.size() + log.length];
                for (logRecordIndex = 0; logRecordIndex < recentLog.size(); logRecordIndex++) {
                    System.arraycopy(toByteArray(recentLog.get(logRecordIndex)), 0, summaryLog, 21 * logRecordIndex, 21);
                }
                System.arraycopy(log, 0, summaryLog, 21 * logRecordIndex, log.length);
            } else {
                summaryLog = new byte[21 * recentLog.size()];
                for (logRecordIndex = 0; logRecordIndex < recentLog.size(); logRecordIndex++) {
                    System.arraycopy(toByteArray(recentLog.get(logRecordIndex)), 0, summaryLog, 21 * logRecordIndex, 21);
                }
            }

            return summaryLog;
        }

        call.cancel();
        showSnackBar(getString(R.string.connection_error) + " " + response.code(), 0, Snackbar.LENGTH_SHORT);
        return new byte[0];
    }

    // requests for F_TX units state

    private Response httpClientGetRequest(String request) throws IOException {
        Request _request = new Request.Builder()
                .url(Settings.URL().concat(request))
                .build();
        call = client.newCall(_request);
        return call.execute();
    }

    private Response httpClientPostRequest(String request) throws IOException {
        Request _request = new Request.Builder()
                .url(Settings.URL().concat(request))
                .post(RequestBody.create(null, ""))
                .build();
        call = client.newCall(_request);
        return call.execute();
    }

    private boolean getState(State state) {
        try {

            Response response = httpClientGetRequest("state.htm");

            if (response.isSuccessful()) {
                state.setState(response.body() != null ? response.body().string() : "");
                call.cancel();

                if (state.getState().length() != 269) {
                    state.setState(getString(R.string.no_response));
                    writeAppLog("HomeActivity.java : getState()" + "\n" + state);
                    return false;
                }

                return true;
            } else {
                state.setState(getString(R.string.connection_error) + response.code());
                writeAppLog("HomeActivity.java : getState()" + "\n" + state);
                return false;
            }

        } catch (IOException ioe) {
            state.setState(getString(R.string.no_connection));
            writeAppLog("HomeActivity.java : getState()" + "\n" + state + "\n" + ioe.toString() + "\n" + NooLiteF.getStackTrace(ioe));
            return false;
        } catch (Exception e) {
            state.setState(getString(R.string.some_thing_went_wrong));
            writeAppLog("HomeActivity.java : getState()" + "\n" + state + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
            return false;
        }
    }

    public void getFTXUnitsState() {

        State state = new State();

        if (getState(state)) {
            nooLitePRF64.setState(state.getState());
        } else {
            showSnackBar(state.getState(), 0, Snackbar.LENGTH_SHORT);
        }

    }

    public void getThermostatState(Thermostat thermostat) {
        if (!getThermostatTargetTemperature(thermostat)) return;

        State state = new State();

        if (getState(state)) {
            nooLitePRF64.setState(state.getState());
        } else {
            thermostatDialogShowToast(state.getState());
        }

        updateThermostatDialog();
    }

    void setThermostatTargetTemperature(final Thermostat thermostat) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                try {

                    Response response = httpClientPostRequest(
                            String.format(Locale.ROOT,
                                    "send.htm?sd=00020800000601%s000000%s",
                                    NooLiteF.getHexString(thermostat.getTargetTemperature()),
                                    thermostat.getId()));
                    if (response.isSuccessful()) {
                        call.cancel();

                        Thread.sleep(Settings.switchTimeout());
                        getThermostatState(thermostat);

                        Thread.sleep(1000);
                        response = httpClientPostRequest(
                                String.format(Locale.ROOT,
                                        "send.htm?sd=00020800000601%s000000%s",
                                        NooLiteF.getHexString(thermostat.getTargetTemperature()),
                                        thermostat.getId()));
                        if (response.isSuccessful()) {
                            call.cancel();

                            Thread.sleep(Settings.switchTimeout());
                            getThermostatState(thermostat);

                            return;
                        }
                    }

                    call.cancel();
                    message = getString(R.string.connection_error) + response.code();
                    writeAppLog("HomeActivity.java : setThermostatTargetTemperature()" + "\n" + message);
                    thermostat.setTargetTemperature(0);
                    thermostatDialogShowToast(message);
                    updateThermostatDialog();

                } catch (IOException ioe) {
                    call.cancel();

                    message = getString(R.string.no_connection);
                    writeAppLog("HomeActivity.java : setThermostatTargetTemperature()" + "\n" + message + "\n" + ioe.toString() + "\n" + NooLiteF.getStackTrace(ioe));
                    thermostat.setTargetTemperature(0);
                    thermostatDialogShowToast(message);
                    updateThermostatDialog();
                } catch (Exception e) {
                    call.cancel();

                    message = getString(R.string.some_thing_went_wrong);
                    writeAppLog("HomeActivity.java : setThermostatTargetTemperature()" + "\n" + message + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
                    thermostat.setTargetTemperature(0);
                    thermostatDialogShowToast(message);
                    updateThermostatDialog();
                }

            }
        }).start();
    }

    private boolean getThermostatTargetTemperature(Thermostat thermostat) {
        String message;

        try {

            Response response = httpClientPostRequest(
                    String.format(Locale.ROOT,
                            "send.htm?sd=010C%s000000000000000000",
                            thermostat.getId()));

            if (response.isSuccessful()) {
                call.cancel();

                response = httpClientPostRequest(
                        String.format(Locale.ROOT,
                                "send.htm?sd=0002080000801F00000000%s",
                                thermostat.getId()));

                if (response.isSuccessful()) {
                    call.cancel();

                    Thread.sleep(100);

                    response = httpClientGetRequest("rxset.htm");

                    if (response.isSuccessful()) {
                        String hex = response.body() != null ? response.body().string() : "000000000000000000000000000000";
                        call.cancel();

                        if (hex.substring(22, 30).equals(thermostat.getId())) {
                            thermostat.setTargetTemperature(Integer.parseInt(hex.substring(14, 16), 16));
                        } else {
                            thermostat.setTargetTemperature(0);
                        }

                        updateAdapterItem(thermostat.getAdapterPosition());

                        return true;
                    }
                }
            }

            call.cancel();
            message = getString(R.string.connection_error) + response.code();
            writeAppLog("HomeActivity.java : setThermostatTargetTemperature()" + "\n" + message);
            thermostatDialogShowToast(message);
            showSnackBar(message, 0, Snackbar.LENGTH_SHORT);
            return false;

        } catch (IOException ioe) {
            call.cancel();

            message = getString(R.string.no_connection);
            writeAppLog("HomeActivity.java : setThermostatTargetTemperature()" + "\n" + message + "\n" + ioe.toString() + "\n" + NooLiteF.getStackTrace(ioe));
            thermostatDialogShowToast(message);
            return false;
        } catch (Exception e) {
            call.cancel();

            message = getString(R.string.some_thing_went_wrong);
            writeAppLog("HomeActivity.java : setThermostatTargetTemperature()" + "\n" + message + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e));
            thermostatDialogShowToast(message);
            return false;
        }
    }

    private void updateThermostatDialog() {
        ThermostatDialog thermostatDialog = (ThermostatDialog) fragmentManager.findFragmentByTag("THERMOSTAT_DIALOG");
        if (thermostatDialog != null) {
            thermostatDialog.updateState();
        }
    }

    private void thermostatDialogShowToast(final String message) {
        ThermostatDialog thermostatDialog = (ThermostatDialog) fragmentManager.findFragmentByTag("THERMOSTAT_DIALOG");
        if (thermostatDialog != null) {
            thermostatDialog.showToast(message);
        }
    }

    // pads between new file loader and old gui refreshers and old cache

    private void updateHomeActivity(PRF64 nooLitePRF64) {
        long time = System.currentTimeMillis();

        setHome(nooLitePRF64.getHome().getName());
        setRooms(nooLitePRF64.getHome().getRooms());

        Log.d("nooLiteF", "updateHomeActivity() - " + (System.currentTimeMillis() - time) + " ms");
        writeAppLog("updateHomeActivity() - " + (System.currentTimeMillis() - time) + " ms");
    }

    private void updateHomeFragment(PRF64 nooLitePRF64) {
        long time = System.currentTimeMillis();

        setPresets(nooLitePRF64.getPresets());
        setUnits(nooLitePRF64.getTXUnits(), nooLitePRF64.getRemoteControllers(), nooLitePRF64.getTemperatureSensors(), nooLitePRF64.getHumidityTemperatureSensors(), nooLitePRF64.getMotionSensors(), nooLitePRF64.getOpenCloseSensors(), nooLitePRF64.getLeakDetectors(), nooLitePRF64.getLightSensors(), nooLitePRF64.getPowerUnitsF(), nooLitePRF64.getPowerSocketsF(), nooLitePRF64.getThermostats(), nooLitePRF64.getRolletUnitsF());
        if (homeFragment.isAdded()) {
            homeFragment.send(fragmentManager, client, nooLiteF, apiFiles);
            homeFragment.setUnits(nooLitePRF64.getHome().getRooms(), nooLitePRF64.getPresets(), nooLitePRF64.getTXUnits(), nooLitePRF64.getRemoteControllers(), nooLitePRF64.getTemperatureSensors(), nooLitePRF64.getHumidityTemperatureSensors(), nooLitePRF64.getMotionSensors(), nooLitePRF64.getOpenCloseSensors(), nooLitePRF64.getLeakDetectors(), nooLitePRF64.getLightSensors(), nooLitePRF64.getPowerUnitsF(), nooLitePRF64.getPowerSocketsF(), nooLitePRF64.getThermostats(), nooLitePRF64.getRolletUnitsF());
        }

        Log.d("nooLiteF", "updateHomeFragment() - " + (System.currentTimeMillis() - time) + " ms");
        writeAppLog("updateHomeFragment() - " + (System.currentTimeMillis() - time) + " ms");
    }

    private void updateRoomFragment(PRF64 nooLitePRF64) {
        long time = System.currentTimeMillis();

        if (roomFragment.isAdded()) {
            roomFragment.setUnits(roomIndex, roomID, nooLitePRF64.getTXUnits(), nooLitePRF64.getRemoteControllers(), nooLitePRF64.getTemperatureSensors(), nooLitePRF64.getHumidityTemperatureSensors(), nooLitePRF64.getMotionSensors(), nooLitePRF64.getOpenCloseSensors(), nooLitePRF64.getLeakDetectors(), nooLitePRF64.getLightSensors(), nooLitePRF64.getPowerUnitsF(), nooLitePRF64.getPowerSocketsF(), nooLitePRF64.getThermostats(), nooLitePRF64.getRolletUnitsF());
            roomFragment.updateRecyclerView();
        }

        Log.d("nooLiteF", "updateRoomFragment() - " + (System.currentTimeMillis() - time) + " ms");
        writeAppLog("updateRoomFragment() - " + (System.currentTimeMillis() - time) + " ms");
    }

    private void updateUnits(PRF64 nooLitePRF64) {
        long time = System.currentTimeMillis();

        nooLiteF.update(nooLitePRF64.getTXUnits(), nooLitePRF64.getRemoteControllers(), nooLitePRF64.getTemperatureSensors(), nooLitePRF64.getHumidityTemperatureSensors(), nooLitePRF64.getMotionSensors(), nooLitePRF64.getOpenCloseSensors(), nooLitePRF64.getLeakDetectors(), nooLitePRF64.getLightSensors(), nooLitePRF64.getPowerUnitsF(), nooLitePRF64.getPowerSocketsF(), nooLitePRF64.getThermostats(), nooLitePRF64.getRolletUnitsF(), nooLitePRF64.getPresets(), nooLitePRF64.getHome().getRooms());
        if (homeFragment.isAdded()) {
            homeFragment.setUnits(nooLitePRF64.getHome().getRooms(), nooLitePRF64.getPresets(), nooLitePRF64.getTXUnits(), nooLitePRF64.getRemoteControllers(), nooLitePRF64.getTemperatureSensors(), nooLitePRF64.getHumidityTemperatureSensors(), nooLitePRF64.getMotionSensors(), nooLitePRF64.getOpenCloseSensors(), nooLitePRF64.getLeakDetectors(), nooLitePRF64.getLightSensors(), nooLitePRF64.getPowerUnitsF(), nooLitePRF64.getPowerSocketsF(), nooLitePRF64.getThermostats(), nooLitePRF64.getRolletUnitsF());
        }
        if (roomFragment.isAdded()) {
            roomFragment.setUnits(roomIndex, roomID, nooLitePRF64.getTXUnits(), nooLitePRF64.getRemoteControllers(), nooLitePRF64.getTemperatureSensors(), nooLitePRF64.getHumidityTemperatureSensors(), nooLitePRF64.getMotionSensors(), nooLitePRF64.getOpenCloseSensors(), nooLitePRF64.getLeakDetectors(), nooLitePRF64.getLightSensors(), nooLitePRF64.getPowerUnitsF(), nooLitePRF64.getPowerSocketsF(), nooLitePRF64.getThermostats(), nooLitePRF64.getRolletUnitsF());
            roomFragment.updateRecyclerView();
        }

        Log.d("nooLiteF", "updateUnits() - " + (System.currentTimeMillis() - time) + " ms");
        writeAppLog("updateUnits() - " + (System.currentTimeMillis() - time) + " ms");
    }

    private void updateAPIFiles(PRF64 nooLitePRF64) {
        long time = System.currentTimeMillis();

        apiFiles.setDevice(nooLitePRF64.getDevice());
        apiFiles.setPreset(nooLitePRF64.getPreset());
        apiFiles.setUser(nooLitePRF64.getUser());

        Log.d("nooLiteF", "updateAPIFiles() - " + (System.currentTimeMillis() - time) + " ms");
        writeAppLog("updateAPIFiles() - " + (System.currentTimeMillis() - time) + " ms");
    }

    private void updateLog(PRF64 nooLitePRF64) {
        long time = System.currentTimeMillis();

        nooLiteF.setLog(nooLitePRF64.getLog());

        Log.d("nooLiteF", "updateLog() - " + (System.currentTimeMillis() - time) + " ms");
        writeAppLog("updateLog() - " + (System.currentTimeMillis() - time) + " ms");
    }

    public static boolean isUpdating() {
        return updating;
    }

    public OkHttpClient getHttpClient() {
        return client;
    }

    public NooLiteF getNooLiteF() {
        return nooLiteF;
    }

    public PRF64 getPRF64() {
        return nooLitePRF64;
    }

    public void updateAdapterItem(int position) {
        if (homeFragment != null && homeFragment.isAdded()) {
            homeFragment.updateUnitsAdapterItem(position);
        }
        if (roomFragment != null && roomFragment.isAdded()) {
            roomFragment.updateUnitsAdapterItem(position);
        }
    }

    public void updateAdapter() {
        if (homeFragment.isAdded()) {
            homeFragment.updateUnitsAdapter();
        }
        if (roomFragment.isAdded()) {
            roomFragment.updateUnitsAdapter();
        }
    }
}
