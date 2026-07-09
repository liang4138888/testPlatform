INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'PROJECT_CREATE', '新建项目', 'PROJECT', '项目管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'PROJECT_CREATE');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'REQUIREMENT_CREATE', '新建需求', 'REQUIREMENT', '需求管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'REQUIREMENT_CREATE');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'REQUIREMENT_ASSIGN', '需求人员分配', 'REQUIREMENT', '需求管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'REQUIREMENT_ASSIGN');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'REQUIREMENT_TRANSITION', '需求状态流转', 'REQUIREMENT', '需求管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'REQUIREMENT_TRANSITION');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'REQUIREMENT_TASK_MANAGE', '维护需求子任务', 'REQUIREMENT', '需求管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'REQUIREMENT_TASK_MANAGE');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'CASE_UPLOAD', '上传 XMind 用例', 'CASE', '用例管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'CASE_UPLOAD');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'CASE_EDIT', '编辑用例树', 'CASE', '用例管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'CASE_EDIT');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'CASE_EXPORT', '导出用例文件', 'CASE', '用例管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'CASE_EXPORT');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'CASE_DELETE', '删除用例集', 'CASE', '用例管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'CASE_DELETE');

INSERT INTO system_permission (permission_code, permission_name, page_code, page_name)
SELECT 'ORGANIZATION_MANAGE', '维护组织架构', 'ORGANIZATION', '组织架构管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'ORGANIZATION_MANAGE');

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r, system_permission p
WHERE r.role_code = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r JOIN system_permission p ON p.permission_code IN (
    'PROJECT_CREATE',
    'REQUIREMENT_CREATE', 'REQUIREMENT_ASSIGN', 'REQUIREMENT_TRANSITION', 'REQUIREMENT_TASK_MANAGE',
    'CASE_UPLOAD', 'CASE_EDIT', 'CASE_EXPORT', 'CASE_DELETE'
)
WHERE r.role_code = 'TESTER'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);
