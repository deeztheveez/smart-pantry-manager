package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Recipe Detail screen: full ingredient list (with a tick or cross for each),
 * the method, and a button to "cook" the recipe (which uses up the ingredients).
 */
public class RecipeDetailActivity extends AppCompatActivity {

    // The key used to receive the recipe id from the previous screen's Intent
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private DatabaseHelper db;
    private Recipe recipe;
    private TextView tvIngredients;
    private MaterialButton btnCook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        NavHelper.applySystemBarPadding(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Read the recipe id that was sent with the Intent
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tvRecipeName);
        TextView tvMeta = findViewById(R.id.tvMeta);
        TextView tvSteps = findViewById(R.id.tvSteps);
        tvIngredients = findViewById(R.id.tvIngredients);
        btnCook = findViewById(R.id.btnCook);

        toolbar.setTitle(recipe.getName());
        tvName.setText(recipe.getName());
        tvMeta.setText(recipe.getMinutes() + " minutes");
        tvSteps.setText(recipe.getSteps());

        btnCook.setOnClickListener(v -> confirmCook());

        showIngredients();
    }

    // Lists each ingredient with ✓ (you have enough) or ✗ (you are missing it)
    private void showIngredients() {
        RecipeMatcher.MatchResult result = RecipeMatcher.evaluate(recipe, db.getAllPantryItems());
        List<RecipeIngredient> missing = result.getMissing();

        StringBuilder text = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean have = !missing.contains(ingredient);
            text.append(have ? "✓  " : "✗  ")
                    .append(ingredient.getName())
                    .append(" – ")
                    .append(IngredientNormaliser.formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append("\n");
        }
        tvIngredients.setText(text.toString().trim());

        // The cook button only works if nothing is missing
        if (missing.isEmpty()) {
            btnCook.setEnabled(true);
            btnCook.setText("Cook this (uses up the ingredients)");
        } else {
            btnCook.setEnabled(false);
            btnCook.setText("Missing ingredients – can't cook yet");
        }
    }

    // Ask first, then remove the ingredients from the pantry
    private void confirmCook() {
        new AlertDialog.Builder(this)
                .setTitle("Cook " + recipe.getName() + "?")
                .setMessage("This removes the ingredients from your pantry.")
                .setPositiveButton("Cook it", (dialog, which) -> {
                    db.cookRecipe(recipe);
                    Toast.makeText(this, "Enjoy your meal! Pantry updated.", Toast.LENGTH_LONG).show();
                    showIngredients();   // refresh ticks and crosses
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}