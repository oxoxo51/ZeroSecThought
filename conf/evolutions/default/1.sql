# Memo

# --- !Ups

CREATE TABLE MEMO (
    ID                      SERIAL NOT NULL PRIMARY KEY,
    PARENT_ID               bigint,
    TITLE                   varchar,
    CONTENT                 varchar,
    CREATE_DATE             date
);

# --- !Downs

DROP TABLE MEMO;