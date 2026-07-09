import { cachedUser } from '../api/auth';

export function isAdmin() {
  return cachedUser()?.roles?.includes('ADMIN') ?? false;
}

export function hasPermission(permission: string) {
  if (isAdmin()) {
    return true;
  }
  return cachedUser()?.permissions?.includes(permission) ?? false;
}
