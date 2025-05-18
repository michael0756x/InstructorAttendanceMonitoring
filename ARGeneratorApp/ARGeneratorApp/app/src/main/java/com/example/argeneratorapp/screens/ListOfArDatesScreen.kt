package com.example.argeneratorapp.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfArDatesScreen(navController: NavController, userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val dateList = remember { mutableStateListOf<Pair<String, Map<String, String>>>() } // Store document ID and data
    val errorMessage = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for suspend functions
    var showDialog by remember { mutableStateOf(false) }
    var documentIdToDelete by remember { mutableStateOf<String?>(null) }

    // Fetch dates from Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("ar_dates")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            dateList.clear()
            snapshot.documents.forEach { document ->
                val dateData = document.data?.map { (key, value) -> key to value.toString() }?.toMap() ?: emptyMap()
                dateList.add(document.id to dateData) // Store document ID and data
            }
        } catch (e: Exception) {
            Log.e("ListOfArDatesScreen", "Error fetching dates: ${e.message}")
            errorMessage.value = "Error fetching dates: ${e.message}"
        }
    }

    // Delete a date by document ID
    fun deleteDate(documentId: String) {
        coroutineScope.launch {
            try {
                firestore.collection("ar_dates").document(documentId).delete().await()
                dateList.removeIf { it.first == documentId }
            } catch (e: Exception) {
                Log.e("ListOfArDatesScreen", "Error deleting date: ${e.message}")
                errorMessage.value = "Error deleting date: ${e.message}"
            }
        }
    }

    // Main UI Layout with styling updates
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF81C784), Color(0xFF4CAF50)) // Matching green gradient background
                )
            )
            .padding(24.dp) // Match padding with HomeScreen
    ) {
        // Display error message if something went wrong
        if (errorMessage.value.isNotEmpty()) {
            Text(text = errorMessage.value, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
        } else {
            // Title
            Text(
                text = "Main Dates",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                color = Color.White, // White text for consistency
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Display the list of AR dates
            if (dateList.isEmpty()) {
                Text(
                    text = "No dates available.",
                    fontSize = 18.sp,
                    color = Color(0xFF757575), // Subtle gray for no data message
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column {
                    dateList.forEach { (documentId, dateData) ->
                        val mainStartDate = dateData["mainStartDate"] ?: ""
                        val mainEndDate = dateData["mainEndDate"] ?: ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)), // Light teal background for cards
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            Log.d(
                                                "Navigation",
                                                "Navigating to ARManagementScreen with userId: $userId, start: $mainStartDate, end: $mainEndDate"
                                            )
                                            navController.navigate("ar_management/$userId/$mainStartDate/$mainEndDate")
                                        }
                                ) {
                                    Text(
                                        text = "Main Date Range: $mainStartDate - $mainEndDate",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                                        color = Color(0xFF388E3C) // Dark green text for the date range
                                    )
                                }

                                IconButton(onClick = {
                                    // Show the confirmation dialog
                                    documentIdToDelete = documentId
                                    showDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete, // Reference the delete icon
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add AR Dates Button
            Button(
                onClick = {
                    navController.navigate("add_ar_dates/$userId")
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Dark green button background
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Add AR Dates",
                    fontSize = 18.sp,
                    color = Color.White // White text for button
                )
            }
        }
    }

    // Confirmation Dialog for Deleting
    if (showDialog && documentIdToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                documentIdToDelete = null
            },
            title = { Text(text = "Confirm Delete") },
            text = { Text("Are you sure you want to delete this date? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDate(documentIdToDelete!!)
                    showDialog = false
                    documentIdToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    documentIdToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
