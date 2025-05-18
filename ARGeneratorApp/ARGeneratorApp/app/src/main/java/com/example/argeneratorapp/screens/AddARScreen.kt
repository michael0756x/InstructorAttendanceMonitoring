package com.example.argeneratorapp.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddARScreen(navController: NavController, userId: String, mainStartDate: String, mainEndDate: String) {
    val firestore = FirebaseFirestore.getInstance()

    // State variables for user inputs
    var selectedDate by remember { mutableStateOf("") }
    var dayName by remember { mutableStateOf("") } // State variable for day name
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var activities by remember { mutableStateOf("Class") } // Default to "Class"
    var designation by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var accomplishments by remember { mutableStateOf("") }

    // State for time availability check
    var isTimeOccupied by remember { mutableStateOf(false) }

    // Snackbar state for messages
    val snackbarHostState = remember { SnackbarHostState() }
    var successMessageVisible by remember { mutableStateOf(false) }
    var errorMessageVisible by remember { mutableStateOf(false) }

    // Date and Time Picker Dialogs
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Function to check for time occupancy
    fun checkTimeOccupancy(date: String, start: String, end: String) {
        if (date.isNotEmpty() && start.isNotEmpty() && end.isNotEmpty()) {
            val reportsRef = firestore.collection("accomplishment_reports")
            reportsRef.whereEqualTo("date", date)
                .whereGreaterThanOrEqualTo("startTime", start)
                .whereLessThan("endTime", end)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    isTimeOccupied = querySnapshot.documents.isNotEmpty()
                }
        }
    }

    // Function to show Date Picker within main date range
    fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startCalendar = Calendar.getInstance().apply {
            time = sdf.parse(mainStartDate) ?: Date()
        }
        val endCalendar = Calendar.getInstance().apply {
            time = sdf.parse(mainEndDate) ?: Date()
        }

        DatePickerDialog(
            context,
            { _, year, month, day ->
                val dateString = String.format("%04d-%02d-%02d", year, month + 1, day)
                onDateSelected(dateString)
                checkTimeOccupancy(dateString, startTime, endTime) // Check time occupancy after selecting a date

                // Calculate the day name
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(dateString)
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // EEEE gives full day name
                dayName = dayFormat.format(date!!) // Update day name state
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = startCalendar.timeInMillis
            datePicker.maxDate = endCalendar.timeInMillis
        }.show()
    }

    // Function to show Time Picker
    fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val timeString = String.format("%02d:%02d", hour, minute)
                onTimeSelected(timeString)
                // Check for occupancy after selecting time
                checkTimeOccupancy(selectedDate, startTime, endTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    // Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Increased padding for better spacing
            .background(Color(0xFFF5F5DC)), // Beige background for the screen
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing for clarity
    ) {
        // Snackbar for messages
        SnackbarHost(hostState = snackbarHostState)

        // Step 1: Select Date
        Button(
            onClick = { showDatePickerDialog { selectedDate = it } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green button color
        ) {
            Text(text = if (selectedDate.isNotEmpty()) selectedDate else "Select Date")
        }

        // Display the day name when the date is selected
        if (dayName.isNotEmpty()) {
            Text(text = "Day: $dayName", modifier = Modifier.padding(top = 8.dp))
        }

        // Step 2: Input Start and End Time
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showTimePickerDialog { startTime = it } },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green button color
            ) {
                Text(text = if (startTime.isNotEmpty()) startTime else "Start Time")
            }

            Button(
                onClick = { showTimePickerDialog { endTime = it } },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green button color
            ) {
                Text(text = if (endTime.isNotEmpty()) endTime else "End Time")
            }
        }

        // Calculate time spent in HH:MM:SS format
        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = timeFormat.parse(startTime)!!
            val end = timeFormat.parse(endTime)!!
            val timeDifference = end.time - start.time

            // Convert milliseconds to hours, minutes, and seconds
            val totalSeconds = (timeDifference / 1000).toInt()
            val hoursSpent = totalSeconds / 3600
            val minutesSpent = (totalSeconds % 3600) / 60
            val secondsSpent = totalSeconds % 60

            timeSpent = String.format("%02d:%02d:%02d", hoursSpent, minutesSpent, secondsSpent) // Format as HH:MM:SS
        }

        TextField(
            value = timeSpent,
            onValueChange = { /* No-op */ },
            label = { Text("Time Spent (HH:MM:SS)") },
            placeholder = { Text("Auto-calculated") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Step 3: Input Activities using Radio Buttons
        Text("Activities:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val options = listOf("Class", "Consultation", "ETL") // Added "ETL" option
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = activities == option,
                            onClick = { activities = option }
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = activities == option,
                        onClick = { activities = option }
                    )
                    Text(option)
                }
            }
        }

        TextField(
            value = designation,
            onValueChange = { designation = it },
            label = { Text("Designation") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = venue,
            onValueChange = { venue = it },
            label = { Text("Venue") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = accomplishments,
            onValueChange = { accomplishments = it },
            label = { Text("Accomplishments") },
            modifier = Modifier.fillMaxWidth()
        )

        // Step 4: Add Accomplishment Report to Firestore
        Button(
            onClick = {
                if (isTimeOccupied) {
                    errorMessageVisible = true // Show error message
                    return@Button
                }

                // Generate a unique ID for the Accomplishment Report
                val arId = firestore.collection("accomplishment_reports").document().id

                val report = AccomplishmentReport(
                    arId = arId, // Assign the generated arId
                    date = selectedDate,
                    startTime = startTime,
                    endTime = endTime,
                    activities = activities,
                    designation = designation,
                    venue = venue,
                    timeSpent = timeSpent,
                    accomplishments = accomplishments,
                    userId = userId,
                    dayName = dayName // Include the day name
                )

                firestore.collection("accomplishment_reports").document(arId).set(report)
                    .addOnSuccessListener {
                        successMessageVisible = true
                    }
                    .addOnFailureListener {
                        errorMessageVisible = true
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9ACD32)) // Yellow-green color
        ) {
            Text("Submit")
        }

        // Snackbar for success or error messages
        if (successMessageVisible) {
            LaunchedEffect(snackbarHostState) {
                snackbarHostState.showSnackbar("Accomplishment Report submitted successfully!")
                successMessageVisible = false
            }
        } else if (errorMessageVisible) {
            LaunchedEffect(snackbarHostState) {
                snackbarHostState.showSnackbar("Time slot already occupied.")
                errorMessageVisible = false
            }
        }
    }
}