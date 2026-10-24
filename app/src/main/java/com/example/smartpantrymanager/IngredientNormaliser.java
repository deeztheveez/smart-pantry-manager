package com.example.smartpantrymanager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Cleans up ingredient names and units so that simple differences
 * ("Tomato" vs "tomatoes", "1 kg" vs "1000 g") do not break the matching.
 */
public class IngredientNormaliser {

    // The units the user can choose from in the Add/Edit screen
    public static final String[] UNITS = {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    // Words that do not change which ingredient it is
    private static final Set<String> FILLER_WORDS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "chopped", "diced", "sliced", "ripe", "whole"));

    // British / American single-word names: both become the same key
    private static final Map<String, String> WORD_SYNONYMS = new HashMap<>();
    // Two-word names
    private static final Map<String, String> PHRASE_SYNONYMS = new HashMap<>();

    static {
        WORD_SYNONYMS.put("aubergine", "eggplant");
        WORD_SYNONYMS.put("courgette", "zucchini");
        WORD_SYNONYMS.put("coriander", "cilantro");
        WORD_SYNONYMS.put("rocket", "arugula");
        WORD_SYNONYMS.put("yoghurt", "yogurt");
        PHRASE_SYNONYMS.put("spring onion", "scallion");
    }

    /**
     * Turns any ingredient name into a simple "key" used only for comparing.
     * "Cherry Tomatoes" -> "cherry tomato", "Aubergines" -> "eggplant".
     */
    public static String normaliseName(String raw) {
        if (raw == null) {
            return "";
        }
        // Lower case, and replace anything that is not a letter with a space
        String cleaned = raw.toLowerCase(Locale.UK).replaceAll("[^a-z ]", " ");
        String[] words = cleaned.trim().split("\\s+");

        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty() || FILLER_WORDS.contains(word)) {
                continue; // skip empty bits and filler words
            }
            String single = singularise(word);
            if (WORD_SYNONYMS.containsKey(single)) {
                single = WORD_SYNONYMS.get(single);
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(single);
        }

        String key = builder.toString();
        if (PHRASE_SYNONYMS.containsKey(key)) {
            key = PHRASE_SYNONYMS.get(key);
        }
        return key;
    }

    /** Very simple plural -> singular rules (no full language processing needed). */
    private static String singularise(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies")) {                         // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes")) {                         // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us")) {   // hummus, couscous stay the same
            return word;
        }
        if (word.endsWith("s")) {                           // eggs -> egg
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    /** Groups units that can be compared with each other. */
    public static String unitFamily(String unit) {
        String u = unit == null ? "" : unit.toLowerCase(Locale.UK);
        switch (u) {
            case "g":
            case "kg":
                return "weight";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "volume";
            case "pcs":
                return "count";
            default:
                return u;
        }
    }

    /** Converts an amount into a base unit: grams, millilitres or pieces. */
    public static double toBase(double quantity, String unit) {
        String u = unit == null ? "" : unit.toLowerCase(Locale.UK);
        switch (u) {
            case "kg":   return quantity * 1000;
            case "l":    return quantity * 1000;
            case "tsp":  return quantity * 5;
            case "tbsp": return quantity * 15;
            case "cup":  return quantity * 240;
            default:     return quantity;   // g, ml and pcs are already base units
        }
    }

    /** Shows 3.0 as "3" and 2.5 as "2.5". */
    public static String formatQuantity(double quantity) {
        if (quantity == Math.rint(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(Math.round(quantity * 100) / 100.0);
    }
}