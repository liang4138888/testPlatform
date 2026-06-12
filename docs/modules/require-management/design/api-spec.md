# 需求管理重构接口与数据设计

## 1. 模块目录

### 后端

```text
backend/src/main/java/com/testplatform/modules/requirement/
├── controller
├── service
├── mapper
├── entity
└── dto
```

可在现有 `requirement` 模块内扩展，不新增独立业务包。

### 前端

```text
frontend/src/api/requirements.ts
frontend/src/views/RequirementView.vue
```

若页面复杂度继续提升，可拆分为：

```text
frontend/src/views/requirement/
├── RequirementBoardView.vue
├── RequirementDetailPanel.vue
├── RequirementTaskPanel.vue
└── RequirementTransitionPanel.vue
```

## 2. 数据库设计

### 2.1 requirement 表扩展

建议新增字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| owner_name | VARCHAR(100) | 提出人 |
| proposed_date | DATE | 提出日期 |
| proposed_iteration | VARCHAR(32) | 提出迭代 |
| release_iteration | VARCHAR(32) | 计划上线迭代 |
| priority | VARCHAR(16) | 优先级：P1/P2/P3 |
| status | VARCHAR(32) | 需求主状态 |

现有字段保留：

- `id`
- `project_id`
- `requirement_no`
- `name`
- `description`
- `created_by`
- `created_at`
- `updated_at`
- `deleted`

### 2.2 requirement_task

需求任务表。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| requirement_id | BIGINT | 需求 ID |
| task_type | VARCHAR(32) | DEV/TEST/ACCEPTANCE |
| role_type | VARCHAR(32) | BACKEND/IOS/ANDROID/MINI_PROGRAM/HARMONY/WEB/TEST/OTHER |
| name | VARCHAR(150) | 任务名称 |
| assignee_id | BIGINT | 负责人 |
| status | VARCHAR(32) | 任务状态 |
| remark | VARCHAR(500) | 备注 |
| sort_order | INT | 排序 |
| created_by | BIGINT | 创建人 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

### 2.3 requirement_history

需求历史表。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| requirement_id | BIGINT | 需求 ID |
| action_type | VARCHAR(64) | 操作类型 |
| field_name | VARCHAR(64) | 字段名 |
| old_value | VARCHAR(500) | 旧值 |
| new_value | VARCHAR(500) | 新值 |
| operator_id | BIGINT | 操作人 |
| remark | VARCHAR(500) | 备注 |
| created_at | DATETIME | 操作时间 |

## 3. 接口设计

Base path: `/api`

### 3.1 需求看板查询

```http
GET /api/requirements?projectId=1&status=DEVELOPING&keyword=促销
```

参数：

- `projectId`：项目 ID，可选。
- `status`：需求主状态，可选。
- `keyword`：关键词，可选。

返回：

```json
[
  {
    "id": 1,
    "projectId": 1,
    "requirementNo": "REQ-20260603-001",
    "name": "促销活动配置支持多端生效",
    "description": "支持配置后在多端同步展示",
    "ownerName": "产品王五",
    "proposedDate": "2026-06-03",
    "proposedIteration": "2026-W23",
    "releaseIteration": "2026-W25",
    "priority": "P2",
    "status": "DEVELOPING",
    "gatePass": false,
    "gateMessage": "还有 2 个开发任务未完成",
    "devTaskDoneCount": 1,
    "devTaskTotalCount": 3,
    "testTaskDoneCount": 0,
    "testTaskTotalCount": 0,
    "createdAt": "2026-06-03T10:00:00",
    "updatedAt": "2026-06-03T12:00:00"
  }
]
```

兼容接口：

```http
GET /api/projects/{projectId}/requirements
```

保留现有项目下需求列表接口，返回字段可扩展为同一响应结构。

### 3.2 创建需求

```http
POST /api/projects/{projectId}/requirements
```

请求：

