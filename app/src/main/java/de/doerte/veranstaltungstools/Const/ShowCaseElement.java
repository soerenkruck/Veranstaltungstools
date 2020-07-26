/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools.Const;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

public class ShowCaseElement {

    final static float TITLE_TEXT_SIZE = 86;
    final static float TEXT_SIZE = 86;

    public static TextPaint titleTextPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setSubpixelText(true);
        paint.setTextSize(TITLE_TEXT_SIZE);
        paint.setFakeBoldText(true);
        return new TextPaint(paint);
    }

    public static TextPaint textPaint() {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(64);
        return new TextPaint(textPaint);
    }

}
