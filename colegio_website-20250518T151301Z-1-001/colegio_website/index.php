<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Colegio de Montalban</title>

    <!-- Favicon -->
    <link rel="icon" href="assets/images/ms-icon-310x310.png" type="image/png">

    <!-- CSS Styles -->
    <link rel="stylesheet" href="assets/css/styles.css">
</head>
<body>
    <!-- Navbar -->
    <header class="navbar">
        <button id="toggleBtn" class="toggle-btn">☰</button>
        <a href="#home" class="navbar-brand">Colegio de Montalban</a>
    </header>

    <!-- Sidebar Navigation -->
    <aside class="sidebar closed" id="sidebar">
        <nav>
            <ul>
                <li><a href="#home" class="sidebar-link">Home</a></li>
                <li><a href="#apps" class="sidebar-link">Apps</a></li>
                <li><a href="#events" class="sidebar-link">Events</a></li>
                <li><a href="#contact" class="sidebar-link">Contact</a></li>
            </ul>
        </nav>
    </aside>


    <!-- Main Content -->
    <main id="mainContent">
        <!-- Home Section -->
        <section id="home" class="banner">
            <div class="home-banner">
    <div class="banner-card">
        <img src="assets/images/banners.png" alt="Banner" />
    </div>
</div>

            <!-- Call to Action Buttons -->
            <div class="cta-buttons">
                <a href="#events" class="cta-button">Explore Events</a>
                <a href="#apps" class="cta-button">Download the App</a>
            </div>
        </section>
        
        <!-- Apps Section -->
        <section id="apps" class="section apps-section">
            <h2>Applications</h2>
            <div class="app-list fade-in">
                <?php
                include('includes/db.php');

                // Fetch apps from the database
                $query = "SELECT * FROM apps ORDER BY id DESC"; // Assuming 'id' is a suitable column for sorting
                $result = $conn->query($query);

                if (!$result) {
                    die("Database query failed: " . $conn->error);
                }

                if ($result->num_rows > 0) {
                    while ($row = $result->fetch_assoc()) {
                        echo "<div class='app'>
                                <h3>" . htmlspecialchars($row['name']) . "</h3>
                                <p>" . htmlspecialchars($row['description']) . "</p>
                                <a href='" . htmlspecialchars($row['download_link']) . "' target='_blank' class='download-btn'>Download</a>
                              </div>";
                    }
                } else {
                    echo "<p>No applications found.</p>";
                }
                ?>
            </div>
        </section>
        
        <!-- Promotional Section -->
        <section id="features" class="section feature-section">
            <h2>Introducing Our Apps</h2>
            <div class="feature-list">
                <div class="feature-box">
                    <h3>Desktop App</h3>
                    <p>Manage instructors and their Daily Time Record (DTR) effortlessly with our desktop application. Featuring fingerprint sensor integration for seamless attendance management and easy printing options.</p>
                </div>
                <div class="feature-box">
                    <h3>Mobile App</h3>
                    <p>Our mobile application enables instructors to manage and create their accomplishment reports on the go. Stay organized and access important features from anywhere.</p>
                </div>
            </div>
        </section>

        <!-- Events Section -->
<section id="events" class="section events-section">
    <h2>Upcoming Events</h2>
    <!-- Date Filter -->
    <form id="dateFilterForm" method="GET" class="date-filter-form">
        <label for="eventDate">Select a date:</label>
        <input type="date" id="eventDate" name="eventDate" value="<?php echo isset($_GET['eventDate']) ? $_GET['eventDate'] : ''; ?>" required>
        <button type="submit">Show Events</button>
        
        <!-- Add "Show All Events" button -->
        <button type="button" id="resetFilterBtn">Upcoming Events</button>
    </form>

    <div class="event-list">
        <?php
        include('includes/db.php');

        // Pagination Variables
        $limit = 5; // Number of events per page
        $page = isset($_GET['page']) ? (int)$_GET['page'] : 1; // Current page
        $offset = ($page - 1) * $limit; // Offset for the SQL query

        // Date filter
        $eventDate = isset($_GET['eventDate']) ? $_GET['eventDate'] : '';

        // Prepare the SQL query to show events for the selected date
        if ($eventDate) {
            // Include past events for the selected date
            $query = "SELECT * FROM events WHERE DATE(date) = ? ORDER BY date ASC LIMIT ? OFFSET ?";
            $stmt = $conn->prepare($query);
            $stmt->bind_param('sii', $eventDate, $limit, $offset);
        } else {
            // Show upcoming events if no date is selected
            $query = "SELECT * FROM events WHERE DATE(date) >= CURDATE() ORDER BY date ASC LIMIT ? OFFSET ?";
            $stmt = $conn->prepare($query);
            $stmt->bind_param('ii', $limit, $offset);
        }

        $stmt->execute();
        $result = $stmt->get_result();

        if (!$result) {
            die("Database query failed: " . $conn->error);
        }

        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                echo "<div class='event fade-in'>
                        <img src='uploads/events/" . htmlspecialchars($row['image']) . "' alt='Event Image' class='event-image'>
                        <div class='event-content'>
                            <h3>" . htmlspecialchars($row['title']) . "</h3>
                            <p><strong>Date: </strong>" . htmlspecialchars($row['date']) . "</p>
                            <p>" . htmlspecialchars($row['description']) . "</p>
                        </div>
                      </div>";
            }
        } else {
            echo "<p>No events found for the selected date.</p>";
        }
        ?>
    </div>

    <!-- Pagination Links -->
    <div class="pagination">
        <?php
        // Fetch total number of upcoming events for pagination
        if ($eventDate) {
            $total_query = "SELECT COUNT(*) as total FROM events WHERE DATE(date) = ?";
            $total_stmt = $conn->prepare($total_query);
            $total_stmt->bind_param('s', $eventDate);
        } else {
            $total_query = "SELECT COUNT(*) as total FROM events WHERE DATE(date) >= CURDATE()";
            $total_stmt = $conn->prepare($total_query);
        }
        $total_stmt->execute();
        $total_result = $total_stmt->get_result();
        $total_row = $total_result->fetch_assoc();
        $total_events = $total_row['total'];
        $total_pages = ceil($total_events / $limit); // Calculate total pages

        // Previous Button
        if ($page > 1) {
            $prev_page = $page - 1;
            echo "<a href='?page=$prev_page&eventDate=" . htmlspecialchars($eventDate) . "' class='pagination-link prev-button'>Previous</a>";
        }

        // Create pagination links
        for ($i = 1; $i <= $total_pages; $i++) {
            if ($i == $page) {
                echo "<span class='current-page'>$i</span> "; // Current page
            } else {
                echo "<a href='?page=$i&eventDate=" . htmlspecialchars($eventDate) . "' class='pagination-link'>$i</a> "; // Other pages
            }
        }

        // Next Button
        if ($page < $total_pages) {
            $next_page = $page + 1;
            echo "<a href='?page=$next_page&eventDate=" . htmlspecialchars($eventDate) . "' class='pagination-link next-button'>Next</a>";
        }
        ?>
    </div>
