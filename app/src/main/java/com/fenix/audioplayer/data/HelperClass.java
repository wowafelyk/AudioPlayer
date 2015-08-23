package com.fenix.audioplayer.data;

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

    public static String timeFormatMillis(int millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        return String.format("%01d:%02d", minutes, seconds);
    }

    public static String timeFormatMillis(String millis) {
        try {
            int value = Integer.valueOf(millis);
            return timeFormatMillis(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String timeFormatSeconds(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        return String.format("%01d:%02d", minutes, seconds);
    }
}
