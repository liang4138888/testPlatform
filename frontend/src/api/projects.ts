import { request } from './http';

export interface Project {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectCreatePayload {
  name: string;
  description?: string;
}

export function listProjects() {
  return request<Project[]>('/api/projects');
}

export function createProject(payload: ProjectCreatePayload) {
  return request<Project>('/api/projects', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
