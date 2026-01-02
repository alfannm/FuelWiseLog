package com.example.fuelwiselog.ui;

import com.example.fuelwiselog.data.FuelRecord;

import java.util.List;

public class FuelSummary {

    public final long totalDistanceKm;
    public final double totalLiters;
    public final double totalCostRm;

    public final Double avgLitersPer100Km;   // null if not enough data
    public final Double avgRmPerKm;          // null if not enough data

    public FuelSummary(long totalDistanceKm, double totalLiters, double totalCostRm,
                       Double avgLitersPer100Km, Double avgRmPerKm) {
        this.totalDistanceKm = totalDistanceKm;
        this.totalLiters = totalLiters;
        this.totalCostRm = totalCostRm;
        this.avgLitersPer100Km = avgLitersPer100Km;
        this.avgRmPerKm = avgRmPerKm;
    }

    /**
     * Computes overall averages using intervals between fill-ups.
     * For intervals: distance = odo[i] - odo[i-1], liters = liters[i], cost = cost[i]
     */
    public static FuelSummary from(List<FuelRecord> records) {
        if (records == null || records.size() < 2) {
            return new FuelSummary(0, 0.0, 0.0, null, null);
        }

        long totalD = 0;
        double totalL = 0.0;
        double totalC = 0.0;

        FuelRecord prev = records.get(0);

        for (int i = 1; i < records.size(); i++) {
            FuelRecord cur = records.get(i);
            long d = cur.getOdometerKm() - prev.getOdometerKm();
            if (d > 0) {
                totalD += d;
                totalL += cur.getLiters();
                totalC += cur.getCostRm();
            }
            prev = cur;
        }

        Double avgLPer100 = null;
        Double avgRmPerKm = null;

        if (totalD > 0) {
            avgLPer100 = (totalL / (double) totalD) * 100.0;
            avgRmPerKm = (totalC / (double) totalD);
        }

        return new FuelSummary(totalD, totalL, totalC, avgLPer100, avgRmPerKm);
    }
}
