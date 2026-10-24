package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves the user's two settings using SharedPreferences
 * (a tiny key-value store, ideal for on/off switches).
 */
public class SettingsManager {

    private static final String PREFS_NAME = "pantry_settings";
    private static final String KEY_ALERTS = "expiry_alerts";
    private static final String KEY_PRIORITISE = "prioritise_expiring";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Show the "items expiring soon" banner on the pantry screen? (default: yes)
    public boolean isAlertsEnabled() {
        return prefs.getBoolean(KEY_ALERTS, true);
    }

    public void setAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERTS, enabled).apply();
    }

    // Put recipes that use expiring ingredients at the top? (default: yes)
    public boolean isPrioritiseExpiring() {
        return prefs.getBoolean(KEY_PRIORITISE, true);
    }

    public void setPrioritiseExpiring(boolean enabled) {
        prefs.edit().putBoolean(KEY_PRIORITISE, enabled).apply();
    }
}