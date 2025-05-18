package com.example.argeneratorapp.screens

data class AccomplishmentReport(
    var arId: String = "",
    var id: String = "",
    val venue: String = "",
    val accomplishments: String = "",
    val date: String = "",                  // Date in YYYY-MM-DD format
    val startTime: String = "",             // Start time in HH:MM:SS format
    val endTime: String = "",               // End time in HH:MM:SS format
    var activities: String = "",            // Type of activity (e.g., Class, Consultation)
    val designation: String = "",
    val timeSpent: String = "",             // Time spent in HH:MM:SS format
    val userId: String = "",                // User ID for the report
    val dayName: String = ""
)
