# API Draft

Base path: `/api`

## Projects

- `GET /projects`: list projects.
- `POST /projects`: create a project.

Create request:

```json
{
  "name": "移动端测试平台",
  "description": "移动端相关测试项目"
}
```

## Requirements

- `GET /projects/{projectId}/requirements`: list requirements under a project.
- `POST /projects/{projectId}/requirements`: create a requirement.

Create request:

```json
{
  "requirementNo": "REQ-001",
  "name": "登录流程改造",
  "description": "登录流程相关测试需求"
}
```

## Case Suites

- `POST /requirements/{requirementId}/case-suites/upload`: upload and parse `.xmind`.
- `GET /case-suites/{suiteId}`: get suite metadata and case tree.
- `PUT /case-suites/{suiteId}/nodes`: save edited case tree.
- `POST /case-suites/{suiteId}/export-xmind`: export current tree as `.xmind`.

## Files

- `GET /files/{fileId}/download`: download original or exported file.

## Common Response

```json
{
  "code": "0",
  "message": "ok",
  "data": {}
}
```
