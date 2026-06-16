import { request } from './http';

export type RequirementStatus =
  | 'COLLECTING'
  | 'PRE_REVIEW'
  | 'DETAIL_REVIEW'
  | 'ASSIGNING'
  | 'DEVELOPING'
  | 'TESTING'
  | 'UI_ACCEPTING'
  | 'PRODUCT_ACCEPTING'
  | 'DONE';

export type RequirementPriority = 'P1' | 'P2' | 'P3';
export type RequirementInvolvedModule = 'FRONTEND' | 'IOS' | 'ANDROID' | 'HARMONY' | 'MINI_PROGRAM' | 'WEB' | 'H5' | 'TEST';
export type RequirementTaskType = 'DEV' | 'TEST' | 'ACCEPTANCE';
export type RequirementRoleType = 'BACKEND' | 'IOS' | 'ANDROID' | 'MINI_PROGRAM' | 'HARMONY' | 'WEB' | 'TEST' | 'OTHER';
export type RequirementTaskStatus =
  | 'DEV_SCHEDULED'
  | 'DEV_TODO'
  | 'DEV_DOING'
  | 'DEV_INTEGRATING'
  | 'DEV_DONE'
  | 'TEST_SCHEDULED'
  | 'TEST_TODO'
  | 'TEST_DOING'
  | 'TEST_DONE';

export interface OptionDefinition<T extends string> {
  value: T;
  label: string;
}

export const REQUIREMENT_STATUS_OPTIONS: OptionDefinition<RequirementStatus>[] = [
  { value: 'COLLECTING', label: '需求采集' },
  { value: 'PRE_REVIEW', label: '需求预评审' },
  { value: 'DETAIL_REVIEW', label: '需求详细评审' },
  { value: 'ASSIGNING', label: '需求分配' },
  { value: 'DEVELOPING', label: '需求开发' },
  { value: 'TESTING', label: '需求测试' },
  { value: 'UI_ACCEPTING', label: 'UI验收' },
  { value: 'PRODUCT_ACCEPTING', label: '产品验收' },
  { value: 'DONE', label: '完成' }
];

export const REQUIREMENT_PRIORITY_OPTIONS: OptionDefinition<RequirementPriority>[] = [
  { value: 'P1', label: '高' },
  { value: 'P2', label: '中' },
  { value: 'P3', label: '低' }
];

export const REQUIREMENT_INVOLVED_MODULE_OPTIONS: OptionDefinition<RequirementInvolvedModule>[] = [
  { value: 'FRONTEND', label: '前台' },
  { value: 'IOS', label: 'IOS' },
  { value: 'ANDROID', label: '安卓' },
  { value: 'HARMONY', label: '鸿蒙' },
  { value: 'MINI_PROGRAM', label: '小程序' },
  { value: 'WEB', label: 'WEB' },
  { value: 'H5', label: 'H5' },
  { value: 'TEST', label: '测试' }
];

export const REQUIREMENT_ROLE_OPTIONS: OptionDefinition<RequirementRoleType>[] = [
  { value: 'BACKEND', label: '服务端' },
  { value: 'IOS', label: 'iOS' },
  { value: 'ANDROID', label: '安卓' },
  { value: 'MINI_PROGRAM', label: '小程序' },
  { value: 'HARMONY', label: '鸿蒙' },
  { value: 'WEB', label: 'Web' },
  { value: 'TEST', label: '测试' },
  { value: 'OTHER', label: '其他' }
];

export const DEV_TASK_STATUS_OPTIONS: OptionDefinition<RequirementTaskStatus>[] = [
  { value: 'DEV_SCHEDULED', label: '待排期' },
  { value: 'DEV_TODO', label: '待开发' },
  { value: 'DEV_DOING', label: '开发中' },
  { value: 'DEV_INTEGRATING', label: '联调中' },
  { value: 'DEV_DONE', label: '开发完成' }
];

export const TEST_TASK_STATUS_OPTIONS: OptionDefinition<RequirementTaskStatus>[] = [
  { value: 'TEST_SCHEDULED', label: '待排期' },
  { value: 'TEST_TODO', label: '待测试' },
  { value: 'TEST_DOING', label: '测试中' },
  { value: 'TEST_DONE', label: '测试完成' }
];

