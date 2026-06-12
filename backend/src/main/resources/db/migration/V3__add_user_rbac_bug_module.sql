CREATE TABLE system_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_system_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_system_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(64) NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_user_role (user_id, role_id),
    KEY idx_system_user_role_user_id (user_id),
    KEY idx_system_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_role_permission (role_id, permission_id),
    KEY idx_system_role_permission_role_id (role_id),
    KEY idx_system_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL,
    expired_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_auth_token_token (token),
    KEY idx_auth_token_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bug (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    requirement_id BIGINT NOT NULL,
    case_suite_id BIGINT NULL,
    bug_no VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT '待修复',
    severity VARCHAR(32) NOT NULL DEFAULT '中',
    priority VARCHAR(32) NOT NULL DEFAULT '中',
    reporter_id BIGINT NOT NULL,
    assignee_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_bug_project_no (project_id, bug_no),
    KEY idx_bug_project_id (project_id),
    KEY idx_bug_requirement_id (requirement_id),
    KEY idx_bug_case_suite_id (case_suite_id),
    KEY idx_bug_status (status),
    KEY idx_bug_assignee_id (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bug_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bug_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    attachment_type VARCHAR(32) NOT NULL DEFAULT 'IMAGE',
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_bug_attachment_bug_id (bug_id),
    KEY idx_bug_attachment_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bug_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bug_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_bug_comment_bug_id (bug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bug_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bug_id BIGINT NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    field_name VARCHAR(64) NULL,
    old_value VARCHAR(500) NULL,
    new_value VARCHAR(500) NULL,
    operator_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_bug_history_bug_id (bug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receiver_id BIGINT NOT NULL,
    sender_id BIGINT NULL,
    biz_type VARCHAR(64) NOT NULL,
    biz_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(1000) NULL,
    read_status VARCHAR(32) NOT NULL DEFAULT 'UNREAD',
    external_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_notification_receiver_id (receiver_id),
    KEY idx_notification_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO system_user (id, username, password_hash, display_name, email) VALUES
(1, 'admin', '123456', '管理员', 'admin@testplatform.local'),
(2, 'tester', '123456', '测试张三', 'tester@testplatform.local'),
(3, 'dev01', '123456', '开发李四', 'dev01@testplatform.local'),
(4, 'product01', '123456', '产品王五', 'product01@testplatform.local');

INSERT INTO system_role (id, role_code, role_name) VALUES
(1, 'ADMIN', '管理员'),
(2, 'TESTER', '测试人员'),
(3, 'DEVELOPER', '开发人员'),
(4, 'PRODUCT', '产品人员');

INSERT INTO system_permission (id, permission_code, permission_name) VALUES
(1, 'BUG_VIEW', '查看 Bug'),
(2, 'BUG_CREATE', '创建 Bug'),
(3, 'BUG_EDIT', '编辑 Bug'),
(4, 'BUG_ASSIGN', '指派 Bug'),
(5, 'BUG_COMMENT', '评论 Bug'),
(6, 'BUG_UPLOAD_IMAGE', '上传 Bug 图片'),
(7, 'BUG_STATUS_TEST', '测试提交待修复或修复完成'),
(8, 'BUG_STATUS_FIXED', '提交已修复'),
(9, 'USER_MANAGE', '用户管理');

INSERT INTO system_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

INSERT INTO system_role_permission (role_id, permission_id)
SELECT 1, id FROM system_permission;

INSERT INTO system_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
(3, 1), (3, 5), (3, 6), (3, 8),
(4, 1), (4, 5), (4, 8);
