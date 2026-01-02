package com.example.fuelwiselog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FuelRecordDao {

    @Insert
    long insert(FuelRecord record);

    @Update
    void update(FuelRecord record);

    @Delete
    void delete(FuelRecord record);

    @Query("DELETE FROM fuel_records")
    void deleteAll();

    // Oldest -> newest (odometer increases). Best for computing distance between fill-ups.
    @Query("SELECT * FROM fuel_records ORDER BY odometerKm ASC")
    LiveData<List<FuelRecord>> getAllByOdometerAsc();

    @Query("SELECT * FROM fuel_records ORDER BY odometerKm DESC LIMIT 1")
    LiveData<FuelRecord> getLastRecordLive();
}
