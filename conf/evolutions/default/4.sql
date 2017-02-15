# MemoTemplate

# --- !Ups

CREATE TABLE "memoTemplate" (
    "id"                      SERIAL NOT NULL PRIMARY KEY,
    "parent_id"               integer,
    "title"                   varchar(30),
    "content"                 varchar(400)
);

# --- !Downs

DROP TABLE "memoTemplate";