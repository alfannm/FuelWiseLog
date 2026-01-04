package com.example.fuelwiselog.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.fuelwiselog.data.FuelRecord;
import com.example.fuelwiselog.data.FuelRepository;
import com.example.fuelwiselog.data.Vehicle;

import java.util.List;

// ViewModel: Acts as a bridge between the UI (Activity) and the Data Layer (Repository).
// It survives screen rotations and holds data so it's not lost when the view resets.
public class FuelViewModel extends AndroidViewModel {

    private final FuelRepository repository;

    // LiveData: Automatically updates the UI when the database changes.
    private final LiveData<List<Vehicle>> vehicles;

    // Fuel Log (needs mileage-ascending list across all vehicles)
    private final LiveData<List<FuelRecord>> allRecordsOrderByVehicleMileageAsc;

    public FuelViewModel(@NonNull Application application) {
        super(application);
        // Repository owns all data operations.
        repository = new FuelRepository(application);

        // Cache LiveData streams used by UI screens.
        vehicles = repository.getVehicles();
        allRecordsOrderByVehicleMileageAsc = repository.getAllRecordsOrderByVehicleMileageAsc();
    }

    // ---------------- Vehicles ----------------

    // -----------------------------
    // Vehicles
    // -----------------------------
    // Returns the list of vehicles for the UI to observe
    public LiveData<List<Vehicle>> getVehicles() {
        return vehicles;
    }

    // Saves a new vehicle to the DB
    public void insertVehicle(Vehicle v) {
        repository.insertVehicle(v);
    }

    // Updates existing vehicle details
    public void updateVehicle(Vehicle v) {
        repository.updateVehicle(v);
    }

    // Removes a vehicle
    public void deleteVehicle(Vehicle v) {
        repository.deleteVehicle(v);
    }

    // -----------------------------
    // Records (new correct insert)
    // -----------------------------
    // Helper method to create a FuelRecord object and save it
    public void insertFuelRecord(long vehicleId, String dateIso, double volumeLiters, double costRm, double mileageKm) {
        // Create a record object for persistence.
        FuelRecord record = new FuelRecord(vehicleId, dateIso, volumeLiters, costRm, mileageKm);
        repository.insert(record);
    }

    public void delete(FuelRecord record) {
        repository.delete(record);
    }

    // Deletes a record by ID (used by the list adapter)
    public void deleteFuelRecordById(long id) {
        repository.deleteFuelRecordById(id);
    }

    // For Main/Home summary
    // Fetches history for one vehicle, sorted by mileage for efficiency calculation
    public LiveData<List<FuelRecord>> getRecordsByVehicleMileageAsc(long vehicleId) {
        // Used by dashboard summaries and efficiency calculations.
        return repository.getRecordsByVehicleMileageAsc(vehicleId);
    }

    public LiveData<List<FuelRecord>> getAllRecordsOrderByVehicleMileageAsc() {
        return allRecordsOrderByVehicleMileageAsc;
    }

    // For AddRecord validation (per vehicle)
    // Gets the highest mileage recorded to validate new entries (must be > last)
    public LiveData<Double> getLastMileage(long vehicleId) {
        // Used for AddRecord validation.
        return repository.getLastMileage(vehicleId);
    }
}