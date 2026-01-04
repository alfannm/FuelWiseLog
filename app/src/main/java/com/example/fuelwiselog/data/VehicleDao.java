package com.example.fuelwiselog.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

// Interface defining database operations (SQL queries) for the Vehicle table
@Dao
public interface VehicleDao {

    // Fetches all vehicles sorted alphabetically (case-insensitive) for the UI list
    @Query("SELECT * FROM vehicles ORDER BY name COLLATE NOCASE")
    LiveData<List<Vehicle>> getAll();

    // Adds a new vehicle to the database
    @Insert
    long insert(Vehicle vehicle);

    // Updates details of an existing vehicle
    @Update
    void update(Vehicle vehicle);

    // Removes a vehicle from the database
    @Delete
    void delete(Vehicle vehicle);
}