</section>
        
<!-- Holidays Section -->
<section id="holidays" class="section holidays-section">
    <h2>Official Holidays</h2>
    <div class="holiday-list">
        <?php
        // Fetch all holidays from the database
        $query = "SELECT `id`, `name`, `date`, `tag` FROM `holidays` ORDER BY `date` ASC";
        $stmt = $conn->prepare($query);
        $stmt->execute();
        $holiday_result = $stmt->get_result();

        if (!$holiday_result) {
            die("Database query failed: " . $conn->error);
        }

        // Store holidays in an array
        $holidays = [];
        while ($holiday = $holiday_result->fetch_assoc()) {
            $holidays[] = $holiday;
        }

        // Get the current date
        $today = date('Y-m-d');

        // Find the index of the first upcoming holiday
        $upcomingHolidayIndex = 0;
        foreach ($holidays as $index => $holiday) {
            if ($holiday['date'] >= $today) {
                $upcomingHolidayIndex = $index;
                break;
            }
        }

        // Display holidays starting from the first upcoming holiday
        for ($i = 0; $i < count($holidays); $i++) {
            $index = ($upcomingHolidayIndex + $i) % count($holidays); // Loop back to the start
            $holiday = $holidays[$index];

            // Format date without the year
            $formattedDate = date("F j", strtotime($holiday['date'])); // Month Day format

            echo "<div class='holiday-card fade-in'>
                    <h3>" . htmlspecialchars($holiday['name']) . "</h3>
                    <p>Date: " . $formattedDate . "</p>
                    <p>Tag: " . htmlspecialchars($holiday['tag']) . "</p>
                  </div>";
        }
        ?>
    </div>

    <!-- Navigation Buttons -->
        <div class="navigation">
            <a href="#" id="prev-button" class="navigation-link prev-button">Previous</a>
            <a href="#" id="next-button" class="navigation-link next-button">Next</a>
        </div>
</section>
        
        <!-- Contact Section -->
        <section id="contact" class="section contact-section">
            <h2>Contact Us</h2>
            <p>If you have any questions or inquiries, feel free to reach out to us at:</p>
            <p>Email: info@colegiodemontalban.com</p>
            <p>Phone: +63 912 345 6789</p>
            <p>Address: Rodriguez, Rizal, Philippines</p>
        </section>
    </main>

    <!-- Footer -->
    <footer>
        <p>&copy; 2024 Colegio de Montalban. All rights reserved.</p>
    </footer>

    <!-- Optional JS File -->
    <script src="assets/js/scripts.js"></script>
    <script>
        // Toggle Sidebar Visibility
        const toggleBtn = document.getElementById('toggleBtn');
        const sidebar = document.getElementById('sidebar');
        const mainContent = document.getElementById('mainContent');
        let isSidebarOpen = false;

        toggleBtn.addEventListener('click', () => {
            if (isSidebarOpen) {
                sidebar.classList.add('closed');
                mainContent.style.marginLeft = "0";
                isSidebarOpen = false;
            } else {
                sidebar.classList.remove('closed');
                mainContent.style.marginLeft = "180px";
                isSidebarOpen = true;
            }
        });

        // Show All Events button functionality
        document.getElementById('resetFilterBtn').addEventListener('click', () => {
            window.location.href = window.location.pathname;
        });
    </script>
</body>
</html>