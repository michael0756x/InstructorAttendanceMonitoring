package com.example.argeneratorapp.screens

import android.content.pm.PackageManager
import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Environment
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(navController: NavController, userId: String, mainStartDate: String, mainEndDate: String) {
    val firestore = Firebase.firestore
    val reportsList = remember { mutableStateListOf<Map<String, Any?>>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userDepartment by remember { mutableStateOf<String?>(null) }
    var userRateHour by remember { mutableStateOf<String?>(null) }
    var exportSuccess by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }

    LaunchedEffect(Unit) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                userName = document.getString("fullName") ?: "Unknown User"
                userDepartment = document.getString("department") ?: "Unknown Department"
                userRateHour = document.getString("rateHour") ?: "Unknown Rate"
            }
            .addOnFailureListener { error ->
                errorMessage = "Failed to fetch user data: ${error.message}"
                showErrorDialog = true
            }

        fetchReportsData(firestore, userId, mainStartDate, mainEndDate, reportsList) { error ->
            errorMessage = error
            showErrorDialog = error != null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFEFEFEF))
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Summary",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Place the template to Download with the name AccomplishmentReport.xlsx and make sure that name of the sheet is Summary Report",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0000000),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (exportSuccess) {
            Text(
                text = "Exported successfully! Check in your downloads folder.",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (reportsList.isEmpty()) {
            Text(
                text = "No reports available for the selected date range.",
                color = Color(0xFF000000),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            reportsList.forEach { report ->
                val reportsData = report["reports"] as? List<Map<String, String>> ?: emptyList()
                val documentId = report["documentId"] as? String ?: ""

                ReportCard(
                    report = reportsData,
                    documentId = documentId,
                    mainStartDate = mainStartDate,
                    mainEndDate = mainEndDate,
                    submittedAt = report["submittedAt"] as? Timestamp ?: Timestamp.now(),
                    totalConsultation = report["totalConsultation"] as? String ?: "00:00:00",
                    totalETL = report["totalETL"] as? String ?: "00:00:00",
                    totalTeachingLoad = report["totalTeachingLoad"] as? String ?: "00:00:00",
                    totalTimeSpent = report["totalTimeSpent"] as? String ?: "00:00:00",
                    userName = userName ?: "Unknown User",
                    classPerDays = report["classPerDays"] as? List<Map<String, String>> ?: emptyList(),
                    userDepartment = userDepartment ?: "Unknown Department",
                    userRateHour = userRateHour ?: "Unknown Rate",
                    actualHours = report["actualHours"] as? String ?: "00:00:00",
                    finalPartSummary = report["finalPartSummary"] as? List<Map<String, Any?>> ?: emptyList(),
                    timeLogs = report["timeLogs"] as? List<Map<String, Any?>> ?: emptyList(),
                    onExportSuccess = { exportSuccess = true },
                    onDelete = { showDeleteConfirmation = true to documentId }
                )
            }
        }

        if (showErrorDialog) {
            ErrorDialog(errorMessage) { showErrorDialog = false }
        }

        if (showDeleteConfirmation.first) {
            DeleteConfirmationDialog(
                onConfirm = {
                    deleteReport(firestore, showDeleteConfirmation.second!!) { success ->
                        if (success) {
                            reportsList.removeIf { it["documentId"] == showDeleteConfirmation.second }
                        } else {
                            errorMessage = "Failed to delete report."
                            showErrorDialog = true
                        }
                        showDeleteConfirmation = false to null
                    }
                },
                onDismiss = { showDeleteConfirmation = false to null }
            )
        }
    }
}


private fun deleteReport(
    firestore: FirebaseFirestore,
    documentId: String,
    onComplete: (Boolean) -> Unit
) {
    firestore.collection("accomplishment_reports_summary").document(documentId)
        .delete()
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener { exception ->
            Log.e("DeleteReport", "Error deleting report: ${exception.message}")
            onComplete(false)
        }
}

@Composable
fun ReportCard(
    report: List<Map<String, String>>,
    documentId: String,
    submittedAt: Timestamp,
    totalConsultation: String,
    finalPartSummary: List<Map<String, Any?>>,
    classPerDays: List<Map<String, String>>,
    timeLogs: List<Map<String, Any?>>,
    totalETL: String,
    totalTeachingLoad: String,
    actualHours: String,
    totalTimeSpent: String,
    mainStartDate: String,
    mainEndDate: String,
    userName: String,
    userDepartment: String,
    userRateHour: String,
    onExportSuccess: () -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Name: $userName", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            Text(text = "Submitted At: ${submittedAt.toDate()}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            Text(text = "Total Consultation: $totalConsultation", color = Color(0xFF4CAF50))
            Text(text = "Total ETL: $totalETL", color = Color(0xFF4CAF50))
            Text(text = "Total Teaching Load: $totalTeachingLoad", color = Color(0xFF4CAF50))
            Text(text = "Total Time Spent: $totalTimeSpent", color = Color(0xFF4CAF50))

            report.forEach { item ->
                Text(
                    text = "Date: ${item["date"] ?: "N/A"}, Activities: ${item["activities"] ?: "N/A"}, Accomplishments: ${item["accomplishments"] ?: "N/A"}",
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            var hasStoragePermission by remember { mutableStateOf(false) }
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                hasStoragePermission = checkStoragePermission(context)
            }

            ExportButton(
                context,
                userName,
                userDepartment,
                userRateHour,
                report,
                timeLogs,
                finalPartSummary,
                totalConsultation,
                totalETL,
                totalTeachingLoad,
                actualHours,
                totalTimeSpent,
                mainStartDate,
                mainEndDate,
                classPerDays,
                onExportSuccess
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete(documentId) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete Report", color = Color.White)
            }
        }
    }
}

