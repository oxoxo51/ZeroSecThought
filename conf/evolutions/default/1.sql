# Memo

# --- !Ups

CREATE TABLE memo (
    id                      bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title                   varchar(255),
    content                 varchar,
    create_date             date
);

# --- !Downs

DROP TABLE memo;