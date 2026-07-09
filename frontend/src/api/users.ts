import { request } from './http';

export interface RoleOption {
  id: number;
  roleCode: string;
  roleName: string;
}

export type UserStatus = 'ACTIVE' | 'DISABLED';

export interface OrganizationOption {
  id: number;
  parentId?: number;
  orgCode: string;
  orgName: string;
  leaderUserId?: number;
  leaderName?: string;
  sortOrder: number;
  status: UserStatus;
  children?: OrganizationOption[];
}

export interface AssignableUser {
  id: number;
  username: string;
  displayName: string;
  email?: string;
  avatar?: string;
  organizationId?: number;
  status?: UserStatus;
  roleCodes?: string[];
  roleNames?: string[];
}

export interface UserCreatePayload {
  username: string;
  password: string;
  displayName: string;
  email?: string;
  avatar?: string;
  organizationId?: number;
  roleCode: string;
  status: UserStatus;
}

export interface UserUpdatePayload {
  displayName: string;
  email?: string;
  avatar?: string;
  organizationId?: number;
  roleCode: string;
  status: UserStatus;
}

export interface PasswordResetPayload {
  password: string;
}

export function listRolesForUser() {
  return request<RoleOption[]>('/api/roles/options');
}

export function listOrganizationTreeForUser() {
  return request<OrganizationOption[]>('/api/organizations/tree');
}

export function listUsers() {
  return request<AssignableUser[]>('/api/users');
}

export function listAssignableUsers() {
  return request<AssignableUser[]>('/api/users/assignable');
}

export function createUser(payload: UserCreatePayload) {
  return request<AssignableUser>('/api/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateUser(userId: number, payload: UserUpdatePayload) {
  return request<AssignableUser>(`/api/users/${userId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function resetUserPassword(userId: number, payload: PasswordResetPayload) {
  return request<AssignableUser>(`/api/users/${userId}/password`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}
