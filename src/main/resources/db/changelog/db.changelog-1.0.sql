-- liquibase formatted sql

-- changeset shoggoth:1
CREATE TABLE user
(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    status VARCHAR(32) NOT NULL
);

-- changeset shoggoth:2
CREATE TABLE file
(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(256) NOT NULL,
    path VARCHAR(256) NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    UNIQUE(name,path)
);

-- changeset shoggoth:3
CREATE TABLE event
(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id INTEGER NOT NULL,
    file_id INTEGER NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    UNIQUE (user_id, file_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (file_id) REFERENCES file(id)
);
