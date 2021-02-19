package com.noolitef;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ScheduleTableDrawable {
    private final float CELL_LINE = 1.0f;
    private final float TABLE_LINE = 2.0f;
    private final float TABLE_RADIUS = 8.0f;

    private final String[] WEEK_DAY_LABEL = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private final String[] HOUR_LABEL = {"0", "3", "6", "9", "12", "15", "18", "21", "24"};

    private Context context;
    private float density;

    private float rowWidth;
    private float tableRadius;
    private float cellWidth;
    private float cellHeight;
    private float intervalHeight;
    private float pixelsPerMinute;
    private float cellLine;
    private float tableLine;
    private float weekDayLabelSize;
    private float temperatureLabelSize;
    private float hourLabelSize;
    private Rect labelBounds;
    private float weekDayLabelLeftMargin;
    private float x;
    private float y;

    private Path path;
    private Paint paint;
    private Canvas tableRowCanvas;
    private Bitmap tableRowBitmap;

    public ScheduleTableDrawable(Context context, float density, int tableWidth) {
        this.context = context;
        this.density = density;
        rowWidth = tableWidth;
        tableRadius = TABLE_RADIUS * density;
        cellWidth = Math.round((rowWidth - tableRadius) / 9);
        cellHeight = Math.round(0.8f * cellWidth);
        if (cellHeight % 2 != 0) {
            cellHeight++;
        }
        intervalHeight = 0.75f * cellHeight;
        pixelsPerMinute = (8 * cellWidth) / 1440;
        cellLine = CELL_LINE * density;
        tableLine = TABLE_LINE * density;
        weekDayLabelSize = cellHeight / 2;
        temperatureLabelSize = intervalHeight / 2;
        hourLabelSize = weekDayLabelSize;
        labelBounds = new Rect();

        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tableRowCanvas = new Canvas();
    }

    public Bitmap drawTopTableRow(ArrayList<ThermostatActivityInterval> thermostatActivityIntervals) {
        tableRowBitmap = Bitmap.createBitmap((int) rowWidth, (int) cellHeight, Bitmap.Config.ARGB_8888);
        tableRowCanvas.setBitmap(tableRowBitmap);

        // draw cell's vertical dividers
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(cellLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        x = cellWidth;
        for (int l = 0; l < 9; l++) {
            tableRowCanvas.drawLine(x, 0, x, cellHeight, paint);
            x += cellWidth;
        }

        // draw table's top bound
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(tableLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        path.reset();
        path.moveTo(0.5f * tableLine, cellHeight);
        path.lineTo(0.5f * tableLine, tableRadius);
        path.arcTo(new RectF(0.5f * tableLine, 0.5f * tableLine, 2 * tableRadius - (0.5f * tableLine), 2 * tableRadius - (0.5f * tableLine)), 180, 90);
        path.lineTo(rowWidth - tableRadius + (0.5f * tableLine), 0.5f * tableLine);
        path.arcTo(new RectF(rowWidth - (2 * tableRadius) + (0.5f * tableLine), 0.5f * tableLine, rowWidth - (0.5f * tableLine), 2 * tableRadius - (0.5f * tableLine)), 270, 90);
        path.lineTo(rowWidth - (0.5f * tableLine), cellHeight);
        tableRowCanvas.drawPath(path, paint);

        // draw week day label
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(weekDayLabelSize);
        paint.getTextBounds(WEEK_DAY_LABEL[0], 0, WEEK_DAY_LABEL[0].length(), labelBounds);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        weekDayLabelLeftMargin = (cellWidth / 2) - (labelBounds.width() / 2);
        y = (cellHeight / 2) + (labelBounds.height() / 2);
        tableRowCanvas.drawText(WEEK_DAY_LABEL[0], weekDayLabelLeftMargin, y, paint);

        // draw working time interval and temperature label
        drawTemperatureTimeIntervals(thermostatActivityIntervals);

        return tableRowBitmap;
    }

    public Bitmap drawMiddleTableRow(int row, ArrayList<ThermostatActivityInterval> thermostatActivityIntervals) {
        tableRowBitmap = Bitmap.createBitmap((int) rowWidth, (int) cellHeight, Bitmap.Config.ARGB_8888);
        tableRowCanvas.setBitmap(tableRowBitmap);

        // draw cell's vertical dividers
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(cellLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        x = cellWidth;
        for (int l = 0; l < 9; l++) {
            tableRowCanvas.drawLine(x, 0, x, cellHeight, paint);
            x += cellWidth;
        }

        // draw row's divider on top
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(cellLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        tableRowCanvas.drawLine(0, 0.5f * cellLine, rowWidth, 0.5f * cellLine, paint);

        // draw table's left and right bounds
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(tableLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        tableRowCanvas.drawLine(0.5f * tableLine, 0, 0.5f * tableLine, cellHeight, paint);
        tableRowCanvas.drawLine(rowWidth - (0.5f * tableLine), 0, rowWidth - (0.5f * tableLine), cellHeight, paint);

        // draw week day label
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(cellHeight / 2);
        paint.getTextBounds(WEEK_DAY_LABEL[row], 0, WEEK_DAY_LABEL[row].length(), labelBounds);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        tableRowCanvas.drawText(WEEK_DAY_LABEL[row], weekDayLabelLeftMargin, y, paint);

        // draw working time interval and temperature label
        drawTemperatureTimeIntervals(thermostatActivityIntervals);

        return tableRowBitmap;
    }

    public Bitmap drawBottomTableRow(ArrayList<ThermostatActivityInterval> thermostatActivityIntervals) {
        tableRowBitmap = Bitmap.createBitmap((int) rowWidth, (int) cellHeight, Bitmap.Config.ARGB_8888);
        tableRowCanvas.setBitmap(tableRowBitmap);

        // draw cell's vertical dividers
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(cellLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        x = cellWidth;
        for (int l = 0; l < 9; l++) {
            tableRowCanvas.drawLine(x, 0, x, cellHeight, paint);
            x += cellWidth;
        }

        // draw row's divider on top
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(cellLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        tableRowCanvas.drawLine(0, 0.5f * cellLine, rowWidth, 0.5f * cellLine, paint);

        // draw week day label
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(cellHeight / 2);
        paint.getTextBounds(WEEK_DAY_LABEL[6], 0, WEEK_DAY_LABEL[6].length(), labelBounds);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        tableRowCanvas.drawText(WEEK_DAY_LABEL[6], weekDayLabelLeftMargin, y, paint);

        // draw working time interval and temperature label
        drawTemperatureTimeIntervals(thermostatActivityIntervals);

        // draw table's bottom bound
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(tableLine);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        path.reset();
        path.moveTo(0.5f * tableLine, 0);
        path.lineTo(0.5f * tableLine, cellHeight - tableRadius - (0.5f * tableLine));
        path.arcTo(new RectF(0.5f * tableLine, cellHeight - (2 * tableRadius) + (0.5f * tableLine), 2 * tableRadius - (0.5f * tableLine), cellHeight - (0.5f * tableLine)), 180, -90);
        path.lineTo(rowWidth - tableRadius + (0.5f * tableLine), cellHeight - (0.5f * tableLine));
        path.arcTo(new RectF(rowWidth - (2 * tableRadius) + (0.5f * tableLine), cellHeight - (2 * tableRadius) + (0.5f * tableLine), rowWidth - (0.5f * tableLine), cellHeight - (0.5f * tableLine)), 90, -90);
        path.lineTo(rowWidth - (0.5f * tableLine), 0);
        tableRowCanvas.drawPath(path, paint);

        return tableRowBitmap;
    }

    public Bitmap drawTimeLine() {
        tableRowBitmap = Bitmap.createBitmap((int) rowWidth, (int) cellHeight, Bitmap.Config.ARGB_8888);
        tableRowCanvas.setBitmap(tableRowBitmap);

        paint.setStrokeWidth(tableLine);
        paint.setTextSize(hourLabelSize);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        paint.getTextBounds(HOUR_LABEL[0], 0, HOUR_LABEL[0].length(), labelBounds);
        x = cellWidth;
        y = tableRadius + labelBounds.height();
        for (int l = 0; l < 9; l++) {
            paint.setStyle(Paint.Style.STROKE);
            tableRowCanvas.drawLine(x, 0, x, tableRadius / 2, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.getTextBounds(HOUR_LABEL[l], 0, HOUR_LABEL[l].length(), labelBounds);
            tableRowCanvas.drawText(HOUR_LABEL[l], x - (labelBounds.width() / 2) - density, y, paint);
            x += cellWidth;
        }

        return tableRowBitmap;
    }

    private void drawTemperatureTimeIntervals(ArrayList<ThermostatActivityInterval> thermostatActivityIntervals) {
        // for debugging
        thermostatActivityIntervals.clear();
        thermostatActivityIntervals.add(new ThermostatActivityInterval(0, 0, 0, 0, 20));
        thermostatActivityIntervals.add(new ThermostatActivityInterval(3, 0, 5, 1, 21));
        thermostatActivityIntervals.add(new ThermostatActivityInterval(7, 30, 7, 33, 22));
        thermostatActivityIntervals.add(new ThermostatActivityInterval(8, 30, 17, 0, 23));
        thermostatActivityIntervals.add(new ThermostatActivityInterval(19, 0, 20, 0, 24));
        thermostatActivityIntervals.add(new ThermostatActivityInterval(21, 0, 23, 0, 25));

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(temperatureLabelSize);
        int beginMinutes;
        int endMinutes;
        String temperature;

        for (ThermostatActivityInterval thermostatActivityInterval : thermostatActivityIntervals) {
            beginMinutes = 60 * thermostatActivityInterval.getBeginHour() + thermostatActivityInterval.getBeginMinute();
            endMinutes = 60 * thermostatActivityInterval.getEndHour() + thermostatActivityInterval.getEndMinute();

            paint.setShader(new LinearGradient(cellLine + cellWidth + (pixelsPerMinute * beginMinutes), 0, cellLine + cellWidth + (pixelsPerMinute * endMinutes), 0, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green))), Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green_light))), Shader.TileMode.CLAMP));
            tableRowCanvas.drawRect(new Rect((int) (cellLine + cellWidth + (pixelsPerMinute * beginMinutes)), (int) (0.25 * cellHeight), (int) (cellLine + cellWidth + (pixelsPerMinute * endMinutes)), (int) cellHeight), paint);
            paint.setShader(null);

            if (endMinutes - beginMinutes > 120) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.white))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            temperature = thermostatActivityInterval.getTemperature() + "°";
            paint.getTextBounds(temperature, 0, temperature.length(), labelBounds);
            tableRowCanvas.drawText(temperature, cellLine + cellWidth + ((intervalHeight - labelBounds.height()) / 2) + (pixelsPerMinute * (60 * thermostatActivityInterval.getBeginHour() + thermostatActivityInterval.getBeginMinute())), 0.25f * cellHeight + (intervalHeight / 2) + (labelBounds.height() / 2), paint);
        }
    }
}