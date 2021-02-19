package com.noolitef.rx;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.noolitef.DateSetterDialog;
import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.OnDateSetListener;
import com.noolitef.R;
import com.noolitef.Room;
import com.noolitef.UnitSettingsFragment;
import com.noolitef.UnitSettingsFragmentListener;
import com.noolitef.settings.Settings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.OkHttpClient;

public class GraphLogFragment extends DialogFragment implements OnDateSetListener {
    private OkHttpClient client;
    private DisplayMetrics display;
    private HomeActivity homeActivity;
    private NooLiteF nooLiteF;
    private byte[] device;
    private byte[] user;
    private ArrayList<Room> rooms;
    private Object sensor;
    private TemperatureSensor temperatureSensor;
    private HumidityTemperatureSensor humidityTemperatureSensor;
    private GraphLog graphLog;
    private Calendar calendar;

    private int year;
    private int month;
    private int date;

    private Button buttonBack;
    private ProgressBar progressBar;
    private ImageView temperatureIcon;
    private TextView temperatureLabel;
    private TextView temperaturePlotLabel;
    private ImageView humidityIcon;
    private TextView humidityLabel;
    private TextView humidityPlotLabel;
    private TextView sensorRoom;
    private TextView sensorName;
    private ImageButton settingsButton;
    private Spinner spinner;
    private Button dateButton;
    private ImageView temperatureLogImage;
    private ImageView humidityTemperatureLogImage;
    private ArrayAdapter spinnerAdapter;

    public GraphLogFragment() {
    }

