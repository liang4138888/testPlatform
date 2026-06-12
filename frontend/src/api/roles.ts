import { request } from './http';

export interface RoleItem {
  id: number;
  roleCode: string;
  roleName: string;
  permissions: string[];
}

export interface PermissionItem {
  id: number;
  permissionCode: string;
  permissionName: string;
}

export function listRoles() {
  return request<RoleItem[]>('/api/roles');
}

export function createRole(payload: { roleCode: string; roleName: string }) {
  return request<RoleItem>('/api/roles', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateRole(roleId: number, payload: { roleCode: string; roleName: string }) {
  return request<RoleItem>(`/api/roles/${roleId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function listPermissions() {
  return request<PermissionItem[]>('/api/permissions');
}

export function createPermission(payload: { permissionCode: string; permissionName: string }) {
  return request<PermissionItem>('/api/permissions', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updatePermission(permissionId: number, payload: { permissionCode: string; permissionName: string }) {
  return request<PermissionItem>(`/api/permissions/${permissionId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function deletePermission(permissionId: number) {
  return request<void>(`/api/permissions/${permissionId}`, { method: 'DELETE' });
}

export function updateRolePermissions(roleId: number, permissions: string[]) {
  return request<RoleItem>(`/api/roles/${roleId}/permissions`, {
    method: 'PUT',
    body: JSON.stringify({ permissions })
  });
}
