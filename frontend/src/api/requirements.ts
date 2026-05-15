import { request } from './http';

export interface Requirement {
  id: number;
  projectId: number;
  requirementNo: string;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RequirementCreatePayload {
  requirementNo: string;
  name: string;
  description?: string;
}

export function listRequirements(projectId: number) {
  return request<Requirement[]>(`/api/projects/${projectId}/requirements`);
}

export function createRequirement(projectId: number, payload: RequirementCreatePayload) {
  return request<Requirement>(`/api/projects/${projectId}/requirements`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
