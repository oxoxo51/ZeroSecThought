# Memo

# --- !Ups

ALTER TABLE "memo" ALTER COLUMN "create_date" timestamp NOT NULL default CURRENT_TIMESTAMP;

# --- !Downs

ALTER TABLE "memo" ALTER COLUMN "create_date" date;
