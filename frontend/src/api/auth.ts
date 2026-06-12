import { request, setToken, clearToken } from './http';

export interface CurrentUser {
  id: number;
  username: string;
  displayName: string;
  avatar?: string;
  roles: string[];
  permissions: string[];
}

export interface LoginResponse {
  token: string;
  user: CurrentUser;
}

export function login(username: string, password: string) {
  return request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  }).then((response) => {
    setToken(response.token);
    localStorage.setItem('test-platform-user', JSON.stringify(response.user));
    return response;
  });
}

export function logout() {
  return request<void>('/api/auth/logout', { method: 'POST' }).finally(() => {
    clearToken();
    localStorage.removeItem('test-platform-user');
  });
}

export function currentUser() {
  return request<CurrentUser>('/api/auth/me').then((user) => {
    localStorage.setItem('test-platform-user', JSON.stringify(user));
    return user;
  });
}

export function cachedUser() {
  const raw = localStorage.getItem('test-platform-user');
  return raw ? (JSON.parse(raw) as CurrentUser) : undefined;
}
