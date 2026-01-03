package com.example.fuelwiselog.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface FuelRecordDao {

    @Insert
    long insert(FuelRecord record);

    @Delete
    void delete(FuelRecord record);

    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY mileageKm ASC")
    LiveData<List<FuelRecord>> getByVehicleMileageAsc(long vehicleId);

    // LiveData version for UI observation (Add Record validation)
    @Query("SELECT mileageKm FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY mileageKm DESC LIMIT 1")
    LiveData<Double> getLastMileage(long vehicleId);

    // For efficiency calculations (previous record per vehicle)
    @Query("SELECT * FROM fuel_records ORDER BY vehicleId ASC, mileageKm ASC")
    LiveData<List<FuelRecord>> getAllOrderByVehicleAndMileageAsc();

    // For delete from Fuel Log adapter (delete by id only)
    @Query("DELETE FROM fuel_records WHERE id = :id")
    void deleteById(long id);

}
