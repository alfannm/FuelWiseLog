package com.example.fuelwiselog.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

// Utility class to handle SharedPreferences (saving small settings like Theme and Active Vehicle)
public final class Prefs {

    private Prefs() {}

    private static final String FILE = "fuelwise_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_SELECTED_VEHICLE_ID = "selected_vehicle_id";

    // Checks the saved theme preference and applies it (Light/Dark/System)
    public static void applySavedNightMode(Context context) {
        AppCompatDelegate.setDefaultNightMode(getNightMode(context));
    }

    public static int getNightMode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        return sp.getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    // Saves the user's theme choice and updates the app appearance immediately
    public static void setNightMode(Context context, int mode) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_NIGHT_MODE, mode)
                .apply();

        AppCompatDelegate.setDefaultNightMode(mode);
    }

    // Retrieves the ID of the last selected vehicle so the app remembers it on restart
    public static long getSelectedVehicleId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        return sp.getLong(KEY_SELECTED_VEHICLE_ID, -1L);
    }

    // Persists the currently active vehicle ID
    public static void setSelectedVehicleId(Context context, long vehicleId) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_SELECTED_VEHICLE_ID, vehicleId)
                .apply();
    }
}