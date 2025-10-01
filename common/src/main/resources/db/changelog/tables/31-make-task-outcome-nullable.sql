ALTER TABLE tasks DROP COLUMN IF EXISTS outcome;
ALTER TABLE tasks
    ADD COLUMN outcome varchar(255);
