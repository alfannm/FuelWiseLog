package com.example.fuelwiselog.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {FuelRecord.class}, version = 1, exportSchema = false)
public abstract class FuelDatabase extends RoomDatabase {

    public abstract FuelRecordDao fuelRecordDao();

    private static volatile FuelDatabase INSTANCE;

    // A small thread pool for DB operations
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static ExecutorService getExecutor() {
        return databaseWriteExecutor;
    }

    public static FuelDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (FuelDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    FuelDatabase.class,
                                    "fuel_db"
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
