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
}