```json
{
  "requirementNo": "REQ-20260603-001",
  "name": "促销活动配置支持多端生效",
  "description": "支持配置后在多端同步展示",
  "ownerName": "产品王五",
  "proposedDate": "2026-06-03",
  "proposedIteration": "2026-W23",
  "releaseIteration": "2026-W25",
  "priority": "P2"
}
```

规则：

- `requirementNo` 可由前端传入；如为空，后端自动生成。
- 初始状态固定为 `COLLECTING`。
- 创建成功写入 `requirement_history`。

### 3.3 需求详情

```http
GET /api/requirements/{requirementId}
```

返回：

- 需求基础信息。
- 任务列表。
- 历史记录。
- 门禁结果。
- 用例集数量。
- Bug 数量和未关闭 Bug 数量。

### 3.4 更新需求基础信息

```http
PUT /api/requirements/{requirementId}
```

请求：

```json
{
  "name": "促销活动配置支持多端生效",
  "description": "补充业务范围",
  "ownerName": "产品王五",
  "proposedDate": "2026-06-03",
  "proposedIteration": "2026-W23",
  "releaseIteration": "2026-W25",
  "priority": "P1"
}
```

规则：

- 关键字段变更写入历史。
- 不通过此接口修改主状态。

### 3.5 需求状态流转

```http
POST /api/requirements/{requirementId}/transition
```

请求：

```json
{
  "targetStatus": "TESTING",
  "remark": "开发任务已完成"
}
```

规则：

- 只允许流转到上一状态或下一状态。
- 进入 `TESTING` 前校验开发门禁。
- 进入 `UI_ACCEPTING` 前校验测试门禁。
- 状态变化写入历史。

### 3.6 任务列表

```http
GET /api/requirements/{requirementId}/tasks
```

返回：

```json
[
  {
    "id": 1,
    "requirementId": 1,
    "taskType": "DEV",
    "roleType": "BACKEND",
    "name": "服务端开发任务",
    "assigneeId": 3,
    "assigneeName": "开发李四",
    "status": "DEV_DOING",
    "remark": "",
    "sortOrder": 1
  }
]
```

### 3.7 新增任务

```http
POST /api/requirements/{requirementId}/tasks
```

请求：

```json
{
  "taskType": "DEV",
  "roleType": "BACKEND",
  "name": "服务端开发任务",
  "assigneeId": 3,
  "status": "DEV_TODO",
  "remark": ""
}
```

### 3.8 更新任务

```http
PUT /api/requirements/{requirementId}/tasks/{taskId}
```

请求：

```json
{
  "roleType": "BACKEND",
  "name": "服务端开发任务",
  "assigneeId": 3,
  "status": "DEV_DONE",
  "remark": "接口已联调完成",
  "sortOrder": 1
}
```

规则：

- 任务状态变化写入历史。
- 任务负责人可更新自己的任务状态。
- 任务基础信息编辑需要 `REQUIREMENT_TASK_EDIT`。

### 3.9 删除任务

```http
DELETE /api/requirements/{requirementId}/tasks/{taskId}
```

规则：

- 逻辑删除。
- 删除任务写入历史。

### 3.10 历史记录

```http
GET /api/requirements/{requirementId}/history
```

## 4. 权限点

建议初始化：

- `REQUIREMENT_VIEW`
- `REQUIREMENT_CREATE`
- `REQUIREMENT_EDIT`
- `REQUIREMENT_TRANSITION`
- `REQUIREMENT_TASK_EDIT`

保留：

- `MENU_REQUIREMENT`
- `DATA_ALL`

## 5. 实现约束

- 不引入工作流引擎。
- 使用现有 MyBatis Plus Mapper 风格。
- Controller 保持薄层，门禁和状态流转逻辑放在 `RequirementService`。
- 所有新增表使用 Flyway 新版本迁移脚本。
- 不修改已执行的历史迁移脚本。
- 前端请求统一通过 `frontend/src/api/requirements.ts`。
- 前端继续使用 Element Plus 和现有页面布局风格。
