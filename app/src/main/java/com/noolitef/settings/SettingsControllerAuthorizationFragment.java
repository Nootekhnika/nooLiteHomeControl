package com.noolitef.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.HomeActivity;
import com.noolitef.R;

import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

public class SettingsControllerAuthorizationFragment extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SharedPreferences sharedPreferences;
    private HomeActivity homeActivity;
    private OkHttpClient client;

    private Button buttonBack;
    private Button buttonSave;
    private TextView textControllerSettingsAuthorization;
    private SwitchCompat switchAuthorization;
    private TextView textLogin;
    private EditText editLogin;
    private TextView textPassword;
    private EditText editPassword;

    private SettingsControllerAuthorizationFragmentListener dismissListener;

    public SettingsControllerAuthorizationFragment() {
    }

    public void send(OkHttpClient client) {
        this.client = client;
    }

    public void setDismissListener(SettingsControllerAuthorizationFragmentListener listener) {
        dismissListener = listener;
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
        View fragmentView = inflater.inflate(R.layout.fragment_settings_controller_authorization, null);
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_button_back);
        buttonBack.setOnClickListener(this);
        buttonSave = (Button) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_button_save);
        buttonSave.setOnClickListener(this);
        textControllerSettingsAuthorization = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_text_link);
        textControllerSettingsAuthorization.setText(Html.fromHtml(
                "<a href=" + Settings.URL() + "pass.htm>Открыть настройки авторизации</a>"));
        textControllerSettingsAuthorization.setLinkTextColor(getResources().getColor(R.color.blue));
        textControllerSettingsAuthorization.setMovementMethod(LinkMovementMethod.getInstance());
        textLogin = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_text_login);
        editLogin = (EditText) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_edit_login);
        textPassword = (TextView) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_text_password);
        editPassword = (EditText) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_edit_password);
        switchAuthorization = (SwitchCompat) fragmentView.findViewById(R.id.fragment_settings_controller_authorization_switch);
        switchAuthorization.setOnCheckedChangeListener(this);

        setAuthorization(sharedPreferences.getBoolean("Authorization", false));
        editLogin.setText(sharedPreferences.getString("Login", Settings.login()));
        editPassword.setText(sharedPreferences.getString("Password", Settings.password()));

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_settings_controller_authorization_button_back:
                dismiss();
                break;
            case R.id.fragment_settings_controller_authorization_button_save:
                saveAuthorizationSettings();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setAuthorization(isChecked);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dismissListener != null) dismissListener.onDismiss();
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

    private void setAuthorization(boolean on) {
        switchAuthorization.setChecked(on);
        if (on) {
            textLogin.setTextColor(getResources().getColor(R.color.black_light));
            editLogin.setFocusable(true);
            editLogin.setFocusableInTouchMode(true);
            editLogin.setTextColor(getResources().getColor(R.color.black_light));
            editLogin.setBackgroundResource(R.drawable.edit_text_background);

            textPassword.setTextColor(getResources().getColor(R.color.black_light));
            editPassword.setFocusable(true);
            editPassword.setFocusableInTouchMode(true);
            editPassword.setTextColor(getResources().getColor(R.color.black_light));
            editPassword.setBackgroundResource(R.drawable.edit_text_background);
        } else {
            textLogin.setTextColor(getResources().getColor(R.color.grey));
            editLogin.setFocusable(false);
            editLogin.setFocusableInTouchMode(false);
            editLogin.setTextColor(getResources().getColor(R.color.grey));
            editLogin.setBackgroundResource(R.drawable.edit_text_background_disable);

            textPassword.setTextColor(getResources().getColor(R.color.grey));
            editPassword.setFocusable(false);
            editPassword.setFocusableInTouchMode(false);
            editPassword.setTextColor(getResources().getColor(R.color.grey));
            editPassword.setBackgroundResource(R.drawable.edit_text_background_disable);
        }
    }

    private void saveAuthorizationSettings() {
        String login = editLogin.getText().toString();
        String password = editPassword.getText().toString();
        if (switchAuthorization.isChecked()) {
            Pattern pattern = Pattern.compile("^[0-9a-zA-Z]*$");
            if (!pattern.matcher(login).matches()) {
                showToast("Логин должен содержать только цифры и символы латинского алфавита");
                return;
            }
            if (!pattern.matcher(password).matches()) {
                showToast("Пароль должен содержать только цифры и символы латинского алфавита");
                return;
            }

            Settings.setLogin(login);
            Settings.setPassword(password);
        } else {
            Settings.setLogin("");
            Settings.setPassword("");
        }
        SharedPreferences sharedPreferences = homeActivity.getSharedPreferences("nooLite", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("Authorization", switchAuthorization.isChecked());
        editor.putString("Login", login);
        editor.putString("Password", password);
        editor.apply();
        dismiss();
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
