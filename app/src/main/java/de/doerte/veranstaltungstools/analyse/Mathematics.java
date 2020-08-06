/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools.analyse;

public class Mathematics {

    public static int map(int i, int in_start, int in_end, int out_start, int out_end) {
        return out_start + ((out_end - out_start) / (in_end - in_start)) * (i - in_start);
    }

    public static float mapFloat(float i, float in_start, float in_end, float out_start, float out_end) {
        return out_start + ((out_end - out_start) / (in_end - in_start)) * (i - in_start);
    }

    public static float sqr(float a) {
        return a*a;
    }

    public static float getSmoothBlue(float x) {
        float blue_val = ((-1)*sqr(0.025f*x)) + 1;
        if (blue_val < 0) {
            blue_val = 0;
        }
        if (blue_val > 1) {
            blue_val = 1;
        }
        return blue_val;
    }
    public static float getSmoothGreen(float x) {
        float green_val = ((-1)*sqr(0.025f*(x-50))) + 1;
        if (green_val < 0) {
            green_val = 0;
        }
        if (green_val > 1) {
            green_val = 1;
        }
        return green_val;
    }
    public static float getSmoothRed(float x) {
        float red_val = ((-1)*sqr(0.025f*(x-100))) + 1;
        if (red_val < 0) {
            red_val = 0;
        }
        if (red_val > 1) {
            red_val = 1;
        }
        return red_val;
    }
}
