package com.example.argeneratorapp.screens

data class UserDetails(
    val department: String = "",
    val email: String = "",
    val fingerprintNo: String = "",
    val fullName: String = "",
    val id: String = "", // Firestore document ID
    val position: String = "",
    val rateHour: String = "",
    val userId: String = "" // Firestore userId field
)