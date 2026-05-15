# 测试平台

基于 AI 辅助开发模式搭建的测试管理平台基础框架。

## 目录结构

- `frontend/`：Vue 3 + Vite + TypeScript + Element Plus 前端应用骨架。
- `backend/`：Spring Boot 2.7 + Java 8 后端服务骨架。
- `docs/`：按项目、模块和需求迭代分类的文档目录。
- `deploy/`：MySQL 本地部署模板。

## 构建方式

整个项目统一使用 Maven 作为后端构建和依赖管理工具。后端模块以 `backend/pom.xml` 为入口，当前兼容本机 JDK 8 环境；后续如需升级 Spring Boot 3，需要先切换到 JDK 17。

## 本地启动

1. 启动 MySQL：

```bash
cd deploy
docker compose up -d
```

2. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

后端默认端口为 `8080`，健康检查接口为 `GET /api/health`。

3. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

前端默认端口为 `5173`，开发环境通过 Vite 将 `/api` 代理到 `http://localhost:8080`。

## 一期范围

- 管理项目和需求。
- 上传新版 `.xmind` 文件。
- 将 XMind 的 `content.json` 解析为结构化用例树。
- 在平台内使用树形编辑器查看和编辑用例。
- 元数据和解析后的用例数据存储到 MySQL。
- 原始文件和导出文件先存储到后端项目目录 `backend/storage/files`。
- 将编辑后的用例重新导出为 `.xmind` 文件。

一期暂不包含登录权限、RBAC、用例执行、缺陷关联和 AI 生成用例能力。

## 当前已实现

- 后端通用响应、异常处理、跨域配置。
- Flyway 数据库初始化脚本。
- 项目创建和项目列表接口。
- 需求创建和需求列表接口。
- 本地文件存储配置和基础服务。
- 前端路由、项目管理页、需求管理页、用例管理入口、文件导出入口。
