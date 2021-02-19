package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.R;

import okhttp3.OkHttpClient;

public class SettingsControllerFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;
    SharedPreferences sharedPreferences;
    private OkHttpClient client;

    private int developerModeStep;
    private long developerModeActivateTimeout;

    private LinearLayout layoutPRF;
    private Button buttonBack;
    private Button buttonIPaddress;
    private Button buttonAuthorization;
    private Button buttonDateTime;
    private Button buttonBackup;
    private Button buttonClearCache;
    private Button buttonClearLog;
    private Button buttonAppLog;
    private Button buttonAboutApp;

    public SettingsControllerFragment() {
    }

    public void send(OkHttpClient client) {
        this.client = client;
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
        View fragmentView = inflater.inflate(R.layout.fragment_settings_controller, null);
        layoutPRF = fragmentView.findViewById(R.id.fragment_settings_controller_layout_prf);
        layoutPRF.setOnClickListener(this);
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_back);
        buttonBack.setOnClickListener(this);
        buttonIPaddress = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_ip_address);
        buttonIPaddress.setOnClickListener(this);
        buttonAuthorization = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_authorization);
        buttonAuthorization.setOnClickListener(this);
        buttonDateTime = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_date_time);
        buttonDateTime.setOnClickListener(this);
        buttonBackup = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_backup);
        buttonBackup.setOnClickListener(this);
        buttonClearCache = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_clear_app_cache);
        buttonClearCache.setOnClickListener(this);
        buttonClearLog = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_clear_log);
        buttonClearLog.setOnClickListener(this);
        buttonAppLog = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_app_log);
        buttonAppLog.setOnClickListener(this);
        if (sharedPreferences.getBoolean("DeveloperMode", false)) {
            developerModeStep = 0;
            buttonAppLog.setVisibility(View.VISIBLE);
        }
        buttonAboutApp = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_button_about_app);
        buttonAboutApp.setOnClickListener(this);
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
            case R.id.fragment_settings_controller_button_back:
                dismiss();
                break;
            case R.id.fragment_settings_controller_layout_prf:
                developerMode();
                break;
            case R.id.fragment_settings_controller_button_ip_address:
                SettingsControllerIPFragment settingsControllerIPFragment = (SettingsControllerIPFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_IP_FRAGMENT");
                if (settingsControllerIPFragment == null) {
                    settingsControllerIPFragment = new SettingsControllerIPFragment();
                    settingsControllerIPFragment.send(client);
                    settingsControllerIPFragment.setSettingsControllerIPFragmentListener(new SettingsControllerIPFragmentListener() {
                        @Override
                        public void onDismiss(boolean update) {
                            if (update) homeActivity.getUnitsState();
                        }
                    });
                }
                if (settingsControllerIPFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(settingsControllerIPFragment, "SETTINGS_CONTROLLER_IP_FRAGMENT").show(settingsControllerIPFragment).commit();
                break;
            case R.id.fragment_settings_controller_button_authorization:
                SettingsControllerAuthorizationFragment settingsControllerAuthorizationFragment = (SettingsControllerAuthorizationFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT");
                if (settingsControllerAuthorizationFragment == null) {
                    settingsControllerAuthorizationFragment = new SettingsControllerAuthorizationFragment();
                    settingsControllerAuthorizationFragment.send(client);
                }
                if (settingsControllerAuthorizationFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(settingsControllerAuthorizationFragment, "SETTINGS_CONTROLLER_AUTHORIZATION_FRAGMENT").show(settingsControllerAuthorizationFragment).commit();
                break;
            case R.id.fragment_settings_controller_button_date_time:
                SettingsControllerDateTimeFragment settingsControllerDateTimeFragment = (SettingsControllerDateTimeFragment) getChildFragmentManager().findFragmentByTag("SETTINGS_CONTROLLER_DATE_TIME_FRAGMENT");
                if (settingsControllerDateTimeFragment == null) {
                    settingsControllerDateTimeFragment = new SettingsControllerDateTimeFragment();
                    settingsControllerDateTimeFragment.send(client);
                }
                if (settingsControllerDateTimeFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(settingsControllerDateTimeFragment, "SETTINGS_CONTROLLER_DATE_TIME_FRAGMENT").show(settingsControllerDateTimeFragment).commit();
                break;
            case R.id.fragment_settings_controller_button_backup:
                SettingsControllerBackupFragment settingsControllerBackupFragment = (SettingsControllerBackupFragment) getChildFragmentManager().findFragmentByTag("BACKUP_FRAGMENT");
                if (settingsControllerBackupFragment == null) {
                    settingsControllerBackupFragment = new SettingsControllerBackupFragment();
                    settingsControllerBackupFragment.send(client);
                }
                if (settingsControllerBackupFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(settingsControllerBackupFragment, "BACKUP_FRAGMENT").show(settingsControllerBackupFragment).commit();
                break;
            case R.id.fragment_settings_controller_button_clear_app_cache:
                ClearAppCacheDialog clearAppCacheDialog = (ClearAppCacheDialog) getChildFragmentManager().findFragmentByTag("CLEAR_APP_CACHE_DIALOG");
                if (clearAppCacheDialog == null) {
                    clearAppCacheDialog = new ClearAppCacheDialog();
                    clearAppCacheDialog.send(client);
                }
                if (clearAppCacheDialog.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(clearAppCacheDialog, "CLEAR_APP_CACHE_DIALOG").show(clearAppCacheDialog).commit();
                break;
            case R.id.fragment_settings_controller_button_clear_log:
                ClearLogDialog clearLogDialog = (ClearLogDialog) getChildFragmentManager().findFragmentByTag("CLEAR_LOG_DIALOG");
                if (clearLogDialog == null) {
                    clearLogDialog = new ClearLogDialog();
                    clearLogDialog.send(client);
                }
                if (clearLogDialog.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(clearLogDialog, "CLEAR_LOG_DIALOG").show(clearLogDialog).commit();
                break;
            case R.id.fragment_settings_controller_button_app_log:
                AppLogFragment appLogFragment = (AppLogFragment) getChildFragmentManager().findFragmentByTag("APP_LOG_FRAGMENT");
                if (appLogFragment == null) {
                    appLogFragment = new AppLogFragment();
                }
                if (appLogFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(appLogFragment, "APP_LOG_FRAGMENT").show(appLogFragment).commit();
                break;
            case R.id.fragment_settings_controller_button_about_app:
                AboutAppFragment aboutAppFragment = (AboutAppFragment) getChildFragmentManager().findFragmentByTag("ABOUT_APP_FRAGMENT");
                if (aboutAppFragment == null) {
                    aboutAppFragment = new AboutAppFragment();
                }
                if (aboutAppFragment.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(aboutAppFragment, "ABOUT_APP_FRAGMENT").show(aboutAppFragment).commit();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private void developerMode() {
        switch (++developerModeStep) {
            case 1:
                developerModeActivateTimeout = System.currentTimeMillis();
                break;
            case 2:
                if (developerModeActivateTimeout + 1000 < System.currentTimeMillis()) {
                    developerModeStep = 0;
                }
                break;
            case 3:
                if (developerModeActivateTimeout + 1000 > System.currentTimeMillis()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (Settings.isDeveloperMode()) {
                        developerModeDeactivate();
                        editor.putBoolean("DeveloperMode", false);
                    } else {
                        developerModeActivate();
                        editor.putBoolean("DeveloperMode", true);
                    }
                    editor.apply();
                    developerModeStep = 0;
                } else {
                    developerModeStep = 0;
                }
                break;
        }
    }

    private void developerModeActivate() {
        Settings.setDeveloperMode(true);
        buttonAppLog.setVisibility(View.VISIBLE);
        showToast("Developer mode activated");
    }

    private void developerModeDeactivate() {
        Settings.setDeveloperMode(false);
        buttonAppLog.setVisibility(View.GONE);
        showToast("Developer mode deactivated");
    }

    private void showToast(final String message) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
