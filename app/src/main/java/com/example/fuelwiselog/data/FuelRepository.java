package com.example.fuelwiselog.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class FuelRepository {

    private final FuelRecordDao dao;
    private final LiveData<List<FuelRecord>> allRecords;
    private final LiveData<FuelRecord> lastRecord;

    public FuelRepository(Application application) {
        FuelDatabase db = FuelDatabase.getInstance(application);
        dao = db.fuelRecordDao();
        allRecords = dao.getAllByOdometerAsc();
        lastRecord = dao.getLastRecordLive();
    }

    public LiveData<List<FuelRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<FuelRecord> getLastRecord() {
        return lastRecord;
    }

    public void insert(FuelRecord record) {
        FuelDatabase.getExecutor().execute(() -> dao.insert(record));
    }

    public void update(FuelRecord record) {
        FuelDatabase.getExecutor().execute(() -> dao.update(record));
    }

    public void delete(FuelRecord record) {
        FuelDatabase.getExecutor().execute(() -> dao.delete(record));
    }

    public void deleteAll() {
        FuelDatabase.getExecutor().execute(dao::deleteAll);
    }
}
