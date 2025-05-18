<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event List</title>
    <link rel="stylesheet" href="assets/css/admin-events.css">
    <script>
        // Function to show alert messages
        function showAlert(message) {
            alert(message);
        }
    </script>
</head>
<body>
    <div class="container">

        <?php
        // Include your database connection file
        include('includes/db.php'); // Ensure this file contains your DB connection logic

        // Check if there's a message to display
        if (isset($_GET['msg'])) {
            if ($_GET['msg'] == 'deleted') {
                echo "<script>showAlert('Event deleted successfully.');</script>";
            } elseif ($_GET['msg'] == 'error') {
                echo "<script>showAlert('Error deleting event.');</script>";
            } elseif ($_GET['msg'] == 'invalid') {
                echo "<script>showAlert('Invalid event ID.');</script>";
            }
        }

        // Pagination variables
        $eventsPerPage = 10; // Number of events to display per page
        $currentPage = isset($_GET['page']) ? (int)$_GET['page'] : 1;
        $offset = ($currentPage - 1) * $eventsPerPage;

        // Search functionality
        $search = isset($_GET['search']) ? $_GET['search'] : '';
        $searchQuery = $search ? "WHERE title LIKE '%$search%' OR description LIKE '%$search%'" : '';

        // Query to count total events
        $countQuery = "SELECT COUNT(*) as total FROM events $searchQuery";
        $countResult = $conn->query($countQuery);
        $totalEvents = $countResult->fetch_assoc()['total'];
        $totalPages = ceil($totalEvents / $eventsPerPage);

        // Sorting functionality
        $sortOrder = isset($_GET['sort']) ? $_GET['sort'] : 'newest';
        if ($sortOrder === 'oldest') {
            $query = "SELECT * FROM events $searchQuery ORDER BY date ASC LIMIT $offset, $eventsPerPage";
        } else {
            $query = "SELECT * FROM events $searchQuery ORDER BY date DESC LIMIT $offset, $eventsPerPage";
        }

        $result = $conn->query($query);

        if (!$result) {
            echo "Error fetching events: " . $conn->error;
            exit();
        }
        ?>

        <div class="events-container">
            <h2>Manage Events</h2>
            <a href="add_event.php" class="add-event-btn">Add New Event</a>

            <!-- Search Bar -->
            <form action="" method="GET" class="search-form">
                <input type="text" name="search" placeholder="Search events..." value="<?php echo htmlspecialchars($search); ?>">
                <button type="submit">Search</button>
            </form>

            <!-- Sorting Options -->
            <form action="" method="GET" class="sort-form">
                <input type="hidden" name="search" value="<?php echo htmlspecialchars($search); ?>">
                <select name="sort" onchange="this.form.submit()">
                    <option value="newest" <?php if ($sortOrder === 'newest') echo 'selected'; ?>>Newest</option>
                    <option value="oldest" <?php if ($sortOrder === 'oldest') echo 'selected'; ?>>Oldest</option>
                </select>
            </form>

            <table>
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Description</th>
                        <th>Date</th>
                        <th>Image</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <?php while ($row = $result->fetch_assoc()): ?>
                        <tr>
                            <td><?php echo htmlspecialchars($row['title']); ?></td>
                            <td><?php echo htmlspecialchars($row['description']); ?></td>
                            <td><?php echo htmlspecialchars($row['date']); ?></td>
                            <td><img src="../uploads/events/<?php echo htmlspecialchars($row['image']); ?>" alt="Event Image" class="event-image"></td>
                            <td>
                                <a href="edit_events.php?id=<?php echo $row['id']; ?>" class="edit-link">Edit</a>
                                <a href="delete_event.php?id=<?php echo $row['id']; ?>" class="delete-link">Delete</a>
                            </td>
                        </tr>
                    <?php endwhile; ?>
                </tbody>
            </table>

            <!-- Pagination Links -->
            <div class="pagination">
                <?php if ($currentPage > 1): ?>
                    <a href="?page=<?php echo $currentPage - 1; ?>&search=<?php echo htmlspecialchars($search); ?>&sort=<?php echo $sortOrder; ?>">Previous</a>
                <?php endif; ?>

                <?php for ($i = 1; $i <= $totalPages; $i++): ?>
                    <a href="?page=<?php echo $i; ?>&search=<?php echo htmlspecialchars($search); ?>&sort=<?php echo $sortOrder; ?>" <?php if ($i == $currentPage) echo 'style="font-weight: bold;"'; ?>>
                        <?php echo $i; ?>
                    </a>
                <?php endfor; ?>

                <?php if ($currentPage < $totalPages): ?>
                    <a href="?page=<?php echo $currentPage + 1; ?>&search=<?php echo htmlspecialchars($search); ?>&sort=<?php echo $sortOrder; ?>">Next</a>
                <?php endif; ?>
            </div>

            <!-- Back Button -->
            <a href="index.php" class="back-btn">Back to Dashboard</a>
        </div>
    </div>
</body>
</html>
