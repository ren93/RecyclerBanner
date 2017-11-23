package com.renny.recyclerbanner.layoutmanager;

import android.content.res.Resources;

import java.util.Locale;

/**
 * Created by Dajavu on 25/10/2017.
 */

public class Util {
    private static float density = Resources.getSystem().getDisplayMetrics().density;

    public static int dp2px(float dpValue) {

        return (int) (dpValue * density + 0.5);
    }

    public static String formatFloat(float value) {
        return String.format(Locale.getDefault(), "%.3f", value);
    }
}
