package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;

/**
 * Settings screen: two on/off switches (saved with SharedPreferences)
 * and two testing buttons for the pantry.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        NavHelper.applySystemBarPadding(findViewById(R.id.root));

        final DatabaseHelper db = DatabaseHelper.getInstance(this);
        final SettingsManager settings = new SettingsManager(this);

        // Show the saved values, and save again whenever a switch is changed
        SwitchCompat switchAlerts = findViewById(R.id.switchAlerts);
        switchAlerts.setChecked(settings.isAlertsEnabled());
        switchAlerts.setOnCheckedChangeListener((button, checked) ->
                settings.setAlertsEnabled(checked));

        SwitchCompat switchPrioritise = findViewById(R.id.switchPrioritise);
        switchPrioritise.setChecked(settings.isPrioritiseExpiring());
        switchPrioritise.setOnCheckedChangeListener((button, checked) ->
                settings.setPrioritiseExpiring(checked));

        // Adds nine example ingredients (handy for demos and screenshots)
        MaterialButton btnSample = findViewById(R.id.btnSample);
        btnSample.setOnClickListener(v -> {
            db.addSamplePantry();
            Toast.makeText(this, "Sample pantry added", Toast.LENGTH_SHORT).show();
        });

        // Removes every pantry item (after asking)
        MaterialButton btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clear pantry")
                .setMessage("Delete ALL ingredients from your pantry?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    db.deleteAllPantryItems();
                    Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());

        NavHelper.setupBottomNav(this, R.id.nav_settings);
    }
}