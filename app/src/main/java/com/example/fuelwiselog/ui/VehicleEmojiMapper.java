package com.example.fuelwiselog.ui;

import java.util.Locale;

// Helper utility that purely handles visual styling (converting text types to icons)
final class VehicleEmojiMapper {

    private VehicleEmojiMapper() {}

    // Map vehicle type strings to emoji-like symbols for UI badges.
    // Takes a string like "Car" or "Honda Civic" and returns a matching emoji (🚗)
    static String getEmoji(String type) {
        if (type == null) {
            return "🛞"; // Default fallback
        }

        String t = type.trim().toLowerCase(Locale.ROOT);
        // Simple string matching to set the appropriate icon for the UI
        if (t.contains("motor")) {
            return "🏍️";
        }
        if (t.contains("lorry") || t.contains("truck")) {
            return "🚛";
        }
        if (t.contains("van")) {
            return "🚐";
        }
        if (t.contains("car")) {
            return "🚗";
        }
        if (t.contains("other")) {
            return "🛞";
        }

        return "🛞"; // Fallback for unknown types
    }
}