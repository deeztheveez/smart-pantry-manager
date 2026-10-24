package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local unit tests for the matching logic (they run on your computer, no emulator needed).
 */
public class MatcherTest {

    // A recipe needing FIVE ingredients
    private Recipe makeRecipe() {
        Recipe recipe = new Recipe(1, "Test Pasta", 20, "Cook it");
        recipe.getIngredients().add(new RecipeIngredient("Pasta", 200, "g"));
        recipe.getIngredients().add(new RecipeIngredient("Tomatoes", 4, "pcs"));
        recipe.getIngredients().add(new RecipeIngredient("Garlic", 2, "pcs"));
        recipe.getIngredients().add(new RecipeIngredient("Olive oil", 2, "tbsp"));
        recipe.getIngredients().add(new RecipeIngredient("Basil", 1, "pcs"));
        return recipe;
    }

    // A pantry holding FOUR of the five ingredients
    private List<PantryItem> pantryWithFour() {
        List<PantryItem> pantry = new ArrayList<>();
        pantry.add(new PantryItem(1, "pasta", 0.5, "kg", null));   // 500 g, enough
        pantry.add(new PantryItem(2, "tomato", 6, "pcs", null));   // singular name, enough
        pantry.add(new PantryItem(3, "Garlic", 3, "pcs", null));
        pantry.add(new PantryItem(4, "Olive oil", 4, "tbsp", null));
        return pantry;
    }

    @Test
    public void fourOfFiveIngredients_isNotSuggested() {
        List<Recipe> recipes = Collections.singletonList(makeRecipe());
        assertEquals(0, RecipeMatcher.strictMatches(recipes, pantryWithFour()).size());
        // ...but it does appear in the separate Almost There list
        assertEquals(1, RecipeMatcher.almostThere(recipes, pantryWithFour()).size());
    }

    @Test
    public void allFiveIngredients_isSuggested() {
        List<PantryItem> pantry = pantryWithFour();
        pantry.add(new PantryItem(5, "Basil", 2, "pcs", null));
        List<Recipe> recipes = Collections.singletonList(makeRecipe());
        assertEquals(1, RecipeMatcher.strictMatches(recipes, pantry).size());
    }

    @Test
    public void notEnoughQuantity_isRejected() {
        List<PantryItem> pantry = pantryWithFour();
        pantry.add(new PantryItem(5, "Basil", 2, "pcs", null));
        pantry.set(0, new PantryItem(1, "Pasta", 100, "g", null)); // only 100 g, needs 200 g
        List<Recipe> recipes = Collections.singletonList(makeRecipe());
        assertEquals(0, RecipeMatcher.strictMatches(recipes, pantry).size());
    }

    @Test
    public void singularAndPluralNamesMatch() {
        assertEquals(IngredientNormaliser.normaliseName("Tomatoes"),
                IngredientNormaliser.normaliseName("tomato"));
        assertEquals(IngredientNormaliser.normaliseName("Strawberries"),
                IngredientNormaliser.normaliseName("strawberry"));
        assertEquals(IngredientNormaliser.normaliseName("Aubergine"),
                IngredientNormaliser.normaliseName("eggplants"));
    }

    @Test
    public void unitsAreConverted() {
        assertEquals(1000.0, IngredientNormaliser.toBase(1, "kg"), 0.001);
        assertEquals(30.0, IngredientNormaliser.toBase(2, "tbsp"), 0.001);
    }
}