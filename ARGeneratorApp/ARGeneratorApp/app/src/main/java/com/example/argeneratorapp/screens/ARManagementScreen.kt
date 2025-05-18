package com.example.argeneratorapp.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARManagementScreen(
    navController: NavController,
    userId: String,
    mainStartDate: String,
    mainEndDate: String
) {
    val firestore = Firebase.firestore
    val arList = remember { mutableStateListOf<AccomplishmentReport>() }
    val errorMessage = remember { mutableStateOf("") }
    val userName = remember { mutableStateOf("") }
    val userDepartment = remember { mutableStateOf("") }
    val userRateHour = remember { mutableStateOf("") }
    val timeLogData = remember { mutableStateListOf<TimeLogData>() }
    val finalPartSummaryData = remember { mutableStateListOf<FinalPartSummaryData>() }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val reportToDelete = remember { mutableStateOf<AccomplishmentReport?>(null) }
    val showEditDialog = remember { mutableStateOf(false) }
    val reportToEdit = remember { mutableStateOf<AccomplishmentReport?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        fetchAccomplishmentReports(firestore, arList, userId, errorMessage, mainStartDate, mainEndDate)
        fetchUserName(firestore, userId, userName, userDepartment, userRateHour,errorMessage)
        fetchTimeLogData(firestore, timeLogData, userId, errorMessage, mainStartDate, mainEndDate)
        fetchFinalPartSummaryData(firestore, finalPartSummaryData, userId, errorMessage, mainStartDate, mainEndDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5DC))
    ) {
        // Snackbar Host at the top
        Box(modifier = Modifier.fillMaxWidth()) {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
        }

        // Header with date range
        Text(
            text = "Accomplishment Report - ($mainStartDate to $mainEndDate)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF4E342E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add AR Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center the buttons horizontally
        ) {
            // First Row of Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Space Evenly for uniform button sizes
            ) {
                // Add AR Button
                Button(
                    onClick = {
                        navController.navigate("add_ar/$userId/$mainStartDate/$mainEndDate")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    modifier = Modifier
                        .weight(1f) // Distribute available width evenly
                        .padding(4.dp) // Padding around each button
                ) {
                    Text(text = "Add AR", color = Color.Black)
                }

                // View Summary Button
                Button(
                    onClick = {
                        navController.navigate("summary/$userId/$mainStartDate/$mainEndDate")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier
                        .weight(1f) // Distribute available width evenly
                        .padding(4.dp) // Padding around each button
                ) {
                    Text(text = "View Summary", color = Color.White)
                }
            }

            // Second Row of Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Space Evenly for uniform button sizes
            ) {
                // Time Log Button
                Button(
                    onClick = {
                        navController.navigate("time_log/$userId/$mainStartDate/$mainEndDate")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                    modifier = Modifier
                        .weight(1f) // Distribute available width evenly
                        .padding(4.dp) // Padding around each button
                ) {
                    Text(text = "Time Log", color = Color.Black)
                }

                // Final Part Summary Button
                Button(
                    onClick = {
                        navController.navigate("final_part_summary/$userId/$mainStartDate/$mainEndDate")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                    modifier = Modifier
                        .weight(1f) // Distribute available width evenly
                        .padding(4.dp) // Padding around each button
                ) {
                    Text(text = "Final Part Summary", color = Color.Black)
                }
            }

            // Third Row of Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Space Evenly for uniform button sizes
            ) {
                // Submit All Data Button
                Button(
                    onClick = {
                        submitAllData(
                            firestore,
                            arList,
                            timeLogData, // Pass the timeLogData
                            finalPartSummaryData, // Pass the finalPartSummaryData
                            userId,
                            userName.value,
                            userDepartment.value,
                            userRateHour.value,
                            mainStartDate,
                            mainEndDate,
                            onSuccess = {
                                // Show a Snackbar on successful submission
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Data successfully submitted!")
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                    modifier = Modifier
                        .weight(1f) // Distribute available width evenly
                        .padding(4.dp) // Padding around each button
                ) {
                    Text(text = "Submit All Data", color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Handle error messages without loading spinner
        if (errorMessage.value.isNotEmpty()) {
            Text(text = errorMessage.value, color = MaterialTheme.colorScheme.error)
        } else {
            if (arList.isEmpty()) {
                Text(text = "No Accomplishment Reports found within the selected date range.", color = Color.Gray)
            } else {
                // Scrollable table with improved style
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .border(2.dp, Color.Gray)
                        .padding(8.dp)
                ) {
                    Column {
                        // Group ARs by date and sort them
                        val groupedArList = arList.groupBy { it.date }.toSortedMap()

                        groupedArList.forEach { (date, reports) ->
                            // Display Date and Day Header
                            val dayOfWeek = getDayOfWeek(date)
                            DateHeader(date = date, dayOfWeek = dayOfWeek)

                            // Display the column headers with enhanced design
                            ColumnHeaders()

                            // Display activities for that date
                            reports.forEach { report ->
                                ActivityRow(
                                    report = report,
                                    onDelete = {
                                        reportToDelete.value = report
                                        showDeleteDialog.value = true
                                    },
                                    onEdit = {
                                        reportToEdit.value = report
                                        showEditDialog.value = true
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog.value && reportToDelete.value != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete Accomplishment Report") },
            text = { Text("Are you sure you want to delete this report?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reportToDelete.value?.let { report ->
                            deleteAccomplishmentReport(firestore, report.id)
                            arList.remove(report) // Remove from the list
                        }
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit report dialog
    if (showEditDialog.value && reportToEdit.value != null) {
        EditAccomplishmentDialog(
            report = reportToEdit.value!!,
            onDismiss = { showEditDialog.value = false },
            onUpdate = { updatedReport ->
                updateAccomplishmentReport(firestore, updatedReport)
                val index = arList.indexOfFirst { it.id == updatedReport.id }
                if (index != -1) {
                    arList[index] = updatedReport // Update the list
                }
            }
        )
    }
}

private fun submitAllData(
    firestore: FirebaseFirestore,
    arList: List<AccomplishmentReport>,
    timeLogData: List<TimeLogData>,
    finalPartSummaryData: List<FinalPartSummaryData>,
    userId: String,
    userName: String,
    userDepartment: String,
    userRateHour: String,
    mainStartDate: String,
    mainEndDate: String,
    onSuccess: () -> Unit // Callback for successful submission
) {
    // Parse mainStartDate and mainEndDate to Date objects
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDate = dateFormat.parse(mainStartDate)
    val endDate = dateFormat.parse(mainEndDate)

    if (startDate == null || endDate == null) {
        Log.e("Submit Data", "Invalid mainStartDate or mainEndDate format")
        return
    }

    // Get the current timestamp and adjust if necessary
    val currentTimestamp = System.currentTimeMillis()
    val submissionDate = when {
        currentTimestamp < startDate.time -> startDate.time // Before start date
        currentTimestamp > endDate.time -> endDate.time // After end date
        else -> currentTimestamp // Within the range
    }

    // Filter reports within the main date range
    val filteredReports = arList.filter { report ->
        report.date >= mainStartDate && report.date <= mainEndDate
    }

    // Calculate totals in seconds for accomplishment reports
    val totalTeachingLoadSeconds = filteredReports.filter { it.activities == "Class" }
        .sumOf { convertTimeToSeconds(it.timeSpent) }
    val totalConsultationSeconds = filteredReports.filter { it.activities == "Consultation" }
        .sumOf { convertTimeToSeconds(it.timeSpent) }
    val totalETLSeconds = filteredReports.filter { it.activities == "ETL" }
        .sumOf { convertTimeToSeconds(it.timeSpent) }
    val totalTimeSpentSeconds = totalTeachingLoadSeconds + totalConsultationSeconds + totalETLSeconds
    val actualHoursSeconds = timeLogData.sumOf { convertTimeToSeconds(it.totalHours) }
    val classActivitiesPerDay = filteredReports
        .filter { it.activities == "Class" }
        .groupBy { it.date }
        .mapValues { entry ->
            entry.value.sumOf { convertTimeToSeconds(it.timeSpent) }
        }

    // If you need the class activities per day as a list
    val classPerDayList = classActivitiesPerDay.map { (date, seconds) ->
        mapOf(
            "date" to date,
            "classPerDayHour" to formatSecondsToHHMMSS(seconds) // Format to HH:MM:SS
        )
    }

    // Create a list of maps for JSON from accomplishment reports
    val reportsList = filteredReports.map { report ->
        mapOf(
            "date" to report.date,
            "dayName" to report.dayName,
            "startTime" to report.startTime,
            "endTime" to report.endTime,
            "activities" to report.activities,
            "designation" to report.designation,
            "venue" to report.venue,
            "timeSpent" to report.timeSpent,
            "accomplishments" to report.accomplishments
        )
    }

    // Create a list of maps for JSON from time log data
    val timeLogsList = timeLogData.filter { log ->
        log.date >= mainStartDate && log.date <= mainEndDate
    }.map { log ->
        mapOf(
            "date" to log.date,
            "actualTimeIn" to log.actualTimeIn,
            "actualTimeOut" to log.actualTimeOut,
            "totalHours" to log.totalHours
        )
    }

    // Create a list of maps for JSON from final part summary data
    val finalPartSummaryList = finalPartSummaryData.filter { summary ->
        summary.date >= mainStartDate && summary.date <= mainEndDate
    }.map { summary ->
        mapOf(
            "date" to summary.date,
            "researchAndExtension" to summary.researchAndExtensionTime,
            "preparationOfLesson" to summary.preparationOfLessonTime,
            "checkingOfTestPapers" to summary.checkingOfTestPapersTime,
            "noneTime" to summary.noneTime,
            "adminNoETL" to summary.adminNoETLTime,
            "breakTime" to summary.breakTime,
            "checkedBy" to summary.checkedBy,
            "approvedBy" to summary.approvedBy
        )
    }

    // Create a new document in Firestore including summary hours in HH:MM:SS format
    val newDocument = hashMapOf(
        "userId" to userId,
        "userName" to userName,
        "userDepartment" to userDepartment,
        "userRateHour" to userRateHour,
        "reports" to reportsList,
        "timeLogs" to timeLogsList,
        "finalPartSummary" to finalPartSummaryList,
        "totalTeachingLoad" to formatSecondsToHHMMSS(totalTeachingLoadSeconds),
        "totalConsultation" to formatSecondsToHHMMSS(totalConsultationSeconds),
        "totalETL" to formatSecondsToHHMMSS(totalETLSeconds),
        "totalTimeSpent" to formatSecondsToHHMMSS(totalTimeSpentSeconds),
        "actualHours" to formatSecondsToHHMMSS(actualHoursSeconds),
        "classPerDays" to classPerDayList,
        "submittedAt" to Timestamp(Date(submissionDate)), // Adjusted date
        "mainStartdate" to mainStartDate,
        "mainEnddate" to mainEndDate,
    )

    firestore.collection("accomplishment_reports_summary")
        .add(newDocument)
        .addOnSuccessListener {
            Log.d("Submit Data", "Data successfully submitted!")
            onSuccess() // Call the success callback
        }
        .addOnFailureListener { e ->
            Log.e("Submit Data", "Error submitting data: $e")
        }
}

// Convert "HH:mm:ss" to total seconds
fun convertTimeToSeconds(time: String): Int {
    val parts = time.split(":")
    return if (parts.size == 3) {
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0
        val seconds = parts[2].toIntOrNull() ?: 0
        hours * 3600 + minutes * 60 + seconds
    } else {
        0
    }
}

// Format total seconds into "HH:mm:ss"
fun formatSecondsToHHMMSS(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun ActivityRow(
    report: AccomplishmentReport,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray)
            .padding(8.dp) // Added padding for spacing
    ) {
        TableCell(text = report.startTime, width = 80.dp)
        TableCell(text = report.endTime, width = 80.dp)
        TableCell(text = report.activities, width = 100.dp)
        TableCell(text = report.designation, width = 100.dp)
        TableCell(text = report.venue, width = 100.dp)
        TableCell(text = report.timeSpent, width = 80.dp)
        TableCell(text = report.accomplishments, width = 150.dp)

        // Edit Button
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
        }

        // Delete Button
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}

@Composable
fun ColumnHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Gray)
            .padding(vertical = 4.dp)
            .background(Color(0xFFE0E0E0)) // Light gray background for headers
    ) {
        TableHeaderCell(text = "Start Time", width = 80.dp)
        TableHeaderCell(text = "End Time", width = 80.dp)
        TableHeaderCell(text = "Activities", width = 100.dp)
        TableHeaderCell(text = "Designation", width = 100.dp)
        TableHeaderCell(text = "Venue", width = 100.dp)
        TableHeaderCell(text = "Time Spent", width = 80.dp)
        TableHeaderCell(text = "Accomplishments", width = 150.dp)
        TableHeaderCell(text = "Actions", width = 50.dp) // Actions cell for Edit/Delete buttons
    }
}

@Composable
fun DateHeader(date: String, dayOfWeek: String) {
    Text(
        text = "$date - $dayOfWeek",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TableHeaderCell(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Black)
            .background(Color(0xFFE0E0E0)) // Light gray background for header cells
            .width(width) // Set the width of the header cell
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TableCell(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Black)
            .width(width) // Set the width of the cell
    ) {
        Text(text = text)
    }
}

// Function to get the day of the week from a date string
fun getDayOfWeek(date: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(date)!!
        SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
    } catch (e: Exception) {
        "Unknown Day"
    }
}

// Function to delete an accomplishment report from Firestore
fun deleteAccomplishmentReport(firestore: FirebaseFirestore, id: String) {
    firestore.collection("accomplishment_reports").document(id).delete()
        .addOnSuccessListener { Log.d("Delete Report", "Report successfully deleted!") }
        .addOnFailureListener { e -> Log.w("Delete Report", "Error deleting report", e) }
}

// Function to update an accomplishment report in Firestore
fun updateAccomplishmentReport(firestore: FirebaseFirestore, report: AccomplishmentReport) {
    firestore.collection("accomplishment_reports").document(report.id)
        .set(report)
        .addOnSuccessListener { Log.d("Update Report", "Report successfully updated!") }
        .addOnFailureListener { e -> Log.w("Update Report", "Error updating report", e) }
}

private fun fetchAccomplishmentReports(
    firestore: FirebaseFirestore,
    arList: MutableList<AccomplishmentReport>,
    userId: String,
    errorMessage: MutableState<String>,
    mainStartDate: String,
    mainEndDate: String
) {
    firestore.collection("accomplishment_reports")
        .whereEqualTo("userId", userId)
        .whereGreaterThanOrEqualTo("date", mainStartDate)
        .whereLessThanOrEqualTo("date", mainEndDate)
        .get()
        .addOnSuccessListener { documents ->
            arList.clear()
            for (document in documents) {
                val report = document.toObject(AccomplishmentReport::class.java)
                report.id = document.id
                arList.add(report)
            }
        }
        .addOnFailureListener { exception ->
            val errorMsg = "Error fetching accomplishment reports: ${exception.localizedMessage ?: "Unknown error"}"
            errorMessage.value = errorMsg
            Log.e("Firestore Error", errorMsg)
            // Optionally, show a Snackbar or Toast to the user
        }
}

private fun fetchUserName(
    firestore: FirebaseFirestore,
    userId: String,
    userName: MutableState<String>,
    userDepartment: MutableState<String>,
    userRateHour: MutableState<String>,
    errorMessage: MutableState<String>
) {
    firestore.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            userName.value = document.getString("fullName") ?: ""
            userDepartment.value = document.getString("department") ?: ""
            userRateHour.value = document.getString("rateHour") ?: ""
        }
        .addOnFailureListener { exception ->
            errorMessage.value = "Error fetching user details: ${exception.message}"
        }
}

private fun fetchTimeLogData(
    firestore: FirebaseFirestore,
    timeLogData: MutableList<TimeLogData>,
    userId: String,
    errorMessage: MutableState<String>,
    mainStartDate: String,
    mainEndDate: String
) {
    firestore.collection("time_logs")
        .whereEqualTo("userId", userId)
        .whereGreaterThanOrEqualTo("date", mainStartDate)
        .whereLessThanOrEqualTo("date", mainEndDate)
        .get()
        .addOnSuccessListener { documents ->
            timeLogData.clear()
            for (document in documents) {
                val log = document.toObject(TimeLogData::class.java)
                timeLogData.add(log)
            }
        }
        .addOnFailureListener { exception ->
            errorMessage.value = "Error fetching time logs: ${exception.message}"
        }
}

private fun fetchFinalPartSummaryData(
    firestore: FirebaseFirestore,
    finalPartSummaryData: MutableList<FinalPartSummaryData>,
    userId: String,
    errorMessage: MutableState<String>,
    mainStartDate: String,
    mainEndDate: String
) {
    firestore.collection("finalPartSummary")
        .whereEqualTo("userId", userId)
        .whereGreaterThanOrEqualTo("date", mainStartDate)
        .whereLessThanOrEqualTo("date", mainEndDate)
        .get()
        .addOnSuccessListener { documents ->
            finalPartSummaryData.clear()
            for (document in documents) {
                val summary = document.toObject(FinalPartSummaryData::class.java)
                finalPartSummaryData.add(summary)
            }
        }
        .addOnFailureListener { exception ->
            errorMessage.value = "Error fetching time logs: ${exception.message}"
        }
    }

// EditAccomplishmentDialog implementation
@Composable
fun EditAccomplishmentDialog(
    report: AccomplishmentReport,
    onDismiss: () -> Unit,
    onUpdate: (AccomplishmentReport) -> Unit
) {
    var startTime by remember { mutableStateOf(report.startTime) }
    var endTime by remember { mutableStateOf(report.endTime) }
    var activities by remember { mutableStateOf(report.activities) }
    var designation by remember { mutableStateOf(report.designation) }
    var venue by remember { mutableStateOf(report.venue) }
    var timeSpent by remember { mutableStateOf(report.timeSpent) }
    var accomplishments by remember { mutableStateOf(report.accomplishments) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Accomplishment Report") },
        text = {
            Column {
                TextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time") }
                )
                TextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time") }
                )
                TextField(
                    value = activities,
                    onValueChange = { activities = it },
                    label = { Text("Activities") }
                )
                TextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation") }
                )
                TextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue") }
                )
                TextField(
                    value = timeSpent,
                    onValueChange = { timeSpent = it },
                    label = { Text("Time Spent") }
                )
                TextField(
                    value = accomplishments,
                    onValueChange = { accomplishments = it },
                    label = { Text("Accomplishments") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedReport = report.copy(
                    startTime = startTime,
                    endTime = endTime,
                    activities = activities,
                    designation = designation,
                    venue = venue,
                    timeSpent = timeSpent,
                    accomplishments = accomplishments
                )
                onUpdate(updatedReport)
                onDismiss()
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}