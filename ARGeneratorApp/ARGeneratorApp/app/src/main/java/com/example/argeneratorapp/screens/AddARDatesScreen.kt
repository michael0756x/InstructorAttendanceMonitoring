package com.example.argeneratorapp.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddARDatesScreen(navController: NavController, userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)) // Lighter green gradient
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Add AR Dates",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            color = Color(0xFF388E3C), // Darker green for the title
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Start Date Selection
        Button(
            onClick = {
                showDatePicker(context) { date ->
                    startDate = date
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)) // Light green for the button
        ) {
            Text(
                text = if (startDate.isNotEmpty()) startDate else "Select Start Date",
                fontSize = 18.sp,
                color = Color.White // White text for contrast
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // End Date Selection
        Button(
            onClick = {
                showDatePicker(context) { date ->
                    endDate = date
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)) // Light green for the button
        ) {
            Text(
                text = if (endDate.isNotEmpty()) endDate else "Select End Date",
                fontSize = 18.sp,
                color = Color.White // White text for contrast
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                    val arData = hashMapOf(
                        "userId" to userId,
                        "mainStartDate" to startDate,
                        "mainEndDate" to endDate
                    )

                    firestore.collection("ar_dates").add(arData)
                        .addOnSuccessListener {
                            navController.navigate("list_of_ar_dates/$userId")
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Error adding date: ${e.message}"
                        }
                } else {
                    errorMessage = "Please select both dates."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Darker green for submit
        ) {
            Text("Submit", fontSize = 18.sp, color = Color.White)
        }

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
        }
    }
}

// Function to show date picker dialog and format date with dashes
private fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        val formattedMonth = (selectedMonth + 1).toString().padStart(2, '0')
        val formattedDay = selectedDay.toString().padStart(2, '0')
        val date = "$selectedYear-$formattedMonth-$formattedDay"
        onDateSelected(date)
    }, year, month, day).show()
}
