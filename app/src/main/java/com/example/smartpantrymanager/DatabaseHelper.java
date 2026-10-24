package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles everything to do with the SQLite database:
 * creating the tables, filling in the recipes the first time,
 * and the Create / Read / Update / Delete (CRUD) methods.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String T_PANTRY = "pantry_items";
    private static final String T_RECIPES = "recipes";
    private static final String T_INGREDIENTS = "recipe_ingredients";

    // One shared helper for the whole app (the "singleton" pattern)
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Turn on foreign keys so deleting a recipe also deletes its ingredient lines
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Runs ONLY the first time the app is installed: build tables and add recipes
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "minutes INTEGER NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE)");

        seedRecipes(db);
    }

    // Only used if DB_VERSION is raised in future: simply rebuild everything
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // =====================================================================
    //  PANTRY CRUD
    // =====================================================================

    // CREATE: add a new pantry item and return its new id
    public long addPantryItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    // READ: get every pantry item, sorted A-Z
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                T_PANTRY, null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                items.add(pantryFromCursor(cursor));
            }
        }
        return items;
    }

    // READ: get one pantry item by id (null if it does not exist)
    public PantryItem getPantryItem(long id) {
        try (Cursor cursor = getReadableDatabase().query(
                T_PANTRY, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return pantryFromCursor(cursor);
            }
        }
        return null;
    }

    // UPDATE: change an existing pantry item (matched by its id)
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(
                T_PANTRY, toValues(item), "id = ?", new String[]{String.valueOf(item.getId())});
    }

    // DELETE: remove one pantry item
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    // DELETE: remove everything from the pantry (used by Settings)
    public void deleteAllPantryItems() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    // Packs a PantryItem into the format SQLite needs
    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        if (item.getExpiryDate() == null) {
            values.putNull("expiry_date");
        } else {
            values.put("expiry_date", item.getExpiryDate());
        }
        return values;
    }

    // Builds a PantryItem from the current row of a database cursor
    private PantryItem pantryFromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    // =====================================================================
    //  RECIPES (read only: the user does not edit recipes)
    // =====================================================================

    // Returns all recipes, each one already filled with its ingredient list
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Recipe> recipes = new LinkedHashMap<>();

        // Step 1: load the recipes themselves
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe recipe = new Recipe(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getInt(c.getColumnIndexOrThrow("minutes")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                recipes.put(recipe.getId(), recipe);
            }
        }

        // Step 2: load every ingredient line and attach it to its recipe
        try (Cursor c = db.query(T_INGREDIENTS, null, null, null, null, null, "id ASC")) {
            while (c.moveToNext()) {
                Recipe recipe = recipes.get(c.getLong(c.getColumnIndexOrThrow("recipe_id")));
                if (recipe != null) {
                    recipe.getIngredients().add(new RecipeIngredient(
                            c.getString(c.getColumnIndexOrThrow("name")),
                            c.getDouble(c.getColumnIndexOrThrow("quantity")),
                            c.getString(c.getColumnIndexOrThrow("unit"))));
                }
            }
        }
        return new ArrayList<>(recipes.values());
    }

    // Finds a single recipe by id (null if not found)
    public Recipe getRecipe(long id) {
        for (Recipe recipe : getAllRecipes()) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        return null;
    }

    // =====================================================================
    //  COOK A RECIPE: take the used ingredients out of the pantry
    // =====================================================================

    public void cookRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // all changes succeed together, or none do
        try {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                String key = IngredientNormaliser.normaliseName(ingredient.getName());
                String family = IngredientNormaliser.unitFamily(ingredient.getUnit());
                double remaining = IngredientNormaliser.toBase(
                        ingredient.getQuantity(), ingredient.getUnit());

                // Use the soonest-expiring stock first (items with no date go last)
                List<PantryItem> rows = new ArrayList<>();
                try (Cursor c = db.rawQuery("SELECT * FROM " + T_PANTRY
                        + " ORDER BY expiry_date IS NULL, expiry_date ASC", null)) {
                    while (c.moveToNext()) {
                        rows.add(pantryFromCursor(c));
                    }
                }

                for (PantryItem row : rows) {
                    if (remaining <= 0.0001) {
                        break;
                    }
                    boolean sameIngredient = key.equals(IngredientNormaliser.normaliseName(row.getName()))
                            && family.equals(IngredientNormaliser.unitFamily(row.getUnit()));
                    if (!sameIngredient) {
                        continue;
                    }

                    double rowAmount = IngredientNormaliser.toBase(row.getQuantity(), row.getUnit());
                    if (rowAmount <= remaining + 0.0001) {
                        // Used it all up: delete this row
                        db.delete(T_PANTRY, "id = ?", new String[]{String.valueOf(row.getId())});
                        remaining -= rowAmount;
                    } else {
                        // Only used part of it: reduce the quantity (in the row's own unit)
                        double leftBase = rowAmount - remaining;
                        double leftInUnit = Math.round(
                                leftBase / IngredientNormaliser.toBase(1, row.getUnit()) * 100.0) / 100.0;
                        ContentValues values = new ContentValues();
                        values.put("quantity", leftInUnit);
                        db.update(T_PANTRY, values, "id = ?", new String[]{String.valueOf(row.getId())});
                        remaining = 0;
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // =====================================================================
    //  SAMPLE PANTRY (for demos and screenshots, used from Settings)
    // =====================================================================

    public void addSamplePantry() {
        addPantryItem(new PantryItem(-1, "Eggs", 6, "pcs", ExpiryUtils.isoFromNowPlusDays(5)));
        addPantryItem(new PantryItem(-1, "Cheddar cheese", 200, "g", ExpiryUtils.isoFromNowPlusDays(20)));
        addPantryItem(new PantryItem(-1, "Butter", 250, "g", ExpiryUtils.isoFromNowPlusDays(30)));
        addPantryItem(new PantryItem(-1, "Bread", 6, "pcs", ExpiryUtils.isoFromNowPlusDays(1)));
        addPantryItem(new PantryItem(-1, "Milk", 500, "ml", ExpiryUtils.isoFromNowPlusDays(2)));
        addPantryItem(new PantryItem(-1, "Tomatoes", 5, "pcs", ExpiryUtils.isoFromNowPlusDays(2)));
        addPantryItem(new PantryItem(-1, "Pasta", 500, "g", null));
        addPantryItem(new PantryItem(-1, "Garlic", 4, "pcs", null));
        addPantryItem(new PantryItem(-1, "Olive oil", 8, "tbsp", null));
    }

    // =====================================================================
    //  SEED DATA: the 19 recipes added on first run
    // =====================================================================

    private void seedRecipes(SQLiteDatabase db) {
        // Each ingredient is written as "Name|quantity|unit"
        addRecipe(db, "Cheese Omelette", 10,
                "1. Beat the eggs in a bowl and season.\n2. Melt the butter in a frying pan over a medium heat.\n3. Pour in the eggs and let them set for a minute.\n4. Add the grated cheese, fold in half and serve.",
                "Eggs|3|pcs", "Cheddar cheese|50|g", "Butter|10|g");

        addRecipe(db, "Tomato and Garlic Pasta", 20,
                "1. Boil the pasta in salted water until tender.\n2. Chop the tomatoes and garlic.\n3. Fry the garlic in the olive oil for one minute, then add the tomatoes and simmer for 10 minutes.\n4. Drain the pasta, stir it into the sauce and serve.",
                "Pasta|200|g", "Tomatoes|4|pcs", "Garlic|2|pcs", "Olive oil|2|tbsp");

        addRecipe(db, "Egg Fried Rice", 15,
                "1. Cook the rice and let it cool slightly.\n2. Scramble the eggs in a hot pan and set aside.\n3. Fry the rice and chopped spring onions for 3 minutes.\n4. Stir in the soy sauce and eggs, then serve.",
                "Rice|150|g", "Eggs|2|pcs", "Soy sauce|2|tbsp", "Spring onions|2|pcs");

        addRecipe(db, "Banana Pancakes", 15,
                "1. Mash the bananas in a bowl.\n2. Whisk in the eggs, flour and milk to make a batter.\n3. Fry spoonfuls of batter for 2 minutes each side.\n4. Serve warm.",
                "Bananas|2|pcs", "Eggs|2|pcs", "Flour|100|g", "Milk|100|ml");

        addRecipe(db, "Classic Pancakes", 20,
                "1. Whisk the flour, eggs and milk into a smooth batter.\n2. Melt a little butter in a frying pan.\n3. Pour in a thin layer of batter and cook for 1 minute each side.\n4. Serve with your favourite topping.",
                "Flour|100|g", "Eggs|2|pcs", "Milk|300|ml", "Butter|20|g");

        addRecipe(db, "Cheese on Toast", 8,
                "1. Heat the grill.\n2. Toast the bread lightly and spread with butter.\n3. Top with grated cheese.\n4. Grill until golden and bubbling.",
                "Bread|4|pcs", "Cheddar cheese|100|g", "Butter|10|g");

        addRecipe(db, "Jacket Potato with Beans", 60,
                "1. Prick the potatoes and bake at 200C for about 55 minutes.\n2. Warm the beans in a pan.\n3. Split the potatoes, add butter and beans.\n4. Top with grated cheese.",
                "Potatoes|2|pcs", "Baked beans|400|g", "Cheddar cheese|40|g", "Butter|10|g");

        addRecipe(db, "Garlic Butter Mushrooms on Toast", 15,
                "1. Slice the mushrooms and crush the garlic.\n2. Melt the butter and fry the mushrooms for 5 minutes.\n3. Add the garlic for the last minute.\n4. Spoon over toasted bread.",
                "Mushrooms|250|g", "Butter|20|g", "Garlic|2|pcs", "Bread|2|pcs");

        addRecipe(db, "Fresh Tomato Soup", 30,
                "1. Fry the chopped onion in the oil until soft.\n2. Add the chopped tomatoes and stock.\n3. Simmer for 20 minutes.\n4. Blend until smooth and season.",
                "Tomatoes|6|pcs", "Onion|1|pcs", "Vegetable stock|500|ml", "Olive oil|1|tbsp");

        addRecipe(db, "Veggie Noodle Stir-fry", 20,
                "1. Cook the noodles and drain.\n2. Stir-fry the sliced carrots, peppers and onion for 5 minutes.\n3. Add the noodles and soy sauce.\n4. Toss for 2 minutes and serve.",
                "Carrots|2|pcs", "Peppers|1|pcs", "Onion|1|pcs", "Soy sauce|2|tbsp", "Noodles|150|g");

        addRecipe(db, "Chickpea Curry", 30,
                "1. Fry the chopped onion until soft.\n2. Stir in the curry powder for one minute.\n3. Add the tomatoes, chickpeas and coconut milk.\n4. Simmer for 20 minutes and serve.",
                "Chickpeas|400|g", "Onion|1|pcs", "Curry powder|2|tbsp", "Tomatoes|3|pcs", "Coconut milk|200|ml");

        addRecipe(db, "Red Lentil Soup", 35,
                "1. Fry the chopped onion and carrots.\n2. Add the rinsed lentils and stock.\n3. Simmer for 25 minutes until soft.\n4. Blend partly and season.",
                "Red lentils|150|g", "Carrots|2|pcs", "Onion|1|pcs", "Vegetable stock|750|ml");

        addRecipe(db, "Berry Yoghurt Bowl", 5,
                "1. Spoon the yoghurt into a bowl.\n2. Top with sliced strawberries and oats.\n3. Drizzle with honey.",
                "Greek yoghurt|150|g", "Strawberries|100|g", "Honey|1|tbsp", "Oats|30|g");

        addRecipe(db, "Scrambled Eggs on Toast", 10,
                "1. Beat the eggs with the milk.\n2. Melt the butter in a pan over a low heat.\n3. Add the eggs and stir gently until just set.\n4. Serve on hot toast.",
                "Eggs|3|pcs", "Bread|2|pcs", "Butter|10|g", "Milk|2|tbsp");

        addRecipe(db, "Tuna Pasta Bake", 30,
                "1. Cook the pasta and drain.\n2. Mix with the tuna and sweetcorn in an oven dish.\n3. Cover with grated cheese.\n4. Bake at 190C for 15 minutes.",
                "Pasta|200|g", "Tuna|160|g", "Sweetcorn|100|g", "Cheddar cheese|50|g");

        addRecipe(db, "Aubergine and Tomato Bake", 40,
                "1. Slice the aubergines and tomatoes.\n2. Layer them in a dish with olive oil.\n3. Top with torn mozzarella.\n4. Bake at 200C for 30 minutes.",
                "Aubergines|2|pcs", "Tomatoes|4|pcs", "Mozzarella|125|g", "Olive oil|2|tbsp");

        addRecipe(db, "Courgette Fritters", 20,
                "1. Grate the courgettes and squeeze out the water.\n2. Mix with the eggs, flour and grated cheese.\n3. Fry spoonfuls for 3 minutes each side.\n4. Serve hot.",
                "Courgettes|2|pcs", "Eggs|2|pcs", "Flour|50|g", "Cheddar cheese|40|g");

        addRecipe(db, "Banana Porridge", 10,
                "1. Heat the oats and milk gently, stirring.\n2. Cook for 5 minutes until creamy.\n3. Top with sliced banana and a drizzle of honey.",
                "Oats|50|g", "Milk|250|ml", "Bananas|1|pcs", "Honey|1|tsp");

        addRecipe(db, "Quick Bean Chilli", 25,
                "1. Fry the chopped onion until soft.\n2. Add the chilli powder, tomatoes and kidney beans.\n3. Simmer for 15 minutes.\n4. Serve with the cooked rice.",
                "Kidney beans|400|g", "Onion|1|pcs", "Tomatoes|3|pcs", "Rice|150|g", "Chilli powder|1|tsp");
    }

    // Helper used by seedRecipes: inserts one recipe and all its ingredient lines
    private void addRecipe(SQLiteDatabase db, String name, int minutes, String steps,
                           String... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("minutes", minutes);
        recipeValues.put("steps", steps);
        long recipeId = db.insert(T_RECIPES, null, recipeValues);

        for (String line : ingredients) {
            String[] parts = line.split("\\|");   // "Eggs|3|pcs" -> Eggs, 3, pcs
            ContentValues values = new ContentValues();
            values.put("recipe_id", recipeId);
            values.put("name", parts[0]);
            values.put("quantity", Double.parseDouble(parts[1]));
            values.put("unit", parts[2]);
            db.insert(T_INGREDIENTS, null, values);
        }
    }
}