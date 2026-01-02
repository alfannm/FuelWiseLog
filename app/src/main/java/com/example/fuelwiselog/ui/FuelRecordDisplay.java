package com.example.fuelwiselog.ui;

import com.example.fuelwiselog.data.FuelRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI model that includes derived metrics:
 * - distance since previous fill-up
 * - L/100km
 * - RM/km
 */
public class FuelRecordDisplay {

    public final FuelRecord record;
    public final Long distanceKm;          // null means N/A
    public final Double litersPer100Km;    // null means N/A
    public final Double rmPerKm;           // null means N/A

    public FuelRecordDisplay(FuelRecord record, Long distanceKm, Double litersPer100Km, Double rmPerKm) {
        this.record = record;
        this.distanceKm = distanceKm;
        this.litersPer100Km = litersPer100Km;
        this.rmPerKm = rmPerKm;
    }

    /**
     * Builds list in chronological order (oldest->newest), computing distance from previous record,
     * then optionally reverses for display (newest->oldest).
     */
    public static List<FuelRecordDisplay> build(List<FuelRecord> records, boolean newestFirst) {
        if (records == null) return Collections.emptyList();

        List<FuelRecordDisplay> out = new ArrayList<>();
        FuelRecord prev = null;

        for (FuelRecord r : records) {
            Long distance = null;
            Double lPer100 = null;
            Double rmPerKm = null;

            if (prev != null) {
                long d = r.getOdometerKm() - prev.getOdometerKm();
                if (d > 0) {
                    distance = d;
                    lPer100 = (r.getLiters() / (double) d) * 100.0;
                    rmPerKm = (r.getCostRm() / (double) d);
                }
            }

            out.add(new FuelRecordDisplay(r, distance, lPer100, rmPerKm));
            prev = r;
        }

        if (newestFirst) {
            Collections.reverse(out);
        }
        return out;
    }
}
