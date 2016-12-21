# Memo

# --- !Ups

ALTER TABLE "memo" ALTER column "title" type varchar(30) collate "ja_JP.utf8";


# --- !Downs

ALTER TABLE "memo" ALTER column "title" type varchar(30);
