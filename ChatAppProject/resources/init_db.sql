
CREATE DATABASE IF NOT EXISTS `chat_app`;
USE `chat_app`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `messages`;
DROP TABLE IF EXISTS `participants`;
DROP TABLE IF EXISTS `conversations`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `attachments`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  `display_name` VARCHAR(255) NOT NULL ,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB;

CREATE TABLE `conversations` (
  `conversation_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) DEFAULT NULL,
  `type` ENUM('Single','Group') NOT NULL DEFAULT 'Single',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`conversation_id`)
) ENGINE=InnoDB;

CREATE TABLE `participants` (
  `user_id` INT NOT NULL,
  `conversation_id` INT NOT NULL,
  `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`conversation_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ,
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`conversation_id`) 
) ENGINE=InnoDB;

CREATE TABLE `messages` (
  `message_id` INT NOT NULL AUTO_INCREMENT,
  `conversation_id` INT NOT NULL,
  `sender_id` INT NOT NULL,
  `type` VARCHAR(45) NOT NULL DEFAULT 'text', -- text, file, voice, etc.
  `content_text` TEXT DEFAULT NULL,
  `file_path` VARCHAR(500) DEFAULT NULL,
  `duration` INT DEFAULT NULL, -- seconds (for voice/video)
  `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`message_id`),
  KEY `idx_messages_conversation` (`conversation_id`),
  KEY `idx_messages_sender` (`sender_id`),
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`conversation_id`) ,
  FOREIGN KEY (`sender_id`) REFERENCES `users`(`user_id`) 
) ENGINE=InnoDB;

-- Sample data
INSERT INTO `users` (`username`, `password`, `email`, `display_name`) VALUES
('minhthang', '123456', 'minhthang@example.com', 'Minh Thang'),
('thuytram', '123456', 'thuytram@example.com', 'Thuy Tram'),
('anhkhoa', '123456', 'anhkhoa@example.com', 'Anh Khoa'),
('ngocanh', '123456', 'ngocanh@example.com', 'Ngoc Anh'),
('quanghuy', '123456', 'quanghuy@example.com', 'Quang Huy'),
('linhchi', '123456', 'linhchi@example.com', 'Linh Chi'),
('thanhdat', '123456', 'thanhdat@example.com', 'Thanh Dat'),
('hoangyen', '123456', 'hoangyen@example.com', 'Hoang Yen'),
('minhtri', '123456', 'minhtri@example.com', 'Minh Tri');

INSERT INTO `conversations` (`name`, `type`) VALUES
('Private chat: Minh Thang - Thuy Tram', 'Single'),
('Java class group', 'Group'),
('Private chat: Anh Khoa - Ngoc Anh', 'Single'),
('Private chat: Quang Huy - Linh Chi', 'Single'),
('Private chat: Thanh Dat - Hoang Yen', 'Single'),
('Private chat: Minh Tri - Minh Thang', 'Single'),
('Database group', 'Group'),
('Project planning', 'Group'),
('UI design group', 'Group'),
('DevOps discussion', 'Group'),
('Team announcements', 'Group'),
('QA testing', 'Group');

INSERT INTO `participants` (`user_id`, `conversation_id`) VALUES
(1, 1),
(2, 1),
(1, 2),
(2, 2),
(3, 2),
(4, 2),
(3, 3),
(4, 3),
(5, 4),
(6, 4),
(7, 5),
(8, 5),
(9, 6),
(1, 6),
(2, 7),
(3, 7);

INSERT INTO `messages` (`conversation_id`, `sender_id`, `type`, `content_text`, `file_path`, `duration`) VALUES
(1, 1, 'text', 'Hi Tram, how was your study today?', NULL, NULL),
(1, 2, 'text', 'Pretty good, I am reviewing database lessons.', NULL, NULL),
(1, 1, 'text', 'I am working on the chat app project.', NULL, NULL),
(2, 3, 'text', 'Guys, who already finished the login part?', NULL, NULL),
(2, 4, 'text', 'I have finished the UI part.', NULL, NULL),
(2, 1, 'text', 'I am working on the MySQL connection.', NULL, NULL),
(2, 2, 'text', 'Remember to add the JAR file into lib.', NULL, NULL),
(2, 3, 'text', 'Right, missing the driver will cause an error immediately.', NULL, NULL),
(2, 4, 'text', 'Tomorrow I will test the message sending feature.', NULL, NULL),
(1, 2, 'text', 'Okay, I will send you a screenshot later.', NULL, NULL),
(3, 3, 'text', 'We should sync about the report.', NULL, NULL),
(3, 4, 'text', 'Sure, I can share my notes.', NULL, NULL),
(4, 5, 'text', 'Do you want to split the tasks?', NULL, NULL),
(4, 6, 'text', 'Yes, I will take the backend.', NULL, NULL),
(5, 7, 'text', 'Are you free for a quick call?', NULL, NULL),
(5, 8, 'text', 'Give me 10 minutes.', NULL, NULL),
(6, 9, 'text', 'Any updates for the database group?', NULL, NULL),
(6, 1, 'text', 'I will post the schema today.', NULL, NULL),
(7, 2, 'text', 'We should finalize the plan.', NULL, NULL),
(7, 3, 'text', 'Agree, let us meet at 3 PM.', NULL, NULL);

