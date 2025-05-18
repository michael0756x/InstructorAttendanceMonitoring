package com.example.argeneratorapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val realtimeDb = Firebase.database.reference
    val context = LocalContext.current

    var user by remember { mutableStateOf<UserDetails?>(null) }
    var attendanceLogs by remember { mutableStateOf<List<AttendanceLog>>(emptyList()) }
    var currentTime by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var currentPage by remember { mutableStateOf(0) }

    // Calculate and return the latest 10 attendance logs based on the current page
    val paginatedLogs: List<AttendanceLog> = remember(attendanceLogs, currentPage) {
        attendanceLogs.sortedByDescending { it.date } // Sort by date descending
            .drop(currentPage * 10) // Skip the first `currentPage * 10` logs
            .take(10) // Take the next 10 logs
    }

    // Fetch user data and attendance logs
    LaunchedEffect(userId) {
        db.collection("users")
            .document(userId)  // Fetching the user using userId
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val userData = documentSnapshot.toObject(UserDetails::class.java)
                user = userData

                if (userData == null) {
                    Toast.makeText(
                        context,
                        "No user found with User ID: $userId",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Firestore", "No user found with User ID: $userId")
                    return@addOnSuccessListener
                }

                // Fetch attendance logs from the Realtime Database using fingerprintNo
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

                        // Filter logs and group by date
                        val groupedLogs = logs.groupBy { it.date }
                            .map { (date, logs) ->
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

    // Live clock state
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000) // Update every second
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Time
        Text(
            text = "Live Time: $currentTime",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Attendance Logs Header
        Text(
            "Attendance Logs",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF388E3C),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF388E3C))
                .padding(vertical = 4.dp)
        ) {
            Text("Date", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Day", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("In", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Out", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Hours", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Attendance Logs
        Column {
            paginatedLogs.forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFF1F8E9))
                ) {
                    Text(log.date, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.dayName, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.timeIn, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.timeOut, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(log.totalHours, modifier = Modifier.weight(1f), fontSize = 12.sp)
                }
            }
        }

        // Pagination Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (currentPage > 0) currentPage -= 1
                },
                enabled = currentPage > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Previous", color = Color.White)
            }
            Button(
                onClick = {
                    if ((currentPage + 1) * 10 < attendanceLogs.size) currentPage += 1
                },
                enabled = (currentPage + 1) * 10 < attendanceLogs.size,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Next", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manage Accomplishment Report Button
        Button(
            onClick = {
                // Navigate to the manage accomplishment report screen
                navController.navigate("list_of_ar_dates/$userId")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
        ) {
            Text("Manage Accomplishment Report", color = Color.White)
        }

        // Logout Button (Green Theme)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Perform logout
                FirebaseAuth.getInstance().signOut()

                // Navigate to login screen and clear back stack
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true } // Clears the back stack
                    launchSingleTop = true              // Prevents duplicate destinations
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green button color
        ) {
            Text("Logout", color = Color.White)
        }
    }
}
