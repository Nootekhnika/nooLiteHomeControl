package com.noolitef.rx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

import com.noolitef.NooLiteF;
import com.noolitef.R;
import com.noolitef.settings.Settings;

import java.util.ArrayList;
import java.util.Calendar;

public class GraphLog {
    public static final int DAY = 1;
    public static final int WEEK = 7;

    private final float LINE_WIDTH_1 = 1.0f;
    private final float LINE_WIDTH_2 = 2.0f;
    private final float DOT_RADIUS = 4.0f;
    private final float PLOT_LABEL_SIZE = 12.0f;
    private final float NO_DATA_LABEL_SIZE = 14.0f;

    private final String NO_DATA_LABEL = "НЕТ ДАННЫХ";
    private final String[] HOUR_LABEL = {"0", "3", "6", "9", "12", "15", "18", "21", "24"};
    private final String[] WEEK_DAY_LABEL = {"пн", "вт", "ср", "чт", "пт", "сб", "вс", ""};
    private final int[] HUMIDITY_PLOT_LABEL = {0, 50, 100};

    private Calendar calendar;
    private Context context;
    private DisplayMetrics display;

    private int imageWidth;
    private float imageHeight;
    private float plotLabelSize;
    private float plotLeftMargin;
    private float plotTopMargin;
    private float plotRightMargin;
    private float plotBottomMargin;
    private float plotWidth;
    private float plotHeight;
    private RectF plotRectangleBounds;
    private float plotRectangleRadius;
    private float graphLeftMargin;
    private float graphTopMargin;
    private float graphRightMargin;
    private float graphBottomMargin;
    private float graphWidth;
    private float graphHeight;
    private float dataLineSpace;
    private float timeLineSpace;
    private float lineWidth1;
    private float lineWidth2;
    private double maxTemperature;
    private double minTemperature;
    private Rect textLabelBounds;
    private float textLabelY;
    private int[] dataBound;
    private float pixelsPerTimeUnit;
    private float pixelsPerOneTenthDegree;
    private float pixelsPerOnePercent;
    private float x;
    private float y;
    private float noDataLabelSize;

    private Paint paint;
    private Canvas temperatureCanvas;
    private Canvas humidityCanvas;
    private Bitmap temperatureBitmap;
    private Bitmap humidityBitmap;

    public GraphLog(Context context, DisplayMetrics display) {
        calendar = Calendar.getInstance();
        this.context = context;
        this.display = display;

        int displayWidthPixels = display.widthPixels;
        int displayHeightPixels = display.heightPixels;
        if (displayWidthPixels < displayHeightPixels) {
            imageWidth = displayWidthPixels;
        } else {
            imageWidth = displayHeightPixels;
        }
        imageWidth -= (32 * display.density);
        imageHeight = 2.0f / 3.0f * imageWidth;
        plotLabelSize = PLOT_LABEL_SIZE * display.density;
        plotRectangleBounds = new RectF();
        plotRectangleRadius = 2 * DOT_RADIUS * display.density;
        plotLeftMargin = 1.5f * plotLabelSize + (plotRectangleRadius / 2) + display.density;
        plotTopMargin = display.density;
        plotRightMargin = display.density;
        plotBottomMargin = plotLabelSize + (plotRectangleRadius / 2) - display.density;
        plotWidth = imageWidth - (plotLeftMargin + plotRightMargin);
        plotHeight = imageHeight - (plotTopMargin + plotBottomMargin);
        graphLeftMargin = plotLeftMargin + (2 * plotRectangleRadius);
        graphTopMargin = plotTopMargin + plotRectangleRadius;
        graphRightMargin = plotRectangleRadius + plotRightMargin;
        graphBottomMargin = 2 * plotRectangleRadius + plotBottomMargin;
        graphWidth = imageWidth - graphLeftMargin - graphRightMargin;
        graphHeight = imageHeight - graphTopMargin - graphBottomMargin;
        dataLineSpace = graphHeight / 2;
        timeLineSpace = 0;
        lineWidth1 = LINE_WIDTH_1 * display.density;
        lineWidth2 = LINE_WIDTH_2 * display.density;
        dataBound = new int[3];
        textLabelBounds = new Rect();
        noDataLabelSize = NO_DATA_LABEL_SIZE * display.density;

        paint = new Paint();
        paint.setAntiAlias(true);
        temperatureCanvas = new Canvas();
        humidityCanvas = new Canvas();
    }

