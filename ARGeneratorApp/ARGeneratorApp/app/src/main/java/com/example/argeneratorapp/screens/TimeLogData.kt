package com.example.argeneratorapp.screens

data class TimeLogData(
    var id: String = "",
    val userId: String = "",                  // User ID
    val date: String = "",            // Selected date in YYYY-MM-DD format
    val dayName: String = "",                  // Day name (e.g., Monday, Tuesday)
    val actualTimeIn: String = "",             // Actual time in in HH:MM format
    val actualTimeOut: String = "",            // Actual time out in HH:MM format
    val totalHours: String = ""                 // Total hours calculated in HH:MM:SS format
)
