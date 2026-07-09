import { request } from './http';

export interface OrganizationNode {
  id: number;
  parentId?: number;
  orgCode: string;
  orgName: string;
  leaderUserId?: number;
  leaderName?: string;
  sortOrder: number;
  status: 'ACTIVE' | 'DISABLED';
  children?: OrganizationNode[];
}

export interface OrganizationPayload {
  parentId?: number;
  orgCode: string;
  orgName: string;
  leaderUserId?: number;
  sortOrder: number;
  status: 'ACTIVE' | 'DISABLED';
}

export function listOrganizationTree() {
  return request<OrganizationNode[]>('/api/organizations/tree');
}

export function createOrganization(payload: OrganizationPayload) {
  return request<OrganizationNode>('/api/organizations', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateOrganization(id: number, payload: OrganizationPayload) {
  return request<OrganizationNode>(`/api/organizations/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}
