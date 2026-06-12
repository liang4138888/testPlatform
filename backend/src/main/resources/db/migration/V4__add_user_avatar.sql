ALTER TABLE system_user ADD COLUMN avatar VARCHAR(255) NULL AFTER email;

UPDATE system_user SET avatar = '管' WHERE username = 'admin';
UPDATE system_user SET avatar = '测' WHERE username = 'tester';
UPDATE system_user SET avatar = '开' WHERE username = 'dev01';
UPDATE system_user SET avatar = '产' WHERE username = 'product01';
