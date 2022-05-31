CREATE TABLE ACCOUNT
(
    ID         UUID         NOT NULL,
    FIRST_NAME VARCHAR(35)  NOT NULL,
    LAST_NAME  VARCHAR(35)  NOT NULL,
    EMAIL      VARCHAR(125) NOT NULL UNIQUE,
    PASSWORD   VARCHAR(256) NOT NULL,
    CREATED_AT TIMESTAMP    NOT NULL,
    UPDATED_AT TIMESTAMP,

    PRIMARY KEY (id)
);

ALTER TABLE ACCOUNT
    ADD CONSTRAINT UQ_EMAIL UNIQUE (EMAIL);
