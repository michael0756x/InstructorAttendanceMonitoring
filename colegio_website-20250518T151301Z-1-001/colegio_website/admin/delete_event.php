<?php
// Include your database connection file
include('includes/db.php');

// Check if the event ID is set
if (isset($_GET['id'])) {
    $eventId = intval($_GET['id']); // Get the event ID from the URL and ensure it's an integer

    // Prepare the SQL statement to prevent SQL injection
    $stmt = $conn->prepare("DELETE FROM events WHERE id = ?");
    $stmt->bind_param("i", $eventId);

    // Execute the statement
    if ($stmt->execute()) {
        // Redirect to the events list page with a success message
        header("Location: events_list.php?msg=deleted");
        exit();
    } else {
        // Redirect to the events list page with an error message
        header("Location: events_list.php?msg=error");
        exit();
    }

    // Close the statement
    $stmt->close();
} else {
    // Redirect to the events list page with an error message if no ID is provided
    header("Location: events_list.php?msg=invalid");
    exit();
}

// Close the database connection
$conn->close();
?>