@Composable
fun ExportButton(
    context: Context,
    userName: String,
    userDepartment: String,
    userRateHour: String,
    reportData: List<Map<String, Any?>>,
    timeLogs: List<Map<String, Any?>>,
    finalPartSummary: List<Map<String, Any?>>,
    totalConsultation: String,
    totalETL: String,
    totalTeachingLoad: String,
    actualHours: String,
    totalTimeSpent: String,
    mainStartDate: String,
    mainEndDate: String,
    classPerDays: List<Map<String, String>>,
    onExportSuccess: () -> Unit
) {
    val hasStoragePermission = remember { mutableStateOf(checkStoragePermission(context)) }

    LaunchedEffect(Unit) {
        hasStoragePermission.value = checkStoragePermission(context)
    }

    Button(
        onClick = {
            when {
                hasStoragePermission.value -> {
                    // If permission is granted, proceed with the export
                    exportToExcel(
                        context,
                        userName,
                        userDepartment,
                        userRateHour,
                        reportData,
                        timeLogs,
                        finalPartSummary,
                        totalConsultation,
                        totalETL,
                        totalTeachingLoad,
                        actualHours,
                        totalTimeSpent,
                        mainStartDate,
                        mainEndDate,
                        classPerDays,
                        onExportSuccess
                    )
                }
                else -> {
                    // If permission is not granted, request it
                    requestStoragePermission(context)
                }
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
    ) {
        Text("Export to Excel", color = Color.White)
    }
}



private fun checkStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

private fun requestStoragePermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
        context.startActivity(intent)
    } else {
        val activity = context as? ComponentActivity
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }
}

// Define a request code for storage permission
private const val STORAGE_PERMISSION_REQUEST_CODE = 123

@Composable
fun ErrorDialog(message: String?, onDismiss: () -> Unit) {
    if (message != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Error", color = Color.Red) },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirm Deletion", color = Color.Red) },
        text = { Text("Are you sure you want to delete this report?") },
        confirmButton = {
            Button(onClick = {
                onConfirm()
            }) {
                Text("Yes")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

private suspend fun fetchReportsData(
    firestore: FirebaseFirestore,
    userId: String,
    mainStartDate: String,
    mainEndDate: String,
    reportsList: MutableList<Map<String, Any?>>,
    onError: (String?) -> Unit
) {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse(mainStartDate)
        val endDate = dateFormat.parse(mainEndDate)

        val startTimestamp = Timestamp(startDate)
        val endTimestamp = Timestamp(endDate)

        val querySnapshot = firestore.collection("accomplishment_reports_summary")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("submittedAt", startTimestamp)
            .whereLessThanOrEqualTo("submittedAt", endTimestamp)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            val reportData = document.data ?: emptyMap()

            // Structure the report including all new fields
            val updatedReport = reportData.toMutableMap().apply {
                put("documentId", document.id)
                put("actualHours", reportData["actualHours"] ?: "00:00:00")
                put("finalPartSummary", reportData["finalPartSummary"] ?: emptyList<Map<String, Any?>>())
                put("submittedAt", reportData["submittedAt"])
                put("timeLogs", reportData["timeLogs"] ?: emptyList<Map<String, Any?>>())
                put("totalConsultation", reportData["totalConsultation"] ?: "00:00:00")
                put("totalETL", reportData["totalETL"] ?: "00:00:00")
                put("totalTeachingLoad", reportData["totalTeachingLoad"] ?: "00:00:00")
                put("totalTimeSpent", reportData["totalTimeSpent"] ?: "00:00:00")
                put("userDepartment", reportData["userDepartment"] ?: "")
                put("userName", reportData["userName"] ?: "")
                put("userId", reportData["userId"] ?: "")
                put("userRateHour", reportData["userRateHour"] ?: "")

                // Add the classPerDay and date range
                put("classPerDays", reportData["classPerDays"] ?: emptyList<Map<String, Any?>>())
                put("mainStartDate", mainStartDate)
                put("mainEndDate", mainEndDate)
            }
            reportsList.add(updatedReport)
        }
    } catch (e: Exception) {
        onError("Error fetching reports: ${e.message}")
    }
}

