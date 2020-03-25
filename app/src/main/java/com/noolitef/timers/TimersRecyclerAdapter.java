package com.noolitef.timers;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noolitef.EmptyHeader;
import com.noolitef.HomeActivity;
import com.noolitef.R;
import com.noolitef.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface OnTimerClickListener {
    void openTimerFragment(Timer timer);
}

class TimersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class EmptyHeaderViewHolder extends RecyclerView.ViewHolder {
        EmptyHeaderViewHolder(View item) {
            super(item);
        }
    }

    class TimerViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout card;
        private TextView name;
        private TextView index;
        private LinearLayout timeOnLayout;
        private TextView timeOnLabel;
        private LinearLayout timeOffLayout;
        private TextView timeOffLabel;
        private TextView monday;
        private TextView tuesday;
        private TextView wednesday;
        private TextView thursday;
        private TextView friday;
        private TextView saturday;
        private TextView sunday;
        private SwitchCompat switcher;

        TimerViewHolder(View item) {
            super(item);
            card = item.findViewById(R.id.card_view_timer);
            name = item.findViewById(R.id.card_view_timer_name);
            index = item.findViewById(R.id.card_view_timer_index);
            timeOnLayout = item.findViewById(R.id.card_view_timer_on_layout);
            timeOnLabel = item.findViewById(R.id.card_view_timer_on_label);
            timeOffLayout = item.findViewById(R.id.card_view_timer_off_layout);
            timeOffLabel = item.findViewById(R.id.card_view_timer_off_label);
            monday = item.findViewById(R.id.card_view_timer_monday);
            tuesday = item.findViewById(R.id.card_view_timer_tuesday);
            wednesday = item.findViewById(R.id.card_view_timer_wednesday);
            thursday = item.findViewById(R.id.card_view_timer_thursday);
            friday = item.findViewById(R.id.card_view_timer_friday);
            saturday = item.findViewById(R.id.card_view_timer_saturday);
            sunday = item.findViewById(R.id.card_view_timer_sunday);
            switcher = item.findViewById(R.id.card_view_timer_switch);
        }

        void setClick(View.OnClickListener listener) {
            card.setOnClickListener(listener);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setIndex(int index) {
            this.index.setText(String.format(Locale.ROOT, "[%02d]", index));
            this.index.setVisibility(View.VISIBLE);
        }

        void setOnTime(int hour, int minute) {
            timeOnLabel.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute));
            timeOnLayout.setVisibility(View.VISIBLE);
        }

        void setOffTime(int hour, int minute) {
            timeOffLabel.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute));
            timeOffLayout.setVisibility(View.VISIBLE);
        }

        void setWorkDays(int workDays) {
            String weekDay = String.format("%8s", Integer.toBinaryString(workDays & 0xFF)).replace(' ', '0');

            if (Settings.isNightMode()) {
                if (weekDay.substring(6, 7).equals("1")) {
                    monday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    monday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    monday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    monday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(5, 6).equals("1")) {
                    tuesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    tuesday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    tuesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    tuesday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(4, 5).equals("1")) {
                    wednesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    wednesday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    wednesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    wednesday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(3, 4).equals("1")) {
                    thursday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    thursday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    thursday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    thursday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(2, 3).equals("1")) {
                    friday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    friday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    friday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    friday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(1, 2).equals("1")) {
                    saturday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    saturday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    saturday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    saturday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(0, 1).equals("1")) {
                    sunday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    sunday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected_light);
                } else {
                    sunday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    sunday.setBackgroundColor(Color.TRANSPARENT);
                }
            } else {
                if (weekDay.substring(6, 7).equals("1")) {
                    monday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    monday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    monday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    monday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(5, 6).equals("1")) {
                    tuesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    tuesday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    tuesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    tuesday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(4, 5).equals("1")) {
                    wednesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    wednesday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    wednesday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    wednesday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(3, 4).equals("1")) {
                    thursday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    thursday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    thursday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    thursday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(2, 3).equals("1")) {
                    friday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    friday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    friday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    friday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(1, 2).equals("1")) {
                    saturday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    saturday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    saturday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    saturday.setBackgroundColor(Color.TRANSPARENT);
                }

                if (weekDay.substring(0, 1).equals("1")) {
                    sunday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
                    sunday.setBackgroundResource(R.drawable.card_view_timer_week_day_selected);
                } else {
                    sunday.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
                    sunday.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }

        void setWorkingState(boolean working) {
            switcher.setChecked(working);
        }

        void setSwitchChange(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
            this.switcher.setOnCheckedChangeListener(onCheckedChangeListener);
        }
    }

    private static final int EMPTY_HEADER = 0;
    private static final int TIMER = 1;

    private HomeActivity homeActivity;
    private Context context;
    private TimersFragment timersFragment;
    private OkHttpClient client;
    private byte[] file;
    private ArrayList<Object> timers;

    private OnTimerClickListener onTimerClickListener;

    TimersRecyclerAdapter(HomeActivity homeActivity, Context context, TimersFragment timersFragment, OkHttpClient client, byte[] file, ArrayList<Object> timers) {
        this.homeActivity = homeActivity;
        this.context = context;
        this.timersFragment = timersFragment;
        this.client = client;
        this.file = file;
        this.timers = timers;
        if (this.timers == null) this.timers = new ArrayList<>();
        setHasStableIds(true);
    }

    void setOnTimerClickListener(OnTimerClickListener listener) {
        onTimerClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return timers.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (timers.get(position) instanceof EmptyHeader) return EMPTY_HEADER;
        if (timers.get(position) instanceof Timer) return TIMER;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view;
        switch (type) {
            case EMPTY_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_timers_empty_header, parent, false);
                return new EmptyHeaderViewHolder(view);
            case TIMER:
                if (Settings.isNightMode()) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_timer_dark, parent, false);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_timer, parent, false);
                }
                return new TimerViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case EMPTY_HEADER:
                bindEmptyHeader(holder, position);
                break;
            case TIMER:
                bindTimer(holder, position);
                break;
        }
    }

    private void bindEmptyHeader(RecyclerView.ViewHolder holder, int position) {
    }

    private void bindTimer(RecyclerView.ViewHolder holder, int position) {
        TimerViewHolder timerViewHolder = (TimerViewHolder) holder;
        final Timer timer = (Timer) timers.get(position);
        timerViewHolder.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimerClickListener.openTimerFragment(timer);
            }
        });
        timerViewHolder.setName(String.valueOf(timer.getName()));
        if (Settings.isDeveloperMode()) {
            timerViewHolder.setIndex(timer.getIndex());
        }
        switch (timer.getType()) {
            case 0:
                switch (timer.getCommand(0)[0] & 0xFF) {
                    case 0:
                    case 2:
                        switch (timer.getCommand(0)[4] & 0xFF) {
                            case 0:
                                timerViewHolder.setOffTime(timer.getOnHour(), timer.getOnMinute());
                                break;
                            case 2:
                                timerViewHolder.setOnTime(timer.getOnHour(), timer.getOnMinute());
                                break;
                            case 13:
                                switch (timer.getCommand(0)[6] & 0xFF) {
                                    case 1:
                                        timerViewHolder.setOnTime(timer.getOnHour(), timer.getOnMinute());
                                        break;
                                    case 255:
                                        timerViewHolder.setOffTime(timer.getOnHour(), timer.getOnMinute());
                                        break;
                                }
                                break;
                        }
                        break;
                    case 254:
                        timerViewHolder.setOnTime(timer.getOnHour(), timer.getOnMinute());
                        break;
                }
                break;
            case 1:
                timerViewHolder.setOnTime(timer.getOnHour(), timer.getOnMinute());
                timerViewHolder.setOffTime(timer.getOffHour(), timer.getOffMinute());
                break;
        }
        timerViewHolder.setWorkDays(timer.getWorkDays());
        timerViewHolder.setWorkingState(timer.isWorking());
        timerViewHolder.setSwitchChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                timer.setWorking(checked);
                changeTimerState(timer);
            }
        });
        timerViewHolder.setIsRecyclable(false);
    }

    private void changeTimerState(final Timer timer) {
        timersFragment.showProgress();
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (timer.isWorking()) {
                        file[4102 + (timer.getIndex() * 119) + 1] = 0;
                    } else {
                        file[4102 + (timer.getIndex() * 119) + 1] = -1;
                    }

                    uploadFile();
                } catch (Exception e) {
                    showSnack("Что-то пошло не так...");
                }
            }
        }).start();
    }

    private void uploadFile() throws IOException {
        String body = "\r\n\r\nContent-Disposition: form-data; name=\"timer\"; filename=\"timer.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .concat(new String(file, "cp1251"))
                .concat("\r\n\r\n\r\n");
        Request request = new Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.getBytes("cp1251")))
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.isSuccessful()) {
            response.close();
            call.cancel();
            done();
        } else {
            response.close();
            call.cancel();
            showSnack("Ошибка соединения " + response.code());
        }
    }

    private void done() {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timersFragment.hideProgress();
            }
        });
    }

    private void showSnack(final String message) {
        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timersFragment.hideProgress();
                homeActivity.showSnackBar(message, 0, Snackbar.LENGTH_SHORT);
            }
        });
    }
}
