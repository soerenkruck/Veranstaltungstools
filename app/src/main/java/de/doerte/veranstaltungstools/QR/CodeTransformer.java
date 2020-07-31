/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools.QR;

public class CodeTransformer {

    public static String getShortCode(String code) {
        String shortCode = "";

        String tmp[] = code.split("-");
        for (int i = 0; i < tmp.length; i++) {
            shortCode += tmp[i].charAt(0);
        }
        return shortCode;
    }
}
