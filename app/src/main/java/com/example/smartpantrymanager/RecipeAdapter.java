package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the recipe list: shows each recipe's name, time,
 * an optional "expiring soon" badge and any missing ingredient.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<RecipeMatcher.MatchResult> results = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setResults(List<RecipeMatcher.MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        final RecipeMatcher.MatchResult result = results.get(position);
        final Recipe recipe = result.getRecipe();

        holder.tvName.setText(recipe.getName());
        holder.tvInfo.setText(recipe.getMinutes() + " mins  •  "
                + recipe.getIngredients().size() + " ingredients");

        // Show the badge only if this recipe uses something about to expire
        holder.tvBadge.setVisibility(result.usesExpiring() ? View.VISIBLE : View.GONE);

        // Show what is missing (only happens in the Almost There list)
        if (result.getMissing().isEmpty()) {
            holder.tvMissing.setVisibility(View.GONE);
        } else {
            StringBuilder names = new StringBuilder();
            for (RecipeIngredient missing : result.getMissing()) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(missing.getName());
            }
            holder.tvMissing.setText("Missing: " + names);
            holder.tvMissing.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvInfo;
        final TextView tvBadge;
        final TextView tvMissing;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvInfo = itemView.findViewById(R.id.tvRecipeInfo);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvMissing = itemView.findViewById(R.id.tvMissing);
        }
    }
}