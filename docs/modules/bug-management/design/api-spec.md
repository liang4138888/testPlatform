# Bug 管理模块接口与实现方案

## 1. 模块目录

### 后端

```text
backend/src/main/java/com/testplatform/modules/auth/
backend/src/main/java/com/testplatform/modules/user/
backend/src/main/java/com/testplatform/modules/bug/
backend/src/main/java/com/testplatform/modules/notification/
```

### 前端

```text
frontend/src/api/auth.ts
frontend/src/api/users.ts
frontend/src/api/bugs.ts
frontend/src/api/notifications.ts
frontend/src/views/LoginView.vue
frontend/src/views/BugView.vue
```

## 2. 数据库表

### 用户/RBAC

- `system_user`
- `system_role`
- `system_permission`
- `system_user_role`
- `system_role_permission`
- `auth_token`

### Bug

- `bug`
- `bug_attachment`
- `bug_comment`
- `bug_history`
- `notification`

## 3. 认证接口

### 登录

```http
POST /api/auth/login
```

请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应：

```json
{
  "token": "token-value",
  "user": {
    "id": 1,
    "username": "admin",
    "displayName": "管理员",
    "roles": ["ADMIN"],
    "permissions": ["BUG_VIEW", "BUG_CREATE"]
  }
}
```

### 当前用户

```http
GET /api/auth/me
```

### 退出登录

```http
POST /api/auth/logout
```

## 4. 用户接口

### 可指派用户

```http
GET /api/users/assignable
```

返回：

```json
[
  {
    "id": 1,
    "username": "dev01",
    "displayName": "开发01"
  }
]
```

## 5. Bug 接口

### 查询列表

```http
GET /api/bugs?projectId=1&requirementId=1&status=OPEN&severity=HIGH&assigneeId=2&keyword=login
```

返回字段：

- `id`
- `bugNo`
- `title`
- `projectId`
- `projectName`
- `requirementId`
- `requirementNo`
- `requirementName`
- `status`
- `severity`
- `priority`
- `reporterName`
- `assigneeName`
- `attachmentCount`
- `updatedAt`

### 创建 Bug

```http
POST /api/requirements/{requirementId}/bugs
```

请求：

```json
{
  "title": "登录按钮点击无响应",
  "description": "输入账号密码后点击登录无响应",
  "caseSuiteId": 10,
  "severity": "HIGH",
  "priority": "HIGH",
  "assigneeId": 2
}
```

规则：

- `requirementId` 必填。
- `bugNo` 后端自动生成。
- `reporterId` 使用当前登录用户。

### Bug 详情

```http
GET /api/bugs/{bugId}
```

返回：

- Bug 基本信息。
- 图片附件列表。
- 评论列表。
- 操作历史列表。

### 更新 Bug

```http
PUT /api/bugs/{bugId}
```

请求：

```json
{
  "title": "登录按钮点击无响应",
  "description": "补充复现步骤",
  "status": "IN_PROGRESS",
  "severity": "HIGH",
  "priority": "HIGH",
  "assigneeId": 2,
  "caseSuiteId": 10
}
```

规则：

- 关键字段变化写入 `bug_history`。
- 状态变更触发通知。
- 指派人变更触发通知。

### 上传图片

```http
POST /api/bugs/{bugId}/images
Content-Type: multipart/form-data
```

字段：

- `file`

规则：

- 支持 png、jpg、jpeg、gif、webp。
- 单张限制 10MB。
- 可多次上传多张图片。
- 上传成功写入 `bug_attachment`。
- 上传成功写入操作历史。

### 查询附件

```http
GET /api/bugs/{bugId}/attachments
```

### 新增评论

```http
POST /api/bugs/{bugId}/comments
```

请求：

```json
{
  "content": "已复现，正在排查。"
}
```

规则：

- 只支持纯文本。
- 新增评论写入操作历史。
- 通知报告人和指派人，排除评论者本人。

### 查询评论

```http
GET /api/bugs/{bugId}/comments
```

### 查询历史

```http
GET /api/bugs/{bugId}/history
```

## 6. 通知接口

### 查询通知

```http
GET /api/notifications
```

### 未读数量

```http
GET /api/notifications/unread-count
```

### 标记单条已读

```http
PUT /api/notifications/{notificationId}/read
```

### 全部已读

```http
PUT /api/notifications/read-all
```

## 7. 文件预览接口

### 图片预览

```http
GET /api/files/{fileId}/preview
```

实现：

- 复用文件读取逻辑。
- `Content-Disposition` 使用 `inline`。
- 用于前端图片缩略图和预览。

## 8. 前端页面

### 登录页

路径：

```text
/login
```

能力：

- 用户名密码登录。
- 登录成功保存 Token。
- 登录成功跳转业务首页。

### 缺陷管理页

路径：

```text
/bugs
```

能力：

- Bug 筛选。
- Bug 列表。
- 新建 Bug。
- 查看和编辑 Bug。
- 上传图片。
- 查看图片。
- 评论流。
- 操作历史。

### Header 通知

能力：

- 显示未读通知数。
- 查看通知列表。
- 标记已读。
- 点击通知打开对应 Bug。

## 9. 权限点

- `BUG_VIEW`
- `BUG_CREATE`
- `BUG_EDIT`
- `BUG_ASSIGN`
- `BUG_COMMENT`
- `BUG_UPLOAD_IMAGE`
- `BUG_CLOSE`
- `USER_MANAGE`

## 10. 复用现有能力

- `ApiResponse`
- `BusinessException`
- `LocalStorageService`
- `FileObjectService`
- `FileController.download`
- `ProjectService`
- `RequirementService`
- `CaseSuiteService`
- `frontend/src/api/http.ts`
- `frontend/src/api/files.ts`
- `RequirementView.vue` 的表格和弹窗模式
- `CaseSuiteListView.vue` 的筛选列表模式
