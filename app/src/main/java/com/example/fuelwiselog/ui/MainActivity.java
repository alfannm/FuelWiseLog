package com.example.fuelwiselog.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.fuelwiselog.R;
import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.databinding.ActivityMainBinding;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FuelViewModel viewModel;
    private FuelRecordAdapter adapter;

    private final DecimalFormat df2 = new DecimalFormat("0.00");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(FuelViewModel.class);

        adapter = new FuelRecordAdapter();
        binding.recyclerRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRecords.setAdapter(adapter);

        adapter.setOnRecordLongClickListener(this::confirmDeleteOne);

        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddRecordActivity.class))
        );

        viewModel.getDisplayRecords().observe(this, list -> {
            adapter.submitList(list);
            boolean empty = (list == null || list.isEmpty());
            binding.tvEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        viewModel.getSummary().observe(this, summary -> {
            if (summary == null || summary.avgLitersPer100Km == null || summary.avgRmPerKm == null) {
                binding.tvAvgLPer100.setText(getString(R.string.avg_l_per_100km, "—"));
                binding.tvAvgRmPerKm.setText(getString(R.string.avg_rm_per_km, "—"));
            } else {
                binding.tvAvgLPer100.setText(getString(R.string.avg_l_per_100km, df2.format(summary.avgLitersPer100Km)));
                binding.tvAvgRmPerKm.setText(getString(R.string.avg_rm_per_km, df2.format(summary.avgRmPerKm)));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            confirmDeleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAll() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.delete_all_message)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete, (d, w) -> viewModel.deleteAll())
                .show();
    }

    private void confirmDeleteOne(FuelRecord record) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm)
                .setMessage("Delete this record?\n\nOdo: " + record.getOdometerKm() + " km")
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete, (d, w) -> viewModel.delete(record))
                .show();
    }
}
