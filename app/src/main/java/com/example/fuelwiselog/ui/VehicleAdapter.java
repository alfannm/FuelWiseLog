package com.example.fuelwiselog.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelwiselog.data.Vehicle;
import com.example.fuelwiselog.databinding.ItemVehicleBinding;

// Adapter for the "Manage Vehicles" list; handles displaying, selecting, editing, and deleting vehicles
public class VehicleAdapter extends ListAdapter<Vehicle, VehicleAdapter.VH> {

    // Callbacks to send user actions (clicks) back to the Activity
    public interface Actions {
        void onEdit(Vehicle v);
        void onSelect(Vehicle v);
        void onDelete(Vehicle v);
    }

    private final Actions actions;
    private long selectedVehicleId = -1;

    public VehicleAdapter(Actions actions) {
        super(DIFF);
        this.actions = actions;
    }

    // Updates which vehicle is currently "Active" and refreshes the list to show the purple highlight
    public void setSelectedVehicleId(long id) {
        // Refresh selection state across visible rows.
        selectedVehicleId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding b = ItemVehicleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemVehicleBinding b;

        VH(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Vehicle v) {
            // Bind base vehicle info.
            b.tvName.setText(v.getName());
            b.tvType.setText(v.getType());
            b.tvVehicleIcon.setText(VehicleEmojiMapper.getEmoji(v.getType()));

            // Show plate only when provided; otherwise hide the text view to save space.
            if (v.getPlateNumber() != null && !v.getPlateNumber().trim().isEmpty()) {
                b.tvPlate.setVisibility(android.view.View.VISIBLE);
                b.tvPlate.setText(v.getPlateNumber());
            } else {
                b.tvPlate.setVisibility(android.view.View.GONE);
            }

            try {
                b.cardColor.setCardBackgroundColor(Color.parseColor(v.getColorHex()));
            } catch (Exception e) {
                b.cardColor.setCardBackgroundColor(Color.LTGRAY);
            }

            // Visual Logic: If this is the selected vehicle, show the "Active" badge and purple border.
            boolean isActive = v.getId() == selectedVehicleId;
            b.tvActive.setVisibility(isActive ? android.view.View.VISIBLE : android.view.View.GONE);
            b.cardOuter.setStrokeColor(isActive ? Color.parseColor("#B67CFF") : Color.TRANSPARENT);

            // Delegate actions to the host screen.
            b.btnEdit.setOnClickListener(view -> actions.onEdit(v));
            b.btnSelect.setOnClickListener(view -> actions.onSelect(v));
            b.btnDelete.setOnClickListener(view -> actions.onDelete(v));
        }
    }

    // DiffUtil: Optimizes list performance by calculating exactly which rows changed
    private static final DiffUtil.ItemCallback<Vehicle> DIFF = new DiffUtil.ItemCallback<Vehicle>() {
        @Override
        public boolean areItemsTheSame(@NonNull Vehicle oldItem, @NonNull Vehicle newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Vehicle oldItem, @NonNull Vehicle newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getType().equals(newItem.getType())
                    && safeEq(oldItem.getPlateNumber(), newItem.getPlateNumber())
                    && safeEq(oldItem.getColorHex(), newItem.getColorHex());
        }

        private boolean safeEq(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null) return false;
            return a.equals(b);
        }
    };
}