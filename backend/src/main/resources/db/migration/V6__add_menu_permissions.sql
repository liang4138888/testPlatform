INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_PROJECT', '菜单-项目管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_PROJECT');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_REQUIREMENT', '菜单-需求管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_REQUIREMENT');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_CASE', '菜单-用例管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_CASE');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_BUG', '菜单-缺陷管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_BUG');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_USER', '菜单-用户管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_USER');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_ROLE', '菜单-权限管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_ROLE');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'ROLE_MANAGE', '角色权限管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'ROLE_MANAGE');

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'DATA_ALL', '查看全部数据'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'DATA_ALL');

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r, system_permission p
WHERE r.role_code = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r JOIN system_permission p ON p.permission_code IN (
    'MENU_PROJECT', 'MENU_REQUIREMENT', 'MENU_CASE', 'MENU_BUG',
    'BUG_VIEW', 'BUG_CREATE', 'BUG_EDIT', 'BUG_ASSIGN', 'BUG_COMMENT', 'BUG_UPLOAD_IMAGE', 'BUG_STATUS_TEST'
)
WHERE r.role_code = 'TESTER'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r JOIN system_permission p ON p.permission_code IN (
    'MENU_PROJECT', 'MENU_REQUIREMENT', 'MENU_BUG',
    'BUG_VIEW', 'BUG_COMMENT', 'BUG_UPLOAD_IMAGE', 'BUG_STATUS_FIXED'
)
WHERE r.role_code = 'DEVELOPER'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r JOIN system_permission p ON p.permission_code IN (
    'MENU_PROJECT', 'MENU_REQUIREMENT', 'MENU_BUG',
    'BUG_VIEW', 'BUG_COMMENT', 'BUG_STATUS_FIXED'
)
WHERE r.role_code = 'PRODUCT'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);
