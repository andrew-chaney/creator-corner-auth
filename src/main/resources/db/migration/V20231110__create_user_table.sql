CREATE TABLE users
(
    user_id          UUID UNIQUE         NOT NULL,
    first_name       VARCHAR(255)        NOT NULL,
    last_name        VARCHAR(255)        NOT NULL,
    email            VARCHAR(255) UNIQUE NOT NULL,
    hashed_password  VARCHAR(255)        NOT NULL,
    created_ts_epoch TIMESTAMP           NOT NULL,
    updated_ts_epoch TIMESTAMP           NOT NULL,
    PRIMARY KEY (user_id)
);
