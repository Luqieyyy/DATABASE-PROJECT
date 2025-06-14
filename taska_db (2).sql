-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 14, 2025 at 05:38 AM
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
  `check_out_time` datetime DEFAULT NULL,
  `manual_checkout` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance_status`
--

INSERT INTO `attendance_status` (`id`, `child_id`, `date`, `is_present`, `reason`, `reason_letter`, `check_in_time`, `check_out_time`, `manual_checkout`) VALUES
(1171, 4, '2025-06-03', 0, NULL, NULL, NULL, NULL, 0),
(1173, 6, '2025-06-03', 1, 'other', 'C:\\Users\\luqma\\Downloads\\medical_leave.webp', '2025-06-03 14:26:00', NULL, 0),
(1174, 7, '2025-06-03', 1, 'sick', 'C:\\Users\\luqma\\Downloads\\parent_letter.webp', NULL, NULL, 0),
(1175, 8, '2025-06-03', 1, 'permission', 'C:\\Users\\luqma\\Downloads\\parent_letter.webp', '2025-06-03 14:27:41', NULL, 0),
(1176, 9, '2025-06-03', 0, NULL, NULL, NULL, NULL, 0),
(1177, 10, '2025-06-03', 1, 'unexcused', 'C:\\Users\\luqma\\Downloads\\clinic_slip.webp', '2025-06-03 14:27:36', NULL, 0),
(1204, 4, '2025-06-04', 0, NULL, NULL, NULL, NULL, 0),
(1206, 6, '2025-06-04', 0, NULL, NULL, NULL, NULL, 0),
(1207, 7, '2025-06-04', 0, NULL, NULL, NULL, NULL, 0),
(1208, 8, '2025-06-04', 0, NULL, NULL, NULL, NULL, 0),
(1209, 9, '2025-06-04', 0, NULL, NULL, NULL, NULL, 0),
(1210, 10, '2025-06-04', 0, NULL, NULL, NULL, NULL, 0),
(1277, 4, '2025-06-05', 1, 'permission', 'C:\\Users\\luqma\\Downloads\\doctor_appointment.webp', '2025-06-05 13:55:00', '2025-06-05 22:53:00', 1),
(1279, 6, '2025-06-05', 1, 'permission', 'C:\\Users\\luqma\\Downloads\\excuse_slip.webp', '2025-06-05 13:54:00', '2025-06-05 18:38:00', 0),
(1280, 7, '2025-06-05', 1, 'other', 'C:\\Users\\luqma\\Downloads\\funeral_notice.webp', '2025-06-05 13:58:00', '2025-06-05 18:38:00', 0),
(1281, 8, '2025-06-05', 1, 'other', 'C:\\Users\\luqma\\Downloads\\doctor_note.webp', '2025-06-05 13:46:00', '2025-06-05 18:38:00', 0),
(1282, 9, '2025-06-05', 1, 'sick', 'C:\\Users\\luqma\\Downloads\\doctor_note.webp', '2025-06-05 15:53:00', '2025-06-05 18:08:00', 0),
(1283, 10, '2025-06-05', 1, 'other', 'C:\\Users\\luqma\\Downloads\\emergency_notice.webp', '2025-06-05 13:55:00', '2025-06-05 18:08:00', 0),
(1286, 4, '2025-06-06', 1, 'unexcused', 'C:\\Users\\luqma\\Downloads\\doctor_appointment.webp', '2025-06-06 00:05:00', '2025-06-06 11:45:00', 1),
(1288, 6, '2025-06-06', 0, NULL, NULL, NULL, NULL, 0),
(1289, 7, '2025-06-06', 0, NULL, NULL, NULL, NULL, 0),
(1290, 8, '2025-06-06', 0, NULL, NULL, NULL, NULL, 0),
(1291, 9, '2025-06-06', 0, NULL, NULL, NULL, NULL, 0),
(1292, 10, '2025-06-06', 0, NULL, NULL, NULL, NULL, 0),
(1301, 4, '2025-06-10', 1, 'sick', 'C:\\Users\\luqma\\Downloads\\hospital_referral.webp', '2025-06-10 10:09:00', NULL, 0),
(1303, 6, '2025-06-10', 0, NULL, NULL, NULL, NULL, 0),
(1304, 7, '2025-06-10', 0, NULL, NULL, NULL, NULL, 0),
(1305, 8, '2025-06-10', 0, NULL, NULL, NULL, NULL, 0),
(1306, 9, '2025-06-10', 0, NULL, NULL, NULL, NULL, 0),
(1307, 10, '2025-06-10', 0, NULL, NULL, NULL, NULL, 0),
(1323, 4, '2025-06-12', 1, 'permission', 'C:\\Users\\luqma\\Downloads\\parent_letter.webp', '2025-06-12 21:28:00', '2025-06-12 21:28:00', 1),
(1325, 6, '2025-06-12', 1, 'other', 'C:\\Users\\luqma\\Downloads\\clinic_slip.webp', '2025-06-12 21:28:00', '2025-06-12 21:28:00', 1),
(1326, 7, '2025-06-12', 1, 'other', 'C:\\Users\\luqma\\Downloads\\medical_leave.webp', '2025-06-12 21:28:00', '2025-06-12 21:28:00', 1),
(1327, 8, '2025-06-12', 0, NULL, NULL, NULL, NULL, 0),
(1328, 9, '2025-06-12', 0, NULL, NULL, NULL, NULL, 0),
(1329, 10, '2025-06-12', 0, NULL, NULL, NULL, NULL, 0),
(1336, 4, '2031-07-01', 1, 'permission', 'C:\\Users\\luqma\\Downloads\\hospital_referral.webp', '2031-07-01 21:58:00', '2031-07-01 21:58:00', 1),
(1337, 6, '2031-07-01', 1, 'other', 'C:\\Users\\luqma\\Downloads\\funeral_notice.webp', '2031-07-01 22:05:00', '2031-07-01 22:05:00', 1),
(1338, 7, '2031-07-01', 0, NULL, NULL, NULL, NULL, 0),
(1339, 8, '2031-07-01', 0, NULL, NULL, NULL, NULL, 0),
(1340, 9, '2031-07-01', 0, NULL, NULL, NULL, NULL, 0),
(1341, 10, '2031-07-01', 0, NULL, NULL, NULL, NULL, 0),
(1361, 4, '2025-06-14', 0, NULL, NULL, NULL, NULL, 0),
(1362, 6, '2025-06-14', 0, NULL, NULL, NULL, NULL, 0),
(1363, 7, '2025-06-14', 0, NULL, NULL, NULL, NULL, 0),
(1364, 8, '2025-06-14', 0, NULL, NULL, NULL, NULL, 0),
(1365, 9, '2025-06-14', 0, NULL, NULL, NULL, NULL, 0),
(1366, 10, '2025-06-14', 0, NULL, NULL, NULL, NULL, 0);

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
(4, 'Luqieyy', '2004-11-02', 'Bahrin', '017-1111111', '37354542334630350D0A'),
(6, 'Dr', '1987-05-29', 'Test', '011-2569101', '34323434324130330D0A'),
(7, 'Mirza', '2003-02-07', 'Rosli', '011-65066294', '30314543323930330D0A'),
(8, 'Puteri', '2004-09-18', 'Rozaimi', '014-3021456', '38423437324230330D0A'),
(9, 'Hajar', '2004-04-07', 'Shamsulbachry', '011-35873736', '41334143323930330D0A'),
(10, 'Annisya', '2004-05-21', 'Yusuf', '012-2801120', '33394541323930330D0A');

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1367;

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
