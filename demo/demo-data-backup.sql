-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: localhost    Database: cputrade
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `cputrade`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cputrade` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `cputrade`;

--
-- Table structure for table `appeals`
--

DROP TABLE IF EXISTS `appeals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appeals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `message` varchar(2000) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkrsgddnmaqmfckj0tokx8xp7` (`user_id`),
  CONSTRAINT `FKkrsgddnmaqmfckj0tokx8xp7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appeals`
--

LOCK TABLES `appeals` WRITE;
/*!40000 ALTER TABLE `appeals` DISABLE KEYS */;
/*!40000 ALTER TABLE `appeals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bulletin_posts`
--

DROP TABLE IF EXISTS `bulletin_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bulletin_posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `body` text NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `resolved` bit(1) NOT NULL,
  `title` varchar(150) NOT NULL,
  `type` enum('FOUND','GENERAL','LOST') NOT NULL DEFAULT 'GENERAL',
  `author_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtj4vfiv8tfcenf7lxaych63hq` (`author_id`),
  CONSTRAINT `FKtj4vfiv8tfcenf7lxaych63hq` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bulletin_posts`
--

LOCK TABLES `bulletin_posts` WRITE;
/*!40000 ALTER TABLE `bulletin_posts` DISABLE KEYS */;
INSERT INTO `bulletin_posts` VALUES (1,'Lost near the District Six library on Monday. Please message me if found!','2026-10-08 19:00:00.000000',_binary '\0','Lost: blue student card holder','LOST',3),(2,'Three keys on a red lanyard. Handed in at the SRC office.','2026-10-07 19:00:00.000000',_binary '\0','Found: set of keys at Bellville cafeteria','FOUND',2),(3,'Meeting in the IT building every Thursday at 14:00. All welcome, bring past papers.','2026-10-06 19:00:00.000000',_binary '\0','Study group for Programming 1 exam','GENERAL',2),(4,'Bring your old textbooks to the Granger Bay student centre on Friday at 12:00 and swap them for the ones you need. No money needed.','2026-10-08 19:00:00.000000',_binary '\0','Second-hand textbook swap day','GENERAL',4),(5,'All printing 10% off during exam week. Show this post at the Bellville print shop.','2026-10-09 19:00:00.000000',_binary '\0','Exam-season printing special','GENERAL',6),(6,'Left behind after the Tuesday lecture. I have it, message me to collect.','2026-10-09 19:00:00.000000',_binary '\0','Found: black umbrella in the Mowbray lecture hall','FOUND',5);
/*!40000 ALTER TABLE `bulletin_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `body` text NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `edited` bit(1) NOT NULL,
  `edited_at` datetime(6) DEFAULT NULL,
  `location_suggestion` enum('IT_BUILDING','LIBRARY','MAIN_GATE','SRC_OFFICE') DEFAULT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `conversation_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc8ljv426x8fj9tcywei40stu9` (`conversation_id`),
  KEY `FKgiqeap8ays4lf684x7m0r2729` (`sender_id`),
  CONSTRAINT `FKc8ljv426x8fj9tcywei40stu9` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`),
  CONSTRAINT `FKgiqeap8ays4lf684x7m0r2729` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` VALUES (1,'Hi Lerato, are the earbuds still available?','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,1,5),(2,'Hi Sizwe! Yes they are. Battery still lasts the whole day.','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,1,3),(3,'Great. Can we meet at the District Six library at 13:00 tomorrow?','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,1,5),(4,'Perfect, see you at the library entrance.','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,1,3),(5,'Hi! Does the backpack fit a 15 inch laptop?','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,2,2),(6,'Yes, with room to spare. I can send more photos if you like.','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,2,3),(7,'Hi Thabo, I saw you are open to a swap. Would a Programming 1 textbook work?','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,3,4),(8,'Yes, that works for me. Which edition is it?','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,3,2),(9,'Latest edition, 2025. Almost no highlighting.','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,3,4),(10,'Do you have a slot on Thursday afternoon for Maths 1?','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,4,2),(11,'Yes, 15:00 to 16:00 in Mowbray works. I will bring past papers.','2026-10-09 19:00:33.000000',_binary '\0',NULL,_binary '\0',NULL,NULL,NULL,4,6);
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `buyer_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `seller_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK88ud06lbsd1b03y1i5cchoj9y` (`product_id`,`buyer_id`),
  KEY `FK11fycx5lvegp8mwplgsl8401h` (`buyer_id`),
  KEY `FKgcmb2bq98wunnce1y9pyj9sp6` (`seller_id`),
  CONSTRAINT `FK11fycx5lvegp8mwplgsl8401h` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKgcmb2bq98wunnce1y9pyj9sp6` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqy0ufa6yoqyqsqkdfll9hp24a` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversations`
