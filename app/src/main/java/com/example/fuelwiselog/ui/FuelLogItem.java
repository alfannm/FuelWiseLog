package com.example.fuelwiselog.ui;

// Helper class specifically for the UI; merges FuelRecord data, Vehicle details, and calculated math into one object
public class FuelLogItem {
    // Identity fields used for diffing and filtering.
    // Vehicle info is merged here so the list adapter doesn't need to look it up again
    public long recordId;
    public long vehicleId;
    public String vehicleName;
    public String vehicleColorHex;
    public String vehicleType;

    // Record details displayed in the list row.
    // Raw data from the fill-up log
    public String dateIso;
    public double liters;
    public double costRm;
    public double mileageKm;

    // Efficiency metrics when a previous record is available.
    // Calculated results (Distance = Current - Previous) shown only if history exists
    public boolean hasEfficiency;
    public double distanceKm;
    public double rmPerKm;
    public double litersPer100Km;
}