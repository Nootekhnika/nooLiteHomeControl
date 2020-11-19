package com.noolitef.presets;

import android.graphics.Color;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noolitef.HomeActivity;
import com.noolitef.NooLiteF;
import com.noolitef.R;
import com.noolitef.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PresetsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class PresetsBlockViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout topPreset;
        private RelativeLayout topPresetButton;
        private TextView topPresetName;
        private TextView topPresetIndex;
        private LinearLayout bottomPreset;
        private RelativeLayout bottomPresetButton;
        private TextView bottomPresetName;
        private TextView bottomPresetIndex;

        PresetsBlockViewHolder(View item) {
            super(item);
            topPreset = item.findViewById(R.id.card_view_preset_top);
            topPresetButton = item.findViewById(R.id.card_view_preset_top_button);
            topPresetName = item.findViewById(R.id.card_view_preset_top_name);
            topPresetIndex = item.findViewById(R.id.card_view_preset_top_index);
            bottomPreset = item.findViewById(R.id.card_view_preset_bottom);
            bottomPresetButton = item.findViewById(R.id.card_view_preset_bottom_button);
            bottomPresetName = item.findViewById(R.id.card_view_preset_bottom_name);
            bottomPresetIndex = item.findViewById(R.id.card_view_preset_bottom_index);
        }

        void setTopPresetPadding(int left, int top, int right, int bottom) {
            topPreset.setPadding(left, top, right, bottom);
        }

        void setBottomPresetPadding(int left, int top, int right, int bottom) {
            bottomPreset.setPadding(left, top, right, bottom);
        }

        void setTopPresetState(int state) {
            if (Settings.isNightMode()) {
                switch (state) {
                    case Preset.OFF:
                        topPresetButton.setBackgroundResource(R.drawable.card_view_background_dark);
                        topPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.grey))));
                        break;
                    case Preset.RUNNING:
                        topPresetButton.setBackgroundResource(R.drawable.card_view_background_activated);
                        topPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.grey))));
                        break;
                }
            } else {
                switch (state) {
                    case Preset.OFF:
                        topPresetButton.setBackgroundResource(R.drawable.card_view_background);
                        topPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.black_light))));
                        break;
                    case Preset.RUNNING:
                        topPresetButton.setBackgroundResource(R.drawable.card_view_background_activated);
                        topPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.white))));
                        break;
                }
            }
        }

        void setBottomPresetState(int state) {
            if (Settings.isNightMode()) {
                switch (state) {
                    case Preset.OFF:
                        bottomPresetButton.setBackgroundResource(R.drawable.card_view_background_dark);
                        bottomPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.grey))));
                        break;
                    case Preset.RUNNING:
                        bottomPresetButton.setBackgroundResource(R.drawable.card_view_background_activated);
                        bottomPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.grey))));
                        break;
                }
            } else {
                switch (state) {
                    case Preset.OFF:
                        bottomPresetButton.setBackgroundResource(R.drawable.card_view_background);
                        bottomPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.black_light))));
                        break;
                    case Preset.RUNNING:
                        bottomPresetButton.setBackgroundResource(R.drawable.card_view_background_activated);
                        bottomPresetName.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(homeActivity, R.color.white))));
                        break;
                }
            }
        }

        void setTopPresetClick(View.OnClickListener onClickListener) {
            this.topPresetButton.setOnClickListener(onClickListener);
        }

        void setTopPresetLongClick(View.OnLongClickListener onLongClickListener) {
            this.topPresetButton.setOnLongClickListener(onLongClickListener);
        }

        void setBottomPresetClick(View.OnClickListener onClickListener) {
            this.bottomPresetButton.setOnClickListener(onClickListener);
        }

        void setBottomPresetLongClick(View.OnLongClickListener onLongClickListener) {
            this.bottomPresetButton.setOnLongClickListener(onLongClickListener);
        }

        void setTopPresetName(String name) {
            this.topPresetName.setText(name);
        }

        void setTopPresetIndex(int index) {
            this.topPresetIndex.setText(String.format(Locale.ROOT, "[%02d]", index));
            this.topPresetIndex.setVisibility(View.VISIBLE);
        }

        void setBottomPresetName(String name) {
            this.bottomPresetName.setText(name);
        }

        void setBottomPresetIndex(int index) {
            this.bottomPresetIndex.setText(String.format(Locale.ROOT, "[%02d]", index));
            this.bottomPresetIndex.setVisibility(View.VISIBLE);
        }

        void setBottomPresetVisible() {
            bottomPresetButton.setVisibility(View.VISIBLE);
        }

        void setBottomPresetInvisible() {
            bottomPresetButton.setVisibility(View.INVISIBLE);
        }
    }

    private final int PRESETS_BLOCK = 0;

    private OnPresetLongClickListener onPresetLongClickListener;
    private HomeActivity homeActivity;
    private OkHttpClient client;
    private Call call;

    private int horizontalPadding;
    private int verticalPadding;
    private int presetsRecyclerPadding;
    private ArrayList<PresetsBlock> presetsBlocks;
    private int presetRunning;

    public PresetsRecyclerAdapter(OkHttpClient client, HomeActivity homeActivity, int presetsRecyclerPadding, int horizontalPadding, int verticalPadding, ArrayList<PresetsBlock> presetsBlocks) {
        this.client = client;
        this.homeActivity = homeActivity;
        this.horizontalPadding = horizontalPadding;
        this.verticalPadding = verticalPadding;
        this.presetsRecyclerPadding = presetsRecyclerPadding;
        this.presetsBlocks = presetsBlocks;
        presetRunning = -1;
        setHasStableIds(true);
    }

    public void setOnPresetLongClickListener(OnPresetLongClickListener listener) {
        onPresetLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return presetsBlocks.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (presetsBlocks.get(position) != null) return PRESETS_BLOCK;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view;
        if (Settings.isNightMode()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_block_dark, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_preset_block, parent, false);
        }
        return new PresetsBlockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case PRESETS_BLOCK:
                bindPresetsBlock(holder, position);
                break;
        }
    }

    private void bindPresetsBlock(RecyclerView.ViewHolder holder, final int position) {
        PresetsBlockViewHolder presetsBlockViewHolder = (PresetsBlockViewHolder) holder;
        final PresetsBlock presetsBlock = presetsBlocks.get(position);
        if (position == 0) {
            presetsBlockViewHolder.setTopPresetPadding(presetsRecyclerPadding + horizontalPadding, verticalPadding, horizontalPadding, verticalPadding / 2);
            presetsBlockViewHolder.setBottomPresetPadding(presetsRecyclerPadding + horizontalPadding, verticalPadding / 2, horizontalPadding, verticalPadding);
        } else {
            if (position < presetsBlocks.size() - 1) {
                presetsBlockViewHolder.setTopPresetPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding / 2);
                presetsBlockViewHolder.setBottomPresetPadding(horizontalPadding, verticalPadding / 2, horizontalPadding, verticalPadding);
            } else {
                presetsBlockViewHolder.setTopPresetPadding(horizontalPadding, verticalPadding, presetsRecyclerPadding + horizontalPadding, verticalPadding / 2);
                presetsBlockViewHolder.setBottomPresetPadding(horizontalPadding, verticalPadding / 2, presetsRecyclerPadding + horizontalPadding, verticalPadding);
            }
        }

        presetsBlockViewHolder.setTopPresetState(presetsBlocks.get(position).getTopPreset().getState());

        presetsBlockViewHolder.setTopPresetClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runPreset(position, presetsBlocks.get(position).getTopPreset());
            }
        });
        presetsBlockViewHolder.setTopPresetLongClick(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onPresetLongClickListener.openPresetFragment(presetsBlock.getTopPreset());
                return true;
            }
        });
        presetsBlockViewHolder.setTopPresetName(presetsBlock.getTopPreset().getName());
        if (Settings.isDeveloperMode()) {
            presetsBlockViewHolder.setTopPresetIndex(presetsBlock.getTopPreset().getIndex());
        }

        if (presetsBlock.getBottomPreset() != null) {
            presetsBlockViewHolder.setBottomPresetState(presetsBlocks.get(position).getBottomPreset().getState());

            presetsBlockViewHolder.setBottomPresetClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    runPreset(position, presetsBlocks.get(position).getBottomPreset());
                }
            });
            presetsBlockViewHolder.setBottomPresetLongClick(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onPresetLongClickListener.openPresetFragment(presetsBlock.getBottomPreset());
                    return true;
                }
            });
            presetsBlockViewHolder.setBottomPresetName(presetsBlock.getBottomPreset().getName());
            if (Settings.isDeveloperMode()) {
                presetsBlockViewHolder.setBottomPresetIndex(presetsBlock.getBottomPreset().getIndex());
            }
            presetsBlockViewHolder.setBottomPresetVisible();
        } else {
            presetsBlockViewHolder.setBottomPresetInvisible();
        }
    }

    private void runPreset(final int position, final Preset preset) {
        //addProgressBar
        Request request;
        if (preset.getIndex() != presetRunning) {
            request = new Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010A%s000000000000000000000000", NooLiteF.getHexString(preset.getIndex())))
                    .build();
            presetRunning = preset.getIndex();
        } else {
            request = new Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010AFF000000000000000000000000"))
                    .build();
            presetRunning = -1;
        }
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                call.cancel();
                presetRunning = -1;
                homeActivity.writeAppLog("PresetsRecyclerAdapter.java : runPreset() : onFailure()\nNo connection\n" + preset.toString() + NooLiteF.getStackTrace(exception));
                showSnack("Нет соединения...");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        call.cancel();
                        preset.setState(Preset.RUNNING);
                        notifyItemChanged(position);
                        int timeout = 0;
                        if (presetRunning != -1) {
                            for (int cmd = 0; cmd < 72; cmd++) {
                                if (preset.getCommand(cmd)[0] != -1) timeout += 250;
                            }
                        } else {
                            timeout = 250;
                        }
                        Thread.sleep(timeout);

                        homeActivity.getFTXUnitsState();

                        preset.setState(Preset.OFF);
                        notifyItemChanged(position);
                        presetRunning = -1;
                    } else {
                        call.cancel();
                        presetRunning = -1;
                        homeActivity.writeAppLog("PresetsRecyclerAdapter.java : runPreset() : onResponse()\nResponse code: " + response.code() + "\n" + preset.toString());
                        showSnack("Ошибка соединения " + response.code());
                    }
                } catch (Exception e) {
                    call.cancel();
                    presetRunning = -1;
                    homeActivity.writeAppLog("PresetsRecyclerAdapter.java : runPreset() : onResponse()\nException\n" + preset.toString() + NooLiteF.getStackTrace(e));
                    showSnack("Что-то пошло не так...");
                }
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
