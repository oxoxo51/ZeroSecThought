# Memo

# --- !Ups

ALTER TABLE "memo" ALTER COLUMN "create_date" timestamp default CURRENT_TIMESTAMP NOT NULL;

# --- !Downs

ALTER TABLE "memo" ALTER COLUMN "create_date" date;
