package com.example.fuelwiselog.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.util.Prefs;
import com.example.fuelwiselog.data.Vehicle;
import com.example.fuelwiselog.databinding.ActivityFuelLogBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Activity that displays the history of fuel records (The "Log" screen)
public class FuelLogActivity extends AppCompatActivity {

    private ActivityFuelLogBinding binding;
    private FuelViewModel viewModel;

    private FuelLogAdapter adapter;

    private List<Vehicle> vehicles = new ArrayList<>();
    private List<FuelRecord> recordsMileageOrdered = new ArrayList<>();

    private long filterVehicleId = -1; // -1 = All vehicles
    private final DecimalFormat df2 = new DecimalFormat("0.00");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFuelLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FuelViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        // Set up the list adapter with a delete callback
        adapter = new FuelLogAdapter(recordId -> {
            // Confirm record deletion before removing.
            new AlertDialog.Builder(FuelLogActivity.this)
                    .setTitle("Delete record?")
                    .setMessage("Delete this fuel record?")
                    .setPositiveButton("Delete", (d, which) -> viewModel.deleteFuelRecordById(recordId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.rvRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecords.setAdapter(adapter);

        // Observe vehicle list to populate the filter dropdown
        viewModel.getVehicles().observe(this, list -> {
            // Vehicles drive filter labels and display metadata.
            vehicles = list;
            setupFilterDropdown();
            recomputeUi();
        });

        // Observe all records (sorted by mileage) to perform efficiency calculations
        viewModel.getAllRecordsOrderByVehicleMileageAsc().observe(this, list -> {
            // Mileage-ordered records are used for efficiency math.
            recordsMileageOrdered = list;
            recomputeUi();
        });
    }

    // Configures the top dropdown to filter logs by specific vehicle
    private void setupFilterDropdown() {
        // Build filter list including the "All Vehicles" option.
        List<String> labels = new ArrayList<>();
        labels.add("All Vehicles");
        for (Vehicle v : vehicles) labels.add(v.getName() + " (" + v.getType() + ")");

        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        binding.actFilter.setAdapter(a);

        // default: All
        binding.actFilter.setText("All Vehicles", false);
        filterVehicleId = -1;

        binding.actFilter.setOnItemClickListener((parent, view, position, id) -> {
            // Map selection to vehicle id or "all".
            if (position == 0) {
                filterVehicleId = -1;
            } else {
                Vehicle v = vehicles.get(position - 1);
                filterVehicleId = v.getId();
            }
            recomputeUi();
        });
    }

    // Core Logic: Processes raw data to calculate efficiency and prepares the display list
    private void recomputeUi() {
        if (vehicles == null || recordsMileageOrdered == null) return;

        // Map vehicles by id for fast lookup.
        Map<Long, Vehicle> vehicleMap = new HashMap<>();
        for (Vehicle v : vehicles) vehicleMap.put(v.getId(), v);

        // Compute efficiency per record using mileage-ascending list.
        Map<Long, FuelLogItem> itemByRecordId = new HashMap<>();

        FuelRecord prev = null;
        long prevVehicleId = -1;

        // Iterate through records (sorted by mileage) to calculate distance between fill-ups
        for (FuelRecord r : recordsMileageOrdered) {
            FuelLogItem item = new FuelLogItem();
            item.recordId = r.getId();
            item.vehicleId = r.getVehicleId();
            item.dateIso = r.getDateIso();
            item.liters = r.getVolumeLiters();
            item.costRm = r.getCostRm();
            item.mileageKm = r.getMileageKm();

            Vehicle v = vehicleMap.get(r.getVehicleId());
            if (v != null) {
                item.vehicleName = v.getName();
                item.vehicleColorHex = v.getColorHex();
                item.vehicleType = v.getType();
            } else {
                item.vehicleName = "Vehicle";
                item.vehicleColorHex = "#B4A7D6";
                item.vehicleType = "Other";
            }

            item.hasEfficiency = false;

            // Efficiency Math: needs two consecutive records for the same vehicle
            if (prev != null && prevVehicleId == r.getVehicleId()) {
                // Only compute efficiency when records are consecutive for a vehicle.
                double distance = r.getMileageKm() - prev.getMileageKm();
                if (distance > 0) {
                    item.hasEfficiency = true;
                    item.distanceKm = distance;
                    // Formula: Cost / Distance
                    item.rmPerKm = r.getCostRm() / distance;
                    // Formula: (Liters / Distance) * 100
                    item.litersPer100Km = (r.getVolumeLiters() / distance) * 100.0;
                }
            }

            itemByRecordId.put(item.recordId, item);

            prev = r;
            prevVehicleId = r.getVehicleId();
        }

        // Build display list (we sort by date desc; ISO date sorts well).
        List<FuelLogItem> display = new ArrayList<>(itemByRecordId.values());
        display.sort((a, b) -> {
            String da = a.dateIso == null ? "" : a.dateIso;
            String db = b.dateIso == null ? "" : b.dateIso;
            return db.compareTo(da); // desc
        });

        // Apply vehicle filter after sorting.
        if (filterVehicleId > 0) {
            List<FuelLogItem> filtered = new ArrayList<>();
            for (FuelLogItem i : display) if (i.vehicleId == filterVehicleId) filtered.add(i);
            display = filtered;
        }

        adapter.submitList(display);

        binding.tvCount.setText(display.size() + " record" + (display.size() == 1 ? "" : "s"));
        binding.layoutEmpty.setVisibility(display.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        // Average efficiency card: if "All", fall back to selected vehicle.
        long avgVehicleId = (filterVehicleId == -1) ? Prefs.getSelectedVehicleId(this) : filterVehicleId;
        updateAverageCard(avgVehicleId);
    }

    // Calculates and displays the overall average statistics for the top card
    private void updateAverageCard(long vehicleId) {
        if (vehicleId <= 0) {
            binding.cardAverage.setVisibility(android.view.View.GONE);
            return;
        }

        // Compute averages from mileage-ordered list for that vehicle.
        List<FuelRecord> list = new ArrayList<>();
        for (FuelRecord r : recordsMileageOrdered) if (r.getVehicleId() == vehicleId) list.add(r);

        if (list.size() < 2) {
            binding.cardAverage.setVisibility(android.view.View.GONE);
            return;
        }

        double sumRmPerKm = 0;
        double sumLPer100 = 0;
        int count = 0;

        for (int i = 1; i < list.size(); i++) {
            FuelRecord cur = list.get(i);
            FuelRecord prev = list.get(i - 1);

            // Skip invalid or non-increasing mileage.
            double dist = cur.getMileageKm() - prev.getMileageKm();
            if (dist <= 0) continue;

            double rmPerKm = cur.getCostRm() / dist;
            double lPer100 = (cur.getVolumeLiters() / dist) * 100.0;

            sumRmPerKm += rmPerKm;
            sumLPer100 += lPer100;
            count++;
        }

        if (count == 0) {
            binding.cardAverage.setVisibility(android.view.View.GONE);
            return;
        }

        binding.cardAverage.setVisibility(android.view.View.VISIBLE);
        binding.tvAvgRm.setText("RM " + df2.format(sumRmPerKm / count));
        binding.tvAvgL.setText(df2.format(sumLPer100 / count) + " L");
    }
}