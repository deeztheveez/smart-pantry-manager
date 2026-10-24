package com.example.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * The adapter is the "bridge" between the list of PantryItem objects
 * and the RecyclerView on screen. It creates one row per item and fills it in.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    // The screen listens for taps through this interface
    public interface OnPantryActionListener {
        void onItemClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnPantryActionListener listener;

    public PantryAdapter(OnPantryActionListener listener) {
        this.listener = listener;
    }

    // Replace the list contents and redraw
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // Called when RecyclerView needs a brand new row: inflate item_pantry.xml
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    // Called to fill a row with the data for one position in the list
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(
                IngredientNormaliser.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        // Work out the expiry message and colour
        long days = ExpiryUtils.daysUntil(item.getExpiryDate());
        String message;
        int colourId;
        if (days == Long.MAX_VALUE) {
            message = "No expiry date";
            colourId = R.color.expiry_none;
        } else if (days < 0) {
            long ago = Math.abs(days);
            message = "Expired " + ago + (ago == 1 ? " day" : " days") + " ago";
            colourId = R.color.expiry_expired;
        } else if (days == 0) {
            message = "Expires today";
            colourId = R.color.expiry_soon;
        } else if (days <= ExpiryUtils.EXPIRING_SOON_DAYS) {
            message = "Expires in " + days + (days == 1 ? " day" : " days");
            colourId = R.color.expiry_soon;
        } else {
            message = "Expires " + ExpiryUtils.formatForDisplay(item.getExpiryDate());
            colourId = R.color.expiry_ok;
        }

        int colour = ContextCompat.getColor(context, colourId);
        holder.tvExpiry.setText(message);
        holder.tvExpiry.setTextColor(colour);
        holder.viewStripe.setBackgroundColor(colour);

        // Tap the row to edit, tap the bin to delete
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Holds references to the views in one row so they are not looked up again and again
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvExpiry;
        final View viewStripe;
        final ImageButton btnDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            viewStripe = itemView.findViewById(R.id.viewStripe);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}