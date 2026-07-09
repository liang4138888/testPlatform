CREATE TABLE system_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NULL,
    org_code VARCHAR(64) NOT NULL,
    org_name VARCHAR(100) NOT NULL,
    leader_user_id BIGINT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_system_organization_code (org_code),
    KEY idx_system_organization_parent_id (parent_id),
    KEY idx_system_organization_leader_user_id (leader_user_id),
    KEY idx_system_organization_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE system_user ADD COLUMN organization_id BIGINT NULL AFTER avatar;
ALTER TABLE system_user ADD KEY idx_system_user_organization_id (organization_id);

INSERT INTO system_organization (id, parent_id, org_code, org_name, sort_order, status)
SELECT 1, NULL, 'DEFAULT', '默认组织', 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM system_organization WHERE org_code = 'DEFAULT');

UPDATE system_user SET organization_id = 1 WHERE organization_id IS NULL;

INSERT INTO system_permission (permission_code, permission_name)
SELECT 'MENU_ORGANIZATION', '菜单-组织架构管理'
WHERE NOT EXISTS (SELECT 1 FROM system_permission WHERE permission_code = 'MENU_ORGANIZATION');

INSERT INTO system_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM system_role r, system_permission p
WHERE r.role_code = 'ADMIN'
AND p.permission_code = 'MENU_ORGANIZATION'
AND NOT EXISTS (SELECT 1 FROM system_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);
