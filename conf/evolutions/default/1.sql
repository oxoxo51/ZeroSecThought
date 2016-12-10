# Memo

# --- !Ups

CREATE TABLE "memo" (
    "id"                      SERIAL NOT NULL PRIMARY KEY,
    "parent_id"               integer,
    "title"                   varchar(30),
    "content"                 varchar(400),
    "create_date"             date
);

# --- !Downs

DROP TABLE "memo";