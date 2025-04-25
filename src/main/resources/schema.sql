-- 1) users
CREATE TABLE IF NOT EXISTS `users` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `email` VARCHAR(255),
                                       `nickname` VARCHAR(255),
                                       `password` VARCHAR(255),
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 2) job_types
CREATE TABLE IF NOT EXISTS `job_types` (
                                           `job_type_id` BIGINT NOT NULL AUTO_INCREMENT,
                                           `name` VARCHAR(100) NOT NULL,
                                           `base_rate` DECIMAL(10,2) NOT NULL,
                                           PRIMARY KEY (`job_type_id`),
                                           UNIQUE KEY `uk_job_types_name` (`name`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 3) neighborhoods
CREATE TABLE IF NOT EXISTS `neighborhoods` (
                                               `neighborhood_id` BIGINT NOT NULL AUTO_INCREMENT,
                                               `neighborhood_name` VARCHAR(100) NOT NULL,
                                               PRIMARY KEY (`neighborhood_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 4) employers
CREATE TABLE IF NOT EXISTS `employers` (
                                           `user_id` BIGINT NOT NULL,
                                           `business_name` VARCHAR(255) NOT NULL,
                                           `name` VARCHAR(255) NOT NULL,
                                           `phone_number` VARCHAR(20),
                                           PRIMARY KEY (`user_id`),
                                           CONSTRAINT `fk_employers_user`
                                               FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 5) workers
CREATE TABLE IF NOT EXISTS `workers` (
                                         `user_id` BIGINT NOT NULL,
                                         `name` VARCHAR(10),
                                         `phone_number` VARCHAR(20) NOT NULL,
                                         `priority_score` DOUBLE DEFAULT 50,
                                         PRIMARY KEY (`user_id`),
                                         UNIQUE KEY `uk_workers_phone` (`phone_number`),
                                         CONSTRAINT `fk_workers_user`
                                             FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 6) employer_requests
CREATE TABLE IF NOT EXISTS `employer_requests` (
                                                   `request_id` BIGINT NOT NULL AUTO_INCREMENT,
                                                   `request_date` DATE,
                                                   `employer_id` BIGINT NOT NULL,
                                                   `location_neighborhood_id` BIGINT NOT NULL,
                                                   `status` ENUM(
                                                       'PENDING',
                                                       'PROCESSING',
                                                       'PARTIALLY_MATCHED',
                                                       'FULLY_MATCHED',
                                                       'CLOSED',
                                                       'CANCELLED'
                                                       ) DEFAULT 'PENDING',
                                                   PRIMARY KEY (`request_id`),
                                                   KEY `idx_er_date_status_loc` (`request_date`,`status`,`location_neighborhood_id`),
                                                   KEY `idx_er_employer_id` (`employer_id`),
                                                   KEY `idx_er_location_neighborhood_id` (`location_neighborhood_id`),
                                                   KEY `idx_er_status` (`status`),
                                                   CONSTRAINT `fk_er_employer`
                                                       FOREIGN KEY (`employer_id`) REFERENCES `employers` (`user_id`),
                                                   CONSTRAINT `fk_er_neighborhood`
                                                       FOREIGN KEY (`location_neighborhood_id`) REFERENCES `neighborhoods` (`neighborhood_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 7) employer_request_details
CREATE TABLE IF NOT EXISTS `employer_request_details` (
                                                          `request_detail_id` BIGINT NOT NULL AUTO_INCREMENT,
                                                          `request_id` BIGINT NOT NULL,
                                                          `job_type_id` BIGINT NOT NULL,
                                                          `required_count` INT NOT NULL,
                                                          `matched_count` INT DEFAULT 0,
                                                          PRIMARY KEY (`request_detail_id`),
                                                          KEY `idx_erd_reqid_jobtype` (`request_id`,`job_type_id`),
                                                          KEY `idx_erd_job_type_id` (`job_type_id`),
                                                          UNIQUE KEY `uk_erd_request_job` (`request_id`,`job_type_id`),
                                                          CONSTRAINT `fk_erd_request`
                                                              FOREIGN KEY (`request_id`) REFERENCES `employer_requests` (`request_id`),
                                                          CONSTRAINT `fk_erd_jobtype`
                                                              FOREIGN KEY (`job_type_id`) REFERENCES `job_types` (`job_type_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 8) worker_requests (파티셔닝 200개, neighborhoods FK 제거)
CREATE TABLE IF NOT EXISTS `worker_requests` (
                                                 `worker_request_id` BIGINT NOT NULL AUTO_INCREMENT,
                                                 `neighborhood_id`   BIGINT NOT NULL,
                                                 `worker_id`         BIGINT NOT NULL,
                                                 `request_date`      DATE    NOT NULL,
                                                 `priority_score`    DOUBLE  DEFAULT 50,
                                                 `status` ENUM(
                                                     'PENDING',
                                                     'PROCESSING',
                                                     'PARTIALLY_MATCHED',
                                                     'FULLY_MATCHED',
                                                     'CLOSED',
                                                     'CANCELLED'
                                                     ) DEFAULT 'PENDING',
                                                 PRIMARY KEY (`worker_request_id`,`neighborhood_id`),
                                                 KEY `idx_wr_worker_id` (`worker_id`),
                                                 KEY `idx_wr_neighborhood_status_date_loc_job_score`
                                                     (`neighborhood_id`,`status`,`request_date`,`priority_score`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    PARTITION BY HASH (`neighborhood_id`)
        PARTITIONS 200;

-- 9) worker_request_job_types
CREATE TABLE IF NOT EXISTS `worker_request_job_types` (
                                                          `worker_request_job_type_id` BIGINT NOT NULL AUTO_INCREMENT,
                                                          `worker_request_id` BIGINT NOT NULL,
                                                          `neighborhood_id` BIGINT NOT NULL,
                                                          `job_type_id` BIGINT NOT NULL,
                                                          PRIMARY KEY (`worker_request_job_type_id`),
                                                          KEY `idx_wrjt_request_job`
                                                              (`worker_request_id`,`neighborhood_id`,`job_type_id`),
                                                          KEY `idx_wrjt_job_request`
                                                              (`job_type_id`,`worker_request_id`,`neighborhood_id`),
                                                          KEY `idx_wrjt_job_type_id` (`job_type_id`),
                                                          UNIQUE KEY `uk_wrjt_request_job`
                                                              (`worker_request_id`,`neighborhood_id`,`job_type_id`),
                                                          CONSTRAINT `fk_wrjt_jobtype`
                                                              FOREIGN KEY (`job_type_id`) REFERENCES `job_types` (`job_type_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- 10) matches
CREATE TABLE IF NOT EXISTS `matches` (
                                         `match_id` BIGINT NOT NULL AUTO_INCREMENT,
                                         `employer_request_detail_id` BIGINT NOT NULL,
                                         `worker_request_id` BIGINT NOT NULL,
                                         `neighborhood_id` BIGINT NOT NULL,
                                         `worker_id` BIGINT NOT NULL,
                                         `match_datetime` DATETIME(6) NOT NULL,
                                         PRIMARY KEY (`match_id`),
                                         KEY `idx_matches_detail_worker`
                                             (`employer_request_detail_id`,`worker_id`),
                                         KEY `idx_matches_worker_date`
                                             (`worker_id`,`match_datetime`),
                                         KEY `idx_matches_date_time_erd`
                                             (`match_datetime`,`employer_request_detail_id`),
                                         KEY `idx_matches_date_time_wr`
                                             (`match_datetime`,`worker_request_id`),
                                         KEY `idx_matches_erd_id` (`employer_request_detail_id`),
                                         KEY `idx_matches_wr_id` (`worker_request_id`),
                                         KEY `idx_matches_worker_id` (`worker_id`),
                                         UNIQUE KEY `uk_match_worker_date` (`worker_id`,`match_datetime`),
                                         CONSTRAINT `fk_matches_erd`
                                             FOREIGN KEY (`employer_request_detail_id`)
                                                 REFERENCES `employer_request_details` (`request_detail_id`),
                                         CONSTRAINT `fk_matches_worker`
                                             FOREIGN KEY (`worker_id`) REFERENCES `workers` (`user_id`)
    -- composite FK (worker_request_id, neighborhood_id) 는 애플리케이션 레벨에서 NO_CONSTRAINT 처리
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;