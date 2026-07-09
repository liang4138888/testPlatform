# Agent Workflow

This repository uses a delegated-agent workflow for non-trivial development work.

## Default Collaboration Mode

For feature work, bug fixes, refactors, permission changes, UI changes, backend API changes, or any task expected to touch multiple files:

1. The main agent acts as coordinator and final reviewer.
2. Spawn one coding sub-agent for implementation.
3. Spawn one testing/review sub-agent for verification, test gaps, and regression risks.
4. The main agent may make small blocking edits only when needed to unblock integration, but should not do all coding locally by default.
5. The main agent performs final review, resolves integration issues, runs final verification commands, and reports the result.

Simple questions, small one-file edits, or direct command requests do not require sub-agents.

## Coding Agent

The coding agent should:

- Own a clearly bounded file/module scope.
- Edit files directly in the workspace.
- Avoid reverting or overwriting changes from the user or other agents.
- Follow existing project patterns before introducing new abstractions.
- Return changed file paths and a concise implementation summary.

When possible, assign the coding agent implementation files only, not verification ownership.

## Testing Agent

The testing/review agent should:

- Review the intended change and changed files.
- Run or recommend focused checks.
- Look for missing backend permission checks, frontend visibility mismatches, data-scope leaks, type errors, migration risks, and stale UI text.
- Avoid broad unrelated refactors.
- Return findings first, then validation commands/results.

If the testing agent makes fixes, it must list exactly what it changed.

## Main Agent Responsibilities

The main agent must:

- Define the task split before spawning agents.
- Keep coding and testing scopes disjoint where possible.
- Continue useful local integration work while agents run.
- Review sub-agent output instead of blindly trusting it.
- Run final verification before completion:
  - `mvn -f pom.xml test`
  - `npm --prefix frontend run typecheck`
  - `git diff --check`
- Mention any checks that could not be run.

## Project-Specific Notes

- Frontend: Vue 3 + Vite + TypeScript + Element Plus.
- Backend: Spring Boot 2.7 + Java 8 + MyBatis Plus + Flyway.
- Permission work must include both frontend visibility and backend enforcement.
- Do not rely on hiding buttons alone for security.
- Keep the case editor's SVG connector approach; do not replace it with a different graph/canvas library unless explicitly requested.
- Do not use `git add .` in this repository; the worktree often contains unrelated generated docs, logs, and worktree state.
