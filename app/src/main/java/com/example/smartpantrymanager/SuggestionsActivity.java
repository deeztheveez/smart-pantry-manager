package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen.
 * "Ready to cook" = strict matches only (nothing missing).
 * "Almost there"  = recipes missing exactly one ingredient (kept separate).
 */
public class SuggestionsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SettingsManager settings;
    private RecipeAdapter adapter;
    private MaterialButtonToggleGroup toggleGroup;
    private TextView tvSummary;
    private TextView tvEmpty;

    private List<RecipeMatcher.MatchResult> readyList = new ArrayList<>();
    private List<RecipeMatcher.MatchResult> almostList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        NavHelper.applySystemBarPadding(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);
        settings = new SettingsManager(this);
        tvSummary = findViewById(R.id.tvSummary);
        tvEmpty = findViewById(R.id.tvEmpty);
        toggleGroup = findViewById(R.id.toggleGroup);

        RecyclerView recyclerView = findViewById(R.id.rvRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipe -> {
            // Open the detail screen and pass the recipe id by Intent
            Intent intent = new Intent(SuggestionsActivity.this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Start on the "Ready to cook" tab, then react whenever the user switches tab
        toggleGroup.check(R.id.btnReady);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showCurrentList();
            }
        });

        NavHelper.setupBottomNav(this, R.id.nav_suggestions);
    }

    // Re-run the matching every time the screen appears, in case the pantry changed
    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> recipes = db.getAllRecipes();

        // THE STRICT-MATCHING RULE is applied here
        readyList = RecipeMatcher.strictMatches(recipes, pantry);
        almostList = RecipeMatcher.almostThere(recipes, pantry);

        boolean expiringFirst = settings.isPrioritiseExpiring();
        RecipeMatcher.sortResults(readyList, expiringFirst);
        RecipeMatcher.sortResults(almostList, expiringFirst);

        showCurrentList();
    }

    // Shows whichever list the selected tab asks for
    private void showCurrentList() {
        boolean showReady = toggleGroup.getCheckedButtonId() == R.id.btnReady;
        List<RecipeMatcher.MatchResult> list = showReady ? readyList : almostList;
        adapter.setResults(list);

        if (showReady) {
            tvSummary.setText(list.size() == 1
                    ? "You can cook 1 recipe right now"
                    : "You can cook " + list.size() + " recipes right now");
            tvEmpty.setText("No recipes match your pantry yet.\nAdd more ingredients to see suggestions.");
        } else {
            tvSummary.setText(list.size() == 1
                    ? "1 recipe is only one ingredient away"
                    : list.size() + " recipes are only one ingredient away");
            tvEmpty.setText("No recipes are just one ingredient short right now.");
        }

        // Friendly message instead of a blank screen
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}