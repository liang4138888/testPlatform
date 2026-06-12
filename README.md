# 测试平台

基于 AI 辅助开发模式搭建的测试管理平台基础框架，当前聚焦项目、需求、XMind 用例导入、脑图编辑、用例集维护和 XMind 导出。

## 目录结构

- `frontend/`：Vue 3 + Vite + TypeScript + Element Plus 前端应用。
- `backend/`：Spring Boot 2.7 + Java 8 后端服务，按企业级分层目录组织。
- `docs/`：按项目、模块和需求迭代分类的文档目录。
- `deploy/`：MySQL 本地部署模板。

## 开发约束

- 需求实现必须严格按照对应模块原型执行；已有 HTML 原型时，页面布局、交互入口、字段展示、弹窗结构和关键视觉效果都应以原型为准，不得只做功能骨架。
- 如果发现原型与口头需求或现有实现冲突，必须先说明差异并按用户确认后的方向实现。

## 后端目录结构

```text
backend/src/main/java/com/testplatform/
├── TestPlatformApplication.java
├── common/                 # 通用响应、异常、基础工具
├── framework/              # Web、跨域、框架级配置
├── infrastructure/         # 文件存储、XMind 解析导出等基础设施适配
├── modules/                # 业务模块
│   ├── casesuite/          # 用例集、用例节点、XMind 上传导出
│   ├── file/               # 文件下载与文件元数据
│   ├── project/            # 项目管理
│   └── requirement/        # 需求管理
└── system/                 # 健康检查、系统级接口
```

业务模块内部统一按 `controller / service / mapper / entity / dto` 分层。

## 技术栈

- 前端：Vue 3、Vite、TypeScript、Element Plus。
- 后端：Spring Boot 2.7、Java 8、MyBatis Plus、Flyway。
- 数据库：MySQL。
- 文件：本地文件存储，默认位于 `backend/storage/files`。
- XMind：支持新版 `.xmind` 文件中的 `content.json` 解析与导出。

## 本地启动

1. 启动 MySQL：

```bash
cd deploy
docker compose up -d
```

2. 启动后端：

```bash
mvn -pl backend org.springframework.boot:spring-boot-maven-plugin:2.7.18:run
```

后端默认端口为 `8080`，健康检查接口为 `GET /api/health`。

3. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

前端默认端口为 `5173`，开发环境通过 Vite 将 `/api` 代理到 `http://localhost:8080`。后端 CORS 已支持本地 `localhost` / `127.0.0.1` 多端口开发访问。

## 常用校验命令

```bash
# 后端测试
mvn -f pom.xml test

# 前端类型检查
npm --prefix frontend run typecheck
```

## 功能范围

### 项目管理

- 创建项目。
- 查看项目列表。
- 需求和用例集按项目归属管理。

### 需求管理

- 按项目维护需求。
- 创建需求。
- 在需求下上传 `.xmind` 文件并解析生成用例集。
- 查看需求下的用例集。
- 直接在需求管理中导出并下载用例集 XMind 文件。

### 用例管理

- 查看全部用例集。
- 按项目、需求筛选用例集。
- 进入用例集编辑页面。
- 删除用例集，删除时会同步删除该用例集下的用例节点。

### 用例编辑

- 以脑图形式展示用例树。
- 使用 SVG 连线展示节点关系。
- 支持展开、收起、缩放。
- 支持新增根节点、新增子节点、删除节点。
- 节点详情使用弹窗编辑。
- 节点支持类型维护：模块、用例、步骤、预期。
- 节点支持执行状态维护：待开始、通过、失败。
- 可直接在脑图节点上修改执行状态。
- 节点边框颜色按节点类型区分，执行状态在状态选择器中使用颜色区分。
- 用例节点操作会调用保存用例接口进行持久化。
- 保存用例树后会生成平台导出的 `.xmind` 文件。

### XMind 导入导出

- 支持上传新版 `.xmind` 文件。
- 解析 `.xmind` 中的 `content.json`。
- 导入时默认节点类型为“用例”。
- 导入节点默认执行状态为“待开始”。
- 支持将平台编辑后的用例树重新导出为 `.xmind` 文件。
- 原始文件和导出文件存储在后端本地文件目录。

## 当前已实现

- 后端通用响应、异常处理、跨域配置。
- Flyway 数据库初始化与增量脚本。
- 项目、需求管理接口。
- 用例集上传、查询、保存、删除接口。
- 用例节点执行状态字段和持久化。
- XMind 解析、导出、文件下载能力。
- 前端项目管理、需求管理、用例管理、脑图编辑完整流程。

## 暂不包含

- 登录权限。
- RBAC。
- 缺陷关联。
- AI 生成用例能力。
- 完整用例执行流转，仅支持用例节点执行状态统计与维护。