private fun exportToExcel(
    context: Context,
    userName: String,
    userDepartment: String,
    userRateHour: String,
    reportData: List<Map<String, Any?>>,
    timeLogs: List<Map<String, Any?>>,
    finalPartSummary: List<Map<String, Any?>>,
    totalConsultation: String,
    totalETL: String,
    totalTeachingLoad: String,
    actualHours: String,
    totalTimeSpent: String,
    mainStartDate: String,
    mainEndDate: String,
    classPerDays: List<Map<String, String>>, // Include classPerDays as a parameter
    onExportSuccess: () -> Unit
) {
    val progressDialog = ProgressDialog(context)
    progressDialog.setMessage("Exporting data...")
    progressDialog.setCancelable(false)
    progressDialog.show()

    CoroutineScope(Dispatchers.IO).launch {
        val formattedDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val templateFile = File(downloadsPath, "AccomplishmentReport.xlsx")
        val newFile = File(downloadsPath, "AccomplishmentReport_$formattedDate.xlsx")

        try {
            val workbook = FileInputStream(templateFile).use { XSSFWorkbook(it) }
            val sheet = workbook.getSheet("Summary Report") ?: workbook.createSheet("Summary Report")

            val rateAndInstitute = "RATE/HR AND INSTITUTE: $userRateHour - $userDepartment"

            setCellValues(sheet, 8, 0, userName, 7)
            setCellValue(sheet, 72, 0, userName)
            setCellValues(sheet, 10, 0, rateAndInstitute, 7)

            setMainStartAndEndDate(sheet, mainStartDate, mainEndDate)

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
            val longDateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

            // Prepare to set classPerDayHour values based on date
            val classPerDayMap = classPerDays.associate {
                it["date"] to (it["classPerDayHour"] ?: "00:00:00")
            }

            // Process time logs
            var currentDateRow = 13
            timeLogs.groupBy { it["date"] }.forEach { (date, logs) ->
                processTimeLogs(sheet, currentDateRow++, dateFormatter, dayFormatter, longDateFormatter, logs)

                // Set classPerDayHour for the current date
                val classPerDayHour = classPerDayMap[date] ?: "00:00:00"
                setCellValue(sheet, currentDateRow - 2, 5, classPerDayHour) // Set in F column

                processReports(sheet, currentDateRow, reportData.filter { it["date"] == date })
                currentDateRow += 10
            }

            // Set totals
            setCellValue(sheet, 55, 5, actualHours)
            setCellValue(sheet, 56, 5, totalTimeSpent)
            setCellValue(sheet, 57, 5, totalTeachingLoad)
            setCellValues(sheet, 61, 0, listOf(totalTeachingLoad, totalConsultation, "", totalETL, actualHours, totalTimeSpent, totalTimeSpent))

            // Set final part summary
            finalPartSummary.firstOrNull()?.let { summary ->
                setFinalPartSummary(sheet, summary)
            }

            FileOutputStream(newFile).use { workbook.write(it) }
            workbook.close()
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()  // Dismiss the loading dialog
                onExportSuccess()  // Notify the main thread on success
            }
        } catch (e: Exception) {
            Log.e("Export", "Error exporting to Excel: ${e.message}", e)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()  // Dismiss the loading dialog even on error
            }
        }
    }
}

