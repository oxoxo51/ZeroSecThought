# Memo

# --- !Ups

ALTER TABLE "memo" ALTER COLUMN "create_date" TYPE timestamp SET DEFAULT CURRENT_TIMESTAMP;

# --- !Downs

ALTER TABLE "memo" ALTER COLUMN "create_date" date;
