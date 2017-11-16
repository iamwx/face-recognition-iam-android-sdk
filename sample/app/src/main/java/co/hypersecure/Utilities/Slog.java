package co.hypersecure.Utilities;

import android.util.Log;

/**
 * Created by Awanish Raj on 03/01/16.
 */
public class Slog {

    public static final boolean isLogging = Utils.debugMode;

    public static void e(String log_tag, String message) {
        if (isLogging)
            Log.e(log_tag, message);
    }

    public static void e(String log_tag, String message, Throwable tr) {
        if (isLogging)
            Log.e(log_tag, message, tr);
    }

    public static void d(String log_tag, String message) {
        if (isLogging)
            Log.d(log_tag, message);
    }

    public static void i(String log_tag, String message) {
        if (isLogging)
            Log.i(log_tag, message);
    }

    public static void v(String log_tag, String message) {
        if (isLogging)
            Log.v(log_tag, message);
    }
}
