package com.noolitef.settings;

import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.BuildConfig;
import com.noolitef.HomeActivity;
import com.noolitef.R;

public class AboutAppFragment extends DialogFragment implements View.OnClickListener {
    private HomeActivity homeActivity;

    private Button buttonBack;
    private LinearLayout layoutAboutApp;
    private TextView textVersion;
    private TextView textUsedLibraries;
    private TextView textEsterEgg;

    private byte click;

    public AboutAppFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();
        click = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View fragmentView = inflater.inflate(R.layout.fragment_about_app, null);
        buttonBack = (Button) fragmentView.findViewById(R.id.fragment_about_app_button_back);
        buttonBack.setOnClickListener(this);
        layoutAboutApp = (LinearLayout) fragmentView.findViewById(R.id.fragment_about_app_layout);
        layoutAboutApp.setOnClickListener(this);
        textVersion = (TextView) fragmentView.findViewById(R.id.fragment_about_app_text_version);
        textVersion.setText(String.format("Версия %s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        textUsedLibraries = fragmentView.findViewById(R.id.fragment_about_app_text_used_libraries);
        textUsedLibraries.setOnClickListener(this);
        textEsterEgg = (TextView) fragmentView.findViewById(R.id.fragment_about_app_text_ester_egg);
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
            case R.id.fragment_about_app_button_back:
                dismiss();
                break;
            case R.id.fragment_about_app_text_used_libraries:
                AboutLibrariesDialog aboutLibrariesDialog = (AboutLibrariesDialog) getChildFragmentManager().findFragmentByTag("ABOUT_LIBRARIES_DIALOG");
                if (aboutLibrariesDialog == null) {
                    aboutLibrariesDialog = new AboutLibrariesDialog();
                }
                if (aboutLibrariesDialog.isAdded()) return;
                getChildFragmentManager().beginTransaction().add(aboutLibrariesDialog, "ABOUT_LIBRARIES_DIALOG").show(aboutLibrariesDialog).commit();
                break;
            case R.id.fragment_about_app_layout:
                switch (++click) {
                    case 3:
                        showToast("Возможно здесь что-то скрыто...");
                        break;
                    case 32:
                        showToast("Осталось совсем немного, поднажмите...");
                        break;
                    case 64:
                        textEsterEgg.setVisibility(View.VISIBLE);
                        break;
                    case 96:
                        showToast("Пожалуй достаточно...");
                        break;
                    case 127:
                        click = 65;
                        showToast("Больше ничего нет...");
                        break;
                }
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

    private void showToast(final String message) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
