package com.example.fuelwiselog.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fuelwiselog.R;
import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.databinding.ActivityAddRecordBinding;

import java.text.DecimalFormat;

public class AddRecordActivity extends AppCompatActivity {

    private ActivityAddRecordBinding binding;
    private FuelViewModel viewModel;

    private Long lastOdoKm = null;

    private final DecimalFormat df0 = new DecimalFormat("0");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FuelViewModel.class);

        // Toolbar back
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Observe last record to enforce increasing odometer
        viewModel.getLastRecord().observe(this, last -> {
            if (last == null) {
                lastOdoKm = null;
                binding.tvLastOdo.setText("Last odometer: — km");
            } else {
                lastOdoKm = last.getOdometerKm();
                binding.tvLastOdo.setText(getString(R.string.last_odometer, df0.format(lastOdoKm)));
            }
        });

        binding.btnSave.setOnClickListener(v -> onSave());
    }

    private void onSave() {
        clearErrors();

        Double liters = parseDouble(binding.etLiters.getText() == null ? null : binding.etLiters.getText().toString());
        Double cost = parseDouble(binding.etCost.getText() == null ? null : binding.etCost.getText().toString());
        Long odometer = parseLong(binding.etOdometer.getText() == null ? null : binding.etOdometer.getText().toString());

        boolean ok = true;

        if (liters == null) {
            binding.tilLiters.setError(getString(R.string.invalid_number));
            ok = false;
        } else if (liters <= 0) {
            binding.tilLiters.setError(getString(R.string.must_be_positive));
            ok = false;
        }

        if (cost == null) {
            binding.tilCost.setError(getString(R.string.invalid_number));
            ok = false;
        } else if (cost <= 0) {
            binding.tilCost.setError(getString(R.string.must_be_positive));
            ok = false;
        }

        if (odometer == null) {
            binding.tilOdometer.setError(getString(R.string.invalid_number));
            ok = false;
        } else if (odometer <= 0) {
            binding.tilOdometer.setError(getString(R.string.must_be_positive));
            ok = false;
        } else if (lastOdoKm != null && odometer <= lastOdoKm) {
            binding.tilOdometer.setError(getString(R.string.odometer_must_increase));
            ok = false;
        }

        if (!ok) return;

        viewModel.insert(liters, cost, odometer);

        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void clearErrors() {
        binding.tilLiters.setError(null);
        binding.tilCost.setError(null);
        binding.tilOdometer.setError(null);
    }

    private Double parseDouble(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String s) {
        try {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }
}
