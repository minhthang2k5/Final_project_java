
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
('ngocanh', '123456', 'ngocanh@example.com', 'Ngoc Anh');

INSERT INTO `conversations` (`name`, `type`) VALUES
('Private chat: Minh Thang - Thuy Tram', 'Single'),
('Java class group', 'Group');

INSERT INTO `participants` (`user_id`, `conversation_id`) VALUES
(1, 1),
(2, 1),
(1, 2),
(2, 2),
(3, 2),
(4, 2);

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
(1, 2, 'text', 'Okay, I will send you a screenshot later.', NULL, NULL);

