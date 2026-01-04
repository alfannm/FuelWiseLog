package com.example.fuelwiselog.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Main database configuration: defines the tables (Entities) and version number
@Database(entities = {Vehicle.class, FuelRecord.class}, version = 2, exportSchema = false)
public abstract class FuelDatabase extends RoomDatabase {

    // Data Access Objects (DAOs) for performing queries on tables
    public abstract VehicleDao vehicleDao();
    public abstract FuelRecordDao fuelRecordDao();

    // Background thread service to handle database writes without freezing the UI
    public static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    // Singleton instance to ensure only one database connection exists at a time
    private static volatile FuelDatabase INSTANCE;

    public static FuelDatabase getInstance(Context context) {
        // Double-checked locking to securely create the instance if it doesn't exist
        if (INSTANCE == null) {
            synchronized (FuelDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FuelDatabase.class, "fuelwise_db")
                            // Rebuilds the database if the version number changes (simple for development)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}