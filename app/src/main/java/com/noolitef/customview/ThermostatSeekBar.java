package com.noolitef.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.noolitef.R;

public class ThermostatSeekBar extends AppCompatSeekBar {

    private int thumbSize;
    private int textColor;
    private TextPaint textPaint;
    private Rect bounds;

    public ThermostatSeekBar(Context context) {
        super(context);
        init();
    }

    public ThermostatSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThermostatSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    private void init() {
        thumbSize = getResources().getDimensionPixelSize(R.dimen.dp_48);
        textColor = R.color.black_light;

        textPaint = new TextPaint();
        textPaint.setColor(ResourcesCompat.getColor(getResources(), textColor, null));
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_small));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        bounds = new Rect();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String progressText = Integer.toString(getProgress() + 5).concat("°C");
        textPaint.getTextBounds(progressText, 0, progressText.length(), bounds);

        int leftPadding = getPaddingLeft() - getThumbOffset();
        int rightPadding = getPaddingRight() - getThumbOffset();
        int width = getWidth() - leftPadding - rightPadding;
        float progressRatio = (float) getProgress() / getMax();
        float thumbOffset = thumbSize * (.5f - progressRatio);
        float thumbX = progressRatio * width + thumbOffset;
        float thumbY = getHeight() / 2f + bounds.height() / 2f;

        canvas.drawText(progressText, thumbX, thumbY, textPaint);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            getProgressDrawable().setAlpha(255);
            textPaint.setColor(ResourcesCompat.getColor(getResources(), textColor, null));
        } else {
            getProgressDrawable().setAlpha(127);
            textPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.grey_800, null));
        }
    }
}
