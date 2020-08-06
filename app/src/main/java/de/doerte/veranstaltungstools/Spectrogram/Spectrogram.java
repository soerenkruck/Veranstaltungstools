/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 * https://developer.android.com/training/custom-views/create-view
 * https://developer.android.com/training/custom-views/custom-drawing
 */

package de.doerte.veranstaltungstools.Spectrogram;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import de.doerte.veranstaltungstools.R;
import de.doerte.veranstaltungstools.analyse.Mathematics;

public class Spectrogram extends View {

    public boolean isColored = false;
    private ArrayList<ArrayList<Float>> valuesX;
    private Paint pointsPaint, backgroundPaint, textPaint;

    private Canvas canvas;

    private int canHeight, canWidth;
    private int pixelSize = 2;

    private int counter = 1;
    private int pause = 5;
    private boolean hasPermission = false;

    public static final int BACKGROUND_SW = Color.rgb(0.2f, 0.2f, 0.2f);
    public static final int BACKGROUND_BL = Color.rgb(0,0, 1f);

    public Spectrogram(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Spectrogram,
                0, 0);

        try {
            isColored = a.getBoolean(R.styleable.Spectrogram_colored, false);
        } finally {
            a.recycle();
        }

        valuesX = new ArrayList<>();
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.canWidth = w + 8;
        this.canHeight = h;
    }

    private void init() {
        canvas = new Canvas();

        pointsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isColored) {

            } else {
                backgroundPaint.setColor(Color.rgb(0.2f, 0.2f, 0.2f));
            }

        }

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(32);
        textPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (hasPermission) {
            //if (counter == pause) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.drawRect(0, 0, canWidth, canHeight, backgroundPaint);
                if (valuesX != null) {
                    for (int ix = 0; ix < valuesX.size() - 1; ix++) {
                        if (valuesX.get(ix) != null) {
                            for (int iy = 0; iy < valuesX.get(ix).size() - 1; iy++) {
                                if (valuesX.get(ix).get(iy) != null) {
                                    if (isColored) {
                                        float x = valuesX.get(ix).get(iy);
                                        //pointsPaint.setColor(Color.rgb(
                                        //        Mathematics.mapFloat(valuesX.get(ix).get(iy), 0, 100, 1, 0),
                                        //        Mathematics.mapFloat(valuesX.get(ix).get(iy), 0, 100, 0.33f, 1),
                                        //        Mathematics.mapFloat(valuesX.get(ix).get(iy), 0, 100, 1, 0)));
                                        float r = Mathematics.getSmoothRed(x);
                                        float g = Mathematics.getSmoothGreen(x);
                                        float b = Mathematics.getSmoothBlue(x);
                                        pointsPaint.setColor(Color.rgb(r, g, b));
                                    } else {
                                        float bw = Mathematics.mapFloat(valuesX.get(ix).get(iy) * pixelSize, 0, 100, 0.2f, 1);
                                        int bwColor = Color.rgb(bw, bw, bw);
                                        pointsPaint.setColor(bwColor);
                                    }
                                    if (valuesX.get(ix).get(iy) > 1)
                                        canvas.drawRect(ix * pixelSize, iy * pixelSize, ix * pixelSize + pixelSize, iy * pixelSize + pixelSize, pointsPaint);
                                }
                            }
                        }
                    }
                }
            } else {
                canvas.drawText(getResources().getString(R.string.outdated1).replace("|", "\n"),
                        64, 64, textPaint);
                canvas.drawText(getResources().getString(R.string.outdated2).replace("|", "\n"),
                        64, 104, textPaint);
            }
        } else {
            canvas.drawText(getResources().getString(R.string.no_mic_permission).replace("|", "\n"),
                    64, 64, textPaint);
        }
            //counter = 0;
        //} else {
          //  counter++;
        //}

        invalidate();
        requestLayout();
    }

    // Pushes the values into at the end of the spectrogram
    public void push(ArrayList<Float> values) {
        if (valuesX.size() > (canWidth/pixelSize)) {
            valuesX.remove(0);
        }
        valuesX.add(values);
    }

    // Setter and Getter
    public void setColored(boolean colored) {
        isColored = colored;
        if (isColored) {
            backgroundPaint.setColor(BACKGROUND_BL);
        } else {
            backgroundPaint.setColor(BACKGROUND_SW);
        }
        invalidate();
        requestLayout();
    }
    public boolean isColored() {
        return isColored;
    }
    public void setPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
    public boolean getPermission() {
        return this.hasPermission;
    }
    public int getPixelSize() { // The default value is 2;
        return pixelSize;
    }
    public void setPixelSize(int size) {
        this.pixelSize = size;
    }
}
