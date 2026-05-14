
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
  `display_name` VARCHAR(255) NOT NULL UNIQUE,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
);

CREATE TABLE `conversations` (
  `conversation_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) DEFAULT NULL,
  `type` ENUM('Single','Group') NOT NULL DEFAULT 'Single',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`conversation_id`)
);

CREATE TABLE `participants` (
  `user_id` INT NOT NULL,
  `conversation_id` INT NOT NULL,
  `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`conversation_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE,
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`conversation_id`) ON DELETE CASCADE
);

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
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`conversation_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sender_id`) REFERENCES `users`(`user_id`) ON DELETE SET NULL
);
