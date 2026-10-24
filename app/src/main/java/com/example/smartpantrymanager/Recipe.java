package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe: a name, a cooking time, the method and its list of ingredients.
 */
public class Recipe {

    private final long id;
    private final String name;
    private final int minutes;
    private final String steps;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, int minutes, String steps) {
        this.id = id;
        this.name = name;
        this.minutes = minutes;
        this.steps = steps;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public int getMinutes() { return minutes; }
    public String getSteps() { return steps; }

    // The list is returned directly so the database code can add to it
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
