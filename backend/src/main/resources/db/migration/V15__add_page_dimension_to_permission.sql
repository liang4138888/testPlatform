ALTER TABLE system_permission
    ADD COLUMN page_code VARCHAR(64) NOT NULL DEFAULT 'SYSTEM' AFTER permission_name,
    ADD COLUMN page_name VARCHAR(100) NOT NULL DEFAULT '系统通用' AFTER page_code,
    ADD KEY idx_system_permission_page_code (page_code);

UPDATE system_permission SET page_code = 'PROJECT', page_name = '项目管理' WHERE permission_code = 'MENU_PROJECT';
UPDATE system_permission SET page_code = 'REQUIREMENT', page_name = '需求管理' WHERE permission_code = 'MENU_REQUIREMENT';
UPDATE system_permission SET page_code = 'CASE', page_name = '用例管理' WHERE permission_code = 'MENU_CASE';
UPDATE system_permission SET page_code = 'BUG', page_name = '缺陷管理' WHERE permission_code = 'MENU_BUG' OR permission_code LIKE 'BUG\_%';
UPDATE system_permission SET page_code = 'ORGANIZATION', page_name = '组织架构管理' WHERE permission_code = 'MENU_ORGANIZATION';
UPDATE system_permission SET page_code = 'USER', page_name = '用户管理' WHERE permission_code = 'MENU_USER' OR permission_code LIKE 'USER\_%';
UPDATE system_permission SET page_code = 'ROLE', page_name = '权限管理' WHERE permission_code = 'MENU_ROLE' OR permission_code LIKE 'ROLE\_%';
UPDATE system_permission SET page_code = 'DATA', page_name = '数据权限' WHERE permission_code LIKE 'DATA\_%';