    public Bitmap drawTemperatureLog(int range, ArrayList<?> dataUnits) {
        drawTemperatureImage();
        if (dataUnits != null && dataUnits.size() > 0) {
            drawTemperatureLines(dataUnits);
            switch (range) {
                case DAY:
                    drawDayLines(temperatureCanvas);
                    drawDayTemperatureGraph(dataUnits);
                    break;
                case WEEK:
                    drawWeekLines(temperatureCanvas);
                    drawWeekTemperatureGraph(dataUnits);
                    break;
            }
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green_light))));
            drawDot(temperatureCanvas, dataUnits);
        } else {
            drawDataLines(temperatureCanvas);
            switch (range) {
                case DAY:
                    drawDayLines(temperatureCanvas);
                    break;
                case WEEK:
                    drawWeekLines(temperatureCanvas);
                    break;
            }
            drawNoDataLabel(temperatureCanvas);
        }
        drawPlotRectangle(temperatureCanvas);
        return temperatureBitmap;
    }

    public Bitmap drawHumidityLog(int range, ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits) {
        drawHumidityImage();
        if (humidityTemperatureUnits != null && humidityTemperatureUnits.size() > 0) {
            drawHumidityLines();
            switch (range) {
                case DAY:
                    drawDayLines(humidityCanvas);
                    drawDayHumidityGraph(humidityTemperatureUnits);
                    break;
                case WEEK:
                    drawWeekLines(humidityCanvas);
                    drawWeekHumidityGraph(humidityTemperatureUnits);
                    break;
            }
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green_light))));
            drawDot(humidityCanvas, humidityTemperatureUnits);
        } else {
            drawDataLines(humidityCanvas);
            switch (range) {
                case DAY:
                    drawDayLines(humidityCanvas);
                    break;
                case WEEK:
                    drawWeekLines(humidityCanvas);
                    break;
            }
            drawNoDataLabel(humidityCanvas);
        }
        drawPlotRectangle(humidityCanvas);
        return humidityBitmap;
    }

    private void drawTemperatureImage() {
        temperatureBitmap = Bitmap.createBitmap(imageWidth, (int) imageHeight, Bitmap.Config.ARGB_8888);
        temperatureCanvas.setBitmap(temperatureBitmap);
        if (Settings.isNightMode()) {
            temperatureCanvas.drawColor(context.getResources().getColor(R.color.black_dark));
        } else {
            temperatureCanvas.drawColor(context.getResources().getColor(R.color.white));
        }
    }

    private void drawHumidityImage() {
        humidityBitmap = Bitmap.createBitmap(imageWidth, (int) imageHeight, Bitmap.Config.ARGB_8888);
        humidityCanvas.setBitmap(humidityBitmap);
        if (Settings.isNightMode()) {
            humidityCanvas.drawColor(context.getResources().getColor(R.color.black_dark));
        } else {
            humidityCanvas.drawColor(context.getResources().getColor(R.color.white));
        }
    }

    private void drawTemperatureLines(ArrayList<?> dataUnits) {
        if (dataUnits.get(0) instanceof TemperatureUnit) {
            ArrayList<TemperatureUnit> temperatureUnits = (ArrayList<TemperatureUnit>) dataUnits;
            maxTemperature = temperatureUnits.get(0).getTemperature();
            minTemperature = temperatureUnits.get(0).getTemperature();
            for (TemperatureUnit temperatureUnit : temperatureUnits) {
                if (temperatureUnit.getTemperature() > maxTemperature) {
                    maxTemperature = temperatureUnit.getTemperature();
                }
                if (temperatureUnit.getTemperature() < minTemperature) {
                    minTemperature = temperatureUnit.getTemperature();
                }
            }
        }
        if (dataUnits.get(0) instanceof HumidityTemperatureUnit) {
            ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits = (ArrayList<HumidityTemperatureUnit>) dataUnits;
            maxTemperature = humidityTemperatureUnits.get(0).getTemperature();
            minTemperature = humidityTemperatureUnits.get(0).getTemperature();
            for (HumidityTemperatureUnit temperatureUnit : humidityTemperatureUnits) {
                if (temperatureUnit.getTemperature() > maxTemperature) {
                    maxTemperature = temperatureUnit.getTemperature();
                }
                if (temperatureUnit.getTemperature() < minTemperature) {
                    minTemperature = temperatureUnit.getTemperature();
                }
            }
        }
        dataBound[0] = (int) Math.round((maxTemperature + 5) / 10) * 10;
        dataBound[2] = (int) Math.round((minTemperature - 5) / 10) * 10;
        dataBound[1] = (dataBound[0] + dataBound[2]) / 2;

        pixelsPerOneTenthDegree = graphHeight / ((dataBound[0] - dataBound[2]) * 10);

        paint.setTextSize(plotLabelSize);
        paint.setStrokeWidth(lineWidth1);
        for (float y = graphTopMargin, dataLine = 0; dataLine < 3; dataLine++, y += dataLineSpace) {
            paint.getTextBounds(String.valueOf(dataBound[(int) dataLine]), 0, String.valueOf(dataBound[(int) dataLine]).length(), textLabelBounds);
            paint.setStyle(Paint.Style.FILL);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            temperatureCanvas.drawText(String.valueOf(dataBound[(int) dataLine]), plotLeftMargin - (textLabelBounds.width() + (plotRectangleRadius / 2)), y + (textLabelBounds.height() / 2) - display.density, paint);
            paint.setStyle(Paint.Style.STROKE);
            temperatureCanvas.drawLine(plotLeftMargin, y, plotLeftMargin + plotRectangleRadius, y, paint);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            }
            temperatureCanvas.drawLine(plotLeftMargin + plotRectangleRadius, y, plotLeftMargin + plotWidth, y, paint);
        }

        // draw zero line if necessary
        if (minTemperature < 0 && 0 < maxTemperature && dataBound[0] != 0 && dataBound[1] != 0 && dataBound[2] != 0) {
            y = graphTopMargin + (pixelsPerOneTenthDegree * dataBound[0] * 10);
            temperatureCanvas.drawLine(plotLeftMargin + plotRectangleRadius, y, plotLeftMargin + plotWidth, y, paint);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            temperatureCanvas.drawLine(plotLeftMargin, y, plotLeftMargin + plotRectangleRadius, y, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.getTextBounds("0", 0, 1, textLabelBounds);
            temperatureCanvas.drawText("0", plotLeftMargin - (textLabelBounds.width() + (plotRectangleRadius / 2)), y + (textLabelBounds.height() / 2) - display.density, paint);
        }
    }

    private void drawHumidityLines() {
        dataBound[2] = HUMIDITY_PLOT_LABEL[0];
        dataBound[1] = HUMIDITY_PLOT_LABEL[1];
        dataBound[0] = HUMIDITY_PLOT_LABEL[2];

        paint.setTextSize(plotLabelSize);
        paint.setStrokeWidth(lineWidth1);
        for (float y = graphTopMargin, dataLine = 0; dataLine < 3; dataLine++, y += dataLineSpace) {
            paint.getTextBounds(String.valueOf(dataBound[(int) dataLine]), 0, String.valueOf(dataBound[(int) dataLine]).length(), textLabelBounds);
            paint.setStyle(Paint.Style.FILL);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            humidityCanvas.drawText(String.valueOf(dataBound[(int) dataLine]), plotLeftMargin - (textLabelBounds.width() + (plotRectangleRadius / 2)), y + (textLabelBounds.height() / 2) - display.density, paint);
            paint.setStyle(Paint.Style.STROKE);
            humidityCanvas.drawLine(plotLeftMargin, y, plotLeftMargin + plotRectangleRadius, y, paint);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            }
            humidityCanvas.drawLine(plotLeftMargin + plotRectangleRadius, y, plotLeftMargin + plotWidth, y, paint);
        }
    }

    private void drawDataLines(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth1);
        for (float y = graphTopMargin, dataLine = 0; dataLine < 3; dataLine++, y += dataLineSpace) {
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            canvas.drawLine(plotLeftMargin, y, plotLeftMargin + plotRectangleRadius, y, paint);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            }
            canvas.drawLine(plotLeftMargin + plotRectangleRadius, y, plotLeftMargin + plotWidth, y, paint);
        }
    }

    private void drawDayLines(Canvas canvas) {
        paint.setTextSize(plotLabelSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setStyle(Paint.Style.FILL);
        paint.getTextBounds(HOUR_LABEL[0], 0, HOUR_LABEL[0].length(), textLabelBounds);
        paint.setStrokeWidth(lineWidth1);
        textLabelY = plotTopMargin + plotHeight + (plotRectangleRadius / 2) + textLabelBounds.height();
        timeLineSpace = graphWidth / 8;
        for (float x = graphLeftMargin, line = 0; line < 9; line++, x += timeLineSpace) {
            paint.setStyle(Paint.Style.STROKE);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            }
            canvas.drawLine(x, plotTopMargin, x, plotTopMargin + plotRectangleRadius + graphHeight + plotRectangleRadius, paint);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            canvas.drawLine(x, plotTopMargin + plotRectangleRadius + graphHeight + plotRectangleRadius, x, plotTopMargin + plotHeight, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.getTextBounds(HOUR_LABEL[(int) line], 0, HOUR_LABEL[(int) line].length(), textLabelBounds);
            canvas.drawText(HOUR_LABEL[(int) line], x - (textLabelBounds.width() / 2) - display.density, textLabelY, paint);
        }
    }

    private void drawWeekLines(Canvas canvas) {
        paint.setTextSize(plotLabelSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setStyle(Paint.Style.FILL);
        paint.getTextBounds(WEEK_DAY_LABEL[0], 0, WEEK_DAY_LABEL[0].length(), textLabelBounds);
        paint.setStrokeWidth(lineWidth1);
        textLabelY = plotTopMargin + plotHeight + (plotRectangleRadius / 2) + textLabelBounds.height();
        timeLineSpace = graphWidth / 7;
        for (float x = graphLeftMargin, line = 0; line < 8; line++, x += timeLineSpace) {
            paint.setStyle(Paint.Style.STROKE);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            }
            canvas.drawLine(x, plotTopMargin, x, plotTopMargin + plotRectangleRadius + graphHeight + plotRectangleRadius, paint);
            if (Settings.isNightMode()) {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
            } else {
                paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
            }
            canvas.drawLine(x, plotTopMargin + plotRectangleRadius + graphHeight + plotRectangleRadius, x, plotTopMargin + plotHeight, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.getTextBounds(WEEK_DAY_LABEL[(int) line], 0, WEEK_DAY_LABEL[(int) line].length(), textLabelBounds);
            canvas.drawText(WEEK_DAY_LABEL[(int) line], x + (timeLineSpace / 2) - (textLabelBounds.width() / 2) - display.density, textLabelY, paint);
        }
    }

    private void drawDayTemperatureGraph(ArrayList<?> dataUnits) {
        pixelsPerTimeUnit = graphWidth / 1440;
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green))));
        Path temperaturePath = new Path();
        if (dataUnits.get(0) instanceof TemperatureUnit) {
            ArrayList<TemperatureUnit> temperatureUnits = (ArrayList<TemperatureUnit>) dataUnits;
            x = graphLeftMargin + (pixelsPerTimeUnit * (60 * temperatureUnits.get(temperatureUnits.size() - 1).getHour() + temperatureUnits.get(temperatureUnits.size() - 1).getMinute()));
            y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) temperatureUnits.get(temperatureUnits.size() - 1).getTemperature()) * 10);
            temperaturePath.moveTo(x, y);
            temperatureCanvas.drawCircle(x, y, display.density, paint);
            for (int i = temperatureUnits.size() - 2; i > -1; i--) {
                x = graphLeftMargin + (pixelsPerTimeUnit * (60 * temperatureUnits.get(i).getHour() + temperatureUnits.get(i).getMinute()));
                y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) temperatureUnits.get(i).getTemperature()) * 10);
                if (Math.abs((NooLiteF.getMillisecond(temperatureUnits.get(i).getYear(), temperatureUnits.get(i).getMonth(), temperatureUnits.get(i).getDay(), temperatureUnits.get(i).getHour(), temperatureUnits.get(i).getMinute(), temperatureUnits.get(i).getSecond()) - NooLiteF.getMillisecond(temperatureUnits.get(i + 1).getYear(), temperatureUnits.get(i + 1).getMonth(), temperatureUnits.get(i + 1).getDay(), temperatureUnits.get(i + 1).getHour(), temperatureUnits.get(i + 1).getMinute(), temperatureUnits.get(i + 1).getSecond()))) < 10800000) {
                    temperaturePath.lineTo(x, y);
                } else {
                    temperaturePath.moveTo(x, y);
                }
                temperatureCanvas.drawCircle(x, y, display.density, paint);
            }
        }
        if (dataUnits.get(0) instanceof HumidityTemperatureUnit) {
            ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits = (ArrayList<HumidityTemperatureUnit>) dataUnits;
            x = graphLeftMargin + (pixelsPerTimeUnit * (60 * humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getHour() + humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getMinute()));
            y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getTemperature()) * 10);
            temperaturePath.moveTo(x, y);
            humidityCanvas.drawCircle(x, y, display.density, paint);
            for (int i = humidityTemperatureUnits.size() - 2; i > -1; i--) {
                x = graphLeftMargin + (pixelsPerTimeUnit * (60 * humidityTemperatureUnits.get(i).getHour() + humidityTemperatureUnits.get(i).getMinute()));
                y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) humidityTemperatureUnits.get(i).getTemperature()) * 10);
                if (Math.abs((NooLiteF.getMillisecond(humidityTemperatureUnits.get(i).getYear(), humidityTemperatureUnits.get(i).getMonth(), humidityTemperatureUnits.get(i).getDay(), humidityTemperatureUnits.get(i).getHour(), humidityTemperatureUnits.get(i).getMinute(), humidityTemperatureUnits.get(i).getSecond()) - NooLiteF.getMillisecond(humidityTemperatureUnits.get(i + 1).getYear(), humidityTemperatureUnits.get(i + 1).getMonth(), humidityTemperatureUnits.get(i + 1).getDay(), humidityTemperatureUnits.get(i + 1).getHour(), humidityTemperatureUnits.get(i + 1).getMinute(), humidityTemperatureUnits.get(i + 1).getSecond()))) < 10800000) {
                    temperaturePath.lineTo(x, y);
                } else {
                    temperaturePath.moveTo(x, y);
                }
                humidityCanvas.drawCircle(x, y, display.density, paint);
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth2);
        temperatureCanvas.drawPath(temperaturePath, paint);
    }

    private void drawDayHumidityGraph(ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits) {
        pixelsPerTimeUnit = graphWidth / 1440;
        pixelsPerOnePercent = graphHeight / (dataBound[0] - dataBound[2]);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green))));
        Path humidityPath = new Path();
        x = graphLeftMargin + (pixelsPerTimeUnit * (60 * humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getHour() + humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getMinute()));
        y = graphTopMargin + (pixelsPerOnePercent * (dataBound[0] - humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getHumidity()));
        humidityPath.moveTo(x, y);
        humidityCanvas.drawCircle(x, y, display.density, paint);
        for (int i = humidityTemperatureUnits.size() - 2; i > -1; i--) {
            x = graphLeftMargin + (pixelsPerTimeUnit * (60 * humidityTemperatureUnits.get(i).getHour() + humidityTemperatureUnits.get(i).getMinute()));
            y = graphTopMargin + (pixelsPerOnePercent * (dataBound[0] - humidityTemperatureUnits.get(i).getHumidity()));
            if (Math.abs((NooLiteF.getMillisecond(humidityTemperatureUnits.get(i).getYear(), humidityTemperatureUnits.get(i).getMonth(), humidityTemperatureUnits.get(i).getDay(), humidityTemperatureUnits.get(i).getHour(), humidityTemperatureUnits.get(i).getMinute(), humidityTemperatureUnits.get(i).getSecond()) - NooLiteF.getMillisecond(humidityTemperatureUnits.get(i + 1).getYear(), humidityTemperatureUnits.get(i + 1).getMonth(), humidityTemperatureUnits.get(i + 1).getDay(), humidityTemperatureUnits.get(i + 1).getHour(), humidityTemperatureUnits.get(i + 1).getMinute(), humidityTemperatureUnits.get(i + 1).getSecond()))) < 10800000) {
                humidityPath.lineTo(x, y);
            } else {
                humidityPath.moveTo(x, y);
            }
            humidityCanvas.drawCircle(x, y, display.density, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth2);
        humidityCanvas.drawPath(humidityPath, paint);
    }

    private void drawWeekTemperatureGraph(ArrayList<?> dataUnits) {
        pixelsPerTimeUnit = graphWidth / 10080;
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green))));
        Path temperaturePath = new Path();
        if (dataUnits.get(0) instanceof TemperatureUnit) {
            ArrayList<TemperatureUnit> temperatureUnits = (ArrayList<TemperatureUnit>) dataUnits;
            x = graphLeftMargin + (pixelsPerTimeUnit * (1440 * (temperatureUnits.get(temperatureUnits.size() - 1).getWeekDay() - 1) + (60 * temperatureUnits.get(temperatureUnits.size() - 1).getHour()) + temperatureUnits.get(temperatureUnits.size() - 1).getMinute()));
            y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) temperatureUnits.get(temperatureUnits.size() - 1).getTemperature()) * 10);
            temperaturePath.moveTo(x, y);
            temperatureCanvas.drawCircle(x, y, display.density, paint);
            for (int i = temperatureUnits.size() - 2; i > -1; i--) {
                x = graphLeftMargin + (pixelsPerTimeUnit * (1440 * (temperatureUnits.get(i).getWeekDay() - 1) + (60 * temperatureUnits.get(i).getHour()) + temperatureUnits.get(i).getMinute()));
                y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) temperatureUnits.get(i).getTemperature()) * 10);
                if (Math.abs((NooLiteF.getMillisecond(temperatureUnits.get(i).getYear(), temperatureUnits.get(i).getMonth(), temperatureUnits.get(i).getDay(), temperatureUnits.get(i).getHour(), temperatureUnits.get(i).getMinute(), temperatureUnits.get(i).getSecond()) - NooLiteF.getMillisecond(temperatureUnits.get(i + 1).getYear(), temperatureUnits.get(i + 1).getMonth(), temperatureUnits.get(i + 1).getDay(), temperatureUnits.get(i + 1).getHour(), temperatureUnits.get(i + 1).getMinute(), temperatureUnits.get(i + 1).getSecond()))) < 10800000) {
                    temperaturePath.lineTo(x, y);
                } else {
                    temperaturePath.moveTo(x, y);
                }
                temperatureCanvas.drawCircle(x, y, display.density, paint);
            }
        }
        if (dataUnits.get(0) instanceof HumidityTemperatureUnit) {
            ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits = (ArrayList<HumidityTemperatureUnit>) dataUnits;
            x = graphLeftMargin + (pixelsPerTimeUnit * (1440 * (humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getWeekDay() - 1) + (60 * humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getHour()) + humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getMinute()));
            y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getTemperature()) * 10);
            temperaturePath.moveTo(x, y);
            humidityCanvas.drawCircle(x, y, display.density, paint);
            for (int i = humidityTemperatureUnits.size() - 2; i > -1; i--) {
                x = graphLeftMargin + (pixelsPerTimeUnit * (1440 * (humidityTemperatureUnits.get(i).getWeekDay() - 1) + (60 * humidityTemperatureUnits.get(i).getHour()) + humidityTemperatureUnits.get(i).getMinute()));
                y = graphTopMargin + (pixelsPerOneTenthDegree * (dataBound[0] - (float) humidityTemperatureUnits.get(i).getTemperature()) * 10);
                if (Math.abs((NooLiteF.getMillisecond(humidityTemperatureUnits.get(i).getYear(), humidityTemperatureUnits.get(i).getMonth(), humidityTemperatureUnits.get(i).getDay(), humidityTemperatureUnits.get(i).getHour(), humidityTemperatureUnits.get(i).getMinute(), humidityTemperatureUnits.get(i).getSecond()) - NooLiteF.getMillisecond(humidityTemperatureUnits.get(i + 1).getYear(), humidityTemperatureUnits.get(i + 1).getMonth(), humidityTemperatureUnits.get(i + 1).getDay(), humidityTemperatureUnits.get(i + 1).getHour(), humidityTemperatureUnits.get(i + 1).getMinute(), humidityTemperatureUnits.get(i + 1).getSecond()))) < 10800000) {
                    temperaturePath.lineTo(x, y);
                } else {
                    temperaturePath.moveTo(x, y);
                }
                humidityCanvas.drawCircle(x, y, display.density, paint);
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth2);
        temperatureCanvas.drawPath(temperaturePath, paint);
    }

    private void drawWeekHumidityGraph(ArrayList<HumidityTemperatureUnit> humidityTemperatureUnits) {
        pixelsPerTimeUnit = graphWidth / 10080;
        pixelsPerOnePercent = graphHeight / (dataBound[0] - dataBound[2]);
        paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.green))));
        Path humidityPath = new Path();
        x = graphLeftMargin + (pixelsPerTimeUnit * (1440 * (humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getWeekDay() - 1) + (60 * humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getHour()) + humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getMinute()));
        y = graphTopMargin + (pixelsPerOnePercent * (dataBound[0] - humidityTemperatureUnits.get(humidityTemperatureUnits.size() - 1).getHumidity()));
        humidityCanvas.drawCircle(x, y, display.density, paint);
        humidityPath.moveTo(x, y);
        for (int i = humidityTemperatureUnits.size() - 2; i > -1; i--) {
            x = graphLeftMargin + (pixelsPerTimeUnit * (1440 * (humidityTemperatureUnits.get(i).getWeekDay() - 1) + (60 * humidityTemperatureUnits.get(i).getHour()) + humidityTemperatureUnits.get(i).getMinute()));
            y = graphTopMargin + (pixelsPerOnePercent * (dataBound[0] - humidityTemperatureUnits.get(i).getHumidity()));
            if (Math.abs((NooLiteF.getMillisecond(humidityTemperatureUnits.get(i).getYear(), humidityTemperatureUnits.get(i).getMonth(), humidityTemperatureUnits.get(i).getDay(), humidityTemperatureUnits.get(i).getHour(), humidityTemperatureUnits.get(i).getMinute(), humidityTemperatureUnits.get(i).getSecond()) - NooLiteF.getMillisecond(humidityTemperatureUnits.get(i + 1).getYear(), humidityTemperatureUnits.get(i + 1).getMonth(), humidityTemperatureUnits.get(i + 1).getDay(), humidityTemperatureUnits.get(i + 1).getHour(), humidityTemperatureUnits.get(i + 1).getMinute(), humidityTemperatureUnits.get(i + 1).getSecond()))) < 10800000) {
                humidityPath.lineTo(x, y);
            } else {
                humidityPath.moveTo(x, y);
            }
            humidityCanvas.drawCircle(x, y, display.density, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth2);
        humidityCanvas.drawPath(humidityPath, paint);
    }

    private void drawPlotRectangle(Canvas canvas) {
        plotRectangleBounds.set(plotLeftMargin + plotRectangleRadius, plotTopMargin, plotLeftMargin + plotWidth, plotTopMargin + plotRectangleRadius + graphHeight + plotRectangleRadius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth2);
        if (Settings.isNightMode()) {
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        } else {
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_light))));
        }
        canvas.drawRoundRect(plotRectangleBounds, plotRectangleRadius, plotRectangleRadius, paint);
    }

    private void drawDot(Canvas canvas, ArrayList<?> dataUnits) {
        paint.setStyle(Paint.Style.FILL);
        if (dataUnits.get(0) instanceof TemperatureUnit) {
            TemperatureUnit temperatureUnit = ((ArrayList<TemperatureUnit>) dataUnits).get(0);
            if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(temperatureUnit.getYear(), temperatureUnit.getMonth(), temperatureUnit.getDay(), temperatureUnit.getHour(), temperatureUnit.getMinute(), temperatureUnit.getSecond())) < 10800000) {
                if (Calendar.getInstance().get(Calendar.DATE) == temperatureUnit.getDay()) {
                    canvas.drawCircle(x, y, DOT_RADIUS * display.density, paint);
                }
            }
        }
        if (dataUnits.get(0) instanceof HumidityTemperatureUnit) {
            HumidityTemperatureUnit humidityTemperatureUnit = ((ArrayList<HumidityTemperatureUnit>) dataUnits).get(0);
            if ((Calendar.getInstance().getTimeInMillis() - NooLiteF.getMillisecond(humidityTemperatureUnit.getYear(), humidityTemperatureUnit.getMonth(), humidityTemperatureUnit.getDay(), humidityTemperatureUnit.getHour(), humidityTemperatureUnit.getMinute(), humidityTemperatureUnit.getSecond())) < 10800000) {
                if (Calendar.getInstance().get(Calendar.DATE) == humidityTemperatureUnit.getDay()) {
                    canvas.drawCircle(x, y, DOT_RADIUS * display.density, paint);
                }
            }
        }
    }

    private void drawNoDataLabel(Canvas canvas) {
        paint.setTextSize(noDataLabelSize);
        paint.getTextBounds(NO_DATA_LABEL, 0, NO_DATA_LABEL.length(), textLabelBounds);
        textLabelBounds.set((int) (graphLeftMargin + (graphWidth / 2) - (textLabelBounds.width() / 2) - (plotRectangleRadius / 2)), (int) (graphTopMargin + (graphHeight / 2) - (textLabelBounds.height() / 2) - (plotRectangleRadius / 2)), (int) (graphLeftMargin + (graphWidth / 2) + (textLabelBounds.width() / 2) + (plotRectangleRadius / 2)), (int) (graphTopMargin + (graphHeight / 2) + (textLabelBounds.height() / 2) + (plotRectangleRadius / 2)));
        paint.setStyle(Paint.Style.FILL);
        if (Settings.isNightMode()) {
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.black_dark))));
        } else {
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.white))));
        }
        canvas.drawRect(textLabelBounds, paint);
        paint.setStyle(Paint.Style.STROKE);
        if (Settings.isNightMode()) {
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey_800))));
        } else {
            paint.setColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, R.color.grey))));
        }
        paint.getTextBounds(NO_DATA_LABEL, 0, NO_DATA_LABEL.length(), textLabelBounds);
        x = graphLeftMargin + (graphWidth / 2) - (textLabelBounds.width() / 2);
        y = graphTopMargin + (graphHeight / 2) + (textLabelBounds.height() / 2);
        canvas.drawText(NO_DATA_LABEL, x - (1 * display.density), y - (2 * display.density), paint);
    }
}
