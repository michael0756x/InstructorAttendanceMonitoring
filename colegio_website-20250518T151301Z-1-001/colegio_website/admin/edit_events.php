<?php
// Include the database connection
include('includes/db.php');

// Check if an event ID is passed via GET
if (isset($_GET['id'])) {
    $event_id = $_GET['id'];

    // Fetch the event details
    $query = "SELECT * FROM events WHERE id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param('i', $event_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $event = $result->fetch_assoc();

    // Check if the event exists
    if (!$event) {
        echo "Event not found.";
        exit;
    }
} else {
    echo "Invalid event ID.";
    exit;
}

// Handle form submission to update the event
if (isset($_POST['update_event'])) {
    $title = $_POST['title'];
    $description = $_POST['description'];
    $date = $_POST['date'];

    // Check if a new image is uploaded
    if ($_FILES['image']['name']) {
        $image = $_FILES['image']['name'];
        $target = "uploads/events/" . basename($image);

        // Upload the image file
        if (!move_uploaded_file($_FILES['image']['tmp_name'], $target)) {
            echo "Failed to upload image";
            $image = $event['image']; // Keep the old image if the upload fails
        }
    } else {
        $image = $event['image']; // Keep the old image if no new one is uploaded
    }

    // Update event in the database
    $update_query = "UPDATE events SET title = ?, description = ?, date = ?, image = ? WHERE id = ?";
    $stmt = $conn->prepare($update_query);
    $stmt->bind_param('ssssi', $title, $description, $date, $image, $event_id);

    if ($stmt->execute()) {
        header("Location: events_list.php"); // Redirect back to the event list page
        exit;
    } else {
        echo "Error updating event.";
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Event</title>
    <link rel="stylesheet" href="assets/css/admin-events.css"> <!-- Updated to use the same CSS file -->
</head>
<body>
    <div class="edit-event-container">
        <h2>Edit Event</h2>
        <form action="edit_events.php?id=<?php echo $event_id; ?>" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="title">Event Title:</label>
                <input type="text" name="title" value="<?php echo htmlspecialchars($event['title']); ?>" required>
            </div>

            <div class="form-group">
                <label for="description">Event Description:</label>
                <textarea name="description" required><?php echo htmlspecialchars($event['description']); ?></textarea>
            </div>

            <div class="form-group">
                <label for="date">Event Date:</label>
                <input type="date" name="date" value="<?php echo htmlspecialchars($event['date']); ?>" required>
            </div>

            <div class="form-group">
                <label for="image">Event Image:</label><br>
                <?php if ($event['image']) { ?>
                    <img src="uploads/events/<?php echo htmlspecialchars($event['image']); ?>" alt="Event Image" class="event-preview-image"><br>
                <?php } ?>
                <input type="file" name="image">
            </div>

            <div class="form-actions">
                <button class="submit-btn" type="submit" name="update_event">Update Event</button>
            </div>
        </form>

        <!-- Back Button -->
        <a href="events_list.php" class="back-btn">Back to Event List</a>
    </div>
</body>
</html>
