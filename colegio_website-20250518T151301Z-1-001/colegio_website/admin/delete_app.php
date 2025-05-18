<?php
include 'includes/auth.php';
include 'includes/db.php';

$id = $_GET['id'];
$conn->query("DELETE FROM apps WHERE id = $id");
header("Location: apps_list.php");
exit;
?>
