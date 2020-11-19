package com.noolitef.rx;

import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.noolitef.NooLiteF;
import com.noolitef.R;

import java.util.ArrayList;

class GraphLogRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    class EmptyGraphViewHolder extends RecyclerView.ViewHolder {
        private ImageView emptyImage;

        EmptyGraphViewHolder(View item) {
            super(item);
            //emptyImage = (ImageView) item.findViewById(R.id.dialog_graph_log_image_empty);
        }

        void setEmptyImage(Bitmap bitmap) {
            this.emptyImage.setImageBitmap(bitmap);
        }
    }

    class TemperatureGraphViewHolder extends RecyclerView.ViewHolder {
        private ImageView temperatureImage;

        TemperatureGraphViewHolder(View item) {
            super(item);
            temperatureImage = (ImageView) item.findViewById(R.id.dialog_graph_log_image_temperature);
        }

        void setTemperatureImage(Bitmap bitmap) {
            this.temperatureImage.setImageBitmap(bitmap);
        }
    }

    class HumidityTemperatureGraphViewHolder extends RecyclerView.ViewHolder {
        private ImageView temperatureImage;
        private ImageView humidityTemperatureImage;

        HumidityTemperatureGraphViewHolder(View item) {
            super(item);
            temperatureImage = (ImageView) item.findViewById(R.id.dialog_graph_log_image_temperature);
            humidityTemperatureImage = (ImageView) item.findViewById(R.id.dialog_graph_log_image_humidity);
        }

        void setTemperatureImage(Bitmap bitmap) {
            this.temperatureImage.setImageBitmap(bitmap);
        }

        void setHumidityImage(Bitmap bitmap) {
            this.humidityTemperatureImage.setImageBitmap(bitmap);
        }
    }

    static final int EMPTY_GRAPH = 0;
    private static final int TEMPERATURE_GRAPH = 1;
    private static final int HUMIDITY_TEMPERATURE_GRAPH = 2;

    private ArrayList<Object> timeUnits;
    private NooLiteF nooLiteF;

    GraphLogRecyclerAdapter(NooLiteF nooLiteF, ArrayList<Object> timeUnits) {
        this.nooLiteF = nooLiteF;
        this.timeUnits = timeUnits;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return timeUnits.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (timeUnits.get(position) instanceof EmptyTimeInterval) return EMPTY_GRAPH;
        if (timeUnits.get(position) instanceof TemperatureTimeInterval) return TEMPERATURE_GRAPH;
        if (timeUnits.get(position) instanceof HumidityTemperatureTimeInterval)
            return HUMIDITY_TEMPERATURE_GRAPH;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view;
        switch (type) {
            case EMPTY_GRAPH:
                //view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_empty_graph, parent, false);
                //EmptyGraphViewHolder emptyGraphViewHolder = new EmptyGraphViewHolder(view);
                //return emptyGraphViewHolder;
            case TEMPERATURE_GRAPH:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_temperature_graph, parent, false);
                TemperatureGraphViewHolder temperatureGraphViewHolder = new TemperatureGraphViewHolder(view);
                return temperatureGraphViewHolder;
            case HUMIDITY_TEMPERATURE_GRAPH:
                //view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_humidity_temperature_graph, parent, false);
                //HumidityTemperatureGraphViewHolder humidityTemperatureGraphViewHolder = new HumidityTemperatureGraphViewHolder(view);
                //return humidityTemperatureGraphViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case EMPTY_GRAPH:
                bindEmptyGraph(holder, position);
                break;
            case TEMPERATURE_GRAPH:
                bindTemperatureGraph(holder, position);
                break;
            case HUMIDITY_TEMPERATURE_GRAPH:
                bindHumidityTemperatureGraph(holder, position);
                break;
        }
    }

    private void bindEmptyGraph(RecyclerView.ViewHolder holder, int position) {
        EmptyGraphViewHolder emptyGraphViewHolder = (EmptyGraphViewHolder) holder;
        //emptyGraphViewHolder.setEmptyImage();
    }

    private void bindTemperatureGraph(RecyclerView.ViewHolder holder, int position) {
        TemperatureGraphViewHolder temperatureGraphViewHolder = (TemperatureGraphViewHolder) holder;
        //temperatureGraphViewHolder.setTemperatureImage();
    }

    private void bindHumidityTemperatureGraph(final RecyclerView.ViewHolder holder, final int position) {
        HumidityTemperatureGraphViewHolder humidityTemperatureGraphViewHolder = (HumidityTemperatureGraphViewHolder) holder;
        //humidityTemperatureGraphViewHolder.setTemperatureImage();
        //humidityTemperatureGraphViewHolder.setHumidityImage();
    }
}
