# Memo

# --- !Ups

ALTER TABLE "memo" ALTER COLUMN "create_date" timestamp default now() NOT NULL;

# --- !Downs

ALTER TABLE "memo" ALTER COLUMN "create_date" date;