private fun setMainStartAndEndDate(sheet: Sheet, mainStartDate: String, mainEndDate: String) {
    val startMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(mainStartDate)!!)
    val startDay = SimpleDateFormat("d", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(mainStartDate)!!)
    val endDay = SimpleDateFormat("d", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(mainEndDate)!!)
    val endYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(mainEndDate)!!)

    val formattedDate = "$startMonth $startDay-$endDay, $endYear"

    // Set formatted date in A8 to G8
    val dateCells = arrayOf(0, 1, 2, 3, 4, 5, 6) // Columns A to G
    dateCells.forEach { colIndex ->
        setCellValue(sheet, 7, colIndex, formattedDate)
    }
}

private fun processTimeLogs(sheet: Sheet, currentDateRow: Int, dateFormatter: SimpleDateFormat, dayFormatter: SimpleDateFormat, longDateFormatter: SimpleDateFormat, logs: List<Map<String, Any?>>) {
    val dateString = logs.firstOrNull()?.get("date")?.toString() ?: ""
    val dayName = dateString.takeIf { it.isNotEmpty() }?.let { dayFormatter.format(dateFormatter.parse(it)) } ?: ""
    val formattedLongDate = dateString.takeIf { it.isNotEmpty() }?.let { longDateFormatter.format(dateFormatter.parse(it)) } ?: ""

    setCellValue(sheet, currentDateRow - 1, 0, formattedLongDate)
    setCellValue(sheet, currentDateRow, 0, dayName)

    logs.firstOrNull()?.let { log ->
        setCellValue(sheet, currentDateRow, 1, log["actualTimeIn"])
        setCellValue(sheet, currentDateRow, 2, log["actualTimeOut"])
        setCellValue(sheet, currentDateRow, 3, log["totalHours"])
    }
}

private fun processReports(sheet: Sheet, currentDateRow: Int, reports: List<Map<String, Any?>>) {
    reports.take(8).forEachIndexed { index, report ->
        val reportRowIndex = currentDateRow + 1 + index
        setCellValue(sheet, reportRowIndex, 0, report["startTime"])
        setCellValue(sheet, reportRowIndex, 1, report["endTime"])
        setCellValue(sheet, reportRowIndex, 2, report["activities"])
        setCellValue(sheet, reportRowIndex, 3, report["designation"])
        setCellValue(sheet, reportRowIndex, 4, report["venue"])
        setCellValue(sheet, reportRowIndex, 5, report["timeSpent"])
        setCellValue(sheet, reportRowIndex, 6, report["accomplishments"])
    }
}

private fun setFinalPartSummary(sheet: Sheet, summary: Map<String, Any?>) {
    setCellValue(sheet, 63, 1, summary["researchAndExtension"])
    setCellValue(sheet, 64, 1, summary["preparationOfLesson"])
    setCellValue(sheet, 65, 1, summary["checkingOfTestPapers"])
    setCellValue(sheet, 66, 1, summary["noneTime"])
    setCellValue(sheet, 63, 4, summary["adminNoETL"])
    setCellValue(sheet, 65, 4, summary["breakTime"])

    (summary["checkedBy"] as? List<Map<String, String>>)?.forEachIndexed { index, checkedBy ->
        if (index < 3) {
            setCellValue(sheet, 72, index + 1, checkedBy["name"])
            setCellValue(sheet, 73, index + 1, checkedBy["position"])
        }
    }

    (summary["approvedBy"] as? Map<String, String>)?.let { approvedBy ->
        setCellValue(sheet, 72, 4, approvedBy["name"])
        setCellValue(sheet, 73, 4, approvedBy["position"])
    }
}

private fun setCellValue(sheet: Sheet, rowIndex: Int, colIndex: Int, value: Any?) {
    sheet.getRow(rowIndex)?.getCell(colIndex)?.setCellValue(value?.toString() ?: "")
}

private fun setCellValues(sheet: Sheet, rowIndex: Int, startColIndex: Int, value: Any?, count: Int) {
    (0 until count).forEach { col ->
        setCellValue(sheet, rowIndex, startColIndex + col, value)
    }
}

private fun setCellValues(sheet: Sheet, rowIndex: Int, startColIndex: Int, values: List<Any?>) {
    values.forEachIndexed { index, value ->
        setCellValue(sheet, rowIndex, startColIndex + index, value)
    }
}
