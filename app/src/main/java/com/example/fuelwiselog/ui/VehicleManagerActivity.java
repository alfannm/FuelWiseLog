package com.example.fuelwiselog.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fuelwiselog.util.Prefs;
import com.example.fuelwiselog.data.Vehicle;
import com.example.fuelwiselog.databinding.ActivityVehicleManagerBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VehicleManagerActivity extends AppCompatActivity {

    private ActivityVehicleManagerBinding binding;
    private FuelViewModel viewModel;
    private VehicleAdapter adapter;

    private Vehicle editing = null;

    private final List<String> vehicleTypes = Arrays.asList("Car", "Motorcycle", "Lorry", "Van", "Others");
    private final List<String> vehicleColors = Arrays.asList(
            "#B4A7D6", "#F5B8D4", "#A7C7E7", "#B8E6D5",
            "#F5D7A8", "#F5B8B8", "#A0BFFF", "#C3E59F"
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FuelViewModel.class);

        setupTypeDropdown();
        setupColorChips();
        setupRecycler();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnToggleForm.setOnClickListener(v -> toggleForm());

        binding.btnCancelVehicle.setOnClickListener(v -> hideForm());
        binding.btnSaveVehicle.setOnClickListener(v -> onSaveVehicle());

        viewModel.getVehicles().observe(this, vehicles -> {
            binding.tvSubtitle.setText(vehicles.size() + " vehicle" + (vehicles.size() == 1 ? "" : "s"));

            binding.layoutEmpty.setVisibility(vehicles.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

            adapter.submitList(new ArrayList<>(vehicles));
            adapter.setSelectedVehicleId(Prefs.getSelectedVehicleId(this));
        });
    }

    private void setupRecycler() {
        adapter = new VehicleAdapter(new VehicleAdapter.Actions() {
            @Override public void onEdit(Vehicle v) { startEdit(v); }

            @Override public void onSelect(Vehicle v) {
                Prefs.setSelectedVehicleId(VehicleManagerActivity.this, v.getId());
                adapter.setSelectedVehicleId(v.getId());
                Toast.makeText(VehicleManagerActivity.this, "Selected: " + v.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override public void onDelete(Vehicle v) {
                new AlertDialog.Builder(VehicleManagerActivity.this)
                        .setTitle("Delete vehicle?")
                        .setMessage("Delete " + v.getName() + "?\nAll related fuel records will also be deleted.")
                        .setPositiveButton("Delete", (d, which) -> viewModel.deleteVehicle(v))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.rvVehicles.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVehicles.setAdapter(adapter);
    }

    private void setupTypeDropdown() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, vehicleTypes);
        binding.actType.setAdapter(a);
        binding.actType.setText(vehicleTypes.get(0), false);
    }

    private void setupColorChips() {
        binding.chipGroupColors.removeAllViews();
        for (int i = 0; i < vehicleColors.size(); i++) {
            String hex = vehicleColors.get(i);

            Chip chip = new Chip(this);
            int chipSize = dpToPx(36);
            int chipMargin = dpToPx(6);
            android.view.ViewGroup.MarginLayoutParams params =
                    new android.view.ViewGroup.MarginLayoutParams(chipSize, chipSize);
            params.setMargins(0, 0, chipMargin, chipMargin);
            chip.setLayoutParams(params);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(true);
            chip.setCheckedIconResource(com.example.fuelwiselog.R.drawable.ic_check_24);
            chip.setCheckedIconTint(ColorStateList.valueOf(Color.BLACK));
            chip.setChipIconVisible(false);
            chip.setCloseIconVisible(false);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setChipCornerRadius(dpToPx(10));
            chip.setText("");
            chip.setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));

            int outlineColor = MaterialColors.getColor(chip,
                    com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
            chip.setChipStrokeWidth(dpToPx(2));
            chip.setChipStrokeColor(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{outlineColor, Color.TRANSPARENT}
            ));

            try {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(hex)));
            } catch (Exception e) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.LTGRAY));
            }

            binding.chipGroupColors.addView(chip);

            if (i == 0) binding.chipGroupColors.check(chip.getId());
        }
    }

    private void toggleForm() {
        if (binding.layoutForm.getVisibility() == android.view.View.VISIBLE) {
            hideForm();
        } else {
            startAdd();
        }
    }

    private void startAdd() {
        editing = null;
        binding.tvFormTitle.setText("Add New Vehicle");
        binding.btnSaveVehicle.setText("Add Vehicle");
        binding.layoutForm.setVisibility(android.view.View.VISIBLE);

        binding.etName.setText("");
        binding.etPlate.setText("");
        binding.actType.setText(vehicleTypes.get(0), false);
        binding.chipGroupColors.check(binding.chipGroupColors.getChildAt(0).getId());
    }

    private void startEdit(Vehicle v) {
        editing = v;
        binding.tvFormTitle.setText("Edit Vehicle");
        binding.btnSaveVehicle.setText("Update Vehicle");
        binding.layoutForm.setVisibility(android.view.View.VISIBLE);

        binding.etName.setText(v.getName());
        binding.etPlate.setText(v.getPlateNumber() == null ? "" : v.getPlateNumber());
        binding.actType.setText(v.getType(), false);

        // select color
        for (int i = 0; i < binding.chipGroupColors.getChildCount(); i++) {
            Chip c = (Chip) binding.chipGroupColors.getChildAt(i);
            String target = vehicleColors.get(i);
            if (target.equalsIgnoreCase(v.getColorHex())) {
                binding.chipGroupColors.check(c.getId());
                break;
            }
        }
    }

    private void hideForm() {
        binding.layoutForm.setVisibility(android.view.View.GONE);
        editing = null;
    }

    private void onSaveVehicle() {
        String name = binding.etName.getText() == null ? "" : binding.etName.getText().toString().trim();
        String plate = binding.etPlate.getText() == null ? "" : binding.etPlate.getText().toString().trim().toUpperCase();
        String type = binding.actType.getText() == null ? vehicleTypes.get(0) : binding.actType.getText().toString();

        if (name.isEmpty()) {
            binding.tilName.setError("Name required");
            return;
        } else {
            binding.tilName.setError(null);
        }

        int checkedIndex = binding.chipGroupColors.indexOfChild(findCheckedChip());
        String colorHex = checkedIndex >= 0 && checkedIndex < vehicleColors.size() ? vehicleColors.get(checkedIndex) : vehicleColors.get(0);

        if (editing == null) {
            Vehicle v = new Vehicle(name, type, colorHex, plate.isEmpty() ? null : plate);
            viewModel.insertVehicle(v);
            Toast.makeText(this, "Vehicle added", Toast.LENGTH_SHORT).show();
        } else {
            editing.setName(name);
            editing.setType(type);
            editing.setPlateNumber(plate.isEmpty() ? null : plate);
            editing.setColorHex(colorHex);
            viewModel.updateVehicle(editing);
            Toast.makeText(this, "Vehicle updated", Toast.LENGTH_SHORT).show();
        }

        hideForm();
    }

    private Chip findCheckedChip() {
        int id = binding.chipGroupColors.getCheckedChipId();
        return binding.chipGroupColors.findViewById(id);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
