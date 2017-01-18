# Memo

# --- !Ups

ALTER TABLE "memo" add column "fav" varchar(1) default '0';


# --- !Downs

ALTER TABLE "memo" drop column "fav";
