package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.R;

import java.util.Locale;
import java.util.regex.Pattern;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class SettingsControllerIPFragment extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SharedPreferences sharedPreferences;
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private Call call;
    private String ip;
    private String dns;
    private boolean useDNS;
    private boolean update;

    private Button buttonBack;
    private Button buttonSave;
    private TextView textControllerSettingsIP;
    private RadioButton radioButtonIP;
    private EditText editIP;
    private RadioButton radioButtonDNS;
    private EditText editDNS;
    private ProgressBar autoSearchProgressBar;
    private SwitchCompat switchAutoSearch;

    private boolean searchControllerThreadRunning;

    private SettingsControllerIPFragmentListener settingsControllerIPFragmentListener;

    public SettingsControllerIPFragment() {
    }

    public void send(final OkHttpClient client) {
        this.client = new OkHttpClient().newBuilder()
                .connectTimeout(250, java.util.concurrent.TimeUnit.MILLISECONDS)
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) {
                        if (response.request().header("Authorization") != null) {
                            Log.d("nooLiteF", "Authorization FAIL");
                            SettingsControllerAuthorizationFragment settingsControllerAuthorizationFragment = (SettingsControllerAuthorizationFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT");
                            if (settingsControllerAuthorizationFragment == null) {
                                settingsControllerAuthorizationFragment = new SettingsControllerAuthorizationFragment();
                                settingsControllerAuthorizationFragment.send(client);
                            }
                            if (settingsControllerAuthorizationFragment.isAdded()) return null;
                            getChildFragmentManager().beginTransaction().add(settingsControllerAuthorizationFragment, "SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT").show(settingsControllerAuthorizationFragment).commit();
                            showToast("Необходима авторизация");
                            return null;
                        }
                        Log.d("nooLiteF", "Authorization POST");
                        String credential = Credentials.basic(Settings.login(), Settings.password());
                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }
                })
                .build();
    }

    public void setSettingsControllerIPFragmentListener(SettingsControllerIPFragmentListener listener) {
        settingsControllerIPFragmentListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
        sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View fragmentView = inflater.inflate(R.layout.fragment_settings_controller_ip, null);
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_ip_button_back);
        buttonBack.setOnClickListener(this);
        buttonSave = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_ip_button_save);
        buttonSave.setOnClickListener(this);
        textControllerSettingsIP = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_ip_text_link);
        textControllerSettingsIP.setText(Html.fromHtml(
                "<a href=" + Settings.URL() + "sett_net.htm>Открыть настройки сети</a>"));
        textControllerSettingsIP.setLinkTextColor(getResources().getColor(R.color.blue));
        textControllerSettingsIP.setMovementMethod(LinkMovementMethod.getInstance());
        radioButtonIP = (RadioButton) fragmentView.findViewById(R.id.fragment_settings_controller_ip_check_box_address);
        radioButtonIP.setOnCheckedChangeListener(this);
        editIP = (EditText) fragmentView.findViewById(R.id.fragment_settings_controller_ip_edit);
        radioButtonDNS = (RadioButton) fragmentView.findViewById(R.id.fragment_settings_controller_ip_check_box_dns);
        radioButtonDNS.setOnCheckedChangeListener(this);
        editDNS = (EditText) fragmentView.findViewById(R.id.fragment_settings_controller_dns_edit);
        autoSearchProgressBar = (ProgressBar) fragmentView.findViewById(R.id.fragment_settings_controller_ip_progress_bar_auto_search);
        switchAutoSearch = (SwitchCompat) fragmentView.findViewById(R.id.fragment_settings_controller_ip_switch_auto_search);
        switchAutoSearch.setOnCheckedChangeListener(this);

        ip = sharedPreferences.getString("URL", "192.168.0.170");
        dns = sharedPreferences.getString("DNS", "noolite.nootech.dns.by:80");
        useDNS = sharedPreferences.getBoolean("useDNS", false);
        editIP.setText(ip);
        editDNS.setText(dns);
        if (useDNS) {
            radioButtonIP.setChecked(false);
            radioButtonDNS.setChecked(true);
        } else {
            radioButtonDNS.setChecked(false);
            radioButtonIP.setChecked(true);
        }

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
        switch (v.getId()) {
            case R.id.fragment_settings_controller_ip_button_back:
                dismiss();
                break;
            case R.id.fragment_settings_controller_ip_button_save:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Pattern pattern;

                if (radioButtonIP.isChecked()) {
                    String sIP = editIP.getText().toString();

                    if (sIP.length() == 0) {
                        showToast("Введите IP");
                        return;
                    }

                    if (useDNS) {
                        editor.putBoolean("useDNS", false);
                        editor.apply();
                        Settings.useDNS(false);
                    }

                    if (ip.equals(sIP)) {
                        update = useDNS;
                        dismiss();
                        return;
                    }

                    pattern = Pattern.compile("^((25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)$");
                    if (pattern.matcher(sIP).matches()) {
                        editor.putString("URL", sIP);
                        editor.apply();
                        Settings.setIP(sIP);
                        Settings.useDNS(false);
                    } else {
                        showToast("У IP-адреса неподходящий формат");
                        return;
                    }
                }

                if (radioButtonDNS.isChecked()) {
                    String sDNS = editDNS.getText().toString();

                    if (sDNS.length() == 0) {
                        showToast("Введите DNS");
                        return;
                    }

                    if (!useDNS) {
                        editor.putBoolean("useDNS", true);
                        editor.apply();
                        Settings.useDNS(true);
                    }

                    if (dns.equals(sDNS)) {
                        update = !useDNS;
                        dismiss();
                        return;
                    }

                    pattern = Pattern.compile("^([a-z0-9]+\\.){2,3}([a-z0-9]+)([:]\\d{1,5})?$");
                    if (pattern.matcher(sDNS).matches()) {
                        if (sDNS.contains(":")) {
                            int port = Integer.parseInt(sDNS.split(":")[1]);
                            if (port > 65535) {
                                showToast("Установите другой TCP/URL порт");
                                return;
                            }
                        }
                        editor.putString("DNS", sDNS);
                        editor.apply();
                        Settings.setDNS(sDNS);
                        Settings.useDNS(true);
                    } else {
                        showToast("У DNS неподходящий формат");
                        return;
                    }
                }

                update = true;
                dismiss();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.fragment_settings_controller_ip_switch_auto_search:
                searchControllerIP(isChecked);
                break;
            case R.id.fragment_settings_controller_ip_check_box_address:
                if (radioButtonIP.isChecked()) radioButtonDNS.setChecked(false);
                break;
            case R.id.fragment_settings_controller_ip_check_box_dns:
                if (radioButtonDNS.isChecked()) radioButtonIP.setChecked(false);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (settingsControllerIPFragmentListener != null)
            settingsControllerIPFragmentListener.onDismiss(update);
        super.onDismiss(dialog);
    }

    @Override
    public void onStop() {
        if (call != null) call.cancel();
        searchControllerIP(false);
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

    private void searchControllerIP(final boolean search) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editIP.setFocusable(!search);
                editIP.setFocusableInTouchMode(!search);
                if (search) {
                    searchControllerIP();
                } else {
                    searchControllerThreadRunning = false;
                    switchAutoSearch.setChecked(false);
                    autoSearchProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void searchControllerIP() {
        final WifiManager wifiManager = (WifiManager) homeActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            autoSearchProgressBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                public void run() {
                    searchControllerThreadRunning = true;
                    String wifiIP = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                    String[] splittedIP = wifiIP.split("\\.");
                    String localIP = String.format(Locale.ROOT, "%s.%s.%s.", splittedIP[0], splittedIP[1], splittedIP[2]);
                    setIP(localIP + "170");
                    if (getController(localIP + "170")) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        searchControllerIP(false);
                        showToast("Поиск контроллера завершен");
                        return;
                    } else {
                        for (int controllerIP = 0; controllerIP < 256; controllerIP++) {
                            setIP(localIP + controllerIP);
                            if (getController(localIP + controllerIP)) {
                                searchControllerIP(false);
                                showToast("Поиск контроллера завершен");
                                return;
                            }
                            if (!searchControllerThreadRunning) return;
                        }
                    }
                    searchControllerIP(false);
                    showToast("Контроллер не найден");
                }
            }).start();
        } else {
            searchControllerIP(false);
            showToast("Для автоматического поиска URL контроллера включите Wi-Fi");
        }
    }

    private boolean getController(String ip) {
        try {
            Request request = new Request.Builder()
                    .url(String.format("http://%s/sysinfo.htm", ip))
                    .build();
            call = client.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()) {
                if (response.body().string().substring(0, 6).equals("PRF-64")) {
                    response.close();
                    call.cancel();
                    return true;
                } else {
                    response.close();
                    call.cancel();
                    return false;
                }
            } else {
                response.close();
                call.cancel();
                return false;
            }
        } catch (Exception e) {
            call.cancel();
            return false;
        }
    }

    private void setIP(final String ip) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editIP.setText(ip);
            }
        });
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
