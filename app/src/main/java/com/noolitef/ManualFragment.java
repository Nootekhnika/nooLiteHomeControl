package com.noolitef;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ManualFragment extends DialogFragment implements View.OnClickListener {
    private Button buttonCancelTop;
    private Button buttonNextTop;
    private LinearLayout layoutRooms;
    private ImageView imageArrowRooms;
    private TextView textRooms;
    private LinearLayout layoutTitle;
    private ImageView imageArrowTitle;
    private TextView textTitle;
    private LinearLayout layoutMenu;
    private ImageView imageArrowMenu;
    private TextView textMenu;
    private TextView textMessage;
    private LinearLayout layoutHome;
    private ImageView imageArrowHome;
    private TextView textHome;
    private LinearLayout layoutRoom;
    private ImageView imageArrowRoom;
    private TextView textRoom;
    private LinearLayout layoutTimers;
    private ImageView imageArrowTimers;
    private TextView textTimers;
    private Button buttonCancelBottom;
    private Button buttonNextBottom;

    private int step;

    private HomeActivity homeActivity;

    public ManualFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        setCancelable(true);
        setRetainInstance(true);

        homeActivity = (HomeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View fragmentView = inflater.inflate(R.layout.fragment_manual, null);
        buttonCancelTop = (Button) fragmentView.findViewById(R.id.fragment_manual_button_cancel_top);
        buttonCancelTop.setOnClickListener(this);
        buttonNextTop = (Button) fragmentView.findViewById(R.id.fragment_manual_button_next_top);
        buttonNextTop.setOnClickListener(this);
        layoutRooms = (LinearLayout) fragmentView.findViewById(R.id.fragment_manual_rooms_button);
        imageArrowRooms = (ImageView) fragmentView.findViewById(R.id.fragment_manual_rooms_arrow);
        textRooms = (TextView) fragmentView.findViewById(R.id.fragment_manual_rooms_text);
        layoutTitle = (LinearLayout) fragmentView.findViewById(R.id.fragment_manual_title_layout);
        imageArrowTitle = (ImageView) fragmentView.findViewById(R.id.fragment_manual_title_arrow);
        textTitle = (TextView) fragmentView.findViewById(R.id.fragment_manual_title_text);
        layoutMenu = (LinearLayout) fragmentView.findViewById(R.id.fragment_manual_menu_button);
        imageArrowMenu = (ImageView) fragmentView.findViewById(R.id.fragment_manual_menu_arrow);
        textMenu = (TextView) fragmentView.findViewById(R.id.fragment_manual_menu_text);
        textMessage = (TextView) fragmentView.findViewById(R.id.fragment_manual_text_message);
        layoutHome = (LinearLayout) fragmentView.findViewById(R.id.fragment_manual_home_button);
        imageArrowHome = (ImageView) fragmentView.findViewById(R.id.fragment_manual_home_arrow);
        textHome = (TextView) fragmentView.findViewById(R.id.fragment_manual_home_text);
        layoutRoom = (LinearLayout) fragmentView.findViewById(R.id.fragment_manual_room_button);
        imageArrowRoom = (ImageView) fragmentView.findViewById(R.id.fragment_manual_room_arrow);
        textRoom = (TextView) fragmentView.findViewById(R.id.fragment_manual_room_text);
        layoutTimers = (LinearLayout) fragmentView.findViewById(R.id.fragment_manual_timers_button);
        imageArrowTimers = (ImageView) fragmentView.findViewById(R.id.fragment_manual_timers_arrow);
        textTimers = (TextView) fragmentView.findViewById(R.id.fragment_manual_timers_text);
        buttonCancelBottom = (Button) fragmentView.findViewById(R.id.fragment_manual_button_cancel_bottom);
        buttonCancelBottom.setOnClickListener(this);
        buttonNextBottom = (Button) fragmentView.findViewById(R.id.fragment_manual_button_next_bottom);
        buttonNextBottom.setOnClickListener(this);
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window fragmentWindow = getDialog().getWindow();
        DisplayMetrics display = new DisplayMetrics();
        homeActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        int displayWidth = display.widthPixels;
        int displayHeight = display.heightPixels;
        if (displayWidth < displayHeight) {
            fragmentWindow.setLayout(displayWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            fragmentWindow.setLayout(displayHeight, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        goToStep(step++);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_manual_button_cancel_top:
            case R.id.fragment_manual_button_cancel_bottom:
                dismiss();
                break;
            case R.id.fragment_manual_button_next_top:
            case R.id.fragment_manual_button_next_bottom:
                goToStep(step++);
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

    private void goToStep(int step) {
        buttonCancelTop.setVisibility(View.INVISIBLE);
        buttonNextTop.setVisibility(View.INVISIBLE);
        layoutRooms.setVisibility(View.VISIBLE);
        imageArrowRooms.setVisibility(View.INVISIBLE);
        textRooms.setVisibility(View.INVISIBLE);
        layoutTitle.setVisibility(View.VISIBLE);
        imageArrowTitle.setVisibility(View.INVISIBLE);
        textTitle.setVisibility(View.INVISIBLE);
        layoutMenu.setVisibility(View.VISIBLE);
        imageArrowMenu.setVisibility(View.INVISIBLE);
        textMenu.setVisibility(View.INVISIBLE);
        textMessage.setVisibility(View.INVISIBLE);
        layoutHome.setVisibility(View.VISIBLE);
        imageArrowHome.setVisibility(View.INVISIBLE);
        textHome.setVisibility(View.INVISIBLE);
        layoutRoom.setVisibility(View.VISIBLE);
        imageArrowRoom.setVisibility(View.INVISIBLE);
        textRoom.setVisibility(View.INVISIBLE);
        layoutTimers.setVisibility(View.VISIBLE);
        imageArrowTimers.setVisibility(View.INVISIBLE);
        textTimers.setVisibility(View.INVISIBLE);
        buttonCancelBottom.setVisibility(View.INVISIBLE);
        buttonNextBottom.setVisibility(View.INVISIBLE);
        switch (step) {
            case 0:
                textMessage.setVisibility(View.VISIBLE);
                buttonCancelBottom.setVisibility(View.VISIBLE);
                buttonNextBottom.setVisibility(View.VISIBLE);
                break;
            case 1:
                layoutRooms.setVisibility(View.INVISIBLE);
                imageArrowRooms.setVisibility(View.VISIBLE);
                textRooms.setVisibility(View.VISIBLE);
                buttonCancelBottom.setVisibility(View.VISIBLE);
                buttonNextBottom.setVisibility(View.VISIBLE);
                break;
            case 2:
                layoutTitle.setVisibility(View.INVISIBLE);
                imageArrowTitle.setVisibility(View.VISIBLE);
                textTitle.setVisibility(View.VISIBLE);
                buttonCancelBottom.setVisibility(View.VISIBLE);
                buttonNextBottom.setVisibility(View.VISIBLE);
                break;
            case 3:
                layoutMenu.setVisibility(View.INVISIBLE);
                imageArrowMenu.setVisibility(View.VISIBLE);
                textMenu.setVisibility(View.VISIBLE);
                buttonCancelBottom.setVisibility(View.VISIBLE);
                buttonNextBottom.setVisibility(View.VISIBLE);
                break;
            case 4:
                layoutHome.setVisibility(View.INVISIBLE);
                imageArrowHome.setVisibility(View.VISIBLE);
                textHome.setVisibility(View.VISIBLE);
                buttonCancelTop.setVisibility(View.VISIBLE);
                buttonNextTop.setVisibility(View.VISIBLE);
                break;
            case 5:
                layoutRoom.setVisibility(View.INVISIBLE);
                imageArrowRoom.setVisibility(View.VISIBLE);
                textRoom.setVisibility(View.VISIBLE);
                buttonCancelTop.setVisibility(View.VISIBLE);
                buttonNextTop.setVisibility(View.VISIBLE);
                break;
            case 6:
                layoutTimers.setVisibility(View.INVISIBLE);
                imageArrowTimers.setVisibility(View.VISIBLE);
                textTimers.setVisibility(View.VISIBLE);
                buttonCancelTop.setVisibility(View.VISIBLE);
                buttonNextTop.setVisibility(View.VISIBLE);
                break;
            case 7:
                imageArrowTitle.setImageResource(R.mipmap.ic_arrow_bottom);
                imageArrowTitle.setVisibility(View.VISIBLE);
                textTitle.setText("about_pull_to_refresh");
                textTitle.setVisibility(View.VISIBLE);
                buttonCancelBottom.setVisibility(View.VISIBLE);
                buttonNextBottom.setVisibility(View.VISIBLE);
                break;
            case 8:
                textMessage.setVisibility(View.VISIBLE);
                buttonCancelBottom.setVisibility(View.VISIBLE);
                buttonNextBottom.setVisibility(View.VISIBLE);
                break;
            case 9:
                dismiss();
        }
    }
}
