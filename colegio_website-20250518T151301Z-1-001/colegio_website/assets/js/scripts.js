// Toggle Sidebar
document.addEventListener('DOMContentLoaded', function() {
    const toggleBtn = document.querySelector('.toggle-btn');
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.getElementById('mainContent');
    let isSidebarOpen = true;

    toggleBtn.addEventListener('click', function() {
        if (isSidebarOpen) {
            sidebar.classList.add('closed'); // Add closed class
            mainContent.style.marginLeft = "0"; // Shift main content back
            isSidebarOpen = false;
        } else {
            sidebar.classList.remove('closed'); // Remove closed class
            mainContent.style.marginLeft = "180px"; // Adjust for open sidebar
            isSidebarOpen = true;
        }
    });

    // Scroll Animation for Fade-in Elements
    window.addEventListener('scroll', function() {
        const elements = document.querySelectorAll('.fade-in');
        const windowHeight = window.innerHeight;

        elements.forEach(function(element) {
            const positionFromTop = element.getBoundingClientRect().top;

            if (positionFromTop - windowHeight <= 0) {
                element.classList.add('visible');
            }
        });
    });

    // Sticky Navbar on Scroll
    window.onscroll = function() {
        var navbar = document.querySelector('.navbar');
        if (window.pageYOffset > 0) {
            navbar.classList.add('sticky');
        } else {
            navbar.classList.remove('sticky');
        }
    };

    // Scroll to Events After Submission or Pagination Operation
    document.querySelector('#events').scrollIntoView({ behavior: 'smooth' });

    // JavaScript for Holiday Cycling
    let currentIndex = 0;
    const holidayCards = document.querySelectorAll('.holiday-card');
    const totalHolidays = holidayCards.length;

    // Function to show the next holiday
    function showNextHoliday() {
        holidayCards[currentIndex].style.display = 'none'; // Hide current holiday
        currentIndex = (currentIndex + 1) % totalHolidays; // Increment index and loop back
        holidayCards[currentIndex].style.display = 'block'; // Show next holiday
    }

    // Function to show the previous holiday
    function showPrevHoliday() {
        holidayCards[currentIndex].style.display = 'none'; // Hide current holiday
        currentIndex = (currentIndex - 1 + totalHolidays) % totalHolidays; // Decrement index and loop back
        holidayCards[currentIndex].style.display = 'block'; // Show previous holiday
    }

    // Initialize display
    holidayCards.forEach((card, index) => {
        if (index !== currentIndex) {
            card.style.display = 'none'; // Hide all except the first one
        }
    });

    // Event listeners for navigation buttons
    document.getElementById('next-button').addEventListener('click', (e) => {
        e.preventDefault();
        showNextHoliday();
    });

    document.getElementById('prev-button').addEventListener('click', (e) => {
        e.preventDefault();
        showPrevHoliday();
    });
});

// JavaScript to reset the date filter and show all events
document.getElementById('resetFilterBtn').addEventListener('click', function() {
    // Clear the date input value
    document.getElementById('eventDate').value = '';

    // Redirect to the same page without the 'eventDate' parameter to show all events
    window.location.href = window.location.pathname;
});
