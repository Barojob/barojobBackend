-- 1) users (Inheritance JOINED + DiscriminatorColumn)
CREATE TABLE users (
                       id               BIGINT         NOT NULL AUTO_INCREMENT,
                       DTYPE            VARCHAR(31)    NOT NULL,
                       email            VARCHAR(255),
                       nickname         VARCHAR(255),
                       password         VARCHAR(255),
                       created_at       DATETIME(6),
                       last_modified_at DATETIME(6),
                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) employers (User 상속)
CREATE TABLE employers (
                           user_id       BIGINT       NOT NULL,
                           business_name VARCHAR(255) NOT NULL,
                           name          VARCHAR(255) NOT NULL,
                           phone_number  VARCHAR(20),
                           PRIMARY KEY (user_id),
                           FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) workers (User 상속)
CREATE TABLE workers (
                         user_id        BIGINT       NOT NULL,
                         name           VARCHAR(10),
                         phone_number   VARCHAR(20)  NOT NULL UNIQUE,
                         priority_score DOUBLE       DEFAULT 50,
                         PRIMARY KEY (user_id),
                         FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) job_types
CREATE TABLE job_types (
                           job_type_id BIGINT      NOT NULL AUTO_INCREMENT,
                           name        VARCHAR(100) NOT NULL UNIQUE,
                           base_rate   DECIMAL(10,2) NOT NULL,
                           PRIMARY KEY (job_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) neighborhoods
CREATE TABLE neighborhoods (
                               neighborhood_id   BIGINT       NOT NULL AUTO_INCREMENT,
                               neighborhood_name VARCHAR(100) NOT NULL,
                               PRIMARY KEY (neighborhood_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) employer_requests
CREATE TABLE employer_requests (
                                   request_id                 BIGINT     NOT NULL AUTO_INCREMENT,
                                   request_date               DATE,
                                   employer_id                BIGINT     NOT NULL,
                                   location_neighborhood_id   BIGINT     NOT NULL,
                                   status                     ENUM(
                                'PENDING',
                                'PROCESSING',
                                'PARTIALLY_MATCHED',
                                'FULLY_MATCHED',
                                'CLOSED',
                                'CANCELLED'
                              ) NOT NULL DEFAULT 'PENDING',
                                   INDEX idx_er_date_status_loc (request_date, status, location_neighborhood_id),
                                   INDEX idx_er_employer_id     (employer_id),
                                   INDEX idx_er_location_neighborhood_id (location_neighborhood_id),
                                   INDEX idx_er_status          (status),
                                   PRIMARY KEY (request_id),
                                   FOREIGN KEY (employer_id)              REFERENCES employers(user_id),
                                   FOREIGN KEY (location_neighborhood_id) REFERENCES neighborhoods(neighborhood_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) employer_request_details
CREATE TABLE employer_request_details (
    -- UserStampedEntity 상속 컬럼
                                          created_by        BIGINT,
                                          created_at        DATETIME(6),
                                          last_modified_by  BIGINT,
                                          last_modified_at  DATETIME(6),

                                          request_detail_id BIGINT     NOT NULL AUTO_INCREMENT,
                                          request_id        BIGINT     NOT NULL,
                                          job_type_id       BIGINT     NOT NULL,
                                          required_count    INT        NOT NULL,
                                          matched_count     INT        NOT NULL DEFAULT 0,

                                          INDEX idx_erd_reqid_jobtype (request_id, job_type_id),
                                          INDEX idx_erd_job_type_id   (job_type_id),
                                          UNIQUE KEY uk_erd_request_job (request_id, job_type_id),
                                          PRIMARY KEY (request_detail_id),
                                          FOREIGN KEY (request_id)    REFERENCES employer_requests(request_id),
                                          FOREIGN KEY (job_type_id)   REFERENCES job_types(job_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE worker_requests (
                                 worker_request_id BIGINT     NOT NULL AUTO_INCREMENT,
                                 neighborhood_id   BIGINT     NOT NULL,
                                 worker_id         BIGINT     NOT NULL,
                                 request_date      DATE       NOT NULL,
                                 priority_score    DOUBLE     DEFAULT 50,
                                 status            ENUM(
                        'PENDING',
                        'PROCESSING',
                        'PARTIALLY_MATCHED',
                        'FULLY_MATCHED',
                        'CLOSED',
                        'CANCELLED'
                     ) NOT NULL DEFAULT 'PENDING',
                                 INDEX idx_wr_worker_id                 (worker_id),
                                 INDEX idx_wr_status_date_loc_job_score (status, request_date, priority_score),
                                 PRIMARY KEY (worker_request_id, neighborhood_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  PARTITION BY HASH (neighborhood_id)
  PARTITIONS 200;

-- 9) worker_request_job_types
CREATE TABLE worker_request_job_types (
    -- UserStampedEntity 상속 컬럼
                                          created_by                   BIGINT,
                                          created_at                   DATETIME(6),
                                          last_modified_by             BIGINT,
                                          last_modified_at             DATETIME(6),

                                          worker_request_job_type_id   BIGINT     NOT NULL AUTO_INCREMENT,
                                          worker_request_id            BIGINT     NOT NULL,
                                          neighborhood_id              BIGINT     NOT NULL,
                                          job_type_id                  BIGINT     NOT NULL,

                                          INDEX idx_wrjt_request_job   (worker_request_id, neighborhood_id, job_type_id),
                                          INDEX idx_wrjt_job_request   (job_type_id, worker_request_id, neighborhood_id),
                                          INDEX idx_wrjt_job_type_id   (job_type_id),
                                          UNIQUE KEY uk_wrjt_request_job (worker_request_id, neighborhood_id, job_type_id),
                                          PRIMARY KEY (worker_request_job_type_id)
    -- 모든 foreignKey=NO_CONSTRAINT 이므로 FKs 생략
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10) matches
CREATE TABLE matches (
    -- TimeStampedEntity 상속 컬럼
                         created_at        DATETIME(6),
                         last_modified_at  DATETIME(6),

                         match_id                    BIGINT     NOT NULL AUTO_INCREMENT,
                         employer_request_detail_id  BIGINT     NOT NULL,
                         worker_request_id           BIGINT     NOT NULL,
                         neighborhood_id             BIGINT     NOT NULL,
                         worker_id                   BIGINT     NOT NULL,
                         match_datetime              DATETIME(6)NOT NULL,

                         INDEX idx_matches_detail_worker  (employer_request_detail_id, worker_id),
                         INDEX idx_matches_worker_date    (worker_id, match_datetime),
                         INDEX idx_matches_date_time_erd  (match_datetime, employer_request_detail_id),
                         INDEX idx_matches_date_time_wr   (match_datetime, worker_request_id),
                         INDEX idx_matches_erd_id         (employer_request_detail_id),
                         INDEX idx_matches_wr_id          (worker_request_id),
                         INDEX idx_matches_worker_id      (worker_id),
                         UNIQUE KEY uk_match_worker_date  (worker_id, match_datetime),
                         PRIMARY KEY (match_id),

                         FOREIGN KEY (employer_request_detail_id)
                             REFERENCES employer_request_details(request_detail_id),
                         FOREIGN KEY (worker_id)
                             REFERENCES workers(user_id)
    -- worker_request 조인은 NO_CONSTRAINT 이므로 FK 생략
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;