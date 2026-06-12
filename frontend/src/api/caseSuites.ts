import { request, type ApiResponse } from './http';

export type NodeType = 'module' | 'case' | 'step' | 'expected';
export type ExecutionStatus = 'PENDING' | 'PASSED' | 'FAILED';

export interface CaseNode {
  id: number;
  parentId?: number | null;
  nodeType: NodeType;
  name: string;
  description?: string;
  sortOrder: number;
  executionStatus?: ExecutionStatus;
  children: CaseNode[];
}

export interface CaseSuiteListItem {
  id: number;
  requirementId: number;
  projectId: number;
  name: string;
  status: string;
  requirementNo: string;
  requirementName: string;
  projectName: string;
  originalFileId?: number;
  exportedFileId?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CaseSuiteSearchParams {
  projectId?: number;
  requirementId?: number;
}

export interface CaseSuiteSummary {
  id: number;
  requirementId: number;
  name: string;
  status: string;
  originalFileId?: number;
  exportedFileId?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CaseSuiteDetail extends CaseSuiteSummary {
  requirementNo: string;
  requirementName: string;
  nodes: CaseNode[];
}

export interface CaseNodePayload {
  nodeType: NodeType;
  name: string;
  description?: string;
  sortOrder: number;
  executionStatus?: ExecutionStatus;
  children?: CaseNodePayload[];
}

export interface CaseSuiteExportResult {
  exportedFileId: number;
  fileName: string;
}

async function parseResponse<T>(response: Response): Promise<T> {
  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || body.code !== '0') {
    throw new Error(body.message || '请求失败');
  }
  return body.data;
}

export function searchCaseSuites(params: CaseSuiteSearchParams = {}) {
  const query = new URLSearchParams();
  if (params.projectId) {
    query.set('projectId', String(params.projectId));
  }
  if (params.requirementId) {
    query.set('requirementId', String(params.requirementId));
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<CaseSuiteListItem[]>(`/api/case-suites${suffix}`);
}

export function listCaseSuites(requirementId: number) {
  return request<CaseSuiteSummary[]>(`/api/requirements/${requirementId}/case-suites`);
}

export function getCaseSuite(suiteId: number) {
  return request<CaseSuiteDetail>(`/api/case-suites/${suiteId}`);
}

export function deleteCaseSuite(suiteId: number) {
  return request<void>(`/api/case-suites/${suiteId}`, {
    method: 'DELETE'
  });
}

export async function uploadCaseSuite(requirementId: number, file: File, name?: string) {
  const formData = new FormData();
  formData.append('file', file);
  if (name?.trim()) {
    formData.append('name', name.trim());
  }
  const response = await fetch(`/api/requirements/${requirementId}/case-suites/upload`, {
    method: 'POST',
    body: formData
  });
  return parseResponse<CaseSuiteDetail>(response);
}

export function saveCaseNodes(suiteId: number, nodes: CaseNodePayload[]) {
  return request<CaseSuiteDetail>(`/api/case-suites/${suiteId}/nodes`, {
    method: 'PUT',
    body: JSON.stringify({ nodes })
  });
}

export function exportCaseSuite(suiteId: number) {
  return request<CaseSuiteExportResult>(`/api/case-suites/${suiteId}/export-xmind`, {
    method: 'POST'
  });
}
