package com.example.fuelwiselog.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

// Interface defining database operations (SQL queries) for fuel records
@Dao
public interface FuelRecordDao {

    // Adds a new fuel fill-up to the database
    @Insert
    long insert(FuelRecord record);

    // Removes a specific fuel record object
    @Delete
    void delete(FuelRecord record);

    // Fetches history for a specific vehicle, sorted by mileage to calculate efficiency (Distance = Current - Previous)
    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY mileageKm ASC")
    LiveData<List<FuelRecord>> getByVehicleMileageAsc(long vehicleId);

    // Gets the highest mileage recorded for a vehicle; used to validate new entries (input must be > last)
    @Query("SELECT mileageKm FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY mileageKm DESC LIMIT 1")
    LiveData<Double> getLastMileage(long vehicleId);

    // Fetches all records in the entire database, grouped by vehicle
    @Query("SELECT * FROM fuel_records ORDER BY vehicleId ASC, mileageKm ASC")
    LiveData<List<FuelRecord>> getAllOrderByVehicleAndMileageAsc();

    // Deletes a specific record by its ID; used when user swipes to delete in the list
    @Query("DELETE FROM fuel_records WHERE id = :id")
    void deleteById(long id);

}