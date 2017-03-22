# Memo

# --- !Ups

ALTER TABLE "memo" ALTER COLUMN "create_date" TYPE timestamp;
ALTER TABLE "memo" ALTER COLUMN "create_date" SET DEFAULT CURRENT_TIMESTAMP;

# --- !Downs

ALTER TABLE "memo" ALTER COLUMN "create_date" TYPE date;
