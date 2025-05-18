<?php
include 'includes/auth.php';
include 'includes/db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $name = $_POST['name'];
    $description = $_POST['description'];
    
    // Handle file upload
    $file_name = $_FILES['app_file']['name'];
    $file_tmp = $_FILES['app_file']['tmp_name'];
    $file_destination = '../uploads/' . $file_name; // Directory to upload

    if (move_uploaded_file($file_tmp, $file_destination)) {
        $stmt = $conn->prepare("INSERT INTO apps (name, description, download_link) VALUES (?, ?, ?)");
        $download_link = 'uploads/' . $file_name; // Store file path in DB
        $stmt->bind_param("sss", $name, $description, $download_link);
        $stmt->execute();
        header("Location: apps_list.php");
        exit;
    } else {
        $error = "Failed to upload app file.";
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Add App</title>
    <link rel="stylesheet" href="assets/css/admin-apps.css"> <!-- Updated CSS file -->
</head>
<body>
    <div class="add-app-container">
        <h2>Add App</h2>
        <?php if (isset($error)) echo "<p>$error</p>"; ?>
        
        <form action="add_app.php" method="POST" enctype="multipart/form-data">
            <label for="name">App Name</label>
            <input type="text" name="name" required>
            <label for="description">Description</label>
            <textarea name="description" required></textarea>
            <label for="app_file">Upload App File</label>
            <input type="file" name="app_file" required>
            <button type="submit" class="submit-btn">Add App</button>
        </form>

        <!-- Back Button -->
        <a href="apps_list.php" class="back-btn">Back to App List</a>
    </div>
</body>
</html>
