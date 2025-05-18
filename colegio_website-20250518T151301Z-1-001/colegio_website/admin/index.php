<?php include 'includes/auth.php'; ?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="assets/css/admin-dashboard.css">
</head>
<body>
    <div class="dashboard-container">
        <h2>Admin Dashboard</h2>
        <ul class="dashboard-menu">
            <li><a href="events_list.php" class="dashboard-link">Manage Events</a></li>
            <li><a href="apps_list.php" class="dashboard-link">Manage Apps</a></li>
        </ul>
        <form action="logout.php" method="POST" class="logout-form">
            <button type="submit" class="logout-btn">Logout</button>
        </form>
    </div>
</body>
</html>
