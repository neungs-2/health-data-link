CREATE TABLE user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE source (
recordkey CHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(100),
    product_name VARCHAR(100),
    product_vender VARCHAR(100),
    mode INT,
    type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (recordkey, user_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE steps_record (
    step_record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recordkey CHAR(36) NOT NULL,
    steps INT,
    distance DECIMAL(20, 10),
    calories DECIMAL(20, 10),
    period_from TIMESTAMP NOT NULL,
    period_to TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recordkey, user_id) REFERENCES source(recordkey, user_id),
    UNIQUE KEY uq_steps_record_user_record_period (user_id, recordkey, period_from, period_to)
);

CREATE TABLE daily_summary (
    user_id BIGINT NOT NULL,
    recordkey CHAR(36) NOT NULL,
    date DATE NOT NULL,
    steps INT,
    calories DECIMAL(20, 10),
    distance DECIMAL(20, 10),
    timezone CHAR(6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, date),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE monthly_summary (
    user_id BIGINT NOT NULL,
    recordkey CHAR(36) NOT NULL,
    date CHAR(7) NOT NULL,
    steps INT,
    calories DECIMAL(20, 10),
    distance DECIMAL(20, 10),
    timezone char(6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recordkey, date),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

INSERT INTO user (name, email) values ('tester', 'tester@email.com')