export interface Requirement {
  id: number;
  projectId: number;
  requirementNo: string;
  name: string;
  description?: string;
  ownerName?: string;
  proposedDate?: string;
  proposedIteration?: string;
  releaseIteration?: string;
  priority?: RequirementPriority;
  status?: RequirementStatus;
  gatePass?: boolean;
  gateMessage?: string;
  devTaskDoneCount?: number;
  devTaskTotalCount?: number;
  testTaskDoneCount?: number;
  testTaskTotalCount?: number;
  caseSuiteCount?: number;
  bugCount?: number;
  openBugCount?: number;
  prd?: string;
  prototype?: string;
  participantDomains?: string;
  involvedModules?: string;
  devAssigneeIds?: string;
  testAssigneeIds?: string;
  tasks?: RequirementTask[];
  histories?: RequirementHistory[];
  createdAt: string;
  updatedAt: string;
}

export interface RequirementTask {
  id: number;
  requirementId: number;
  taskType: RequirementTaskType;
  roleType: RequirementRoleType;
  name: string;
  assigneeId?: number;
  assigneeName?: string;
  status: RequirementTaskStatus;
  remark?: string;
  sortOrder?: number;
  startTime?: string;
  endTime?: string;
}

export interface RequirementHistory {
  id: number;
  requirementId: number;
  actionType: string;
  fieldName?: string;
  oldValue?: string;
  newValue?: string;
  operatorId?: number;
  operatorName?: string;
  remark?: string;
  createdAt: string;
}

export interface RequirementDetail extends Requirement {}

export interface RequirementBoardQuery {
  projectId?: number;
  status?: RequirementStatus | '';
  keyword?: string;
}

export interface RequirementCreatePayload {
  requirementNo?: string;
  name: string;
  description?: string;
  ownerName?: string;
  proposedDate?: string;
  proposedIteration?: string;
  releaseIteration?: string;
  priority?: RequirementPriority;
  prd?: string;
  prototype?: string;
  participantDomains?: string;
  involvedModules?: string;
}

export type RequirementUpdatePayload = Omit<RequirementCreatePayload, 'requirementNo'>;

export interface RequirementAssignPayload {
  devAssigneeIds?: string;
  testAssigneeIds?: string;
}

export interface RequirementTransitionPayload {
  targetStatus: RequirementStatus;
  remark?: string;
  devAssigneeIds?: string;
  testAssigneeIds?: string;
}

export interface RequirementTaskPayload {
  taskType: RequirementTaskType;
  roleType: RequirementRoleType;
  name: string;
  assigneeId?: number;
  status: RequirementTaskStatus;
  remark?: string;
  sortOrder?: number;
  startTime: string;
  endTime: string;
}

function buildQuery(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : '';
}

export function listRequirementBoard(params: RequirementBoardQuery = {}) {
  return request<Requirement[]>(`/api/requirements${buildQuery({
    projectId: params.projectId,
    status: params.status,
    keyword: params.keyword
  })}`);
}

export function listRequirements(projectId: number) {
  return request<Requirement[]>(`/api/projects/${projectId}/requirements`);
}

export function createRequirement(projectId: number, payload: RequirementCreatePayload) {
  return request<Requirement>(`/api/projects/${projectId}/requirements`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function getRequirementDetail(requirementId: number) {
  return request<RequirementDetail>(`/api/requirements/${requirementId}`);
}

export function updateRequirement(requirementId: number, payload: RequirementUpdatePayload) {
  return request<RequirementDetail>(`/api/requirements/${requirementId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function assignRequirement(requirementId: number, payload: RequirementAssignPayload) {
  return request<RequirementDetail>(`/api/requirements/${requirementId}/assign`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function transitionRequirement(requirementId: number, payload: RequirementTransitionPayload) {
  return request<RequirementDetail>(`/api/requirements/${requirementId}/transition`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function listRequirementTasks(requirementId: number) {
  return request<RequirementTask[]>(`/api/requirements/${requirementId}/tasks`);
}

export function createRequirementTask(requirementId: number, payload: RequirementTaskPayload) {
  return request<RequirementTask>(`/api/requirements/${requirementId}/tasks`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateRequirementTask(requirementId: number, taskId: number, payload: Partial<RequirementTaskPayload>) {
  return request<RequirementTask>(`/api/requirements/${requirementId}/tasks/${taskId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function deleteRequirementTask(requirementId: number, taskId: number) {
  return request<void>(`/api/requirements/${requirementId}/tasks/${taskId}`, {
    method: 'DELETE'
  });
}

export function listRequirementHistory(requirementId: number) {
  return request<RequirementHistory[]>(`/api/requirements/${requirementId}/history`);
}
