# Architecture

## Overview

The platform is a front-end/back-end separated application.

- Frontend: Vue 3, Vite, TypeScript, Element Plus.
- Backend: Spring Boot 2.7, Java 8, MyBatis-Plus.
- Database: MySQL 8.x.
- File storage: local directory under `backend/storage/files`.

## Phase 1 Modules

- Project management: stores project metadata.
- Requirement management: stores requirements under projects.
- Case suite management: stores each uploaded XMind file and parsed case tree.
- XMind parser/exporter: reads modern `.xmind` `content.json` and exports edited data back to `.xmind`.
- File service: stores original and exported files in the local file directory.

## Data Flow

1. User selects a project and requirement.
2. User uploads a modern `.xmind` file.
3. Backend stores the original file under `backend/storage/files`.
4. Backend extracts `content.json`, validates the agreed template, and writes structured case nodes to MySQL.
5. Frontend displays the parsed tree and allows edits.
6. Backend persists edited case nodes.
7. Export creates a new `.xmind` file from the current case tree and stores it in the local file directory.
