<?php
include 'includes/auth.php';
include 'includes/db.php';

$id = $_GET['id'];
$app = $conn->query("SELECT * FROM apps WHERE id = $id")->fetch_assoc();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $name = $_POST['name'];
    $description = $_POST['description'];
    
    // Check if a new file was uploaded
    if (!empty($_FILES['app_file']['name'])) {
        $file_name = $_FILES['app_file']['name'];
        $file_tmp = $_FILES['app_file']['tmp_name'];
        $file_destination = '../uploads/' . $file_name;
        move_uploaded_file($file_tmp, $file_destination);
        $download_link = 'uploads/' . $file_name;
    } else {
        // If no new file is uploaded, retain the old file link
        $download_link = $app['download_link'];
    }

    $stmt = $conn->prepare("UPDATE apps SET name=?, description=?, download_link=? WHERE id=?");
    $stmt->bind_param("sssi", $name, $description, $download_link, $id);
    $stmt->execute();
    header("Location: apps_list.php");
    exit;
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Edit App</title>
    <link rel="stylesheet" href="assets/css/admin-apps.css"> <!-- Updated CSS file -->
</head>
<body>
    <div class="edit-app-container">
        <h2>Edit App</h2>
        <form action="edit_app.php?id=<?php echo $id; ?>" method="POST" enctype="multipart/form-data">
            <label for="name">App Name</label>
            <input type="text" name="name" value="<?php echo htmlspecialchars($app['name']); ?>" required>
            <label for="description">Description</label>
            <textarea name="description" required><?php echo htmlspecialchars($app['description']); ?></textarea>
            <label for="app_file">Replace App File</label>
            <input type="file" name="app_file">
            <p>Current file: <a href="../<?php echo htmlspecialchars($app['download_link']); ?>">Download</a></p>
            <button type="submit" class="submit-btn">Update App</button>
        </form>

        <!-- Back Button -->
        <a href="apps_list.php" class="back-btn">Back to App List</a>
    </div>
</body>
</html>
