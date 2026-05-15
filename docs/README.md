# 文档目录说明

`docs` 按“项目级文档、模块级文档、需求迭代文档”分类，避免后期模块和需求变多后混在一起。

## 目录结构

```text
docs/
├── README.md
├── project/
│   ├── ai-rules.md
│   └── architecture.md
└── modules/
    └── case-management/
        ├── design/
        │   └── api-spec.md
        └── requirements/
            └── phase-1/
                ├── prd.html
                └── task-breakdown.md
```

## 分类规则

- `project/`：放全项目通用文档，例如整体架构、AI 开发规则、工程规范、部署约定。
- `modules/`：按业务模块分类，例如 `case-management`、`execution-management`、`defect-management`。
- `modules/{module}/requirements/`：放该模块下每次需求或迭代的文档。
- `modules/{module}/design/`：放该模块相对稳定的设计文档，例如接口、数据模型、页面说明。

## 新需求文档命名

后续每次新增需求，建议使用以下格式：

```text
docs/modules/{模块名}/requirements/{需求编号或迭代名}/
├── prd.md 或 prd.html
├── task-breakdown.md
└── acceptance.md
```

示例：

```text
docs/modules/case-management/requirements/phase-2/
docs/modules/execution-management/requirements/phase-1/
docs/modules/defect-management/requirements/REQ-20260515-defect-link/
```

## 当前一期文档

- 用例管理一期交互原型：`docs/modules/case-management/requirements/phase-1/prd.html`
- 用例管理一期任务拆分：`docs/modules/case-management/requirements/phase-1/task-breakdown.md`
- 用例管理接口草案：`docs/modules/case-management/design/api-spec.md`
- 项目架构说明：`docs/project/architecture.md`
- AI 开发规则：`docs/project/ai-rules.md`
