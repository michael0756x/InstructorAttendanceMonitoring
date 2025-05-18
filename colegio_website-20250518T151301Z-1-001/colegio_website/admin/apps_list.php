<?php
include 'includes/auth.php';
include 'includes/db.php';

// Pagination variables
$appsPerPage = 5; // Number of apps to display per page
$currentPage = isset($_GET['page']) ? (int)$_GET['page'] : 1;
$offset = ($currentPage - 1) * $appsPerPage;

// Query to count total apps
$countQuery = "SELECT COUNT(*) as total FROM apps";
$countResult = $conn->query($countQuery);
$totalApps = $countResult->fetch_assoc()['total'];
$totalPages = ceil($totalApps / $appsPerPage);

// Query to fetch apps for the current page
$query = "SELECT * FROM apps LIMIT $offset, $appsPerPage";
$result = $conn->query($query);
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Apps</title>
    <link rel="stylesheet" href="assets/css/admin-apps.css"> <!-- New CSS file -->
</head>
<body>
    <div class="apps-container">
        <h2>Manage Apps</h2>
        <a href="add_app.php" class="add-app-btn">Add New App</a>
        
        <table>
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Download Link</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <?php while ($row = $result->fetch_assoc()): ?>
                    <tr>
                        <td><?php echo htmlspecialchars($row['name']); ?></td>
                        <td><?php echo htmlspecialchars($row['description']); ?></td>
                        <td><a href="<?php echo htmlspecialchars('/colegio_website/' . $row['download_link']); ?>">Download</a></td>
                        <td>
                            <a href="edit_app.php?id=<?php echo $row['id']; ?>" class="edit-link">Edit</a>
                            <a href="delete_app.php?id=<?php echo $row['id']; ?>" class="delete-link">Delete</a>
                        </td>
                    </tr>
                <?php endwhile; ?>
            </tbody>
        </table>

        <!-- Pagination Links -->
        <div class="pagination">
            <?php if ($currentPage > 1): ?>
                <a href="?page=<?php echo $currentPage - 1; ?>">Previous</a>
            <?php endif; ?>

            <?php for ($i = 1; $i <= $totalPages; $i++): ?>
                <a href="?page=<?php echo $i; ?>" <?php if ($i == $currentPage) echo 'class="active"'; ?>>
                    <?php echo $i; ?>
                </a>
            <?php endfor; ?>

            <?php if ($currentPage < $totalPages): ?>
                <a href="?page=<?php echo $currentPage + 1; ?>">Next</a>
            <?php endif; ?>
        </div>

        <!-- Back Button -->
        <a href="index.php" class="back-btn">Back to Dashboard</a>
    </div>
</body>
</html>