    public void send(OkHttpClient client, NooLiteF nooLiteF, byte[] device, byte[] user, ArrayList<Room> rooms, Object sensor) {
        this.client = client;
        this.device = device;
        this.rooms = rooms;
        this.nooLiteF = nooLiteF;
        this.user = user;
        this.sensor = sensor;
        if (sensor instanceof TemperatureSensor) {
            temperatureSensor = (TemperatureSensor) sensor;
        }
        if (sensor instanceof HumidityTemperatureSensor) {
            humidityTemperatureSensor = (HumidityTemperatureSensor) sensor;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(true);

        homeActivity = (HomeActivity) getActivity();

        display = new DisplayMetrics();
        homeActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        graphLog = new GraphLog(getContext(), display);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);

        setGraphLogListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView;
        if (Settings.isNightMode()) {
            fragmentView = inflater.inflate(R.layout.fragment_graph_log_dark, null);
        } else {
            fragmentView = inflater.inflate(R.layout.fragment_graph_log, null);
        }
        buttonBack = (Button) fragmentView.findViewById(R.id.dialog_graph_log_button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.dialog_graph_log_progress_bar);
        //progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
        temperatureIcon = (ImageView) fragmentView.findViewById(R.id.dialog_graph_log_icon_temperature);
        temperatureLabel = (TextView) fragmentView.findViewById(R.id.dialog_graph_log_label_temperature);
        temperaturePlotLabel = (TextView) fragmentView.findViewById(R.id.dialog_graph_log_plot_label_temperature);
        humidityIcon = (ImageView) fragmentView.findViewById(R.id.dialog_graph_log_icon_humidity);
        humidityLabel = (TextView) fragmentView.findViewById(R.id.dialog_graph_log_label_humidity);
        humidityPlotLabel = (TextView) fragmentView.findViewById(R.id.dialog_graph_log_plot_label_humidity);
        sensorRoom = (TextView) fragmentView.findViewById(R.id.dialog_graph_log_label_room);
        sensorName = (TextView) fragmentView.findViewById(R.id.dialog_graph_log_label_name);
        settingsButton = (ImageButton) fragmentView.findViewById(R.id.dialog_graph_log_button_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UnitSettingsFragment unitSettingsFragment = (UnitSettingsFragment) getChildFragmentManager().findFragmentByTag("UNIT_SETTINGS_DIALOG");
                if (unitSettingsFragment == null) {
                    unitSettingsFragment = new UnitSettingsFragment();
                    unitSettingsFragment.setUnitSettingsFragmentListener(new UnitSettingsFragmentListener() {
                        @Override
                        public void onDismiss(final boolean unbind, final boolean update, final String room, final String name) {
                            if (!unbind) {
                                if (update) {
                                    homeActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sensorRoom.setText(room);
                                            sensorName.setText(name);
                                            homeActivity.showProgressBar();
                                            homeActivity.hardUpdatePRF64();
                                        }
                                    });
                                }
                            } else {
                                homeActivity.showProgressBar();
                                homeActivity.hardUpdatePRF64();
                                dismiss();
                            }
                        }
                    });
                }
                if (unitSettingsFragment.isAdded()) return;
                unitSettingsFragment.send(client, device, user, rooms, sensor);
                getChildFragmentManager().beginTransaction().add(unitSettingsFragment, "UNIT_SETTINGS_DIALOG").show(unitSettingsFragment).commit();
            }
        });
        spinner = (Spinner) fragmentView.findViewById(R.id.dialog_graph_log_spinner_range);
        dateButton = (Button) fragmentView.findViewById(R.id.dialog_graph_log_button_date);
        dateButton.setText(String.format(Locale.ROOT, "%02d.%02d.%02d", date, month, year));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateSetterDialog dateSetterDialog = (DateSetterDialog) getChildFragmentManager().findFragmentByTag("DATE_SETTER_DIALOG");
                if (dateSetterDialog == null) {
                    dateSetterDialog = new DateSetterDialog();
                }
                if (dateSetterDialog.isAdded()) return;
                dateSetterDialog.setDate(year, month, date);
                getChildFragmentManager().beginTransaction().add(dateSetterDialog, "dateSetterDialog").show(dateSetterDialog).commit();
            }
        });
        temperatureLogImage = (ImageView) fragmentView.findViewById(R.id.dialog_graph_log_image_temperature);
        humidityTemperatureLogImage = (ImageView) fragmentView.findViewById(R.id.dialog_graph_log_image_humidity);
        if (Settings.isNightMode()) {
            spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.timeRange, R.layout.simple_spinner_drop_down_item_grey);
        } else {
            spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.timeRange, android.R.layout.simple_spinner_dropdown_item);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                drawGraph(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);

        if (temperatureSensor != null) {
            sensorRoom.setText(temperatureSensor.getRoom());
            sensorName.setText(temperatureSensor.getName());
        }
        if (humidityTemperatureSensor != null) {
            sensorRoom.setText(humidityTemperatureSensor.getRoom());
            sensorName.setText(humidityTemperatureSensor.getName());
        }

        getDialog().setCanceledOnTouchOutside(true);
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
    public void onDismiss(DialogInterface dialog) {
        if (nooLiteF != null) {
            nooLiteF.interruptThread();
        }

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

    private void setGraphLogListener() {
        if (nooLiteF == null) { // костыль для корректного сворачивания
            dismiss();
            return;
        }
        nooLiteF.setGraphLogListener(new GraphLogListener() {
            @Override
            public void temperatureLog(final boolean successfully, final int range, final ArrayList<TemperatureUnit> temperatureUnits) {
                if (getDialog() != null) {
                    final ArrayList<TemperatureUnit> sortedTemperatureUnits = sortTemperatureUnits(range, temperatureUnits);
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (temperatureUnits != null && temperatureUnits.size() > 0) {
                                temperatureLabel.setText(String.format(Locale.ROOT, "%.1f°C", temperatureUnits.get(0).getTemperature()));
                                if (temperatureUnits.size() > 0 && (Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(temperatureUnits.get(0).getYear(), temperatureUnits.get(0).getMonth(), temperatureUnits.get(0).getDay(), temperatureUnits.get(0).getHour(), temperatureUnits.get(0).getMinute(), temperatureUnits.get(0).getSecond())) > 10800000) {
                                    temperatureLabel.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                                }
                                temperaturePlotLabel.setVisibility(View.VISIBLE);
                                temperatureLogImage.setImageBitmap(graphLog.drawTemperatureLog(range, sortedTemperatureUnits));
                                if (!successfully) {
                                    Toast.makeText(getContext(), "Ошибка соединения...", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Toast.makeText(getContext(), "Нет данных...", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            spinner.setEnabled(true);
                            dateButton.setEnabled(true);
                        }
                    });
                }
            }

            @Override
            public void humidityTemperatureLog(final boolean successfully, final int range, final ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits) {
                if (getDialog() != null) {
                    final ArrayList<HumidityTemperatureUnit> sortedHumidityTemperatureUnits = sortHumidityTemperatureUnits(range, humidityTemperatureUnits);
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (humidityTemperatureUnits != null && humidityTemperatureUnits.size() > 0) {
                                temperatureLabel.setText(String.format(Locale.ROOT, "%.1f°C", humidityTemperatureUnits.get(0).getTemperature()));
                                temperaturePlotLabel.setVisibility(View.VISIBLE);
                                humidityIcon.setVisibility(View.VISIBLE);
                                humidityLabel.setVisibility(View.VISIBLE);
                                humidityLabel.setText(String.format(Locale.ROOT, "%d%%", humidityTemperatureUnits.get(0).getHumidity()));
                                if (humidityTemperatureUnits.size() > 0 && (Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(humidityTemperatureUnits.get(0).getYear(), humidityTemperatureUnits.get(0).getMonth(), humidityTemperatureUnits.get(0).getDay(), humidityTemperatureUnits.get(0).getHour(), humidityTemperatureUnits.get(0).getMinute(), humidityTemperatureUnits.get(0).getSecond())) > 10800000) {
                                    temperatureLabel.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                                    humidityLabel.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.grey))));
                                }
                                humidityPlotLabel.setVisibility(View.VISIBLE);
                                temperatureLogImage.setImageBitmap(graphLog.drawTemperatureLog(range, sortedHumidityTemperatureUnits));
                                humidityTemperatureLogImage.setVisibility(View.VISIBLE);
                                humidityTemperatureLogImage.setImageBitmap(graphLog.drawHumidityLog(range, sortedHumidityTemperatureUnits));
                                if (!successfully) {
                                    Toast.makeText(getContext(), "Ошибка соединения...", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Toast.makeText(getContext(), "Нет данных...", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            spinner.setEnabled(true);
                            dateButton.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    private void drawGraph(int position) {
        spinner.setEnabled(false);
        dateButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        switch (position) {
            case 0:
                if (temperatureSensor != null) {
                    nooLiteF.getTemperatureLog(GraphLog.DAY, temperatureSensor);
                }
                if (humidityTemperatureSensor != null) {
                    nooLiteF.getHumidityTemperatureLog(GraphLog.DAY, humidityTemperatureSensor);
                }
                break;
            case 1:
                if (temperatureSensor != null) {
                    nooLiteF.getTemperatureLog(GraphLog.WEEK, temperatureSensor);
                }
                if (humidityTemperatureSensor != null) {
                    nooLiteF.getHumidityTemperatureLog(GraphLog.WEEK, humidityTemperatureSensor);
                }
                break;
        }
    }

    private ArrayList<TemperatureUnit> sortTemperatureUnits(int range, ArrayList<TemperatureUnit> temperatureUnits) {
        Calendar calendar = Calendar.getInstance();
        ArrayList<TemperatureUnit> sortedTemperatureUnits = new ArrayList<>();
        switch (range) {
            case GraphLog.DAY:
                for (TemperatureUnit temperatureUnit : temperatureUnits) {
                    if (2000 + temperatureUnit.getYear() == year && temperatureUnit.getMonth() == month && temperatureUnit.getDay() == date) {
                        sortedTemperatureUnits.add(temperatureUnit);
                    }
                }
                break;
            case GraphLog.WEEK:
                calendar.set(year, month - 1, date);
                int week = calendar.get(Calendar.WEEK_OF_YEAR);
                for (TemperatureUnit temperatureUnit : temperatureUnits) {
                    calendar.set(2000 + temperatureUnit.getYear(), temperatureUnit.getMonth() - 1, temperatureUnit.getDay());
                    if (2000 + temperatureUnit.getYear() == year && calendar.get(Calendar.WEEK_OF_YEAR) == week) {
                        sortedTemperatureUnits.add(temperatureUnit);
                    }
                }
                break;
        }
        return sortedTemperatureUnits;
    }

    private ArrayList<HumidityTemperatureUnit> sortHumidityTemperatureUnits(int range, ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits) {
        Calendar calendar = Calendar.getInstance();
        ArrayList<HumidityTemperatureUnit> sortedHumidityTemperatureUnits = new ArrayList<>();
        switch (range) {
            case GraphLog.DAY:
                for (HumidityTemperatureUnit temperatureUnit : humidityTemperatureUnits) {
                    if (2000 + temperatureUnit.getYear() == year && temperatureUnit.getMonth() == month && temperatureUnit.getDay() == date) {
                        sortedHumidityTemperatureUnits.add(temperatureUnit);
                    }
                }
                break;
            case GraphLog.WEEK:
                calendar.set(year, month - 1, date);
                int week = calendar.get(Calendar.WEEK_OF_YEAR);
                for (HumidityTemperatureUnit temperatureUnit : humidityTemperatureUnits) {
                    calendar.set(2000 + temperatureUnit.getYear(), temperatureUnit.getMonth() - 1, temperatureUnit.getDay());
                    if (2000 + temperatureUnit.getYear() == year && calendar.get(Calendar.WEEK_OF_YEAR) == week) {
                        sortedHumidityTemperatureUnits.add(temperatureUnit);
                    }
                }
                break;
        }
        return sortedHumidityTemperatureUnits;
    }

    @Override
    public void onDateSet(int year, int month, int date) {
        dateButton.setText(String.format(Locale.ROOT, "%02d.%02d.%02d", date, month + 1, year));
        this.year = year;
        this.month = month + 1;
        this.date = date;
        drawGraph(spinner.getSelectedItemPosition());
    }
}
