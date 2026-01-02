package com.example.fuelwiselog.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.data.FuelRepository;

import java.util.List;

public class FuelViewModel extends AndroidViewModel {

    private final FuelRepository repository;

    private final LiveData<List<FuelRecordDisplay>> displayRecords;
    private final LiveData<FuelSummary> summary;
    private final LiveData<FuelRecord> lastRecord;

    public FuelViewModel(@NonNull Application application) {
        super(application);
        repository = new FuelRepository(application);

        lastRecord = repository.getLastRecord();

        displayRecords = Transformations.map(
                repository.getAllRecords(),
                records -> FuelRecordDisplay.build(records, true) // newest first
        );

        summary = Transformations.map(
                repository.getAllRecords(),
                FuelSummary::from
        );
    }

    public LiveData<List<FuelRecordDisplay>> getDisplayRecords() {
        return displayRecords;
    }

    public LiveData<FuelSummary> getSummary() {
        return summary;
    }

    public LiveData<FuelRecord> getLastRecord() {
        return lastRecord;
    }

    public void insert(double liters, double costRm, long odometerKm) {
        FuelRecord record = new FuelRecord(System.currentTimeMillis(), liters, costRm, odometerKm);
        repository.insert(record);
    }

    public void delete(FuelRecord record) {
        repository.delete(record);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
