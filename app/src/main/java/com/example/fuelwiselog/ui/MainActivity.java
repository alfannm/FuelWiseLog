package com.example.fuelwiselog.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

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

        vm = new ViewModelProvider(this).get(FuelViewModel.class);

        setupThemeDropdown();

        binding.cardManageVehicles.setOnClickListener(v ->
                startActivity(new Intent(this, VehicleManagerActivity.class)));

        binding.cardAddRecord.setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        binding.cardFuelLog.setOnClickListener(v ->
                startActivity(new Intent(this, FuelLogActivity.class)));

        vm.getVehicles().observe(this, vehicles -> {
            // Ensure we have a selected vehicle if any exist
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

    private void setupThemeDropdown() {
        String[] themes = getResources().getStringArray(R.array.themes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, themes);
        binding.actTheme.setAdapter(adapter);

        int mode = Prefs.getNightMode(this);
        int initialIndex = 2; // System default
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            initialIndex = 0;
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            initialIndex = 1;
        }
        binding.actTheme.setText(themes[initialIndex], false);

        binding.actTheme.setOnItemClickListener((parent, view, position, id) -> {
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

    private void updateActionEnabledState() {
        boolean hasVehicle = selectedVehicleId >= 0;

        binding.cardAddRecord.setEnabled(hasVehicle);
        binding.cardFuelLog.setEnabled(hasVehicle);

        binding.cardAddRecord.setAlpha(hasVehicle ? 1f : 0.5f);
        binding.cardFuelLog.setAlpha(hasVehicle ? 1f : 0.5f);
    }

    private void renderSelectedVehicleCard(List<Vehicle> vehicles) {
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
            binding.cardNoVehicle.setVisibility(View.VISIBLE);
            binding.cardCurrentVehicle.setVisibility(View.GONE);
            binding.cardSummary.setVisibility(View.GONE);
            return;
        }

        binding.cardNoVehicle.setVisibility(View.GONE);
        binding.cardCurrentVehicle.setVisibility(View.VISIBLE);

        binding.tvVehicleName.setText(sel.getName());
        binding.tvVehicleType.setText(sel.getType());
        try {
            binding.cardVehicleColor.setCardBackgroundColor(Color.parseColor(sel.getColorHex()));
        } catch (Exception ignored) {}
    }

    private void observeSelectedVehicleRecords() {
        if (selectedVehicleId < 0) return;

        if (recordsLiveData != null) {
            recordsLiveData.removeObservers(this);
        }
        recordsLiveData = vm.getRecordsByVehicleMileageAsc(selectedVehicleId);
        recordsLiveData.observe(this, records -> {
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

    private HomeSummary computeSummary(List<FuelRecord> records) {
        if (records == null || records.size() < 2) return null;

        // records are already mileage ASC from DAO
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
