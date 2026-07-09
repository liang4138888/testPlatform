import { request, requestBlob } from './http';

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

function filenameFromDisposition(disposition: string | null, fallback: string) {
  if (!disposition) {
    return fallback;
  }
  const encoded = disposition.match(/filename\*=UTF-8''([^;]+)/)?.[1];
  if (encoded) {
    return decodeURIComponent(encoded);
  }
  return disposition.match(/filename="?([^";]+)"?/)?.[1] ?? fallback;
}

export async function downloadFile(fileId: number) {
  const response = await requestBlob(fileDownloadUrl(fileId));
  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filenameFromDisposition(response.headers.get('Content-Disposition'), `file-${fileId}`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export async function previewFileObjectUrl(fileId: number) {
  const response = await requestBlob(filePreviewUrl(fileId));
  return URL.createObjectURL(await response.blob());
}

export async function openFilePreview(fileId: number) {
  const url = await previewFileObjectUrl(fileId);
  window.open(url, '_blank');
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
}
