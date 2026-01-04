package com.example.fuelwiselog.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Defines the "vehicles" table in the database; represents a car or motorcycle
@Entity(tableName = "vehicles")
public class Vehicle {

    // Unique ID for each vehicle (Primary Key)
    @PrimaryKey(autoGenerate = true)
    private long id;

    // Display name shown throughout the UI (e.g., "My Honda")
    @NonNull
    private String name;

    // Vehicle type (Car, Motorcycle, Lorry) used to select the emoji icon
    @NonNull
    private String type;

    // Hex color code used for styling the vehicle's tag in the list
    @NonNull
    private String colorHex; // e.g. "#B4A7D6"

    // Optional license plate number for user reference
    @Nullable
    private String plateNumber;

    // Constructor to create a new Vehicle object
    public Vehicle(@NonNull String name, @NonNull String type, @NonNull String colorHex, @Nullable String plateNumber) {
        this.name = name;
        this.type = type;
        this.colorHex = colorHex;
        this.plateNumber = plateNumber;
    }

    // Standard Getters and Setters to access or modify vehicle data
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getType() { return type; }
    public void setType(@NonNull String type) { this.type = type; }

    @NonNull public String getColorHex() { return colorHex; }
    public void setColorHex(@NonNull String colorHex) { this.colorHex = colorHex; }

    @Nullable public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(@Nullable String plateNumber) { this.plateNumber = plateNumber; }
}