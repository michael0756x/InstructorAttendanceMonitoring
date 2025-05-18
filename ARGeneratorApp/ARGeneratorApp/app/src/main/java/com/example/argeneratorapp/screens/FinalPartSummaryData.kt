package com.example.argeneratorapp.screens

data class FinalPartSummaryData(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val researchAndExtensionTime: String = "",
    val preparationOfLessonTime: String = "",
    val checkingOfTestPapersTime: String = "",
    val noneTime: String = "",
    val adminNoETLTime: String = "",
    val breakTime: String = "",
    val checkedBy: List<Map<String, String>> = listOf(),
    val approvedBy: Map<String, String> = mapOf()
) {
    // Function to convert the data class to a Map for Firestore submission
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "date" to date,
            "researchAndExtensionTime" to researchAndExtensionTime,
            "preparationOfLessonTime" to preparationOfLessonTime,
            "checkingOfTestPapersTime" to checkingOfTestPapersTime,
            "noneTime" to noneTime,
            "adminNoETLTime" to adminNoETLTime,
            "breakTime" to breakTime,
            "checkedBy" to checkedBy,
            "approvedBy" to approvedBy
        )
    }
}
