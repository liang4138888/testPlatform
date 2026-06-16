ALTER TABLE requirement ADD COLUMN owner_name VARCHAR(100) NULL AFTER name;
ALTER TABLE requirement ADD COLUMN proposed_date VARCHAR(20) NULL AFTER owner_name;
ALTER TABLE requirement ADD COLUMN proposed_iteration VARCHAR(50) NULL AFTER proposed_date;
ALTER TABLE requirement ADD COLUMN release_iteration VARCHAR(50) NULL AFTER proposed_iteration;
ALTER TABLE requirement ADD COLUMN priority VARCHAR(10) NULL AFTER release_iteration;
