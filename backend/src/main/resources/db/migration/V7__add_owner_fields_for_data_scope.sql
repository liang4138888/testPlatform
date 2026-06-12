ALTER TABLE project ADD COLUMN created_by BIGINT NULL AFTER description;
ALTER TABLE requirement ADD COLUMN created_by BIGINT NULL AFTER description;
ALTER TABLE case_suite ADD COLUMN created_by BIGINT NULL AFTER status;

UPDATE project SET created_by = 1 WHERE created_by IS NULL;
UPDATE requirement SET created_by = 1 WHERE created_by IS NULL;
UPDATE case_suite SET created_by = 1 WHERE created_by IS NULL;

CREATE INDEX idx_project_created_by ON project (created_by);
CREATE INDEX idx_requirement_created_by ON requirement (created_by);
CREATE INDEX idx_case_suite_created_by ON case_suite (created_by);
