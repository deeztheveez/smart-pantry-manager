package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add / Edit Ingredient screen.
 * If an item id arrives in the Intent we are EDITING; otherwise we are ADDING.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    // The "key" used to pass the item id between screens in an Intent
    public static final String EXTRA_ITEM_ID = "item_id";

    private DatabaseHelper db;
    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private TextInputEditText etExpiry;
    private Spinner spUnit;
    private MaterialButton btnDelete;

    private long itemId = -1;          // -1 means "adding a new item"
    private String expiryIso = null;   // chosen expiry date as yyyy-MM-dd (or null)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        NavHelper.applySystemBarPadding(findViewById(R.id.root));

        db = DatabaseHelper.getInstance(this);

        // Find all the views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        btnDelete = findViewById(R.id.btnDelete);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnClearDate = findViewById(R.id.btnClearDate);

        // Back arrow in the toolbar closes this screen
        toolbar.setNavigationOnClickListener(v -> finish());

        // Fill the unit drop-down from our list of units
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, IngredientNormaliser.UNITS);
        spUnit.setAdapter(unitAdapter);

        // Did the previous screen send us an item id? If so, we are editing.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            loadItemForEditing();
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            expiryIso = null;
            etExpiry.setText("");
        });
        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    // Edit mode: fetch the item from the database and fill in the form
    private void loadItemForEditing() {
        PantryItem item = db.getPantryItem(itemId);
        if (item == null) {
            finish();   // item no longer exists
            return;
        }

        ((MaterialToolbar) findViewById(R.id.toolbar)).setTitle("Edit Ingredient");
        btnDelete.setVisibility(android.view.View.VISIBLE);

        etName.setText(item.getName());
        etQuantity.setText(IngredientNormaliser.formatQuantity(item.getQuantity()));

        // Select the saved unit in the drop-down
        for (int i = 0; i < IngredientNormaliser.UNITS.length; i++) {
            if (IngredientNormaliser.UNITS[i].equalsIgnoreCase(item.getUnit())) {
                spUnit.setSelection(i);
                break;
            }
        }

        expiryIso = item.getExpiryDate();
        if (expiryIso != null) {
            etExpiry.setText(ExpiryUtils.formatForDisplay(expiryIso));
        }
    }

    // Opens the calendar pop-up
    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        new DatePickerDialog(this, (picker, year, month, day) -> {
            // Months start at 0 in Java, so add 1
            expiryIso = String.format(Locale.UK, "%04d-%02d-%02d", year, month + 1, day);
            etExpiry.setText(ExpiryUtils.formatForDisplay(expiryIso));
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
    }

    // INPUT VALIDATION: returns true only if the form is filled in sensibly
    private boolean validateForm() {
        boolean valid = true;

        String name = getText(etName).trim();
        if (name.isEmpty()) {
            tilName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            tilName.setError("The name must contain some letters");
            valid = false;
        } else if (name.length() > 40) {
            tilName.setError("Please keep the name under 40 characters");
            valid = false;
        } else {
            tilName.setError(null);
        }

        String quantityText = getText(etQuantity).trim();
        if (quantityText.isEmpty()) {
            tilQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                double quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    tilQuantity.setError("Quantity must be more than 0");
                    valid = false;
                } else if (quantity > 100000) {
                    tilQuantity.setError("That quantity looks too large");
                    valid = false;
                } else {
                    tilQuantity.setError(null);
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError("Please enter a valid number");
                valid = false;
            }
        }
        return valid;
    }

    // CREATE or UPDATE, depending on the mode
    private void saveItem() {
        if (!validateForm()) {
            return;   // stop here and show the errors
        }

        String name = getText(etName).trim();
        double quantity = Double.parseDouble(getText(etQuantity).trim());
        String unit = spUnit.getSelectedItem().toString();

        PantryItem item = new PantryItem(itemId, name, quantity, unit, expiryIso);

        if (itemId == -1) {
            db.addPantryItem(item);
            Toast.makeText(this, name + " added to your pantry", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(item);
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish();   // go back to the pantry list (it reloads in onResume)
    }

    // DELETE (only available in edit mode)
    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle("Delete ingredient").setMessage("Remove this ingredient from your pantry?").setPositiveButton("Delete", (dialog, which) -> {
            db.deletePantryItem(itemId);
            Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
            finish();
        }).setNegativeButton("Cancel", null).show();
    }

    // Small helper: safely get the text from an input box
    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }
}