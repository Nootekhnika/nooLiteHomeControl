package com.noolitef.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.noolitef.R;

public class PercentageSeekBar extends AppCompatSeekBar {

    private int thumbSize;
    private TextPaint textPaint;
    private Rect bounds;

    public PercentageSeekBar(Context context) {
        super(context);
        init();
    }

    public PercentageSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PercentageSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setTextColor(int color) {
        textPaint.setColor(ResourcesCompat.getColor(getResources(), color, null));
    }

    private void init() {
        thumbSize = getResources().getDimensionPixelSize(R.dimen.dp_48);

        textPaint = new TextPaint();
        textPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.black_light, null));
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_small));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        bounds = new Rect();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String progressText = Integer.toString(getProgress()).concat("%");
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
}
