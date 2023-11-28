CREATE TABLE IF NOT EXISTS REPOSITORY
(
    ID          VARCHAR(40) PRIMARY KEY,
    URL         VARCHAR(255) NOT NULL,
    NAME        VARCHAR(64)  NOT NULL,
    DESCRIPTION VARCHAR(255),
    OWNER       VARCHAR(64)  NOT NULL,
    ACTIVE      BOOLEAN      NOT NULL,
    CREATED     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY  VARCHAR(64)  NOT NULL,
    UPDATED     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UPDATED_BY  VARCHAR(64)  NOT NULL,
    CONSTRAINT URL_UN UNIQUE (URL)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS HARVESTER_RUN
(
    ID             VARCHAR(40) PRIMARY KEY,
    CORRELATION_ID VARCHAR(128) NOT NULL,
    REPOSITORY_ID  VARCHAR(40)  NOT NULL,
    REPOSITORY_URL VARCHAR(255) NOT NULL,
    REVISION       VARCHAR(64),
    STARTED        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FINISHED TIMESTAMP,
    STATUS         VARCHAR(64)  NOT NULL,
    REASON         TEXT,
    CONSTRAINT HARVESTER_RUN_REPOSITORY_ID_fk
        FOREIGN KEY (REPOSITORY_ID) REFERENCES REPOSITORY (ID)
) ENGINE = InnoDB;
