# Memo

# --- !Ups

ALTER TABLE "memo" ALTER column "title" varchar(30) collate "ja_JP.utf8";


# --- !Downs

ALTER TABLE "memo" ALTER column "title" varchar(30);
