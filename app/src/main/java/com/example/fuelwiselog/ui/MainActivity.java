package com.example.fuelwiselog.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.fuelwiselog.R;
import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.data.Vehicle;
import com.example.fuelwiselog.databinding.ActivityMainBinding;
import com.example.fuelwiselog.util.Prefs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// The Dashboard screen: Entry point of the application.
// Displays summary statistics and navigation to other features.
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FuelViewModel vm;

    private final DecimalFormat df2 = new DecimalFormat("0.00");

    private long selectedVehicleId = -1L;
    private LiveData<List<FuelRecord>> recordsLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Prefs.applySavedNightMode(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel to access database data
        vm = new ViewModelProvider(this).get(FuelViewModel.class);

        // Theme selector is managed via a dropdown adapter.
        setupThemeDropdown();

        // Quick actions to navigate to other screens.
        binding.cardManageVehicles.setOnClickListener(v ->
                startActivity(new Intent(this, VehicleManagerActivity.class)));

        binding.cardAddRecord.setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        binding.cardFuelLog.setOnClickListener(v ->
                startActivity(new Intent(this, FuelLogActivity.class)));

        // Observe vehicle list to handle the "Current Vehicle" display
        vm.getVehicles().observe(this, vehicles -> {
            // Ensure we have a selected vehicle if any exist.
            selectedVehicleId = Prefs.getSelectedVehicleId(this);
            if (vehicles != null && !vehicles.isEmpty()) {
                boolean exists = false;
                for (Vehicle vv : vehicles) if (vv.getId() == selectedVehicleId) exists = true;
                if (!exists) {
                    selectedVehicleId = vehicles.get(0).getId();
                    Prefs.setSelectedVehicleId(this, selectedVehicleId);
                }
            } else {
                selectedVehicleId = -1L;
                Prefs.setSelectedVehicleId(this, -1L);
            }

            renderSelectedVehicleCard(vehicles);
            updateActionEnabledState();
            observeSelectedVehicleRecords(); // updates summary + count
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh selection after coming back from VehicleManager
        selectedVehicleId = Prefs.getSelectedVehicleId(this);
        renderSelectedVehicleCard(vm.getVehicles().getValue());
        updateActionEnabledState();
        observeSelectedVehicleRecords();
    }

    // Configures the dropdown to switch between Light, Dark, and System themes
    private void setupThemeDropdown() {
        // Custom adapter disables filtering and uses fixed theme list.
        String[] themes = getResources().getStringArray(R.array.themes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, themes) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = themes;
                        results.count = themes.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        binding.actTheme.setAdapter(adapter);

        // Pick initial selection from preferences.
        int mode = Prefs.getNightMode(this);
        int initialIndex = 2; // System default
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            initialIndex = 0;
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            initialIndex = 1;
        }
        binding.actTheme.setText(themes[initialIndex], false);

        binding.actTheme.setOnItemClickListener((parent, view, position, id) -> {
            // Apply the selected theme mode.
            binding.actTheme.setText(themes[position], false);
            binding.actTheme.dismissDropDown();
            binding.actTheme.clearFocus();
            int newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (position == 0) {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (position == 1) {
                newMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            Prefs.setNightMode(this, newMode);
        });
    }

    // Disables "Add Record" and "Fuel Log" buttons if no vehicle is selected (prevents errors)
    private void updateActionEnabledState() {
        // Disable actions when no vehicle is available.
        boolean hasVehicle = selectedVehicleId >= 0;

        binding.cardAddRecord.setEnabled(hasVehicle);
        binding.cardFuelLog.setEnabled(hasVehicle);

        binding.cardAddRecord.setAlpha(hasVehicle ? 1f : 0.5f);
        binding.cardFuelLog.setAlpha(hasVehicle ? 1f : 0.5f);
    }

    // Updates the UI with the selected vehicle's details (Name, Icon, Color)
    private void renderSelectedVehicleCard(List<Vehicle> vehicles) {
        // Resolve the selected vehicle from the list.
        Vehicle sel = null;
        if (vehicles != null) {
            for (Vehicle v : vehicles) {
                if (v.getId() == selectedVehicleId) {
                    sel = v;
                    break;
                }
            }
        }

        if (sel == null) {
            // Show empty state if selection is missing.
            binding.cardNoVehicle.setVisibility(View.VISIBLE);
            binding.cardCurrentVehicle.setVisibility(View.GONE);
            binding.cardSummary.setVisibility(View.GONE);
            return;
        }

        binding.cardNoVehicle.setVisibility(View.GONE);
        binding.cardCurrentVehicle.setVisibility(View.VISIBLE);

        // Bind selected vehicle details.
        binding.tvVehicleName.setText(sel.getName());
        binding.tvVehicleType.setText(sel.getType());
        binding.tvCurrentVehicleIcon.setText(VehicleEmojiMapper.getEmoji(sel.getType()));
        try {
            binding.cardVehicleColor.setCardBackgroundColor(Color.parseColor(sel.getColorHex()));
        } catch (Exception ignored) {}
    }

    // Watches the database for changes to the selected vehicle's records to update the summary card
    private void observeSelectedVehicleRecords() {
        if (selectedVehicleId < 0) return;

        // Swap observers when selection changes.
        if (recordsLiveData != null) {
            recordsLiveData.removeObservers(this);
        }
        recordsLiveData = vm.getRecordsByVehicleMileageAsc(selectedVehicleId);
        recordsLiveData.observe(this, records -> {
            // Update summary and record count.
            int count = records == null ? 0 : records.size();
            binding.tvRecordCount.setText(String.valueOf(count));

            HomeSummary summary = computeSummary(records);
            if (summary == null) {
                binding.cardSummary.setVisibility(View.GONE);
            } else {
                binding.cardSummary.setVisibility(View.VISIBLE);
                binding.tvAvgRmPerKm.setText("RM " + df2.format(summary.avgRmPerKm));
                binding.tvAvgLPer100.setText(df2.format(summary.avgLPer100) + "L");
                binding.tvTotalDistance.setText("Total Distance: " + df2.format(summary.totalDistanceKm) + " km");
                binding.tvTotalCost.setText("Total Cost: RM " + df2.format(summary.totalCostRm));
            }
        });
    }

    // Calculates total statistics and average efficiency based on the history list
    private HomeSummary computeSummary(List<FuelRecord> records) {
        if (records == null || records.size() < 2) return null;

        // Records are already mileage ASC from DAO.
        double firstMileage = records.get(0).getMileageKm();
        double lastMileage = records.get(records.size() - 1).getMileageKm();
        double totalDistance = lastMileage - firstMileage;
        if (totalDistance <= 0) return null;

        double totalCost = 0;
        for (FuelRecord r : records) totalCost += r.getCostRm();

        List<Double> rmPerKm = new ArrayList<>();
        List<Double> lPer100 = new ArrayList<>();

        for (int i = 1; i < records.size(); i++) {
            FuelRecord prev = records.get(i - 1);
            FuelRecord cur = records.get(i);
            double dist = cur.getMileageKm() - prev.getMileageKm();
            if (dist <= 0) continue;

            rmPerKm.add(cur.getCostRm() / dist);
            lPer100.add((cur.getVolumeLiters() / dist) * 100.0);
        }

        if (rmPerKm.isEmpty()) return null;

        double avgRm = 0;
        double avgL = 0;
        for (double v : rmPerKm) avgRm += v;
        for (double v : lPer100) avgL += v;

        avgRm /= rmPerKm.size();
        avgL /= lPer100.size();

        return new HomeSummary(totalDistance, totalCost, avgRm, avgL);
    }

    // Simple data holder for the dashboard statistics
    private static class HomeSummary {
        final double totalDistanceKm;
        final double totalCostRm;
        final double avgRmPerKm;
        final double avgLPer100;

        HomeSummary(double d, double c, double rm, double l) {
            totalDistanceKm = d;
            totalCostRm = c;
            avgRmPerKm = rm;
            avgLPer100 = l;
        }
    }
}