-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 05, 2025 at 12:14 PM
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
-- Database: `assignment`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `action_type` text NOT NULL,
  `query_text` text DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `audit_log`
--

INSERT INTO `audit_log` (`id`, `username`, `action_type`, `query_text`, `timestamp`) VALUES
(1, 'abood', 'Log in', 'SELECT salt,role, password FROM users WHERE username=?;', '2025-04-05 08:50:20'),
(2, 'abood', 'View all users', 'SELECT username, name, role FROM users', '2025-04-05 08:51:18');

-- --------------------------------------------------------

--
-- Table structure for table `invoice`
--

CREATE TABLE `invoice` (
  `id` int(11) NOT NULL,
  `licensePlate` varchar(10) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `paymentStatus` tinyint(1) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT 'Unknown',
  `handler` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `payment_history`
--

CREATE TABLE `payment_history` (
  `id` int(11) NOT NULL,
  `licensePlate` varchar(10) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `paymentStatus` tinyint(1) DEFAULT NULL,
  `archivedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `payment_method` varchar(50) NOT NULL DEFAULT 'Unknown',
  `handler` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment_history`
--

INSERT INTO `payment_history` (`id`, `licensePlate`, `amount`, `paymentStatus`, `archivedAt`, `payment_method`, `handler`) VALUES
(5, '564789', 100.00, 1, '2025-04-05 07:14:21', 'Unknown', 'mo'),
(6, '564789', 30.00, 1, '2025-04-05 07:15:43', 'Cash', 'mo'),
(7, 'CAN-5670', 100.00, 1, '2025-04-05 07:46:16', 'Cash', 'mo'),
(8, 'CAN-5670', 25.00, 1, '2025-04-05 07:46:23', 'Card', 'mo');

-- --------------------------------------------------------

--
-- Table structure for table `schedule`
--

CREATE TABLE `schedule` (
  `id` int(11) NOT NULL,
  `licensePlate` varchar(20) DEFAULT NULL,
  `scheduledDate` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `service_history`
--

CREATE TABLE `service_history` (
  `id` int(11) NOT NULL,
  `licensePlate` varchar(10) NOT NULL,
  `mechanicUsername` varchar(32) NOT NULL,
  `serviceDescription` text NOT NULL,
  `serviceDate` timestamp NOT NULL DEFAULT current_timestamp(),
  `partsUsed` varchar(255) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `service_history`
--

INSERT INTO `service_history` (`id`, `licensePlate`, `mechanicUsername`, `serviceDescription`, `serviceDate`, `partsUsed`, `amount`) VALUES
(1, '524862', 'mech', 'sa', '2025-04-05 05:59:49', NULL, 20.00),
(2, '524862', 'mech', 'sd6', '2025-04-05 05:59:49', 'Oil Filter (x1)', 30.00),
(3, 'IND-2345', 'mech', 'Oil change', '2025-04-05 06:51:27', 'Oil Filter (x1)', 30.00),
(4, 'IND-2345', 'mech', 'Brake Pads', '2025-04-05 06:51:27', 'Brake Pad (x4)', 100.00),
(5, '564789', 'mech', 'Brakes', '2025-04-05 07:13:54', 'Brake Pad (x4)', 100.00),
(6, '564789', 'mech', 'OIl filer', '2025-04-05 07:13:54', 'Oil Filter (x1)', 30.00),
(7, 'CAN-5670', 'mech', 'BRA', '2025-04-05 07:23:56', 'Brake Pad (x4)', 100.00),
(8, 'CAN-5670', 'mech', 'OIL', '2025-04-05 07:23:56', 'Oil Filter (x1)', 25.00);

-- --------------------------------------------------------

--
-- Table structure for table `spare_parts_inventory`
--

CREATE TABLE `spare_parts_inventory` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `quantityInStock` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `spare_parts_inventory`
--

INSERT INTO `spare_parts_inventory` (`id`, `name`, `quantityInStock`) VALUES
(1, 'Brake Pad', 88),
(2, 'Oil Filter', 146),
(3, 'Air Filter', 200),
(4, 'Spark Plug', 300),
(5, 'Timing Belt', 50),
(6, 'Battery', 75),
(7, 'Headlight Bulb', 250),
(8, 'Wiper Blade', 180),
(9, 'Brake Disc', 60),
(10, 'Suspension Spring', 40),
(11, 'Radiator', 30),
(12, 'Alternator', 25),
(13, 'Power Steering Pump', 40),
(14, 'Starter Motor', 45),
(15, 'Clutch Kit', 50),
(16, 'Wheel Rim', 60),
(17, 'Exhaust Pipe', 80),
(18, 'Fuel Pump', 55),
(19, 'Shock Absorber', 70),
(20, 'Transmission Fluid', 100);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `username` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `role` varchar(50) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `salt` varbinary(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`username`, `name`, `email`, `phone`, `role`, `password`, `salt`) VALUES
('abood', 'Abdulla', 'admin@gmail.com', '00521656', 'Supervisor', 'CE5BA09445D6045ED4AF01B60B09E539C6DF63311626F7741C8FDF00F7DEBFB7', 0xe2511a93174838d22898a7035e441c17),
('mech', 'mechanic', 'mech2gmail.com', '8518252', 'Mechanic', '463F6AA0928EAE67EFB1739A6326700AFCD20ABCD6F41AACC2D691DD114EE535', 0x78bac32700957724d06c6a68d860ea7d2d6cd46a),
('mo', 'Mohammed', 'mo@gmail.com', '596122wd', 'Clerk', 'C3270450EEAF0AFD5320EF20FFDBDA4CCB7A7BAE61E927869A743EC70AAD24C8', 0x07f3e8d9a277477d565fe10c800222c4c33fcbc8);

-- --------------------------------------------------------

--
-- Table structure for table `vehicle`
--

CREATE TABLE `vehicle` (
  `licensePlate` varchar(255) NOT NULL,
  `make` varchar(255) NOT NULL,
  `model` varchar(255) NOT NULL,
  `status` varchar(50) DEFAULT 'Awaiting Service',
  `customerID` varchar(11) NOT NULL,
  `customerPhone` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `vehicle`
--

INSERT INTO `vehicle` (`licensePlate`, `make`, `model`, `status`, `customerID`, `customerPhone`) VALUES
('EGY-4321', 'Chevrolet', 'Malibu', 'Awaiting Service', 'EG987654321', '20111223344'),
('KSA-1123', 'Nissan', 'Altima', 'Awaiting Service', 'SA456789012', '966543210987'),
('QTR-1234', 'Toyota', 'Corolla', 'Awaiting Service', 'QA123456789', '9745551234'),
('UAE-6789', 'Ford', 'Explorer', 'Awaiting Service', 'AE112233445', '97154567890'),
('UK-7890', 'Audi', 'A4', 'Awaiting Service', 'GB123456789', '447012345678'),
('USA-5678', 'Honda', 'Civic', 'Awaiting Service', 'US987654321', '1234567890');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `invoice`
--
ALTER TABLE `invoice`
  ADD PRIMARY KEY (`id`),
  ADD KEY `licensePlate` (`licensePlate`);

--
-- Indexes for table `payment_history`
--
ALTER TABLE `payment_history`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `schedule`
--
ALTER TABLE `schedule`
  ADD PRIMARY KEY (`id`),
  ADD KEY `licensePlate` (`licensePlate`);

--
-- Indexes for table `service_history`
--
ALTER TABLE `service_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `licensePlate` (`licensePlate`),
  ADD KEY `mechanicUsername` (`mechanicUsername`);

--
-- Indexes for table `spare_parts_inventory`
--
ALTER TABLE `spare_parts_inventory`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`username`);

--
-- Indexes for table `vehicle`
--
ALTER TABLE `vehicle`
  ADD PRIMARY KEY (`licensePlate`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `invoice`
--
ALTER TABLE `invoice`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `payment_history`
--
ALTER TABLE `payment_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `schedule`
--
ALTER TABLE `schedule`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `service_history`
--
ALTER TABLE `service_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `spare_parts_inventory`
--
ALTER TABLE `spare_parts_inventory`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `invoice`
--
ALTER TABLE `invoice`
  ADD CONSTRAINT `invoice_ibfk_1` FOREIGN KEY (`licensePlate`) REFERENCES `vehicle` (`licensePlate`) ON DELETE CASCADE;

--
-- Constraints for table `schedule`
--
ALTER TABLE `schedule`
  ADD CONSTRAINT `schedule_ibfk_1` FOREIGN KEY (`licensePlate`) REFERENCES `vehicle` (`licensePlate`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `service_history`
--
ALTER TABLE `service_history`
  ADD CONSTRAINT `service_history_ibfk_2` FOREIGN KEY (`mechanicUsername`) REFERENCES `users` (`username`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