--

LOCK TABLES `conversations` WRITE;
/*!40000 ALTER TABLE `conversations` DISABLE KEYS */;
INSERT INTO `conversations` VALUES (1,'2026-10-09 19:00:33.000000',5,6,3),(2,'2026-10-09 19:00:33.000000',2,4,3),(3,'2026-10-09 19:00:33.000000',4,1,2),(4,'2026-10-09 19:00:33.000000',2,14,6);
/*!40000 ALTER TABLE `conversations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `message` varchar(255) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `type` enum('CONFIRM_RECEIPT_REQUESTED','LISTING_REMOVED','NEW_MESSAGE','PRICE_DROP','RECEIPT_CONFIRMED','SAVED_SEARCH_MATCH','VENDOR_APPROVED','WISHLIST_ITEM_SOLD') NOT NULL,
  `recipient_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqqnsjxlwleyjbxlmm213jaj3f` (`recipient_id`),
  CONSTRAINT `FKqqnsjxlwleyjbxlmm213jaj3f` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'2026-10-09 19:00:33.000000','/chats/1','sizwe_d sent you a message about \"JBL wireless earbuds with charging case\"',_binary '\0','NEW_MESSAGE',3),(2,'2026-10-09 19:00:33.000000','/chats/1','lerato_m sent you a message about \"JBL wireless earbuds with charging case\"',_binary '\0','NEW_MESSAGE',5),(3,'2026-10-09 19:00:33.000000','/chats/1','sizwe_d sent you a message about \"JBL wireless earbuds with charging case\"',_binary '\0','NEW_MESSAGE',3),(4,'2026-10-09 19:00:33.000000','/chats/1','lerato_m sent you a message about \"JBL wireless earbuds with charging case\"',_binary '\0','NEW_MESSAGE',5),(5,'2026-10-09 19:00:33.000000','/products/6','lerato_m marked \"JBL wireless earbuds with charging case\" as sold to you — confirm you received it so you can rate them',_binary '\0','CONFIRM_RECEIPT_REQUESTED',5),(6,'2026-10-09 19:00:33.000000','/products/6','sizwe_d confirmed they received \"JBL wireless earbuds with charging case\"',_binary '\0','RECEIPT_CONFIRMED',3),(7,'2026-10-09 19:00:33.000000','/chats/2','thabo_s sent you a message about \"Red Kanken-style backpack\"',_binary '\0','NEW_MESSAGE',3),(8,'2026-10-09 19:00:33.000000','/chats/2','lerato_m sent you a message about \"Red Kanken-style backpack\"',_binary '\0','NEW_MESSAGE',2),(9,'2026-10-09 19:00:33.000000','/chats/3','aisha_n sent you a message about \"Scientific calculator (2nd-F, 10-digit)\"',_binary '\0','NEW_MESSAGE',2),(10,'2026-10-09 19:00:33.000000','/chats/3','thabo_s sent you a message about \"Scientific calculator (2nd-F, 10-digit)\"',_binary '\0','NEW_MESSAGE',4),(11,'2026-10-09 19:00:33.000000','/chats/3','aisha_n sent you a message about \"Scientific calculator (2nd-F, 10-digit)\"',_binary '\0','NEW_MESSAGE',2),(12,'2026-10-09 19:00:33.000000','/chats/4','thabo_s sent you a message about \"Maths & Physics tutoring (per hour)\"',_binary '\0','NEW_MESSAGE',6),(13,'2026-10-09 19:00:33.000000','/chats/4','campusprint sent you a message about \"Maths & Physics tutoring (per hour)\"',_binary '\0','NEW_MESSAGE',2);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `product_id` bigint(20) NOT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `sort_order` int(11) NOT NULL,
  PRIMARY KEY (`product_id`,`sort_order`),
  CONSTRAINT `FKqnq71xsohugpqwf3c9gxmsuy` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `CONSTRAINT_1` CHECK (`sort_order` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` VALUES (1,'http://localhost:8080/uploads/products/demo-calculator.jpg',0),(1,'http://localhost:8080/uploads/products/demo-calculator2.jpg',1),(2,'http://localhost:8080/uploads/products/demo-textbooks.jpg',0),(3,'http://localhost:8080/uploads/products/demo-sneakers.jpg',0),(4,'http://localhost:8080/uploads/products/demo-backpack.jpg',0),(5,'http://localhost:8080/uploads/products/demo-bookshelf.jpg',0),(6,'http://localhost:8080/uploads/products/demo-earbuds.jpg',0),(7,'http://localhost:8080/uploads/products/demo-microscope.jpg',0),(8,'http://localhost:8080/uploads/products/demo-chairs.jpg',0),(9,'http://localhost:8080/uploads/products/demo-guitar.jpg',0),(10,'http://localhost:8080/uploads/products/demo-kettle.jpg',0),(11,'http://localhost:8080/uploads/products/demo-kettle2.jpg',0),(12,'http://localhost:8080/uploads/products/demo-bicycle.jpg',0),(13,'http://localhost:8080/uploads/products/demo-documents.jpg',0),(14,'http://localhost:8080/uploads/products/demo-tutor.jpg',0),(15,'http://localhost:8080/uploads/products/demo-tablet.jpg',0);
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_active` bit(1) NOT NULL,
  `buyer_confirmed` bit(1) NOT NULL DEFAULT b'0',
  `buyer_confirmed_at` datetime(6) DEFAULT NULL,
  `campus` enum('BELLVILLE','DISTRICT_SIX','GRANGER_BAY','MOWBRAY','WELLINGTON') DEFAULT NULL,
  `category` enum('CLOTHING','ELECTRONICS','OTHER','SERVICES','TEXTBOOKS') NOT NULL,
  `item_condition` enum('FAIR','GOOD','LIKE_NEW','NEW','POOR') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text DEFAULT NULL,
  `open_to_swap` bit(1) NOT NULL DEFAULT b'0',
  `price` decimal(10,2) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `sold` bit(1) NOT NULL,
  `sold_at` datetime(6) DEFAULT NULL,
  `swap_preferences` varchar(300) DEFAULT NULL,
  `title` varchar(150) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `view_count` bigint(20) NOT NULL,
  `seller_id` bigint(20) NOT NULL,
  `sold_to_user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbgw3lyxhsml3kfqnfr45o0vbj` (`seller_id`),
  KEY `FKfbq2pefh0t8qnh5qhbl2va4jh` (`sold_to_user_id`),
  CONSTRAINT `FKbgw3lyxhsml3kfqnfr45o0vbj` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfbq2pefh0t8qnh5qhbl2va4jh` FOREIGN KEY (`sold_to_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,_binary '',_binary '\0',NULL,'BELLVILLE','ELECTRONICS','GOOD','2026-10-08 19:00:00.000000','Does everything Maths 1 and Physics 1 need: trig, logs, stats. Batteries just replaced. Basic Citizen calculator included as a spare.',_binary '',180.00,1,_binary '\0',NULL,'A Programming 1 textbook','Scientific calculator (2nd-F, 10-digit)','2026-10-08 19:00:00.000000',59,2,NULL),(2,_binary '',_binary '\0',NULL,'BELLVILLE','TEXTBOOKS','GOOD','2026-10-07 19:00:00.000000','Five classic philosophy and physics set books. Spines are worn but every page is intact. Sold as a bundle only.',_binary '\0',450.00,1,_binary '\0',NULL,NULL,'Philosophy & physics textbook bundle (5 books)','2026-10-09 19:03:42.000000',21,2,NULL),(3,_binary '',_binary '\0',NULL,'BELLVILLE','CLOTHING','GOOD','2026-10-06 19:00:00.000000','Comfortable everyday trainers, worn for one semester. Cleaned and ready to go.',_binary '\0',220.00,1,_binary '\0',NULL,NULL,'Blue sports sneakers, size 9','2026-10-06 19:00:00.000000',73,2,NULL),(4,_binary '',_binary '\0',NULL,'DISTRICT_SIX','CLOTHING','LIKE_NEW','2026-10-08 19:00:00.000000','Fits a 15\" laptop plus books. Hardly used, no marks or tears. Perfect for the daily campus commute.',_binary '\0',380.00,1,_binary '\0',NULL,NULL,'Red Kanken-style backpack','2026-10-08 19:00:00.000000',64,3,NULL),(5,_binary '',_binary '\0',NULL,'DISTRICT_SIX','TEXTBOOKS','FAIR','2026-10-05 19:00:00.000000','Clearing my shelf. Mixed first-year study books and novels, free to anyone who will use them. Collect at District Six library.',_binary '\0',0.00,1,_binary '\0',NULL,NULL,'Free: shelf of old study books','2026-10-05 19:00:00.000000',15,3,NULL),(6,_binary '',_binary '','2026-10-09 19:00:33.000000','DISTRICT_SIX','ELECTRONICS','LIKE_NEW','2026-09-27 19:00:00.000000','True wireless earbuds, battery lasts a full day of lectures. Comes with the original charging case.',_binary '\0',500.00,0,_binary '','2026-10-09 19:00:33.000000',NULL,'JBL wireless earbuds with charging case','2026-10-09 19:03:48.000000',42,3,5),(7,_binary '',_binary '\0',NULL,'GRANGER_BAY','OTHER','GOOD','2026-10-07 19:00:00.000000','Classic brass microscope, great for biology or as a desk piece. Lenses are clean and the focus works smoothly.',_binary '\0',1200.00,1,_binary '\0',NULL,NULL,'Brass student microscope','2026-10-07 19:00:00.000000',75,4,NULL),(8,_binary '',_binary '\0',NULL,'GRANGER_BAY','OTHER','GOOD','2026-10-04 19:00:00.000000','Two folding chairs with a matching bistro table, ideal for a res balcony. Sturdy and easy to carry.',_binary '\0',300.00,1,_binary '\0',NULL,NULL,'Blue folding chairs (set of 2)','2026-10-04 19:00:00.000000',7,4,NULL),(9,_binary '',_binary '\0',NULL,'MOWBRAY','OTHER','GOOD','2026-10-03 19:00:00.000000','Warm-sounding acoustic guitar, new strings. Great for beginners. Open to a swap for a keyboard or ukulele.',_binary '',750.00,1,_binary '\0',NULL,'A keyboard or a ukulele','Acoustic guitar','2026-10-03 19:00:00.000000',48,4,NULL),(10,_binary '',_binary '\0',NULL,'WELLINGTON','OTHER','LIKE_NEW','2026-10-08 19:00:00.000000','Fast-boil kettle, perfect for res rooms. Works perfectly, no limescale.',_binary '\0',90.00,1,_binary '\0',NULL,NULL,'White electric kettle (1.7L)','2026-10-08 19:00:00.000000',51,5,NULL),(11,_binary '',_binary '\0',NULL,'WELLINGTON','ELECTRONICS','GOOD','2026-10-02 19:00:00.000000','Glass kettle with automatic switch-off. Used for one year.',_binary '\0',150.00,1,_binary '\0',NULL,NULL,'Glass kettle with stand','2026-10-02 19:00:00.000000',28,5,NULL),(12,_binary '',_binary '\0',NULL,'MOWBRAY','OTHER','GOOD','2026-10-06 19:00:00.000000','Reliable commuter bike, gears and brakes recently serviced. Saves the taxi fare to campus.',_binary '\0',1500.00,1,_binary '\0',NULL,NULL,'City bicycle','2026-10-06 19:00:00.000000',61,5,NULL),(13,_binary '',_binary '\0',NULL,'BELLVILLE','SERVICES','NEW','2026-10-07 19:00:00.000000','Assignment printing and spiral binding, collected on campus. Price is per bound document. Same-day turnaround before 12:00.',_binary '\0',45.00,50,_binary '\0',NULL,NULL,'Printing & document binding','2026-10-09 19:27:43.000000',64,6,NULL),(14,_binary '',_binary '\0',NULL,'MOWBRAY','SERVICES','NEW','2026-10-05 19:00:00.000000','One-on-one tutoring for first-year Maths and Physics. Past-paper practice included. Book a slot by sending a message.',_binary '\0',120.00,20,_binary '\0',NULL,NULL,'Maths & Physics tutoring (per hour)','2026-10-05 19:00:00.000000',23,6,NULL),(15,_binary '',_binary '\0',NULL,'BELLVILLE','ELECTRONICS','LIKE_NEW','2026-10-04 19:00:00.000000','Graphics tablet, ideal for design and architecture students. Includes pen and USB cable.',_binary '\0',650.00,1,_binary '\0',NULL,NULL,'Drawing tablet with pen','2026-10-04 19:00:00.000000',17,6,NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `push_subscriptions`
--

DROP TABLE IF EXISTS `push_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `push_subscriptions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `auth_key` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `endpoint` varchar(500) NOT NULL,
  `p256dh_key` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6vabkyu7g789ehysoff7cfumf` (`endpoint`),
  KEY `FK1v577hpc7v9mdrm2uyk6kqgnl` (`user_id`),
  CONSTRAINT `FK1v577hpc7v9mdrm2uyk6kqgnl` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `push_subscriptions`
--

LOCK TABLES `push_subscriptions` WRITE;
/*!40000 ALTER TABLE `push_subscriptions` DISABLE KEYS */;
/*!40000 ALTER TABLE `push_subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ratings`
--

DROP TABLE IF EXISTS `ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ratings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `score` int(11) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `ratee_id` bigint(20) NOT NULL,
  `rater_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKedofhvl0l9o9b3twlwib6bve2` (`product_id`,`rater_id`),
  KEY `FKeb1u0ng8vp1os0vwrqf2dh0qh` (`ratee_id`),
  KEY `FKfcvojhiy6y8q4eyh0oxvwth6n` (`rater_id`),
  CONSTRAINT `FK228us4dg38ewge41gos8y761r` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKeb1u0ng8vp1os0vwrqf2dh0qh` FOREIGN KEY (`ratee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfcvojhiy6y8q4eyh0oxvwth6n` FOREIGN KEY (`rater_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ratings`
--

LOCK TABLES `ratings` WRITE;
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
INSERT INTO `ratings` VALUES (1,'Exactly as described and Lerato was easy to meet. Smooth deal!','2026-10-09 19:00:33.000000',5,6,3,5);
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `details` varchar(1000) DEFAULT NULL,
  `reason` enum('HARASSMENT','INAPPROPRIATE','OTHER','SCAM','SPAM') NOT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `status` enum('DISMISSED','PENDING','REVIEWED') NOT NULL,
  `reported_product_id` bigint(20) DEFAULT NULL,
  `reported_user_id` bigint(20) DEFAULT NULL,
  `reporter_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5ir8y2ti7b6s0x2us7rk0fi5c` (`reported_product_id`),
  KEY `FKb3bqi44mjskbnwupr31nfq5ui` (`reported_user_id`),
  KEY `FKd3qiw2om5d2oh5xb7fbdcq225` (`reporter_id`),
  CONSTRAINT `FK5ir8y2ti7b6s0x2us7rk0fi5c` FOREIGN KEY (`reported_product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKb3bqi44mjskbnwupr31nfq5ui` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKd3qiw2om5d2oh5xb7fbdcq225` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `saved_searches`
--

DROP TABLE IF EXISTS `saved_searches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saved_searches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` enum('CLOTHING','ELECTRONICS','OTHER','SERVICES','TEXTBOOKS') DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `keyword` varchar(150) DEFAULT NULL,
  `max_price` decimal(10,2) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKanhwgxvq6uwppnkiymaybnb8j` (`user_id`),
  CONSTRAINT `FKanhwgxvq6uwppnkiymaybnb8j` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `saved_searches`
--

LOCK TABLES `saved_searches` WRITE;
/*!40000 ALTER TABLE `saved_searches` DISABLE KEYS */;
/*!40000 ALTER TABLE `saved_searches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_blocks`
--

DROP TABLE IF EXISTS `user_blocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_blocks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `blocked_id` bigint(20) NOT NULL,
  `blocker_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6kwyqs53ciqfxmlhquyq5socr` (`blocker_id`,`blocked_id`),
  KEY `FK7k3mfgb03bnmwh81vqb1u5h80` (`blocked_id`),
  CONSTRAINT `FK7k3mfgb03bnmwh81vqb1u5h80` FOREIGN KEY (`blocked_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKgvu85oyjrfafwttb7iphgmm0v` FOREIGN KEY (`blocker_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_blocks`
--

LOCK TABLES `user_blocks` WRITE;
/*!40000 ALTER TABLE `user_blocks` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_blocks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_status` enum('ACTIVE','SUSPENDED') NOT NULL,
  `campus_handle` varchar(150) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `last_active_at` datetime(6) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ADMIN','STUDENT','VENDOR') NOT NULL,
  `suspended_until` datetime(6) DEFAULT NULL,
  `suspension_reason` varchar(500) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `vendor_approved` bit(1) NOT NULL,
  `verified` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhyi4wjgppmn0asmt53vv2jeoq` (`campus_handle`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'ACTIVE','admin@cputmarket.ac.za','2026-10-09 19:00:00.000000','admin@cputmarket.ac.za','2026-10-09 19:04:19.000000','$2b$10$l5OcihujLu8354gkqn9v4ehF/J5vH2VG/nR9PazmMc.pRsq1w/R76','ADMIN',NULL,NULL,'admin',_binary '',_binary ''),(2,'ACTIVE','thabo_s@cputmarket.ac.za','2026-10-09 19:00:00.000000','thabo_s@mycput.ac.za','2026-10-09 19:28:02.000000','$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW','STUDENT',NULL,NULL,'thabo_s',_binary '',_binary ''),(3,'ACTIVE','lerato_m@cputmarket.ac.za','2026-10-09 19:00:00.000000','lerato_m@mycput.ac.za','2026-10-09 19:00:33.000000','$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW','STUDENT',NULL,NULL,'lerato_m',_binary '',_binary ''),(4,'ACTIVE','aisha_n@cputmarket.ac.za','2026-10-09 19:00:00.000000','aisha_n@mycput.ac.za','2026-10-09 19:00:33.000000','$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW','STUDENT',NULL,NULL,'aisha_n',_binary '',_binary ''),(5,'ACTIVE','sizwe_d@cputmarket.ac.za','2026-10-09 19:00:00.000000','sizwe_d@mycput.ac.za','2026-10-09 19:00:32.000000','$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW','STUDENT',NULL,NULL,'sizwe_d',_binary '',_binary ''),(6,'ACTIVE','campusprint@cputmarket.ac.za','2026-10-09 19:00:00.000000','hello@campusprint.example','2026-10-09 19:27:43.000000','$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW','VENDOR',NULL,NULL,'campusprint',_binary '',_binary ''),(7,'ACTIVE','quickfix_it@cputmarket.ac.za','2026-10-09 19:00:00.000000','support@quickfix.example',NULL,'$2a$10$1JOOqnU.se6C.EeqL2mIfuBuICTZ4XPODrlZqWpCNuZV1G4rxYgzW','VENDOR',NULL,NULL,'quickfix_it',_binary '\0',_binary '');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist_items`
--

DROP TABLE IF EXISTS `wishlist_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtp53unkks741xiqi6m620i7mx` (`user_id`,`product_id`),
  KEY `FKqxj7lncd242b59fb78rqegyxj` (`product_id`),
  CONSTRAINT `FKmmj2k1i459yu449k3h1vx5abp` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqxj7lncd242b59fb78rqegyxj` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist_items`
--

LOCK TABLES `wishlist_items` WRITE;
/*!40000 ALTER TABLE `wishlist_items` DISABLE KEYS */;
INSERT INTO `wishlist_items` VALUES (1,'2026-10-09 19:00:33.000000',1,5),(2,'2026-10-09 19:00:33.000000',4,5),(3,'2026-10-09 19:00:33.000000',1,4),(4,'2026-10-09 19:00:33.000000',15,2),(5,'2026-10-09 19:00:33.000000',12,2);
/*!40000 ALTER TABLE `wishlist_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-09 20:19:54
