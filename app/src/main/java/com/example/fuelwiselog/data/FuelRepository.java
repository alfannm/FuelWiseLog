package com.example.fuelwiselog.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Repository for Vehicles + Fuel Records.
 * All writes run on {@link FuelDatabase#DB_EXECUTOR}.
 */
// Mediator class that handles data operations, separating the Database from the UI
public class FuelRepository {

    private final VehicleDao vehicleDao;
    private final FuelRecordDao fuelRecordDao;

    public FuelRepository(Application app) {
        // Repository owns DAO references and write thread.
        FuelDatabase db = FuelDatabase.getInstance(app);
        vehicleDao = db.vehicleDao();
        fuelRecordDao = db.fuelRecordDao();
    }

    // Returns a live list of vehicles that updates the UI automatically when changes occur
    public LiveData<List<Vehicle>> getVehicles() {
        return vehicleDao.getAll();
    }

    // Adds a vehicle on a background thread to keep the app responsive
    public void insertVehicle(Vehicle v) {
        // Writes are executed on the DB executor.
        FuelDatabase.DB_EXECUTOR.execute(() -> vehicleDao.insert(v));
    }

    // Updates vehicle details on a background thread
    public void updateVehicle(Vehicle v) {
        // Writes are executed on the DB executor.
        FuelDatabase.DB_EXECUTOR.execute(() -> vehicleDao.update(v));
    }

    // Deletes a vehicle on a background thread
    public void deleteVehicle(Vehicle v) {
        // Writes are executed on the DB executor.
        FuelDatabase.DB_EXECUTOR.execute(() -> vehicleDao.delete(v));
    }

    // ---------------- Fuel Records ----------------
    // Fetches logs for a specific car, sorted by mileage (needed to calculate distance between fill-ups)
    public LiveData<List<FuelRecord>> getRecordsByVehicleMileageAsc(long vehicleId) {
        // LiveData stream for a vehicle's records ordered by mileage.
        return fuelRecordDao.getByVehicleMileageAsc(vehicleId);
    }

    // Gets the highest mileage recorded to validate that new entries aren't lower than history
    public LiveData<Double> getLastMileage(long vehicleId) {
        // Latest mileage for validation in AddRecord.
        return fuelRecordDao.getLastMileage(vehicleId);
    }

    // Fetches all records for all vehicles
    public LiveData<List<FuelRecord>> getAllRecordsOrderByVehicleMileageAsc() {
        // All records ordered by vehicle + mileage for efficiency math.
        return fuelRecordDao.getAllOrderByVehicleAndMileageAsc();
    }

    /** Alias used by FuelViewModel */
    // Saves a new fuel log on a background thread
    public void insert(FuelRecord r) {
        FuelDatabase.DB_EXECUTOR.execute(() -> fuelRecordDao.insert(r));
    }

    /** Alias used by FuelViewModel */
    // Deletes a specific record object on a background thread
    public void delete(FuelRecord r) {
        FuelDatabase.DB_EXECUTOR.execute(() -> fuelRecordDao.delete(r));
    }

    // Deletes a record by ID (useful for swipe-to-delete actions in the list)
    public void deleteFuelRecordById(long id) {
        // Used by log screen for quick deletion.
        FuelDatabase.DB_EXECUTOR.execute(() -> fuelRecordDao.deleteById(id));
    }
}