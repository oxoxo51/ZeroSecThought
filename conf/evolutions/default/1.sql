# Memo

# --- !Ups

CREATE TABLE MEMO(
    ID                      SERIAL NOT NULL PRIMARY KEY,
--    id                      bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    PARENT_ID               bigint,
    TITLE                   varchar,
    CONTENT                 varchar,
    CREATE_DATE             date
);

# --- !Downs

DROP TABLE memo;