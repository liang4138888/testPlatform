import { request } from './http';

export interface RoleOption {
  id: number;
  roleCode: string;
  roleName: string;
}

export interface AssignableUser {
  id: number;
  username: string;
  displayName: string;
  avatar?: string;
  roleCodes?: string[];
  roleNames?: string[];
}

export interface UserCreatePayload {
  username: string;
  password: string;
  displayName: string;
  email?: string;
  avatar?: string;
  roleCode: string;
  status: string;
}

export function listRolesForUser() {
  return request<RoleOption[]>('/api/roles');
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
