package com.example.argeneratorapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLogScreen(navController: NavController, documentId: String, mainStartDate: String, mainEndDate: String) {
    val db = FirebaseFirestore.getInstance()
    val realtimeDb = Firebase.database.reference
    val context = LocalContext.current

    var user by remember { mutableStateOf<UserDetails?>(null) }
    var attendanceLogs by remember { mutableStateOf<List<AttendanceLog>>(emptyList()) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Fetch user data and attendance logs
    LaunchedEffect(documentId) {
        db.collection("users")
            .document(documentId)  // Fetching the user using documentId from users collection
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val userData = documentSnapshot.toObject(UserDetails::class.java)
                user = userData

                if (userData == null) {
                    Toast.makeText(
                        context,
                        "No user found with Document ID: $documentId",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Firestore", "No user found with Document ID: $documentId")
                    return@addOnSuccessListener
                }

                // Fetch attendance logs based on fingerprintNo
                realtimeDb.child("attendance").child(userData.fingerprintNo)
                    .get()
                    .addOnSuccessListener { dataSnapshot ->
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(context, "No attendance logs found", Toast.LENGTH_SHORT)
                                .show()
                            Log.d(
                                "RealtimeDatabase",
                                "No logs found for fingerprintNo: ${userData.fingerprintNo}"
                            )
                            return@addOnSuccessListener
                        }

                        val logs = dataSnapshot.children.mapNotNull { snapshot ->
                            snapshot.getValue(AttendanceLog::class.java)
                        }

                        // Filter logs within the date range and group by date
                        val filteredLogs = logs.filter { log ->
                            try {
                                val logDate = dateFormat.parse(log.date)
                                val startDate = dateFormat.parse(mainStartDate)
                                val endDate = dateFormat.parse(mainEndDate)
                                logDate != null && logDate in startDate..endDate
                            } catch (e: Exception) {
                                Log.e("DateParsing", "Error parsing date: ${log.date}", e)
                                false
                            }
                        }

                        // Group by date and get the earliest time-in and latest time-out
                        val groupedLogs = filteredLogs.groupBy { it.date }
                            .map { (date, logs) ->
                                // Ensure that the earliest time-in and latest time-out are selected
                                val earliestIn =
                                    logs.filter { it.status == "IN" }.minByOrNull { it.time }
                                val latestOut =
                                    logs.filter { it.status == "OUT" }.maxByOrNull { it.time }

                                // Handle null values if no sign-in or sign-out records are available
                                val timeIn = earliestIn?.time ?: "N/A"
                                val timeOut = latestOut?.time ?: "N/A"
                                val dayName = getDayName(date)

                                // Calculate total hours between timeIn and timeOut
                                val totalHours = calculateTotalHours(timeIn, timeOut)

                                AttendanceLog(
                                    date = date,
                                    timeIn = timeIn,
                                    timeOut = timeOut,
                                    totalHours = totalHours,
                                    dayName = dayName,
                                    userId = userData.userId // Use userId field here
                                )
                            }

                        attendanceLogs = groupedLogs
                        Log.d("GroupedLogs", "Grouped logs: $groupedLogs")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error fetching attendance: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("RealtimeDatabase", "Error fetching logs", e)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("Firestore", "Error fetching user", e)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()  // Ensure the column takes up all the available height
            .background(Color(0xFFE8F5E9)) // Light green background
            .padding(16.dp) // Increased padding for better spacing
    ) {
        // User Details Section
        user?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xFF2E7D32)) // Dark green background
                    .padding(16.dp) // Extra padding for user section
            ) {
                Text(
                    "Name: ${it.fullName}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp // Larger font size
                )
                Spacer(modifier = Modifier.height(8.dp)) // Increased spacing
                Text(
                    "ID: ${it.id}",
                    color = Color.White,
                    fontSize = 16.sp // Slightly smaller font size
                )
                Text(
                    "Position: ${it.position}",
                    color = Color.White,
                    fontSize = 16.sp // Slightly smaller font size
                )
                Text(
                    "Department: ${it.department}",
                    color = Color.White,
                    fontSize = 16.sp // Slightly smaller font size
                )
                Text(
                    "Rate/Hour: ${it.rateHour}",
                    color = Color.White,
                    fontSize = 16.sp // Slightly smaller font size
                )
            }
        } ?: Text(
            "Loading user data...",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

        // Attendance Logs Header
        Text(
            "Attendance Logs",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp, // Slightly larger font size
            modifier = Modifier.padding(bottom = 8.dp), // Increased bottom padding
            color = Color(0xFF2E7D32)
        )

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32)) // Dark green header
                .padding(vertical = 4.dp) // Compact padding
        ) {
            Text(
                "Date",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp // Slightly larger font size
            )
            Text(
                "Day",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp // Slightly larger font size
            )
            Text(
                "In",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp // Slightly larger font size
            )
            Text(
                "Out",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp // Slightly larger font size
            )
            Text(
                "Hours",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp // Slightly larger font size
            )
        }

        // Attendance Logs List
        Column(
            modifier = Modifier
                .weight(1f) // This will make the attendance logs take the remaining space
                .padding(top = 4.dp) // Padding above the logs
        ) {
            attendanceLogs.forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp) // Reduced vertical padding
                        .background(Color(0xFFF1F8E9)) // Light green rows
                ) {
                    Text(log.date, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.dayName, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.timeIn, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.timeOut, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.totalHours, modifier = Modifier.weight(1f), fontSize = 12.sp)
                }
            }
        }

        // Message if no logs
        if (attendanceLogs.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing
            Text(
                "No attendance logs available for the selected date range.",
                color = Color.Red,
                fontSize = 12.sp // Smaller font size
            )
        }

        // Submit Button at the bottom
        Spacer(modifier = Modifier.height(8.dp)) // Added space before the button
        Button(
            onClick = {
                submitLogsToFirestore(attendanceLogs, user?.userId ?: "")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Padding at the bottom to avoid overlap
        ) {
            Text("Submit Logs", fontSize = 14.sp) // Slightly smaller font size
        }
    }
}

// Function to calculate the total hours between sign-in and sign-out
fun calculateTotalHours(timeIn: String, timeOut: String): String {
    if (timeIn != "N/A" && timeOut != "N/A") {
        try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val startTime = format.parse(timeIn)
            val endTime = format.parse(timeOut)

            if (startTime == null || endTime == null) {
                return "N/A"
            }

            val duration = endTime.time - startTime.time
            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
            val seconds = (duration % (1000 * 60)) / 1000

            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            return "N/A"
        }
    }
    return "N/A"
}

// Function to get the day of the week from the date
fun getDayName(date: String): String {
    try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = format.parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateObj
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "Unknown"
    } catch (e: Exception) {
        return "Unknown"
    }
}

// Function to submit attendance logs to Firestore
fun submitLogsToFirestore(logs: List<AttendanceLog>, userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val logsRef = firestore.collection("attendance_logs").document(userId)
    logsRef.set(mapOf("logs" to logs))
        .addOnSuccessListener {
            Log.d("Firestore", "Logs submitted successfully.")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error submitting logs", e)
        }
}

