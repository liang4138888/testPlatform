import { ElMessage } from 'element-plus';

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export class ApiError extends Error {
  code?: string;
  toastShown: boolean;

  constructor(message: string, code?: string, toastShown = false) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.toastShown = toastShown;
  }
}

export function getToken() {
  return localStorage.getItem('test-platform-token');
}

export function setToken(token: string) {
  localStorage.setItem('test-platform-token', token);
}

export function clearToken() {
  localStorage.removeItem('test-platform-token');
}

export function isErrorToastShown(error: unknown) {
  return Boolean(error && typeof error === 'object' && 'toastShown' in error && (error as { toastShown?: boolean }).toastShown);
}

export function showErrorMessage(error: unknown, fallbackMessage: string) {
  if (isErrorToastShown(error)) {
    return;
  }
  ElMessage.error(error instanceof Error ? error.message : fallbackMessage);
}

function handleUnauthorized() {
  clearToken();
  window.location.href = '/login';
}

function throwApiError(message: string, code?: string): never {
  if (code === 'UNAUTHORIZED') {
    handleUnauthorized();
    throw new ApiError(message || '登录已过期', code);
  }
  const errorMessage = message || '请求失败';
  ElMessage.error(errorMessage);
  throw new ApiError(errorMessage, code, true);
}

async function parseJsonBody<T>(response: Response): Promise<ApiResponse<T> | undefined> {
  const contentType = response.headers.get('Content-Type') ?? '';
  if (!contentType.includes('application/json')) {
    return undefined;
  }
  return (await response.json()) as ApiResponse<T>;
}

export async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  const body = await parseJsonBody<T>(response);
  if (!response.ok || body?.code !== '0') {
    throwApiError(body?.message || '请求失败', body?.code);
  }

  return body.data;
}

export async function requestBlob(url: string, options: RequestInit = {}) {
  const headers = new Headers(options.headers);
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  if (!response.ok) {
    const body = await parseJsonBody<unknown>(response);
    if (body) {
      throwApiError(body.message || '请求失败', body.code);
    }
    throwApiError('请求失败');
  }

  return response;
}
