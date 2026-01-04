package com.example.fuelwiselog.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Defines the "fuel_records" table. Each row represents one fill-up.
@Entity(
        tableName = "fuel_records",
        // Links this record to a Vehicle. If the Vehicle is deleted, its records are deleted too.
        foreignKeys = @ForeignKey(
                entity = Vehicle.class,
                parentColumns = "id",
                childColumns = "vehicleId",
                onDelete = ForeignKey.CASCADE
        ),
        // Indexes speed up queries, specifically when sorting by mileage to calculate efficiency.
        indices = {
                @Index("vehicleId"),
                @Index(value = {"vehicleId", "mileageKm"})
        }
)
public class FuelRecord {

    // Unique ID for this specific log entry (Primary Key).
    @PrimaryKey(autoGenerate = true)
    private long id;

    // The ID of the vehicle this record belongs to.
    private long vehicleId;

    // Date stored as "YYYY-MM-DD" string so it sorts correctly.
    private String dateIso;

    // The raw data input by the user at the gas station.
    private double volumeLiters;
    private double costRm;
    private double mileageKm;

    public FuelRecord(long vehicleId, String dateIso, double volumeLiters, double costRm, double mileageKm) {
        this.vehicleId = vehicleId;
        this.dateIso = dateIso;
        this.volumeLiters = volumeLiters;
        this.costRm = costRm;
        this.mileageKm = mileageKm;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public String getDateIso() { return dateIso; }
    public void setDateIso(String dateIso) { this.dateIso = dateIso; }

    public double getVolumeLiters() { return volumeLiters; }
    public void setVolumeLiters(double volumeLiters) { this.volumeLiters = volumeLiters; }

    public double getCostRm() { return costRm; }
    public void setCostRm(double costRm) { this.costRm = costRm; }

    public double getMileageKm() { return mileageKm; }
    public void setMileageKm(double mileageKm) { this.mileageKm = mileageKm; }
}