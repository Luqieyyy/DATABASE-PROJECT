-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 05, 2025 at 07:48 AM
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
(1, 'luqman', '12345', 'luqman.jpg', 'Muhammad Luqman'),
(2, 'put', '123', 'puteri.jpg\r\n', 'puteri dayana');

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
  `reason_letter` varchar(100) DEFAULT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `check_out_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance_status`
--

INSERT INTO `attendance_status` (`id`, `child_id`, `date`, `is_present`, `reason`, `reason_letter`, `check_in_time`, `check_out_time`) VALUES
(1171, 4, '2025-06-03', 0, NULL, NULL, NULL, NULL),
(1172, 5, '2025-06-03', 1, NULL, NULL, '2025-06-03 14:26:00', NULL),
(1173, 6, '2025-06-03', 1, NULL, NULL, '2025-06-03 14:26:00', NULL),
(1174, 7, '2025-06-03', 1, NULL, NULL, NULL, NULL),
(1175, 8, '2025-06-03', 1, NULL, NULL, '2025-06-03 14:27:41', NULL),
(1176, 9, '2025-06-03', 0, NULL, NULL, NULL, NULL),
(1177, 10, '2025-06-03', 1, NULL, NULL, '2025-06-03 14:27:36', NULL),
(1178, 11, '2025-06-03', 0, NULL, NULL, NULL, NULL),
(1180, 4, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1181, 5, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1182, 6, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1183, 7, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1184, 8, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1185, 9, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1186, 10, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1187, 11, '2025-06-04', 0, NULL, NULL, NULL, NULL),
(1188, 4, '2025-06-05', 0, NULL, NULL, NULL, NULL),
(1189, 5, '2025-06-05', 0, NULL, NULL, NULL, NULL),
(1190, 6, '2025-06-05', 0, NULL, NULL, NULL, NULL),
(1191, 7, '2025-06-05', 0, NULL, NULL, NULL, NULL),
(1192, 8, '2025-06-05', 1, NULL, NULL, '2025-06-05 13:46:19', NULL),
(1193, 9, '2025-06-05', 0, NULL, NULL, NULL, NULL),
(1194, 10, '2025-06-05', 0, NULL, NULL, NULL, NULL),
(1195, 11, '2025-06-05', 0, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `children`
--

CREATE TABLE `children` (
  `child_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `birth_date` date DEFAULT NULL,
  `parent_name` varchar(100) DEFAULT NULL,
  `parent_contact` varchar(20) DEFAULT NULL,
  `nfc_uid` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `children`
--

INSERT INTO `children` (`child_id`, `name`, `birth_date`, `parent_name`, `parent_contact`, `nfc_uid`) VALUES
(4, 'Luqieyy', '2004-11-02', 'LuqmanBahrin', '017-1111111', '37354542334630350D0A'),
(5, 'Husky', '2004-11-03', 'Luq', '017-2834650', '42373234394130320D0A'),
(6, 'Dr', '1987-05-29', 'Test', '011-2569101', '34323434324130330D0A'),
(7, 'Mirza', '2003-02-07', 'Rosli', '011-65066924', '30314543323930330D0A'),
(8, 'Puteri', '2004-09-18', 'Rozaimi', '014-3021456', '38423437324230330D0A'),
(9, 'Hajar', '2004-04-07', 'Shamsulbachry', '011-35873736', '41334143323930330D0A'),
(10, 'Annisya', '2004-05-21', 'Yusuf', '012-2801120', '33394541323930330D0A'),
(11, 'hngf', '0001-01-01', 'bvfgc', '0123456789', '');

-- --------------------------------------------------------

--
-- Table structure for table `staff`
--

CREATE TABLE `staff` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `contact_number` varchar(20) DEFAULT NULL,
  `role` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `staff`
--

INSERT INTO `staff` (`id`, `name`, `contact_number`, `role`) VALUES
(1, 'Siti Nor Aisyah binti Ahmad', '011-2345 6789', 'Teacher'),
(2, 'Muhammad Faiz bin Hassan', '013-4567 8901', 'Admin'),
(4, 'Tan Mei Ling', '017-7890 1234', 'Teacher'),
(5, 'Azlan bin Zulkifli', '018-8901 2345', 'Staff'),
(6, 'Muhammad Luqman', '017-3743683', 'Admin');

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
-- Indexes for table `attendance_status`
--
ALTER TABLE `attendance_status`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_attendance` (`child_id`,`date`);

--
-- Indexes for table `children`
--
ALTER TABLE `children`
  ADD PRIMARY KEY (`child_id`),
  ADD UNIQUE KEY `nfc_uid` (`nfc_uid`);

--
-- Indexes for table `staff`
--
ALTER TABLE `staff`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `attendance_status`
--
ALTER TABLE `attendance_status`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1196;

--
-- AUTO_INCREMENT for table `staff`
--
ALTER TABLE `staff`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attendance_status`
--
ALTER TABLE `attendance_status`
  ADD CONSTRAINT `attendance_status_ibfk_1` FOREIGN KEY (`child_id`) REFERENCES `children` (`child_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
