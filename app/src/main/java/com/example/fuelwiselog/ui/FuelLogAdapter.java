package com.example.fuelwiselog.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelwiselog.databinding.ItemFuelRecordBinding;

import java.text.DecimalFormat;

// Adapter class that manages the list of fuel records shown in the RecyclerView
public class FuelLogAdapter extends ListAdapter<FuelLogItem, FuelLogAdapter.VH> {

    // Callback interface to handle delete clicks in the main Activity
    public interface Actions {
        void onDelete(long recordId);
    }

    private final Actions actions;
    private final DecimalFormat df2 = new DecimalFormat("0.00");
    private final DecimalFormat df0 = new DecimalFormat("0");

    public FuelLogAdapter(Actions actions) {
        super(DIFF);
        this.actions = actions;
    }

    // Creates a new view holder for a single list item layout
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFuelRecordBinding b = ItemFuelRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    // Binds data to the view at a specific position
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemFuelRecordBinding b;

        VH(ItemFuelRecordBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        // Populates the UI elements with data from the FuelLogItem
        void bind(FuelLogItem item) {
            // Populate header details for the record row.
            b.tvVehicleName.setText(item.vehicleName == null ? "Vehicle" : item.vehicleName);
            b.tvDate.setText(item.dateIso == null ? "" : item.dateIso);
            b.tvVehicleIcon.setText(VehicleEmojiMapper.getEmoji(item.vehicleType));

            try {
                b.cardColor.setCardBackgroundColor(Color.parseColor(item.vehicleColorHex));
            } catch (Exception ignored) {}

            // Format and show stats.
            b.tvVolume.setText(df2.format(item.liters) + "L");
            b.tvCost.setText("RM" + df2.format(item.costRm));
            b.tvMileage.setText(df0.format(item.mileageKm) + "km");

            // Delegate delete action to the host screen (Activity).
            b.btnDelete.setOnClickListener(v -> actions.onDelete(item.recordId));

            // Shows efficiency stats only if previous history exists; otherwise hides the block
            if (item.hasEfficiency) {
                // Show efficiency block when enough data exists.
                b.layoutEfficiency.setVisibility(android.view.View.VISIBLE);
                b.tvNoEfficiency.setVisibility(android.view.View.GONE);

                b.tvSince.setText("Since last fill-up (" + df0.format(item.distanceKm) + " km)");
                b.tvRmPerKm.setText("RM " + df2.format(item.rmPerKm));
                b.tvLPer100.setText(df2.format(item.litersPer100Km) + " L");
            } else {
                // Hide efficiency block for first or incomplete records.
                b.layoutEfficiency.setVisibility(android.view.View.GONE);
                b.tvNoEfficiency.setVisibility(android.view.View.VISIBLE);
            }
        }
    }

    // DiffUtil optimization: calculates differences between lists to animate updates efficiently
    private static final DiffUtil.ItemCallback<FuelLogItem> DIFF = new DiffUtil.ItemCallback<FuelLogItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull FuelLogItem oldItem, @NonNull FuelLogItem newItem) {
            return oldItem.recordId == newItem.recordId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FuelLogItem o, @NonNull FuelLogItem n) {
            return o.vehicleId == n.vehicleId
                    && safeEq(o.vehicleName, n.vehicleName)
                    && safeEq(o.vehicleColorHex, n.vehicleColorHex)
                    && safeEq(o.vehicleType, n.vehicleType)
                    && safeEq(o.dateIso, n.dateIso)
                    && o.liters == n.liters
                    && o.costRm == n.costRm
                    && o.mileageKm == n.mileageKm
                    && o.hasEfficiency == n.hasEfficiency
                    && o.distanceKm == n.distanceKm
                    && o.rmPerKm == n.rmPerKm
                    && o.litersPer100Km == n.litersPer100Km;
        }

        private boolean safeEq(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null) return false;
            return a.equals(b);
        }
    };
}