# Memo

# --- !Ups

CREATE TABLE memo (
    id                      bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    parent_id               bigint,
    title                   varchar(100),
    content                 varchar(2000),
    create_date             date
);

# --- !Downs

DROP TABLE memo;