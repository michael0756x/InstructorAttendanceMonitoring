<?php
$servername = "sql201.infinityfree.com"; // Use the provided MySQL Hostname
$username = "if0_37566759"; // Use the provided MySQL Username
$password = "michael0756x"; // Use the provided MySQL Password
$dbname = "if0_37566759_cdmi"; // Use the provided MySQL Database Name

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>
