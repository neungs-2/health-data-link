CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR2(255),
    email VARCHAR2(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)

CREATE TABLE source (
    recordkey VARCHAR(255),
    user_id INT,
    name VARCHAR(255),
    product_name VARCHAR(255),
    product_vender VARCHAR(255),
    mode INT,
    type VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (recordkey, user_id),
    FOREIGN KEY (user_id) REFERENCES user(id)
)

CREATE TABLE steps_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    recordkey VARCHAR(255) NOT NULL,
    steps INT,
    distance DOUBLE,
    calories DOUBLE,
    period_from TIMESTAMP,
    period_to TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (recordkey, user_id) REFERENCES source(recordkey, user_id)
)

CREATE TABLE daily_summary (
    user_id BIGINT,
    recordkey VARCHAR(255),
    period DATE,
    steps INT,
    calories INT,
    distance INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, period),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (recordkey, user_id) REFERENCES source(recordkey, user_id)
)

CREATE TABLE monthly_summary (
    user_id BIGINT,
    recordkey VARCHAR(255),
    period CHAR(7),
    steps INT,
    calories INT,
    distance INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, period),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (recordkey, user_id) REFERENCES source(recordkey, user_id)
)