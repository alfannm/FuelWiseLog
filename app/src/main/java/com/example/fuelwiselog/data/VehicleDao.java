package com.example.fuelwiselog.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface VehicleDao {

    @Query("SELECT * FROM vehicles ORDER BY name COLLATE NOCASE")
    LiveData<List<Vehicle>> getAll();

    @Insert
    long insert(Vehicle vehicle);

    @Update
    void update(Vehicle vehicle);

    @Delete
    void delete(Vehicle vehicle);
}
