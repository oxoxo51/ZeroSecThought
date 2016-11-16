# Memo

# --- !Ups

CREATE TABLE memo (
    id                      bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    parent_id               bigint,
    title                   varchar,
    content                 varchar,
    create_date             date
);

# --- !Downs

DROP TABLE memo;