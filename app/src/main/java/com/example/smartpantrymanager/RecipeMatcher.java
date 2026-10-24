package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * THE STRICT-MATCHING RULE.
 *
 * A recipe is only "suggested" if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. If even one ingredient is missing
 * (or there is not enough of it), the recipe is NOT suggested.
 */
public class RecipeMatcher {

    /** The result of checking one recipe against the pantry. */
    public static class MatchResult {
        private final Recipe recipe;
        private final List<RecipeIngredient> missing;  // ingredients the user lacks
        private final boolean usesExpiring;            // uses something expiring soon?

        MatchResult(Recipe recipe, List<RecipeIngredient> missing, boolean usesExpiring) {
            this.recipe = recipe;
            this.missing = missing;
            this.usesExpiring = usesExpiring;
        }

        public Recipe getRecipe() { return recipe; }
        public List<RecipeIngredient> getMissing() { return missing; }
        public boolean usesExpiring() { return usesExpiring; }
    }

    /** Checks ONE recipe against the pantry (used on the Recipe Detail screen). */
    public static MatchResult evaluate(Recipe recipe, List<PantryItem> pantry) {
        return evaluate(recipe, buildTotals(pantry), buildExpiringKeys(pantry));
    }

    /** Only the recipes with NOTHING missing: the strict suggestions list. */
    public static List<MatchResult> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        return filterByMissingCount(recipes, pantry, 0);
    }

    /** Recipes missing EXACTLY one ingredient: the separate "Almost There" list. */
    public static List<MatchResult> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        return filterByMissingCount(recipes, pantry, 1);
    }

    private static List<MatchResult> filterByMissingCount(List<Recipe> recipes,
                                                          List<PantryItem> pantry,
                                                          int missingCount) {
        Map<String, Double> totals = buildTotals(pantry);
        Set<String> expiringKeys = buildExpiringKeys(pantry);

        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe, totals, expiringKeys);
            if (result.getMissing().size() == missingCount) {
                results.add(result);
            }
        }
        return results;
    }

    /** Core check: compares each recipe ingredient with what the pantry holds. */
    private static MatchResult evaluate(Recipe recipe, Map<String, Double> totals,
                                        Set<String> expiringKeys) {
        List<RecipeIngredient> missing = new ArrayList<>();
        boolean usesExpiring = false;

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            String key = IngredientNormaliser.normaliseName(ingredient.getName());
            String totalKey = key + "|" + IngredientNormaliser.unitFamily(ingredient.getUnit());
            double needed = IngredientNormaliser.toBase(ingredient.getQuantity(), ingredient.getUnit());
            double have = totals.containsKey(totalKey) ? totals.get(totalKey) : 0.0;

            if (have + 0.0001 < needed) {
                missing.add(ingredient);          // not enough, so this recipe fails the strict rule
            } else if (expiringKeys.contains(key)) {
                usesExpiring = true;              // enough, and the stock is about to expire
            }
        }
        return new MatchResult(recipe, missing, usesExpiring);
    }

    /**
     * Adds up everything in the pantry, grouped by (cleaned name + unit family).
     * Two rows such as "Eggs 2 pcs" and "egg 3 pcs" are added together to 5.
     */
    private static Map<String, Double> buildTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = IngredientNormaliser.normaliseName(item.getName())
                    + "|" + IngredientNormaliser.unitFamily(item.getUnit());
            double amount = IngredientNormaliser.toBase(item.getQuantity(), item.getUnit());
            double existing = totals.containsKey(key) ? totals.get(key) : 0.0;
            totals.put(key, existing + amount);
        }
        return totals;
    }

    /** The cleaned names of pantry items that are expiring soon (or already expired). */
    private static Set<String> buildExpiringKeys(List<PantryItem> pantry) {
        Set<String> keys = new HashSet<>();
        for (PantryItem item : pantry) {
            long days = ExpiryUtils.daysUntil(item.getExpiryDate());
            if (days != Long.MAX_VALUE && days <= ExpiryUtils.EXPIRING_SOON_DAYS) {
                keys.add(IngredientNormaliser.normaliseName(item.getName()));
            }
        }
        return keys;
    }

    /** Sorts results A-Z, optionally putting "use it up" recipes first. */
    public static void sortResults(List<MatchResult> results, final boolean expiringFirst) {
        Collections.sort(results, (a, b) -> {
            if (expiringFirst && a.usesExpiring() != b.usesExpiring()) {
                return a.usesExpiring() ? -1 : 1;
            }
            return a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
        });
    }
}