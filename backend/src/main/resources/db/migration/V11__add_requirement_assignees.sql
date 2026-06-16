ALTER TABLE requirement ADD COLUMN dev_assignee_ids VARCHAR(200) NULL AFTER involved_modules;
ALTER TABLE requirement ADD COLUMN test_assignee_ids VARCHAR(200) NULL AFTER dev_assignee_ids;
