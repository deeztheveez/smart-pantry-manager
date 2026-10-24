package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared helper code used by the three main screens
 * (Pantry, Recipes and Settings) so we do not repeat it three times.
 */
public class NavHelper {

    /**
     * Connects the bottom navigation bar. Tapping a tab opens that screen
     * using an Intent, then closes the current one.
     */
    public static void setupBottomNav(final Activity activity, final int selectedItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(selectedItemId);   // highlight the current tab

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true;                      // already on this screen
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = PantryActivity.class;
            } else if (id == R.id.nav_suggestions) {
                target = SuggestionsActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            activity.startActivity(new Intent(activity, target));  // open the new screen
            activity.finish();                                     // close this one
            activity.overridePendingTransition(0, 0);              // no slide animation
            return true;
        });
    }

    /**
     * Keeps the screen content clear of the status bar and navigation bar
     * on newer Android versions that draw edge-to-edge.
     */
    public static void applySystemBarPadding(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
    }
}