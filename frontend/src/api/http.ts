export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  });

  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || body.code !== '0') {
    throw new Error(body.message || '请求失败');
  }

  return body.data;
}
