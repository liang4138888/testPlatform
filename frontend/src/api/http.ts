export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
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

  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || body.code !== '0') {
    if (body.code === 'UNAUTHORIZED') {
      clearToken();
      window.location.href = '/login';
    }
    throw new Error(body.message || '请求失败');
  }

  return body.data;
}
