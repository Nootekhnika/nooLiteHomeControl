package com.noolitef.timers;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.noolitef.EmptyHeader;
import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.PRF64;
import com.noolitef.R;
import com.noolitef.Room;
import com.noolitef.Thermostat;
import com.noolitef.ftx.RolletUnitF;
import com.noolitef.presets.Preset;
import com.noolitef.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimersFragment extends Fragment {
    private OkHttpClient client;
    private PRF64 nooLitePRF64;
    private byte[] file;
    private HomeActivity homeActivity;
    private TimersFragment timersFragment = this;

    private ArrayList<Room> rooms;
    private ArrayList<Object> devices;
    private ArrayList<Preset> presets;
    private ArrayList<Timer> timers;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView timersRecyclerView;
    private RecyclerView.LayoutManager timersRecyclerLayoutManager;
    private TimersRecyclerAdapter timersRecyclerAdapter;

    private TimersBlockFragment timersBlockFragment;

    public TimersFragment() {
    }

    public void send(OkHttpClient client, PRF64 nooLitePRF64, ArrayList<Room> rooms, ArrayList<Object> devices, ArrayList<Preset> presets) {
        this.client = client;
        this.nooLitePRF64 = nooLitePRF64;
        this.rooms = rooms;
        this.devices = removeUnsupportedDevices(devices);
        this.presets = presets;
    }

    private ArrayList<Object> removeUnsupportedDevices(ArrayList<Object> devices) {
        if (devices == null) return null;
        ArrayList<Object> supportedDevices = new ArrayList<>();
        for (Object device : devices) {
            if (device instanceof Thermostat) continue;
            if (device instanceof RolletUnitF) continue;
            supportedDevices.add(device);
        }
        return supportedDevices;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View timersFragmentView;
        timersFragmentView = inflater.inflate(R.layout.fragment_timers, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) timersFragmentView.findViewById((R.id.swipe_refresh_layout_timers));
        swipeRefreshLayout.setProgressViewEndTarget(true, getResources().getDimensionPixelOffset(R.dimen.swipe_progressbar_toolbar_offset) + getResources().getDimensionPixelOffset(R.dimen.dp_16));
        swipeRefreshLayout.setColorSchemeResources(R.color.black_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTimers();
            }
        });
        timersRecyclerView = (RecyclerView) timersFragmentView.findViewById(R.id.recycler_view_timers);

        homeActivity = (HomeActivity) getActivity();
        timersRecyclerLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        timersRecyclerView.setLayoutManager(timersRecyclerLayoutManager);

        setTimers(null);

        file = nooLitePRF64.getTimer();
        if (file[0] != -1) {
            parseTimers(file);
        } else {
            getTimers();
        }

        return timersFragmentView;
    }

    private void getTimers() {
        setRefreshing(true);

        new Thread(new Runnable() {
            @Override
            public void run() {

                int attempt = 25;
                while (client.dispatcher().runningCallsCount() > 0 && attempt > 0) {
                    try {
                        Thread.sleep(250);
                        attempt--;
                    } catch (InterruptedException e) {
                        showSnack(homeActivity.getString(R.string.no_connection));
                        setRefreshing(false);
                        return;
                    }
                }

                final Request request = new Request.Builder()
                        .url(Settings.URL() + "timer.bin")
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                        setRefreshing(false);
                        homeActivity.writeAppLog(homeActivity.getString(R.string.no_connection).concat("\n").concat(NooLiteF.getStackTrace(e)));
                        showSnack(homeActivity.getString(R.string.no_connection));
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        if (response.isSuccessful()) {
                            try {
                                parseTimers(response);
                                call.cancel();
                            } catch (Exception e) {
                                call.cancel();
                                setRefreshing(false);
                                homeActivity.writeAppLog(homeActivity.getString(R.string.some_thing_went_wrong).concat("\n").concat(NooLiteF.getStackTrace(e)));
                                showSnack(homeActivity.getString(R.string.some_thing_went_wrong));
                            }
                        } else {
                            call.cancel();
                            setRefreshing(false);
                            homeActivity.writeAppLog(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
                            showSnack(homeActivity.getString(R.string.connection_error).concat(" ").concat(String.valueOf(response.code())));
                        }
                    }
                });

            }
        }).start();
    }

    private void parseTimers(byte[] file) {
        try {
            if (file.length != 8198) throw new IOException("File ''timer.bin'' is not full...");
            timers = new ArrayList<>();
            for (int b = 4102, fi = 0, ti = 0; b < 7910; fi++, b += 119) {
                if (file[b] != -1) {
                    String name;
                    byte[] stringBytes = new byte[32];
                    for (int sb = 0; sb < 32; sb++) {
                        if ((file[6 + (fi * 32) + sb] & 0xff) != 0) {
                            stringBytes[sb] = file[6 + (fi * 32) + sb];
                        } else {
                            stringBytes[sb] = 32;
                        }
                    }
                    name = new String(stringBytes, "cp1251").trim();

                    boolean working = false;
                    if (file[b + 1] != -1) {
                        working = true;
                    }

                    timers.add(new Timer(fi, name, file[b], working, file[b + 2], file[b + 3], file[b + 4], file[b + 5], file[b + 6]));

                    byte[] command;
                    for (int ci = 0; ci < 8; ci++) {
                        command = new byte[14];
                        for (int cb = b + 7 + (ci * 14), cbi = 0; cb < b + 7 + (ci * 14) + 14; cbi++, cb++) {
                            command[cbi] = file[cb];
                        }
                        timers.get(ti).setCommand(ci, command);
                    }
                    ti++;
                }
            }
            setTimers(timers);
        } catch (Exception e) {
            homeActivity.writeAppLog(homeActivity.getString(R.string.some_thing_went_wrong).concat("\n").concat(NooLiteF.getStackTrace(e)));
            showSnack(homeActivity.getString(R.string.some_thing_went_wrong));
        }
    }

    private void parseTimers(Response response) throws IOException {
        timers = new ArrayList<>();
        file = response.body().bytes();
        if (file.length != 8198) throw new IOException("File ''timer.bin'' is not full...");
        for (int b = 4102, fi = 0, ti = 0; b < 7910; fi++, b += 119) {
            if (file[b] != -1) {
                String name;
                byte[] stringBytes = new byte[32];
                for (int sb = 0; sb < 32; sb++) {
                    if ((file[6 + (fi * 32) + sb] & 0xff) != 0) {
                        stringBytes[sb] = file[6 + (fi * 32) + sb];
                    } else {
                        stringBytes[sb] = 32;
                    }
                }
                name = new String(stringBytes, "cp1251").trim();

                boolean working = false;
                if (file[b + 1] != -1) {
                    working = true;
                }

                timers.add(new Timer(fi, name, file[b], working, file[b + 2], file[b + 3], file[b + 4], file[b + 5], file[b + 6]));

                byte[] command;
                for (int ci = 0; ci < 8; ci++) {
                    command = new byte[14];
                    for (int cb = b + 7 + (ci * 14), cbi = 0; cb < b + 7 + (ci * 14) + 14; cbi++, cb++) {
                        command[cbi] = file[cb];
                    }
                    timers.get(ti).setCommand(ci, command);
                }
                ti++;
            }
        }
        nooLitePRF64.setTimer(file);
        setTimers(timers);
    }

    private void setTimers(final ArrayList<Timer> timers) {
        ArrayList<Object> timersUnits = new ArrayList<>();
        timersUnits.add(new EmptyHeader());
        if (timers != null && timers.size() > 0) {
            timersUnits.addAll(timers);
            timersUnits.add(new EmptyHeader());
        }

        final ArrayList<Object> finalTimersUnits = timersUnits;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timersRecyclerAdapter = new TimersRecyclerAdapter(homeActivity, getContext(), timersFragment, client, file, finalTimersUnits);
                timersRecyclerAdapter.setOnTimerClickListener(new OnTimerClickListener() {
                    @Override
                    public void openTimerFragment(Timer timer) {
                        showTimerFragment(timer);
                    }
                });
                timersRecyclerView.setAdapter(timersRecyclerAdapter);
                setRefreshing(false);
            }
        });
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu;
        if (Settings.isNightMode()) {
            Context context = new ContextThemeWrapper(getActivity(), R.style.PopupMenuDark);
            popupMenu = new PopupMenu(context, view);
        } else {
            popupMenu = new PopupMenu(getActivity(), view);
        }
        popupMenu.inflate(R.menu.timers_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isAdded()) return false;
                switch (menuItem.getItemId()) {
                    case R.id.timers_popup_menu_item_update:
                        getTimers();
                        return true;
                    case R.id.timers_popup_menu_item_new_timer:
                        if (timers != null) showTimerFragment(null);
                        else
                            homeActivity.showSnackBar("Сначала обновите...", 0, Snackbar.LENGTH_SHORT);
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void showTimerFragment(Timer timer) {
        TimerFragment timerFragment = (TimerFragment) getChildFragmentManager().findFragmentByTag("TIMER_FRAGMENT");
        if (timerFragment == null) {
            timerFragment = new TimerFragment();
            timerFragment.setTimerFragmentListener(new TimerFragmentListener() {
                @Override
                public void onDismiss() {
                    getTimers();
                }
            });
        }
        if (timerFragment.isAdded()) return;
        timerFragment.send(client, file, timer, devices, presets);
        getChildFragmentManager().beginTransaction().add(timerFragment, "TIMER_FRAGMENT").show(timerFragment).commit();
    }

    public void showProgress() {
        setRefreshing(true);
    }

    public void hideProgress() {
        setRefreshing(false);
    }

    private void setRefreshing(final boolean refreshing) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (refreshing) blockUI();
//                else unblockUI();
                swipeRefreshLayout.setRefreshing(refreshing);
            }
        });
    }

    private void showSnack(final String message) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homeActivity.showSnackBar(message, 0, Snackbar.LENGTH_SHORT);
            }
        });
    }
}
