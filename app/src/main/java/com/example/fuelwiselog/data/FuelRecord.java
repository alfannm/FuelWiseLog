package com.example.fuelwiselog.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_records")
public class FuelRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long timestamp;      // System.currentTimeMillis()
    private double liters;       // fuel volume in liters
    private double costRm;       // total cost in RM
    private long odometerKm;     // odometer reading in km

    public FuelRecord(long timestamp, double liters, double costRm, long odometerKm) {
        this.timestamp = timestamp;
        this.liters = liters;
        this.costRm = costRm;
        this.odometerKm = odometerKm;
    }

    // Room needs this setter for autoGenerate primary key
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public double getCostRm() {
        return costRm;
    }

    public void setCostRm(double costRm) {
        this.costRm = costRm;
    }

    public long getOdometerKm() {
        return odometerKm;
    }

    public void setOdometerKm(long odometerKm) {
        this.odometerKm = odometerKm;
    }
}
