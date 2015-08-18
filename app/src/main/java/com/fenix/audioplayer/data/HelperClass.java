package com.fenix.audioplayer.data;

import android.util.Log;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by fenix on 16.08.2015.
 */
public class HelperClass {
    public static String getFolder(String s) {
        String path = new File(s).getParent();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String timeFormat(int millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        return String.format("%01d:%02d", minutes, seconds);
    }

    public static String timeFormat(String millis) {
        int value = 0;
        try {
            value = Integer.valueOf(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeFormat(value);
    }
    public static void getPrefs(){

    }
}
