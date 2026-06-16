ALTER TABLE requirement ADD COLUMN prd VARCHAR(500) NULL AFTER description;
ALTER TABLE requirement ADD COLUMN prototype VARCHAR(500) NULL AFTER prd;
ALTER TABLE requirement ADD COLUMN participant_domains VARCHAR(200) NULL AFTER prototype;
