<?php
include 'includes/auth.php';
include 'includes/db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $title = $_POST['title'];
    $description = $_POST['description'];
    $date = $_POST['date'];
    
    // Handle file upload for event image
    $image = $_FILES['image']['name'];
    $target = "../uploads/events/" . basename($image);

    if (move_uploaded_file($_FILES['image']['tmp_name'], $target)) {
        echo "Image uploaded successfully.";
    } else {
        echo "Failed to upload image.";
    }

    $stmt = $conn->prepare("INSERT INTO events (title, description, date, image) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssss", $title, $description, $date, $image);
    $stmt->execute();
    header("Location: events_list.php");
    exit;
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Add Event</title>
    <link rel="stylesheet" href="assets/css/admin-events.css">
</head>
<body>
    <div class="add-event-container">
        <h2>Add Event</h2>
        <form action="add_event.php" method="POST" enctype="multipart/form-data">
            <label for="title">Title</label>
            <input type="text" name="title" required>
            
            <label for="description">Description</label>
            <textarea name="description" required></textarea>
            
            <label for="date">Event Date</label>
            <input type="date" name="date" required>
            
            <label for="image">Event Image</label>
            <input type="file" name="image" required>
            
            <button class="submit-btn" type="submit">Add Event</button>
        </form>
        <!-- Back Button -->
        <a href="events_list.php" class="back-btn">Back to Manage Events</a>
    </div>
</body>
</html>
