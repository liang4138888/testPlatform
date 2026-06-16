ALTER TABLE requirement ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'COLLECTING' AFTER description;
UPDATE requirement SET status = 'COLLECTING' WHERE status IS NULL OR status = '';
CREATE INDEX idx_requirement_status ON requirement (status);
