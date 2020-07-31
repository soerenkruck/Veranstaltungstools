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
    private int pixelSize = 2;

    private int counter = 1;
    private int pause = 5;

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
        pointsPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //if (counter == pause) {
            if (valuesX != null) {
                for (int ix = 0; ix < valuesX.size() - 1; ix++) {
                    if (valuesX.get(ix) != null) {
                        for (int iy = 0; iy < valuesX.get(ix).size() - 1; iy++) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (valuesX.get(ix).get(iy) > 0.1) {
                                    if (valuesX.get(ix).get(iy) != null) {
                                        if (isColored) {
                                            pointsPaint.setColor(Color.rgb(Mathematics.mapFloat(valuesX.get(ix).get(iy) * pixelSize, 0, 100, 0f, 1),
                                                    0f,
                                                    Mathematics.mapFloat(valuesX.get(ix).get(iy), 0, 100, 1f, 0f)));
                                        } else {
                                            float bw = Mathematics.mapFloat(valuesX.get(ix).get(iy) * pixelSize, 0, 100, 1, 0);
                                            int bwColor = Color.rgb(bw, bw, bw);
                                            pointsPaint.setColor(bwColor);
                                        }
                                    }
                                }
                            }
                            if (valuesX.get(ix).get(iy) > 0.05)
                                canvas.drawRect(ix * pixelSize, iy * pixelSize, ix * pixelSize + pixelSize, iy * pixelSize + pixelSize, pointsPaint);
                        }
                    }
                }
            }
            //counter = 0;
        //} else {
          //  counter++;
        //}

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
        if (valuesX.size() > (canWidth/pixelSize)) {
            valuesX.remove(0);
        }
        valuesX.add(values);
    }

    public int getPixelSize() { // The default value is 2;
        return pixelSize;
    }
    public void setPixelSize(int size) {
        this.pixelSize = size;
    }
}
