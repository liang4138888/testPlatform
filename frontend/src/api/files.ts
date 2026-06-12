import { request } from './http';

export interface FileObject {
  id: number;
  originalName: string;
  fileKind: 'ORIGINAL' | 'EXPORTED' | 'BUG_IMAGE';
  sizeBytes: number;
  createdAt: string;
}

export function listCaseSuiteFiles(suiteId: number) {
  return request<FileObject[]>(`/api/case-suites/${suiteId}/files`);
}

export function fileDownloadUrl(fileId: number) {
  return `/api/files/${fileId}/download`;
}

export function filePreviewUrl(fileId: number) {
  return `/api/files/${fileId}/preview`;
}
