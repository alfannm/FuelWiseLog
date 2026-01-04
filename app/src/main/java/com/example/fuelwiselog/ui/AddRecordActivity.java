package com.example.fuelwiselog.ui;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fuelwiselog.util.Prefs;
import com.example.fuelwiselog.data.Vehicle;
import com.example.fuelwiselog.databinding.ActivityAddRecordBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// Activity screen for adding a new fuel fill-up record
public class AddRecordActivity extends AppCompatActivity {

    private ActivityAddRecordBinding binding;
    private FuelViewModel viewModel;

    private final SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");

    private List<Vehicle> vehicles = new ArrayList<>();
    private long selectedVehicleId = -1;
    private double lastMileage = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Connect to ViewModel to handle database operations
        viewModel = new ViewModelProvider(this).get(FuelViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        // Seed the date input with today's date.
        Calendar cal = Calendar.getInstance();
        binding.etDate.setText(iso.format(cal.getTime()));

        binding.etDate.setOnClickListener(v -> {
            // Allow picking a date up to today only.
            Calendar c = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(
                    AddRecordActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);
                        binding.etDate.setText(iso.format(picked.getTime()));
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            // Restrict DatePicker to prevent selecting future dates
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });

        // Load vehicles from database; required to add a record
        viewModel.getVehicles().observe(this, list -> {
            vehicles = list;

            // Without vehicles, this screen cannot proceed.
            if (vehicles.isEmpty()) {
                Toast.makeText(this, "No vehicles. Add a vehicle first.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Build the dropdown and select a default vehicle.
            setupVehicleDropdown();

            // Auto-select the last used vehicle (from Prefs) or the first in list
            long prefId = Prefs.getSelectedVehicleId(this);
            if (prefId > 0) {
                setSelectedVehicle(prefId);
            } else {
                setSelectedVehicle(vehicles.get(0).getId());
            }
        });

        binding.btnSave.setOnClickListener(v -> saveRecord());
    }

    // Populates the dropdown menu with vehicle names
    private void setupVehicleDropdown() {
        // Map vehicles into readable dropdown labels.
        List<String> labels = new ArrayList<>();
        for (Vehicle v : vehicles) labels.add(v.getName() + " (" + v.getType() + ")");

        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        binding.actVehicle.setAdapter(a);

        binding.actVehicle.setOnItemClickListener((parent, view, position, id) -> {
            Vehicle v = vehicles.get(position);
            setSelectedVehicle(v.getId());
        });
    }

    // Updates UI with selected vehicle info and fetches its last mileage
    private void setSelectedVehicle(long vehicleId) {
        selectedVehicleId = vehicleId;

        // Find the selected vehicle to populate the preview.
        Vehicle v = null;
        for (Vehicle vv : vehicles) if (vv.getId() == vehicleId) { v = vv; break; }

        if (v != null) {
            binding.layoutVehiclePreview.setVisibility(android.view.View.VISIBLE);
            binding.tvPreviewName.setText(v.getName());
            binding.tvPreviewType.setText(v.getType());
            binding.tvPreviewVehicleIcon.setText(VehicleEmojiMapper.getEmoji(v.getType()));
            try {
                binding.cardPreviewColor.setCardBackgroundColor(Color.parseColor(v.getColorHex()));
            } catch (Exception ignored) {}

            // update dropdown text to match
            binding.actVehicle.setText(v.getName() + " (" + v.getType() + ")", false);
        }

        // Load last mileage to validate the current entry (prevent logic errors).
        viewModel.getLastMileage(vehicleId).observe(this, last -> {
            if (last == null) {
                lastMileage = -1;
                binding.tvLastMileage.setText("Last recorded: — km");
            } else {
                lastMileage = last;
                binding.tvLastMileage.setText("Last recorded: " + ((long) lastMileage) + " km");
            }
        });
    }

    // Validates input fields and saves the data to the database
    private void saveRecord() {
        clearErrors();

        // Read values and validate user input.
        String dateIso = binding.etDate.getText() == null ? "" : binding.etDate.getText().toString().trim();
        Double liters = parseDouble(binding.etLiters.getText());
        Double cost = parseDouble(binding.etCost.getText());
        Long mileage = parseLong(binding.etMileage.getText());

        boolean ok = true;

        if (selectedVehicleId <= 0) {
            binding.tilVehicle.setError("Select a vehicle");
            ok = false;
        }
        if (dateIso.isEmpty()) {
            binding.tilDate.setError("Pick a date");
            ok = false;
        } else if (isFutureDate(dateIso)) {
            binding.tilDate.setError("Date cannot be in the future");
            ok = false;
        }
        if (liters == null || liters <= 0) {
            binding.tilLiters.setError("Invalid liters");
            ok = false;
        }
        if (cost == null || cost <= 0) {
            binding.tilCost.setError("Invalid cost");
            ok = false;
        }
        // Critical check: New mileage must be higher than previous history
        if (mileage == null || mileage <= 0) {
            binding.tilMileage.setError("Invalid mileage");
            ok = false;
        } else if (lastMileage > 0 && mileage <= lastMileage) {
            binding.tilMileage.setError("Mileage must be greater than " + ((long) lastMileage));
            ok = false;
        }

        if (!ok) return;

        // Persist selected vehicle for other screens.
        Prefs.setSelectedVehicleId(this, selectedVehicleId);

        // Insert record after validation passes.
        viewModel.insertFuelRecord(selectedVehicleId, dateIso, liters, cost, mileage);

        Toast.makeText(this, "Record added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void clearErrors() {
        binding.tilVehicle.setError(null);
        binding.tilDate.setError(null);
        binding.tilLiters.setError(null);
        binding.tilCost.setError(null);
        binding.tilMileage.setError(null);
    }

    // Helper to ensure users don't log future dates
    private boolean isFutureDate(String dateIso) {
        try {
            iso.setLenient(false);
            Date picked = iso.parse(dateIso);
            if (picked == null) return true;
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            return picked.after(today.getTime());
        } catch (Exception e) {
            return true;
        }
    }

    private Double parseDouble(CharSequence cs) {
        try {
            if (cs == null) return null;
            String s = cs.toString().trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(CharSequence cs) {
        try {
            if (cs == null) return null;
            String s = cs.toString().trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }
}