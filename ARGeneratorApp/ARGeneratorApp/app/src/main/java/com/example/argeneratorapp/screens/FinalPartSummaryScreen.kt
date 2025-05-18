package com.example.argeneratorapp.screens

import android.util.Log
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalPartSummaryScreen(navController: NavController, userId: String, mainStartDate: String, mainEndDate: String) {
    // Firestore instance
    val firestore = FirebaseFirestore.getInstance()

    // State variables for time inputs
    var researchAndExtensionTime by remember { mutableStateOf("") }
    var preparationOfLessonTime by remember { mutableStateOf("") }
    var checkingOfTestPapersTime by remember { mutableStateOf("") }
    var noneTime by remember { mutableStateOf("") }
    var adminNoETLTime by remember { mutableStateOf("") }
    var breakTime by remember { mutableStateOf("") }

    // State variable for selected date
    var date by remember { mutableStateOf("") }

    // State variables for checked by and approved by inputs
    var checkedBy1Name by remember { mutableStateOf(TextFieldValue("")) }
    var checkedBy1Position by remember { mutableStateOf(TextFieldValue("")) }
    var checkedBy2Name by remember { mutableStateOf(TextFieldValue("")) }
    var checkedBy2Position by remember { mutableStateOf(TextFieldValue("")) }
    var checkedBy3Name by remember { mutableStateOf(TextFieldValue("")) }
    var checkedBy3Position by remember { mutableStateOf(TextFieldValue("")) }
    var approvedByName by remember { mutableStateOf(TextFieldValue("")) }
    var approvedByPosition by remember { mutableStateOf(TextFieldValue("")) }

    // State variable for Firestore data
    val summaryDataList = remember { mutableStateListOf<FinalPartSummaryData>() }

    val context = LocalContext.current

    @Composable
    fun CheckedBySection(checkedBy: MutableList<Pair<String, String>>) {
        Text("Checked by:", style = MaterialTheme.typography.bodyLarge)
        checkedBy.forEachIndexed { index, pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = pair.first,
                    onValueChange = { checkedBy[index] = Pair(it, pair.second) },
                    label = { Text("Name ${index + 1}") }
                )
                TextField(
                    value = pair.second,
                    onValueChange = { checkedBy[index] = Pair(pair.first, it) },
                    label = { Text("Position ${index + 1}") }
                )
            }
        }
    }

    @Composable
    fun ApprovedBySection(name: MutableState<TextFieldValue>, position: MutableState<TextFieldValue>) {
        Text("Approved by:", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") }
            )
            TextField(
                value = position.value,
                onValueChange = { position.value = it },
                label = { Text("Position") }
            )
        }
    }

    // Function to show Time Picker
    fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                // Set seconds to 00
                val second = 0
                // Format the time as HH:mm:ss with fixed seconds
                val timeString = String.format("%02d:%02d:%02d", hour, minute, second)
                onTimeSelected(timeString)
            },
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            true
        ).show()
    }


    // Function to submit data to Firestore
    fun submitData() {
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

        val submissionDateObject = Date(submissionDate)

        val timestamp = Timestamp(submissionDateObject)

        // Convert Timestamp to only date (year, month, day)
        val dateOnlyString = timestamp.toDate().let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) // Format without time
        }

        // Create an instance of FinalPartSummaryData without the id
        val summaryData = FinalPartSummaryData(
            id = "", // Leave empty for now; it will be set after submission
            userId = userId,
            date = dateOnlyString,
            researchAndExtensionTime = researchAndExtensionTime,
            preparationOfLessonTime = preparationOfLessonTime,
            checkingOfTestPapersTime = checkingOfTestPapersTime,
            noneTime = noneTime,
            adminNoETLTime = adminNoETLTime,
            breakTime = breakTime,
            checkedBy = listOf(
                mapOf("name" to checkedBy1Name.text, "position" to checkedBy1Position.text),
                mapOf("name" to checkedBy2Name.text, "position" to checkedBy2Position.text),
                mapOf("name" to checkedBy3Name.text, "position" to checkedBy3Position.text)
            ),
            approvedBy = mapOf(
                "name" to approvedByName.text,
                "position" to approvedByPosition.text
            )
        )

        // Add the summary data to Firestore and retrieve the document ID
        firestore.collection("finalPartSummary")
            .add(summaryData.toMap())
            .addOnSuccessListener { documentReference ->
                // Show Toast notification
                Toast.makeText(context, "Data submitted successfully!", Toast.LENGTH_SHORT).show()

                // Update the summaryData with the generated document ID
                val updatedSummaryData = summaryData.copy(id = documentReference.id)

                // Optionally, you can also save this updated data back to Firestore
                firestore.collection("finalPartSummary").document(updatedSummaryData.id)
                    .set(updatedSummaryData.toMap())
                    .addOnSuccessListener {
                        // Handle successful update if necessary
                        // Add the newly created summary data to the list
                        summaryDataList.add(updatedSummaryData)
                    }
            }
            .addOnFailureListener { e ->
                // Show error notification
                Toast.makeText(context, "Submission failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to delete data from Firestore
    fun deleteData(id: String) {
        firestore.collection("finalPartSummary").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Data deleted successfully!", Toast.LENGTH_SHORT).show()
                summaryDataList.removeIf { summaryData -> summaryData.id == id }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun fetchSummaryDetails() {
        firestore.collection("finalPartSummary")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    Log.d("FirestoreQuery", "Fetched document: ${document.data}")
                    val summaryData = document.data

                    val approvedBy = summaryData?.get("approvedBy") as? Map<*, *>
                    val approvedByNameValue = approvedBy?.get("name") as? String ?: ""
                    val approvedByPositionValue = approvedBy?.get("position") as? String ?: ""

                    if (approvedByName.text.isEmpty()) {
                        approvedByName = TextFieldValue(approvedByNameValue)
                    }
                    if (approvedByPosition.text.isEmpty()) {
                        approvedByPosition = TextFieldValue(approvedByPositionValue)
                    }

                    val checkedByList = summaryData?.get("checkedBy") as? List<Map<String, String>> ?: emptyList()

                    if (checkedByList.size > 0 && checkedBy1Name.text.isEmpty()) {
                        checkedBy1Name = TextFieldValue(checkedByList[0]["name"] ?: "")
                        checkedBy1Position = TextFieldValue(checkedByList[0]["position"] ?: "")
                    }
                    if (checkedByList.size > 1 && checkedBy2Name.text.isEmpty()) {
                        checkedBy2Name = TextFieldValue(checkedByList[1]["name"] ?: "")
                        checkedBy2Position = TextFieldValue(checkedByList[1]["position"] ?: "")
                    }
                    if (checkedByList.size > 2 && checkedBy3Name.text.isEmpty()) {
                        checkedBy3Name = TextFieldValue(checkedByList[2]["name"] ?: "")
                        checkedBy3Position = TextFieldValue(checkedByList[2]["position"] ?: "")
                    }
                } else {
                    Log.d("FirestoreQuery", "No documents found.")
                    Toast.makeText(context, "No summary details found for this user.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreQuery", "Failed to fetch details: ${e.message}", e)
                Toast.makeText(context, "Failed to fetch summary details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to fetch data from Firestore
    fun fetchData() {
        firestore.collection("finalPartSummary")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                summaryDataList.clear()
                for (document in documents) {
                    val summaryData = document.toObject(FinalPartSummaryData::class.java).copy(id = document.id)
                    summaryDataList.add(summaryData)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch data when the composable is first displayed
    LaunchedEffect(Unit) {
        fetchData()
        fetchSummaryDetails()
    }

    // Adding a vertical scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5DC)) // Beige background for the screen
            .verticalScroll(scrollState), // Enable vertical scrolling
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("SUMMARY OF OTHERS CONSULTATION HRS - Category", style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = { showTimePickerDialog { researchAndExtensionTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (researchAndExtensionTime.isNotEmpty()) researchAndExtensionTime else "RESEARCH AND EXTENSION Time")
        }
        Button(
            onClick = { showTimePickerDialog { preparationOfLessonTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (preparationOfLessonTime.isNotEmpty()) preparationOfLessonTime else "PREPARATION OF LESSON Time")
        }
        Button(
            onClick = { showTimePickerDialog { checkingOfTestPapersTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (checkingOfTestPapersTime.isNotEmpty()) checkingOfTestPapersTime else "CHECKING OF TEST PAPERS Time")
        }
        Button(
            onClick = { showTimePickerDialog { noneTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (noneTime.isNotEmpty()) noneTime else "NONE Time")
        }

        // Section: SUMMARY OF PARTIME ADMIN (CDM)
        Text("SUMMARY OF PARTIME ADMIN (CDM) - Category", style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = { showTimePickerDialog { adminNoETLTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (adminNoETLTime.isNotEmpty()) adminNoETLTime else "ADMIN NO ETL Time")
        }
        Button(
            onClick = { showTimePickerDialog { breakTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (breakTime.isNotEmpty()) breakTime else "BREAK TIME")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Checked by:", style = MaterialTheme.typography.bodyLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = checkedBy1Name,
                    onValueChange = { checkedBy1Name = it },
                    label = { Text("Checked By 1 Name") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = checkedBy1Position,
                    onValueChange = { checkedBy1Position = it },
                    label = { Text("Position 1") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = checkedBy2Name,
                    onValueChange = { checkedBy2Name = it },
                    label = { Text("Checked By 2 Name") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = checkedBy2Position,
                    onValueChange = { checkedBy2Position = it },
                    label = { Text("Position 2") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = checkedBy3Name,
                    onValueChange = { checkedBy3Name = it },
                    label = { Text("Checked By 3 Name") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = checkedBy3Position,
                    onValueChange = { checkedBy3Position = it },
                    label = { Text("Position 3") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Approved by:", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = approvedByName,
                    onValueChange = { approvedByName = it },
                    label = { Text("Approved By Name") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = approvedByPosition,
                    onValueChange = { approvedByPosition = it },
                    label = { Text("Approved By Position") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text("Previously Submitted Data:", style = MaterialTheme.typography.bodyLarge)

        // Iterate through the summary data list and display each entry with a delete button
        summaryDataList.forEach { summaryData ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = summaryData.date, style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = { deleteData(summaryData.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = { submitData() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9ACD32)) // Yellow-green color
        ) {
            Text("Submit Summary")
        }
    }
}