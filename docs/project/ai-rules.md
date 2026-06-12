# AI Development Rules

## Workflow

- Read `docs/README.md`, the relevant module requirement document, `docs/project/architecture.md`, and the relevant module API/design document before implementing a task.
- Keep changes scoped to the requested task.
- Update documentation when public behavior, APIs, schemas, or setup steps change.
- Do not introduce authentication, RBAC, execution tracking, or AI-generated cases in phase 1 unless the scope changes.

## Backend Rules

- Use Spring Boot 2.7 and Java 8 in the current local environment. Upgrade to Spring Boot 3 only after switching the project JDK to 17.
- Keep backend packages under `common`, `framework`, `infrastructure`, `modules`, and `system`.
- Put business code under `modules/{module}/controller|service|mapper|entity|dto`.
- Keep controllers thin; put business logic in services.
- Store files in the configured local directory and metadata in MySQL.
- Parse only modern `.xmind` files with `content.json` in phase 1.
- Return clear validation errors for unsupported files or invalid templates.

## Backend Logging Rules

- Use Log4j2 for backend logging; do not add ad-hoc `System.out` or `printStackTrace` output.
- All `/api/**` requests must go through the common request logging filter so new interfaces automatically output logs.
- Request logs must include `traceId`, method, path, query/request parameters, request body, response status, response body, client IP, and request cost.
- Keep log fields in the `key=value` style separated by ` | ` so entries are easy to scan and grep.
- SQL execution logs must stay enabled through MyBatis Log4j2 logging so executed SQL and parameters are visible during backend debugging.
- Business exceptions should log as `warn`; unexpected system exceptions should log as `error` with stack trace.
- Do not log sensitive values in clear text. If future interfaces add passwords, tokens, cookies, or secrets, mask them before logging.

## Frontend Rules

- Use Vue 3 Composition API and TypeScript.
- Use Element Plus for common controls.
- Keep page state explicit and typed.
- Avoid hard-coded API response shapes outside API client modules once real APIs are added.

## Testing Rules

- Add focused tests around XMind parsing and export.
- Run backend tests after backend changes.
- Run frontend typecheck/build after frontend changes.
