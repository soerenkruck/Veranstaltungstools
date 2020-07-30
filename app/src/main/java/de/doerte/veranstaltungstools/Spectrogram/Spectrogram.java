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
import java.util.Random;

import de.doerte.veranstaltungstools.R;
import de.doerte.veranstaltungstools.analyse.Mathematics;

public class Spectrogram extends View {

    public boolean isColored = false;
    private ArrayList<ArrayList<Float>> valuesX;
    private Paint pointsPaint;

    private Canvas canvas;

    private int canHeight, canWidth;

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

        this.canWidth = w - 32;
        this.canHeight = h;
    }

    private void init() {
        canvas = new Canvas();

        pointsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (isColored)
            pointsPaint.setColor(Color.BLUE);
        else
            pointsPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (valuesX != null) {
            for (int ix = 0; ix < valuesX.size()-1; ix++) {
                for (int iy = 0; iy < valuesX.get(ix).size()-1; iy++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (isColored) {
                            pointsPaint.setColor(Color.rgb(Mathematics.mapFloat(valuesX.get(ix).get(iy)*4, 0, 100, 0.2f, 1),
                                    0.1f,
                                    Mathematics.mapFloat(valuesX.get(ix).get(iy), 0, 100, 0.8f, 0f)));
                        } else {
                            pointsPaint.setColor(Color.rgb(Mathematics.mapFloat(valuesX.get(ix).get(iy)*4, 0, 100, 1, 0),
                                    Mathematics.mapFloat(valuesX.get(ix).get(iy)*4, 0, 100, 1, 0),
                                    Mathematics.mapFloat(valuesX.get(ix).get(iy)*4, 0, 100, 1, 0)));
                        }
                    }

                    canvas.drawRect(ix*4, iy*4, ix*4+4, iy*4+4, pointsPaint);

                }
            }
        }

        invalidate();
        requestLayout();

    }

    public void setColored(boolean colored) {
        isColored = colored;
        invalidate();
        requestLayout();
    }
    public boolean isColored() {
        return isColored;
    }

    public void push(ArrayList<Float> values) {
        if (valuesX.size() > canWidth/4) {
            valuesX.remove(0);
        }

        valuesX.add(values);
    }
}
