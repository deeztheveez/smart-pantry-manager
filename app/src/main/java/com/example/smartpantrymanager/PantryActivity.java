package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient the user has.
 * This is the launcher screen (the first one that opens).
 */
public class PantryActivity extends AppCompatActivity {

    private static final String TAG = "PantryActivity";

    private DatabaseHelper db;
    private SettingsManager settings;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private TextView tvBanner;

    // Lifecycle step 1: the screen is created. Set things up ONCE here.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        Log.d(TAG, "onCreate: screen created");

        NavHelper.applySystemBarPadding(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);
        settings = new SettingsManager(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvBanner = findViewById(R.id.tvExpiryBanner);

        // Set up the RecyclerView with our custom adapter
        RecyclerView recyclerView = findViewById(R.id.rvPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new PantryAdapter.OnPantryActionListener() {
            @Override
            public void onItemClicked(PantryItem item) {
                // Open the Add/Edit screen in "edit" mode, passing the item's id by Intent
                Intent intent = new Intent(PantryActivity.this, AddEditIngredientActivity.class);
                intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(PantryItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setAdapter(adapter);

        // The + button opens the Add/Edit screen in "add" mode (no id passed)
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v ->
                startActivity(new Intent(PantryActivity.this, AddEditIngredientActivity.class)));

        NavHelper.setupBottomNav(this, R.id.nav_pantry);
    }

    // Lifecycle step 2: the screen is visible again (also after coming back from
    // Add/Edit). Reloading here means the list is always up to date.
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: reloading the pantry from the database");
        loadPantry();
    }

    // Reads the database and updates the list, empty message and banner
    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setItems(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Count items that are expired or expiring soon
        int soonCount = 0;
        for (PantryItem item : items) {
            long days = ExpiryUtils.daysUntil(item.getExpiryDate());
            if (days != Long.MAX_VALUE && days <= ExpiryUtils.EXPIRING_SOON_DAYS) {
                soonCount++;
            }
        }

        if (settings.isAlertsEnabled() && soonCount > 0) {
            tvBanner.setText("⚠ " + soonCount + (soonCount == 1 ? " item is" : " items are")
                    + " expiring soon or expired. Check the Recipes tab to use "
                    + (soonCount == 1 ? "it" : "them") + " up!");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    // Ask before deleting, so nothing disappears by accident
    private void confirmDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}