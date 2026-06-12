import { request } from './http';

export interface BugListItem {
  id: number;
  bugNo: string;
  title: string;
  projectId: number;
  requirementId: number;
  status: string;
  severity: string;
  priority: string;
  reporterId: number;
  assigneeId?: number;
  attachmentCount: number;
  updatedAt: string;
}

export interface BugDetail extends BugListItem {
  description?: string;
  caseSuiteId?: number;
  attachments: BugAttachment[];
  comments: BugComment[];
  histories: BugHistory[];
}

export interface BugAttachment {
  id: number;
  fileId: number;
  attachmentType: string;
  createdAt: string;
}

export interface BugComment {
  id: number;
  bugId: number;
  content: string;
  createdBy: number;
  createdAt: string;
}

export interface BugHistory {
  id: number;
  actionType: string;
  fieldName?: string;
  oldValue?: string;
  newValue?: string;
  operatorId: number;
  createdAt: string;
}

export interface BugPayload {
  title: string;
  description?: string;
  caseSuiteId?: number;
  severity: string;
  priority: string;
  assigneeId?: number;
}

export interface BugUpdatePayload extends BugPayload {
  status?: string;
}

export function listBugs(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return request<BugListItem[]>(`/api/bugs${query ? `?${query}` : ''}`);
}

export function createBug(requirementId: number, payload: BugPayload) {
  return request<BugDetail>(`/api/requirements/${requirementId}/bugs`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function getBug(bugId: number) {
  return request<BugDetail>(`/api/bugs/${bugId}`);
}

export function updateBug(bugId: number, payload: Partial<BugUpdatePayload>) {
  return request<BugDetail>(`/api/bugs/${bugId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function uploadBugImage(bugId: number, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<BugAttachment>(`/api/bugs/${bugId}/images`, {
    method: 'POST',
    body: formData
  });
}

export function addBugComment(bugId: number, content: string) {
  return request<BugComment>(`/api/bugs/${bugId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content })
  });
}
