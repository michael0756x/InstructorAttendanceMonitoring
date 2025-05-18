-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 15, 2024 at 03:14 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `colegio_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `apps`
--

CREATE TABLE `apps` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `download_link` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `apps`
--

INSERT INTO `apps` (`id`, `name`, `description`, `download_link`) VALUES
(1, 'Desktop App', 'DTR', 'uploads/AutoClicker-3.0.exe'),
(3, 'Mobile App', 'AR', 'uploads/Valorant Tracker - Installer.exe');

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

CREATE TABLE `events` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `events`
--

INSERT INTO `events` (`id`, `title`, `description`, `image`, `date`) VALUES
(1, 'Walang pasok', 'Dahil sa bagyo', 'more-leaves-on-green.png', '2024-09-20'),
(2, 'abderfg', 'hijklmnop', 'wallpapersden.com_lo-fi-room-cool-anime_3584x2048.jpg', '2024-09-26'),
(3, 'gdfhfghxer', 'graffcxzadfaf', 'wallpapersden.com_lo-fi-room-cool-anime_3584x2048.jpg', '2024-09-26'),
(5, 'gdfhxdfawf', 'ezsrfwafwa', '13_01_wafwaf303vastu7-ll.jpg', '2024-09-25'),
(6, 'gdhssehaertshawfawfawfawfawfwadawdaw', 'ezharhahaerafwa', 'harhaehre03vastu7-ll.jpg', '2024-09-25'),
(8, 'abcd', 'efgh', 'ijkl.jpg', '2024-09-25'),
(9, 'abcdefgh', 'qrqrqwreqw', 'zdbzzfdg.jpg', '2024-09-25'),
(10, 'abaff', 'fASfas', 'zgzfdfbzfdg.jpg', '2024-09-25'),
(11, '523rrfds', '13rdsfaf', 'zg341afdg.jpg', '2024-09-25'),
(12, '21st Anniversary ', '6 am to 12 pm\r\n', 'waldwadawdaw_3584x2048.jpg', '2024-09-27'),
(13, 'dwawadwad', 'wdaawdawdwaddawfg', '12344.jpg', '2024-09-30'),
(14, 'avxnfgtjtr', 'egaegag', 'waldwadawdaw_3584x2048.jpg', '2024-09-29'),
(15, 'fafaewfawe', 'faeaeewa', '43235.jpg', '2024-09-30'),
(16, 'wgdfsatj', 'duyudyjd', '46436345.jpg', '2024-09-30'),
(17, 'srghsth', 'aresgrseat', '43634.jpg', '2024-09-30'),
(18, 'hgfdjvcn', 'sya4ysty', '325325.jpg', '2024-09-30'),
(19, 'asfzg', 'cnxdths', '3687645.jpg', '2024-09-30'),
(20, 'kdytukd', 'hassdfa', '874958.jpg', '2024-09-30'),
(21, 'sample', 'wala lang di wow', 'wallpapersden.com_lo-fi-room-cool-anime_3584x2048.jpg', '2024-10-02'),
(22, 'egewgdfzs', 'grfdghzdfbhz', 'modelos-de-design-de-calendario-de-ano-novo-de-2024_202454-529.avif', '2024-10-02'),
(23, 'WALANG PASOK', 'KASI GUSTO KO LANG WALA', 'images.jpg', '2024-10-02');

-- --------------------------------------------------------

--
-- Table structure for table `holidays`
--

CREATE TABLE `holidays` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `date` date NOT NULL,
  `tag` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `holidays`
--

INSERT INTO `holidays` (`id`, `name`, `date`, `tag`) VALUES
(1, 'New Year\'s Day', '2024-01-01', 'Regular Holiday'),
(2, 'Maundy Thursday', '2024-04-06', 'Regular Holiday'),
(3, 'Good Friday', '2024-04-07', 'Regular Holiday'),
(4, 'Araw ng Kagitingan', '2024-04-09', 'Regular Holiday'),
(5, 'Labor Day', '2024-05-01', 'Regular Holiday'),
(6, 'Independence Day', '2024-06-12', 'Regular Holiday'),
(7, 'National Heroes Day', '2024-08-28', 'Regular Holiday'),
(8, 'Bonifacio Day', '2024-11-30', 'Regular Holiday'),
(9, 'Christmas Day', '2024-12-25', 'Regular Holiday'),
(10, 'Rizal Day', '2024-12-30', 'Regular Holiday'),
(11, 'Eid\'l Fitr', '2024-04-10', 'Special Non-Working Holiday'),
(12, 'Eid\'l Adha', '2024-06-28', 'Special Non-Working Holiday'),
(13, 'Chinese New Year', '2024-02-10', 'Special Non-Working Holiday'),
(14, 'EDSA People Power Revolution', '2024-02-25', 'Special Non-Working Holiday'),
(15, 'Black Saturday', '2024-04-08', 'Special Non-Working Holiday'),
(16, 'Ninoy Aquino Day', '2024-08-21', 'Special Non-Working Holiday'),
(17, 'All Saints\' Day', '2024-11-01', 'Special Non-Working Holiday'),
(18, 'All Souls\' Day', '2024-11-02', 'Special Non-Working Holiday'),
(19, 'Feast of the Immaculate Conception', '2024-12-08', 'Special Non-Working Holiday'),
(20, 'Christmas Eve', '2024-12-24', 'Special Non-Working Holiday'),
(21, 'New Year\'s Eve', '2024-12-31', 'Special Non-Working Holiday');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`) VALUES
(1, 'admin', '0192023a7bbd73250516f069df18b500');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `apps`
--
ALTER TABLE `apps`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `holidays`
--
ALTER TABLE `holidays`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `apps`
--
ALTER TABLE `apps`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `events`
--
ALTER TABLE `events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `holidays`
--
ALTER TABLE `holidays`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
