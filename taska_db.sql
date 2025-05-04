-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 04, 2025 at 05:04 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `taska_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `profile_picture` varchar(100) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id`, `username`, `password`, `profile_picture`, `name`) VALUES
(1, 'luqman', '12345', 'luqman.jpg', 'Muhammad Luqman');

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `id` int(11) NOT NULL,
  `child_id` int(11) NOT NULL,
  `scan_time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`id`, `child_id`, `scan_time`) VALUES
(169, 9, '2025-05-04 21:23:17'),
(170, 7, '2025-05-04 21:23:25'),
(171, 4, '2025-05-04 21:24:40'),
(172, 5, '2025-05-04 21:42:59'),
(173, 10, '2025-05-04 21:43:23');

-- --------------------------------------------------------

--
-- Table structure for table `attendance_status`
--

CREATE TABLE `attendance_status` (
  `id` int(11) NOT NULL,
  `child_id` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `is_present` tinyint(1) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `scan_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance_status`
--

INSERT INTO `attendance_status` (`id`, `child_id`, `date`, `is_present`, `reason`, `scan_time`) VALUES
(413, 4, '2025-05-04', 1, '', '2025-05-04 21:24:40'),
(414, 5, '2025-05-04', 1, '', '2025-05-04 21:42:59'),
(415, 6, '2025-05-04', 0, 'Sick', NULL),
(416, 7, '2025-05-04', 1, '', '2025-05-04 21:23:25'),
(417, 8, '2025-05-04', 0, 'Unexcused', NULL),
(418, 9, '2025-05-04', 1, '', '2025-05-04 21:23:17'),
(419, 10, '2025-05-04', 1, '', '2025-05-04 21:43:23');

-- --------------------------------------------------------

--
-- Table structure for table `children`
--

CREATE TABLE `children` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `birth_date` date DEFAULT NULL,
  `parent_contact` varchar(20) DEFAULT NULL,
  `nfc_uid` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `children`
--

INSERT INTO `children` (`id`, `name`, `birth_date`, `parent_contact`, `nfc_uid`) VALUES
(4, 'Luqieyy', NULL, NULL, '37354542334630350D0A'),
(5, 'Husky', NULL, 'Luq', '42373234394130320D0A'),
(6, 'Dr', NULL, 'Test', '34323434324130330D0A'),
(7, 'Mirza', NULL, 'Rosli', '30314543323930330D0A'),
(8, 'Puteri', NULL, 'Rozaimi', '38423437324230330D0A'),
(9, 'Hajar', NULL, 'Shamsulbachry', '41334143323930330D0A'),
(10, 'Annisya', NULL, 'Yusuf', '33394541323930330D0A');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`id`),
  ADD KEY `child_id` (`child_id`);

--
-- Indexes for table `attendance_status`
--
ALTER TABLE `attendance_status`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_attendance` (`child_id`,`date`);

--
-- Indexes for table `children`
--
ALTER TABLE `children`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nfc_uid` (`nfc_uid`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=174;

--
-- AUTO_INCREMENT for table `attendance_status`
--
ALTER TABLE `attendance_status`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=421;

--
-- AUTO_INCREMENT for table `children`
--
ALTER TABLE `children`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `attendance_status`
--
ALTER TABLE `attendance_status`
  ADD CONSTRAINT `attendance_status_ibfk_1` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
