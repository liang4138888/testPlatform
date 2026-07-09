<template>
  <section class="requirement-prototype" v-loading="boardLoading">
    <main class="prototype-main">
      <header class="prototype-toolbar">
        <div>
          <h1>需求看板</h1>
          <div class="meta">{{ visibleRequirements.length }} 个需求</div>
        </div>
        <div class="toolbar-actions">
          <el-select v-model="filters.projectId" class="project-select" placeholder="选择项目" @change="loadBoard">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
          <label class="search">
            <input v-model="filters.keyword" type="search" placeholder="搜索需求" @input="selectFirstVisible" />
          </label>
          <button v-if="canCreateRequirement" class="primary-btn" type="button" :disabled="!filters.projectId" @click="openCreateDialog">+ 新建需求</button>
        </div>
      </header>

      <nav class="status-filters">
        <button
          v-for="item in statusFilters"
          :key="item.value"
          class="status-item"
          :class="{ active: filters.status === item.value, dragover: dragOverStatus === item.value, 'can-drop': isDropAllowed(item.value) }"
          type="button"
          @click="changeStatus(item.value)"
          @dragover.prevent="onStatusDragOver(item.value)"
          @dragleave="dragOverStatus = undefined"
          @drop="dropRequirementToStatus(item.value)"
        >
          <span class="status-label"><span class="dot" :class="dotClass(item.value)"></span><span>{{ item.label }}</span></span>
          <span v-if="draggingRequirement && dragOverStatus === item.value && item.value !== '全部'" class="drop-hint">释放流转</span>
          <span v-else class="count">{{ item.count }}</span>
        </button>
      </nav>

      <section class="board">
        <button
          v-for="requirement in visibleRequirements"
          :key="requirement.id"
          class="requirement-card"
          :class="{ active: selectedRequirement?.id === requirement.id, dragging: draggingRequirement?.id === requirement.id }"
          type="button"
          :draggable="canTransitionRequirement"
          @dragstart="startRequirementDrag(requirement, $event)"
          @dragend="endRequirementDrag"
          @click="selectRequirement(requirement)"
        >
          <span>
            <h2 class="card-title">{{ requirement.name }}</h2>
            <p class="card-desc">{{ requirement.description || '暂无需求说明' }}</p>
            <span class="card-foot">
              <span class="badge blue">{{ requirement.requirementNo || `REQ-${requirement.id}` }}</span>
              <span class="badge">{{ requirement.ownerName || '-' }}</span>
              <span class="badge">{{ requirement.proposedIteration || '-' }}</span>
              <span class="badge">上线 {{ requirement.releaseIteration || '-' }}</span>
              <span class="badge" :class="statusBadgeClass(requirement)">{{ statusLabel(requirement.status) }}</span>
              <span class="badge" :class="gateBadgeClass(requirement)">{{ requirement.status === 'DONE' ? '已完成' : gateText(requirement) }}</span>
            </span>
          </span>
          <span class="badge" :class="priorityClass(requirement.priority)">{{ requirement.priority || 'P2' }}</span>
        </button>
        <div v-if="!visibleRequirements.length" class="empty">暂无匹配需求</div>
      </section>
    </main>

    <aside class="side-panel">
      <section class="panel" v-if="selectedRequirement">
        <div class="panel-head">
          <h2>{{ selectedRequirement.name }}</h2>
          <span class="badge" :class="statusBadgeClass(selectedRequirement)">{{ statusLabel(selectedRequirement.status) }}</span>
        </div>
        <p class="detail-desc">{{ selectedRequirement.description || '暂无需求说明' }}</p>
        <div class="detail-grid">
          <div><strong>需求编号</strong><span>{{ selectedRequirement.requirementNo || `REQ-${selectedRequirement.id}` }}</span></div>
          <div><strong>提出人</strong><span>{{ selectedRequirement.ownerName || '-' }}</span></div>
          <div><strong>提出时间</strong><span>{{ selectedRequirement.proposedDate || '-' }}</span></div>
          <div><strong>提出迭代</strong><span>{{ selectedRequirement.proposedIteration || '-' }}</span></div>
          <div><strong>上线迭代</strong><span>{{ selectedRequirement.releaseIteration || '-' }}</span></div>
          <div><strong>优先级</strong><span>{{ selectedRequirement.priority || 'P2' }}</span></div>
          <div><strong>参与域</strong><span>{{ selectedRequirement.participantDomains || '-' }}</span></div>
          <div><strong>涉及模块</strong><span>{{ involvedModuleText(selectedRequirement.involvedModules) }}</span></div>
        </div>
        <div class="link-info">
          <div><strong>PRD</strong><span>{{ selectedRequirement.prd || '-' }}</span></div>
          <div><strong>原型</strong><span>{{ selectedRequirement.prototype || '-' }}</span></div>
          <div><strong>开发人员</strong><span>{{ assigneeText(selectedRequirement.devAssigneeIds) }}</span></div>
          <div><strong>测试人员</strong><span>{{ assigneeText(selectedRequirement.testAssigneeIds) }}</span></div>
        </div>
        <div class="flow">
          <div
            v-for="(status, index) in REQUIREMENT_STATUS_OPTIONS"
            :key="status.value"
            class="flow-step"
            :class="flowStepClass(index)"
          >
            {{ status.label }}
          </div>
        </div>
      </section>

      <section class="panel" v-if="selectedRequirement && normalizedStatus(selectedRequirement) === 'ASSIGNING'">
        <div class="panel-head"><h2>人员分配</h2></div>
        <div class="assignment-form">
          <label>开发人员</label>
          <el-select v-model="assignmentForm.devAssigneeIds" :disabled="!canAssignRequirement" multiple filterable collapse-tags collapse-tags-tooltip placeholder="选择开发人员">
            <el-option v-for="user in users" :key="`dev-${user.id}`" :label="user.displayName || user.username" :value="user.id" />
          </el-select>
          <label>测试人员</label>
          <el-select v-model="assignmentForm.testAssigneeIds" :disabled="!canAssignRequirement" multiple filterable collapse-tags collapse-tags-tooltip placeholder="选择测试人员">
            <el-option v-for="user in users" :key="`test-${user.id}`" :label="user.displayName || user.username" :value="user.id" />
          </el-select>
          <div class="assignment-tip">人员分配会在流转到下一状态时保存</div>
        </div>
      </section>

      <section class="panel" v-if="selectedRequirement">
        <div class="panel-head">
          <h2>{{ taskPanelTitle }}</h2>
          <button v-if="canManageStageTasks" class="ghost-btn" type="button" @click="openTaskDialog()">新建子任务</button>
          <span v-else-if="activeTasks.length" class="badge">{{ doneActiveTasks.length }}/{{ activeTasks.length }}</span>
        </div>
        <div v-if="activeTasks.length" class="task-list">
          <button v-for="task in activeTasks" :key="task.id" class="task" type="button" @click="openTaskDialog(task)">
            <span>
              <span class="task-name">{{ task.name }}</span>
              <span class="task-role">{{ roleLabel(task.roleType) }}{{ task.assigneeName ? ` · ${task.assigneeName}` : '' }}</span>
              <span class="task-role">{{ formatDateTime(task.startTime) }} ~ {{ formatDateTime(task.endTime) }}</span>
            </span>
            <span class="task-side">
              <select class="task-status-select" :disabled="!canManageRequirementTask" :class="taskStatusClass(task.status)" :value="task.status" @click.stop @change="(event) => changeTaskStatus(task, event)">
                <option v-for="item in taskStatusOptions(task)" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </span>
          </button>
        </div>
        <div v-else class="empty">{{ canManageStageTasks ? '暂无子任务，请先创建' : '当前状态无任务门禁' }}</div>
        <div class="case-actions">
          <button v-if="canUploadCase" class="ghost-btn" type="button" @click="openUploadDialog(selectedRequirement)">上传 XMind</button>
          <button class="ghost-btn" type="button" @click="openSuitePicker(selectedRequirement)">查看用例</button>
        </div>
      </section>

      <section class="panel" v-if="selectedRequirement">
        <div class="panel-head"><h2>状态流转</h2></div>
        <div class="transition">
          <div class="blocker" :class="{ warning: Boolean(nextStatus && !gatePass(selectedRequirement)) }">
            {{ selectedRequirement.status === 'DONE' ? '流程已完成' : gateText(selectedRequirement) }}
          </div>
          <div class="transition-row">
            <button v-if="canTransitionRequirement" class="ghost-btn" type="button" :disabled="!previousStatus || transitionLoading" @click="submitTransition(previousStatus)">← {{ previousStatusLabel }}</button>
            <button v-if="canTransitionRequirement" class="primary-btn" type="button" :disabled="!nextStatus || !gatePass(selectedRequirement) || transitionLoading" @click="submitTransition(nextStatus)">{{ nextStatusLabel }} →</button>
            <span v-if="!canTransitionRequirement" class="badge">无流转权限</span>
          </div>
        </div>
      </section>
    </aside>
  </section>

  <el-dialog v-model="dialogVisible" title="新建需求" width="520px" class="prototype-dialog">
    <el-form label-position="top">
      <div class="form-note">新建后自动进入“需求采集”状态</div>
      <el-form-item label="需求名称" required><el-input v-model="form.name" maxlength="36" placeholder="例如：促销活动配置支持多端生效" /></el-form-item>
      <el-form-item label="提出人" required>
        <el-select v-model="form.ownerName" filterable allow-create default-first-option placeholder="选择或输入提出人">
          <el-option v-for="owner in ownerOptions" :key="owner" :label="owner" :value="owner" />
        </el-select>
      </el-form-item>
      <div class="form-row">
        <el-form-item label="提出时间" required><el-date-picker v-model="form.proposedDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="提出迭代" required>
          <el-select v-model="form.proposedIteration" filterable allow-create default-first-option placeholder="选择或输入迭代">
            <el-option v-for="iteration in iterationOptions" :key="`proposed-${iteration}`" :label="iteration" :value="iteration" />
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="计划上线迭代" required>
        <el-select v-model="form.releaseIteration" filterable allow-create default-first-option placeholder="选择或输入迭代">
          <el-option v-for="iteration in iterationOptions" :key="`release-${iteration}`" :label="iteration" :value="iteration" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="form.priority">
          <el-option v-for="item in REQUIREMENT_PRIORITY_OPTIONS" :key="item.value" :label="`${item.value} ${item.label}`" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="涉及模块">
        <el-select v-model="form.involvedModules" multiple collapse-tags collapse-tags-tooltip placeholder="选择涉及模块">
          <el-option v-for="item in REQUIREMENT_INVOLVED_MODULE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="PRD"><el-input v-model="form.prd" maxlength="500" placeholder="填写 PRD 链接或文档位置" /></el-form-item>
      <el-form-item label="原型"><el-input v-model="form.prototype" maxlength="500" placeholder="填写原型链接或文件位置" /></el-form-item>
      <el-form-item label="参与域"><el-input v-model="form.participantDomains" maxlength="200" placeholder="例如：交易、商品、促销" /></el-form-item>
      <el-form-item label="需求说明"><el-input v-model="form.description" type="textarea" maxlength="120" placeholder="简单描述目标、范围或业务背景" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="uploadVisible" title="上传 XMind" width="520px">
    <el-form label-width="88px">
      <el-form-item label="需求"><el-input :model-value="uploadRequirementLabel" disabled /></el-form-item>
      <el-form-item label="用例集名称"><el-input v-model="uploadForm.name" maxlength="150" placeholder="默认使用文件名" /></el-form-item>
      <el-form-item label="文件" required><input type="file" accept=".xmind" @change="onFileChange" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="uploadVisible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="submitUpload">上传并解析</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="taskDialogVisible" :title="taskForm.id ? '编辑子任务' : '新建子任务'" width="560px">
    <el-form label-position="top">
      <el-form-item label="子任务名称" required><el-input v-model="taskForm.name" maxlength="150" placeholder="填写子任务名称" /></el-form-item>
      <el-form-item label="负责人" required>
        <el-select v-model="taskForm.assigneeId" filterable placeholder="选择负责人">
          <el-option v-for="user in stageAssigneeOptions" :key="user.id" :label="user.displayName || user.username" :value="user.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" required>
        <el-select v-model="taskForm.status">
          <el-option v-for="item in stageTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <div class="form-row">
        <el-form-item label="开始时间" required><el-date-picker v-model="taskForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间" required><el-date-picker v-model="taskForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      </div>
      <el-form-item label="备注"><el-input v-model="taskForm.remark" type="textarea" maxlength="500" placeholder="填写备注" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="taskForm.id" type="danger" plain @click="deleteTask">删除</el-button>
      <el-button @click="taskDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="taskSaving" @click="saveTask">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="suitePickerVisible" title="选择用例集" width="640px">
    <el-table v-loading="suiteLoading" :data="caseSuites" empty-text="该需求下暂无用例集，请先上传 XMind">
      <el-table-column prop="name" label="用例集" min-width="180" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="updatedAt" label="更新时间" width="190" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="canEditCase" link type="primary" @click="goToEditor(row.id)">编辑用例</el-button>
          <el-button v-if="canExportCase" link type="primary" @click="downloadSuiteFiles(row.id)">文件导出</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { listProjects, type Project } from '../api/projects';
import { listAssignableUsers, type AssignableUser } from '../api/users';
import { listCaseSuites, uploadCaseSuite, exportCaseSuite, type CaseSuiteSummary } from '../api/caseSuites';
import { downloadFile } from '../api/files';
import { showErrorMessage } from '../api/http';
import { hasPermission } from '../utils/permissions';
import {
  createRequirement,
  createRequirementTask,
  deleteRequirementTask,
  DEV_TASK_STATUS_OPTIONS,
  listRequirements,
  listRequirementTasks,
  REQUIREMENT_INVOLVED_MODULE_OPTIONS,
  REQUIREMENT_PRIORITY_OPTIONS,
  REQUIREMENT_ROLE_OPTIONS,
  REQUIREMENT_STATUS_OPTIONS,
  TEST_TASK_STATUS_OPTIONS,
  transitionRequirement,
  updateRequirementTask,
  type Requirement,
  type RequirementInvolvedModule,
  type RequirementPriority,
  type RequirementRoleType,
  type RequirementStatus,
  type RequirementTask,
  type RequirementTaskStatus,
  type RequirementTaskType
} from '../api/requirements';

const router = useRouter();
const projects = ref<Project[]>([]);
const users = ref<AssignableUser[]>([]);
const requirements = ref<Requirement[]>([]);
const selectedRequirement = ref<Requirement>();
const caseSuites = ref<CaseSuiteSummary[]>([]);
const boardLoading = ref(false);
const saving = ref(false);
const uploading = ref(false);
const suiteLoading = ref(false);
const transitionLoading = ref(false);
const draggingRequirement = ref<Requirement>();
const dragOverStatus = ref<RequirementStatus | '全部'>();
const dialogVisible = ref(false);
const uploadVisible = ref(false);
const suitePickerVisible = ref(false);
const taskDialogVisible = ref(false);
const taskSaving = ref(false);
const uploadRequirement = ref<Requirement>();
const uploadFile = ref<File>();

const filters = reactive<{ projectId?: number; status: RequirementStatus | '全部'; keyword: string }>({
  projectId: undefined,
  status: '全部',
  keyword: ''
});

const assignmentForm = reactive({
  devAssigneeIds: [] as number[],
  testAssigneeIds: [] as number[]
});

const form = reactive({
  name: '',
  ownerName: '',
  proposedDate: '',
  proposedIteration: '',
  releaseIteration: '',
  priority: 'P2' as RequirementPriority,
  involvedModules: [] as RequirementInvolvedModule[],
  prd: '',
  prototype: '',
  participantDomains: '',
  description: ''
});

const uploadForm = reactive({ name: '' });
const taskForm = reactive({
  id: undefined as number | undefined,
  name: '',
  assigneeId: undefined as number | undefined,
  status: 'DEV_TODO' as RequirementTaskStatus,
  startTime: '',
  endTime: '',
  remark: ''
});
const iterationOptions = computed(() => {
  const values = new Set<string>();
  requirements.value.forEach((item) => {
    if (item.proposedIteration) values.add(item.proposedIteration);
    if (item.releaseIteration) values.add(item.releaseIteration);
  });
  const currentYear = new Date().getFullYear();
  const startWeek = Math.max(1, weekOfYear(new Date()) - 2);
  for (let index = 0; index < 12; index += 1) {
    values.add(`${currentYear}-W${String(startWeek + index).padStart(2, '0')}`);
  }
  return Array.from(values).sort();
});

const ownerOptions = computed(() => {
  const names = new Set(users.value.map((user) => user.displayName || user.username).filter(Boolean));
  requirements.value.forEach((item) => {
    if (item.ownerName) names.add(item.ownerName);
  });
  return Array.from(names);
});

const statusFilters = computed(() => [
  { value: '全部' as const, label: '全部', count: requirements.value.length },
  ...REQUIREMENT_STATUS_OPTIONS.map((status) => ({
    value: status.value,
    label: status.label,
    count: requirements.value.filter((item) => normalizedStatus(item) === status.value).length
  }))
]);

const visibleRequirements = computed(() => {
  const query = filters.keyword.trim().toLowerCase();
  return requirements.value.filter((item) => {
    const statusMatched = filters.status === '全部' || normalizedStatus(item) === filters.status;
    const queryMatched = !query || [item.requirementNo, item.name, item.ownerName, item.description].some((value) => value?.toLowerCase().includes(query));
    return statusMatched && queryMatched;
  });
});

const currentStatusIndex = computed(() => REQUIREMENT_STATUS_OPTIONS.findIndex((item) => item.value === normalizedStatus(selectedRequirement.value)));
const previousStatus = computed(() => currentStatusIndex.value > 0 ? REQUIREMENT_STATUS_OPTIONS[currentStatusIndex.value - 1].value : undefined);
const nextStatus = computed(() => currentStatusIndex.value >= 0 && currentStatusIndex.value < REQUIREMENT_STATUS_OPTIONS.length - 1 ? REQUIREMENT_STATUS_OPTIONS[currentStatusIndex.value + 1].value : undefined);
const previousStatusLabel = computed(() => previousStatus.value ? statusLabel(previousStatus.value) : '上一状态');
const nextStatusLabel = computed(() => nextStatus.value ? statusLabel(nextStatus.value) : '完成');

const canCreateRequirement = computed(() => hasPermission('REQUIREMENT_CREATE'));
const canAssignRequirement = computed(() => hasPermission('REQUIREMENT_ASSIGN'));
const canTransitionRequirement = computed(() => hasPermission('REQUIREMENT_TRANSITION'));
const canManageRequirementTask = computed(() => hasPermission('REQUIREMENT_TASK_MANAGE'));
const canUploadCase = computed(() => hasPermission('CASE_UPLOAD'));
const canEditCase = computed(() => hasPermission('CASE_EDIT'));
const canExportCase = computed(() => hasPermission('CASE_EXPORT'));

const selectedTasks = computed(() => selectedRequirement.value?.tasks ?? []);
const canManageStageTasks = computed(() => canManageRequirementTask.value && ['DEVELOPING', 'TESTING'].includes(normalizedStatus(selectedRequirement.value)));
const stageTaskType = computed<RequirementTaskType>(() => normalizedStatus(selectedRequirement.value) === 'TESTING' ? 'TEST' : 'DEV');
const stageTaskStatusOptions = computed(() => stageTaskType.value === 'TEST' ? TEST_TASK_STATUS_OPTIONS : DEV_TASK_STATUS_OPTIONS);
const stageAssigneeOptions = computed(() => {
  const ids = normalizedStatus(selectedRequirement.value) === 'TESTING'
    ? parseAssigneeIds(selectedRequirement.value?.testAssigneeIds)
    : parseAssigneeIds(selectedRequirement.value?.devAssigneeIds);
  return ids.map((id) => users.value.find((user) => user.id === id)).filter((user): user is AssignableUser => Boolean(user));
});
const activeTasks = computed(() => selectedTasks.value.filter((task) => {
  const status = normalizedStatus(selectedRequirement.value);
  if (status === 'DEVELOPING') return task.taskType === 'DEV';
  if (status === 'TESTING') return task.taskType === 'TEST';
  return false;
}));
const doneActiveTasks = computed(() => activeTasks.value.filter(isTaskDone));
const taskPanelTitle = computed(() => {
  const status = normalizedStatus(selectedRequirement.value);
  if (status === 'DEVELOPING') return '开发任务池';
  if (status === 'TESTING') return '测试任务池';
  if (status === 'UI_ACCEPTING') return 'UI验收任务';
  if (status === 'PRODUCT_ACCEPTING') return '产品验收任务';
  return '阶段任务';
});

const uploadRequirementLabel = computed(() => uploadRequirement.value ? `${uploadRequirement.value.requirementNo || `REQ-${uploadRequirement.value.id}`} ${uploadRequirement.value.name}` : '');

async function loadProjects() {
  try {
    projects.value = await listProjects();
    filters.projectId = projects.value[0]?.id;
    await Promise.allSettled([loadUsers(), loadBoard()]);
  } catch (error) {
    showErrorMessage(error, '项目加载失败');
  }
}

async function loadUsers() {
  try {
    users.value = await listAssignableUsers();
  } catch {
    users.value = [];
  }
}

async function loadBoard() {
  if (!filters.projectId) {
    requirements.value = [];
    return;
  }
  boardLoading.value = true;
  try {
    requirements.value = await listRequirements(filters.projectId);
  } catch (error) {
    showErrorMessage(error, '需求加载失败');
    requirements.value = [];
  } finally {
    boardLoading.value = false;
  }
  selectFirstVisible();
}

function selectFirstVisible() {
  if (!selectedRequirement.value || !visibleRequirements.value.some((item) => item.id === selectedRequirement.value?.id)) {
    selectedRequirement.value = visibleRequirements.value[0];
  }
  syncAssignmentForm();
  loadSelectedTasks();
}

function selectRequirement(requirement: Requirement) {
  selectedRequirement.value = requirement;
  syncAssignmentForm();
  loadSelectedTasks();
}

function changeStatus(status: RequirementStatus | '全部') {
  filters.status = status;
  selectFirstVisible();
}

function openCreateDialog() {
  if (!canCreateRequirement.value) {
    ElMessage.warning('无新建需求权限');
    return;
  }
  form.name = '';
  form.ownerName = '';
  form.proposedDate = new Date().toISOString().slice(0, 10);
  form.proposedIteration = '';
  form.releaseIteration = '';
  form.priority = 'P2';
  form.involvedModules = [];
  form.prd = '';
  form.prototype = '';
  form.participantDomains = '';
  form.description = '';
  dialogVisible.value = true;
}

async function submit() {
  if (!canCreateRequirement.value) {
    ElMessage.warning('无新建需求权限');
    return;
  }
  if (!filters.projectId) return;
  if (!form.name.trim() || !form.ownerName.trim() || !form.proposedDate || !form.proposedIteration.trim() || !form.releaseIteration.trim()) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  saving.value = true;
  try {
    const created = await createRequirement(filters.projectId, {
      requirementNo: generateRequirementNo(),
      name: form.name.trim(),
      ownerName: form.ownerName.trim(),
      proposedDate: form.proposedDate,
      proposedIteration: form.proposedIteration.trim(),
      releaseIteration: form.releaseIteration.trim(),
      priority: form.priority,
      involvedModules: form.involvedModules.join(','),
      prd: form.prd.trim() || undefined,
      prototype: form.prototype.trim() || undefined,
      participantDomains: form.participantDomains.trim() || undefined,
      description: form.description.trim() || undefined
    });
    ElMessage.success('需求创建成功');
    dialogVisible.value = false;
    await loadBoard();
    selectedRequirement.value = requirements.value.find((item) => item.id === created.id) ?? created;
  } catch (error) {
    showErrorMessage(error, '需求创建失败');
  } finally {
    saving.value = false;
  }
}

async function submitTransition(targetStatus?: RequirementStatus) {
  if (!selectedRequirement.value || !targetStatus) return;
  await transitionRequirementTo(selectedRequirement.value, targetStatus);
}

function syncAssignmentForm() {
  assignmentForm.devAssigneeIds = parseAssigneeIds(selectedRequirement.value?.devAssigneeIds);
  assignmentForm.testAssigneeIds = parseAssigneeIds(selectedRequirement.value?.testAssigneeIds);
}

function parseAssigneeIds(value?: string) {
  return value?.split(',').map((item) => Number(item)).filter(Boolean) ?? [];
}

async function loadSelectedTasks() {
  const requirement = selectedRequirement.value;
  if (!requirement) return;
  try {
    const tasks = await listRequirementTasks(requirement.id);
    requirements.value = requirements.value.map((item) => item.id === requirement.id ? { ...item, tasks } : item);
    if (selectedRequirement.value?.id === requirement.id) {
      selectedRequirement.value = { ...selectedRequirement.value, tasks };
    }
  } catch {
    if (!selectedRequirement.value?.tasks) {
      selectedRequirement.value = { ...requirement, tasks: [] };
    }
  }
}

function startRequirementDrag(requirement: Requirement, event: DragEvent) {
  if (!canTransitionRequirement.value) {
    event.preventDefault();
    return;
  }
  draggingRequirement.value = requirement;
  event.dataTransfer?.setData('text/plain', String(requirement.id));
  event.dataTransfer?.setDragImage(createDragImage(requirement), 16, 16);
}

function endRequirementDrag() {
  draggingRequirement.value = undefined;
  dragOverStatus.value = undefined;
}

function onStatusDragOver(status: RequirementStatus | '全部') {
  if (canTransitionRequirement.value && status !== '全部') {
    dragOverStatus.value = status;
  }
}

function isDropAllowed(status: RequirementStatus | '全部') {
  const requirement = draggingRequirement.value;
  if (!canTransitionRequirement.value || !requirement || status === '全部') return false;
  const currentIndex = REQUIREMENT_STATUS_OPTIONS.findIndex((item) => item.value === normalizedStatus(requirement));
  const targetIndex = REQUIREMENT_STATUS_OPTIONS.findIndex((item) => item.value === status);
  return currentIndex >= 0 && targetIndex >= 0 && Math.abs(targetIndex - currentIndex) === 1;
}

function createDragImage(requirement: Requirement) {
  const element = document.createElement('div');
  element.className = 'drag-preview';
  element.textContent = requirement.name;
  document.body.appendChild(element);
  requestAnimationFrame(() => element.remove());
  return element;
}

async function dropRequirementToStatus(targetStatus: RequirementStatus | '全部') {
  const requirement = draggingRequirement.value;
  endRequirementDrag();
  if (!canTransitionRequirement.value || !requirement || targetStatus === '全部') return;
  await transitionRequirementTo(requirement, targetStatus);
}

async function transitionRequirementTo(requirement: Requirement, targetStatus: RequirementStatus) {
  if (!canTransitionRequirement.value) {
    ElMessage.warning('无状态流转权限');
    return;
  }
  const currentStatus = normalizedStatus(requirement);
  const currentIndex = REQUIREMENT_STATUS_OPTIONS.findIndex((item) => item.value === currentStatus);
  const targetIndex = REQUIREMENT_STATUS_OPTIONS.findIndex((item) => item.value === targetStatus);
  if (currentIndex < 0 || targetIndex < 0 || Math.abs(targetIndex - currentIndex) !== 1) return;
  transitionLoading.value = true;
  try {
    const assignmentPatch = currentStatus === 'ASSIGNING' && targetStatus === 'DEVELOPING'
      ? {
          devAssigneeIds: selectedRequirement.value?.id === requirement.id ? assignmentForm.devAssigneeIds.join(',') : requirement.devAssigneeIds,
          testAssigneeIds: selectedRequirement.value?.id === requirement.id ? assignmentForm.testAssigneeIds.join(',') : requirement.testAssigneeIds
        }
      : undefined;
    const payload = assignmentPatch
      ? { targetStatus, ...assignmentPatch }
      : { targetStatus };
    const updated = await transitionRequirement(requirement.id, payload);
    const merged = assignmentPatch ? { ...updated, ...assignmentPatch } : updated;
    requirements.value = requirements.value.map((item) => item.id === requirement.id ? { ...item, ...merged } : item);
    selectedRequirement.value = selectedRequirement.value?.id === requirement.id ? { ...selectedRequirement.value, ...merged } : selectedRequirement.value;
    syncAssignmentForm();
    await loadSelectedTasks();
    ElMessage.success('状态已更新');
  } catch (error) {
    showErrorMessage(error, '状态流转失败');
  } finally {
    transitionLoading.value = false;
  }
}

function openTaskDialog(task?: RequirementTask) {
  if (!selectedRequirement.value || !canManageStageTasks.value) return;
  taskForm.id = task?.id;
  taskForm.name = task?.name ?? '';
  taskForm.assigneeId = task?.assigneeId ?? stageAssigneeOptions.value[0]?.id;
  taskForm.status = task?.status ?? (stageTaskType.value === 'TEST' ? 'TEST_TODO' : 'DEV_TODO');
  taskForm.startTime = toInputDateTime(task?.startTime) || toInputDateTime(new Date().toISOString());
  taskForm.endTime = toInputDateTime(task?.endTime) || toInputDateTime(new Date().toISOString());
  taskForm.remark = task?.remark ?? '';
  taskDialogVisible.value = true;
}

async function saveTask() {
  if (!canManageRequirementTask.value) {
    ElMessage.warning('无子任务管理权限');
    return;
  }
  if (!selectedRequirement.value) return;
  if (!taskForm.name.trim() || !taskForm.assigneeId || !taskForm.startTime || !taskForm.endTime) {
    ElMessage.warning('请填写子任务必填信息');
    return;
  }
  if (taskForm.endTime < taskForm.startTime) {
    ElMessage.warning('结束时间不能早于开始时间');
    return;
  }
  taskSaving.value = true;
  try {
    const payload = {
      taskType: stageTaskType.value,
      roleType: (stageTaskType.value === 'TEST' ? 'TEST' : 'BACKEND') as RequirementRoleType,
      name: taskForm.name.trim(),
      assigneeId: taskForm.assigneeId,
      status: taskForm.status,
      startTime: taskForm.startTime,
      endTime: taskForm.endTime,
      remark: taskForm.remark.trim() || undefined
    };
    if (taskForm.id) {
      await updateRequirementTask(selectedRequirement.value.id, taskForm.id, payload);
    } else {
      await createRequirementTask(selectedRequirement.value.id, payload);
    }
    taskDialogVisible.value = false;
    await loadSelectedTasks();
    ElMessage.success('子任务已保存');
  } catch (error) {
    showErrorMessage(error, '子任务保存失败');
  } finally {
    taskSaving.value = false;
  }
}

async function deleteTask() {
  if (!canManageRequirementTask.value) {
    ElMessage.warning('无子任务管理权限');
    return;
  }
  if (!selectedRequirement.value || !taskForm.id) return;
  taskSaving.value = true;
  try {
    await deleteRequirementTask(selectedRequirement.value.id, taskForm.id);
    taskDialogVisible.value = false;
    await loadSelectedTasks();
    ElMessage.success('子任务已删除');
  } catch (error) {
    showErrorMessage(error, '子任务删除失败');
  } finally {
    taskSaving.value = false;
  }
}

async function changeTaskStatus(task: RequirementTask, event: Event) {
  if (!canManageRequirementTask.value) {
    ElMessage.warning('无子任务管理权限');
    return;
  }
  if (!selectedRequirement.value) return;
  const target = event.target as HTMLSelectElement;
  const status = target.value as RequirementTaskStatus;
  if (status === task.status) return;
  try {
    await updateRequirementTask(selectedRequirement.value.id, task.id, {
      taskType: task.taskType,
      roleType: task.roleType,
      name: task.name,
      assigneeId: task.assigneeId,
      status,
      startTime: toInputDateTime(task.startTime),
      endTime: toInputDateTime(task.endTime),
      remark: task.remark,
      sortOrder: task.sortOrder
    });
    await loadSelectedTasks();
    ElMessage.success('子任务状态已更新');
  } catch (error) {
    target.value = task.status;
    showErrorMessage(error, '子任务状态更新失败');
  }
}

function taskStatusOptions(task: RequirementTask) {
  return task.taskType === 'TEST' ? TEST_TASK_STATUS_OPTIONS : DEV_TASK_STATUS_OPTIONS;
}

function taskStatusClass(status: RequirementTaskStatus) {
  if (status.endsWith('_DONE')) return 'done';
  if (status.endsWith('_DOING') || status === 'DEV_INTEGRATING') return 'doing';
  if (status.endsWith('_TODO')) return 'todo';
  return 'scheduled';
}

function openUploadDialog(requirement: Requirement) {
  if (!canUploadCase.value) {
    ElMessage.warning('无上传 XMind 权限');
    return;
  }
  uploadRequirement.value = requirement;
  uploadForm.name = '';
  uploadFile.value = undefined;
  uploadVisible.value = true;
}

function onFileChange(event: Event) {
  uploadFile.value = (event.target as HTMLInputElement).files?.[0];
}

async function submitUpload() {
  if (!canUploadCase.value) {
    ElMessage.warning('无上传 XMind 权限');
    return;
  }
  if (!uploadRequirement.value || !uploadFile.value) {
    ElMessage.warning('请选择 .xmind 文件');
    return;
  }
  uploading.value = true;
  try {
    const suite = await uploadCaseSuite(uploadRequirement.value.id, uploadFile.value, uploadForm.name.trim() || undefined);
    ElMessage.success('上传并解析成功');
    uploadVisible.value = false;
    router.push({ path: '/cases/edit', query: { suiteId: String(suite.id) } });
  } catch (error) {
    showErrorMessage(error, '上传失败');
  } finally {
    uploading.value = false;
  }
}

async function openSuitePicker(requirement: Requirement) {
  uploadRequirement.value = requirement;
  suitePickerVisible.value = true;
  suiteLoading.value = true;
  try {
    caseSuites.value = await listCaseSuites(requirement.id);
  } catch (error) {
    showErrorMessage(error, '用例集加载失败');
  } finally {
    suiteLoading.value = false;
  }
}

function goToEditor(suiteId: number) {
  if (!canEditCase.value) {
    ElMessage.warning('无编辑用例权限');
    return;
  }
  suitePickerVisible.value = false;
  router.push({ path: '/cases/edit', query: { suiteId: String(suiteId) } });
}

async function downloadSuiteFiles(suiteId: number) {
  if (!canExportCase.value) {
    ElMessage.warning('无导出用例权限');
    return;
  }
  try {
    const result = await exportCaseSuite(suiteId);
    ElMessage.success(`导出成功：${result.fileName}`);
    await downloadFile(result.exportedFileId);
  } catch (error) {
    showErrorMessage(error, '导出失败');
  }
}

function normalizedStatus(requirement?: Requirement) {
  return requirement?.status ?? 'COLLECTING';
}

function statusLabel(status?: RequirementStatus) {
  return REQUIREMENT_STATUS_OPTIONS.find((item) => item.value === (status ?? 'COLLECTING'))?.label ?? '需求采集';
}

function dotClass(status: RequirementStatus | '全部') {
  if (status === 'DONE') return 'green';
  if (['DEVELOPING', 'TESTING'].includes(status)) return 'blue';
  if (['UI_ACCEPTING', 'PRODUCT_ACCEPTING'].includes(status)) return 'amber';
  return '';
}

function statusBadgeClass(requirement: Requirement) {
  const status = normalizedStatus(requirement);
  if (status === 'DONE') return 'green';
  if (['DEVELOPING', 'TESTING'].includes(status)) return 'blue';
  if (['UI_ACCEPTING', 'PRODUCT_ACCEPTING'].includes(status)) return 'amber';
  return '';
}

function priorityClass(priority?: RequirementPriority) {
  if (priority === 'P1') return 'red';
  if (priority === 'P2') return 'amber';
  return '';
}

function gatePass(requirement: Requirement) {
  if (requirement.gatePass !== undefined) return requirement.gatePass;
  const status = normalizedStatus(requirement);
  if (status === 'ASSIGNING') return assignmentForm.devAssigneeIds.length > 0 && assignmentForm.testAssigneeIds.length > 0;
  if (status === 'DEVELOPING') return taskGate(requirement, 'DEV', 'DEV_DONE').pass;
  if (status === 'TESTING') return taskGate(requirement, 'TEST', 'TEST_DONE').pass;
  return true;
}

function gateText(requirement: Requirement) {
  if (requirement.gateMessage) return requirement.gateMessage;
  const status = normalizedStatus(requirement);
  if (status === 'ASSIGNING') return gatePass(requirement) ? '人员已分配，可进入需求开发' : '请先分配开发人员和测试人员';
  if (status === 'DEVELOPING') return taskGate(requirement, 'DEV', 'DEV_DONE').text;
  if (status === 'TESTING') return taskGate(requirement, 'TEST', 'TEST_DONE').text;
  return '可进入下一状态';
}

function gateBadgeClass(requirement: Requirement) {
  return gatePass(requirement) || requirement.status === 'DONE' ? 'green' : 'amber';
}

function flowStepClass(index: number) {
  if (index < currentStatusIndex.value) return 'done';
  if (index === currentStatusIndex.value) return 'current';
  return '';
}

function roleLabel(role?: string) {
  return REQUIREMENT_ROLE_OPTIONS.find((item) => item.value === role)?.label ?? role ?? '-';
}

function involvedModuleText(value?: string) {
  return value?.split(',')
    .map((item) => REQUIREMENT_INVOLVED_MODULE_OPTIONS.find((option) => option.value === item)?.label ?? item)
    .join('、') ?? '-';
}

function assigneeText(value?: string) {
  const names = parseAssigneeIds(value).map((id) => users.value.find((user) => user.id === id)).filter(Boolean).map((user) => user?.displayName || user?.username);
  return names.length ? names.join('、') : '-';
}

function taskGate(requirement: Requirement, taskType: 'DEV' | 'TEST', doneStatus: RequirementTaskStatus) {
  const assignedIds = parseAssigneeIds(taskType === 'DEV' ? requirement.devAssigneeIds : requirement.testAssigneeIds);
  const tasks = selectedTasks.value.filter((task) => task.taskType === taskType);
  const taskAssigneeIds = new Set(tasks.map((task) => task.assigneeId).filter(Boolean));
  const missing = assignedIds.filter((id) => !taskAssigneeIds.has(id));
  if (missing.length) {
    return { pass: false, text: taskType === 'DEV' ? '仍有开发人员未创建开发子任务' : '仍有测试人员未创建测试子任务' };
  }
  if (!tasks.length || tasks.some((task) => task.status !== doneStatus)) {
    return { pass: false, text: taskType === 'DEV' ? '仍有开发子任务未完成' : '仍有测试子任务未完成' };
  }
  return { pass: true, text: taskType === 'DEV' ? '开发子任务已全部完成，可进入需求测试' : '测试子任务已全部完成，可进入 UI 验收' };
}

function toInputDateTime(value?: string) {
  if (!value) return '';
  return value.slice(0, 19);
}

function formatDateTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function generateRequirementNo() {
  const date = new Date();
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  const suffix = String(Date.now()).slice(-5);
  return `REQ-${yyyy}${mm}${dd}-${suffix}`;
}

function weekOfYear(date: Date) {
  const firstDay = new Date(date.getFullYear(), 0, 1);
  const dayOffset = (firstDay.getDay() + 6) % 7;
  return Math.ceil(((date.getTime() - firstDay.getTime()) / 86400000 + dayOffset + 1) / 7);
}

function isTaskDone(task: RequirementTask) {
  return [...DEV_TASK_STATUS_OPTIONS, ...TEST_TASK_STATUS_OPTIONS].some((item) => item.value === task.status && item.label.endsWith('完成'));
}

onMounted(loadProjects);
</script>

<style scoped>
.requirement-prototype {
  --bg: #f5f7fa;
  --surface: #ffffff;
  --surface-soft: #f9fafc;
  --line: #dde3ec;
  --line-strong: #c8d1df;
  --text: #172033;
  --muted: #647086;
  --blue: #2563eb;
  --blue-soft: #eaf1ff;
  --green: #16835f;
  --green-soft: #e8f6f0;
  --amber: #b4690e;
  --amber-soft: #fff4df;
  --red: #bf2f45;
  --red-soft: #fff0f2;
  --shadow: 0 10px 30px rgba(23, 32, 51, 0.08);
  display: grid;
  grid-template-columns: minmax(460px, 1fr) 390px;
  min-height: calc(100vh - 104px);
  margin: -24px;
  background: var(--bg);
  color: var(--text);
}

button,
input { font: inherit; }
button { border: 0; cursor: pointer; }
button:disabled { cursor: not-allowed; opacity: 0.48; }

.prototype-sidebar {
  padding: 20px 16px;
  border-right: 1px solid var(--line);
  background: #fbfcfe;
}

.prototype-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 22px;
  font-weight: 800;
}

.brand-icon {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--blue);
  color: #fff;
  font-weight: 900;
}

.main-nav { display: grid; gap: 6px; }
.menu-item,
.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 38px;
  padding: 8px 10px;
  border-radius: 8px;
  color: var(--muted);
  background: transparent;
  text-align: left;
}
.menu-item.active,
.status-item.active {
  color: var(--text);
  background: var(--blue-soft);
  font-weight: 800;
}
.status-label { display: flex; align-items: center; min-width: 0; gap: 8px; }
.dot { width: 8px; height: 8px; flex: 0 0 auto; border-radius: 999px; background: var(--line-strong); }
.dot.blue { background: var(--blue); }
.dot.green { background: var(--green); }
.dot.amber { background: var(--amber); }
.count { min-width: 24px; padding: 2px 7px; border-radius: 999px; background: #edf1f7; color: var(--muted); font-size: 12px; text-align: center; }

.prototype-main { min-width: 0; padding: 22px 22px 28px; }
.prototype-toolbar { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
h1 { margin: 0 0 6px; font-size: 24px; line-height: 1.25; }
h2 { margin: 0; font-size: 18px; line-height: 1.35; }
.meta { color: var(--muted); font-size: 13px; }
.toolbar-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; justify-content: flex-end; }
.project-select { width: 180px; }
.search { position: relative; width: 260px; }
.search input { width: 100%; height: 38px; padding: 0 12px 0 34px; border: 1px solid var(--line); border-radius: 8px; background: var(--surface); color: var(--text); outline: 0; }
.search::before { content: "⌕"; position: absolute; top: 6px; left: 12px; color: var(--muted); font-size: 18px; }

.primary-btn,
.ghost-btn { display: inline-flex; align-items: center; justify-content: center; gap: 8px; min-height: 38px; border-radius: 8px; font-weight: 800; }
.primary-btn { padding: 0 14px; background: var(--blue); color: #fff; }
.ghost-btn { padding: 0 12px; border: 1px solid var(--line); background: var(--surface); color: var(--text); }

.status-filters { display: flex; gap: 8px; margin-bottom: 14px; padding: 8px 4px 12px; overflow-x: auto; }
.status-filters .status-item { flex: 0 0 auto; min-height: 34px; padding: 7px 10px; border: 1px solid var(--line); background: var(--surface); }
.status-filters .status-item.active { border-color: var(--blue); background: var(--blue-soft); }
.status-filters .status-item.can-drop { border-color: var(--green); box-shadow: 0 0 0 2px rgba(22, 131, 95, 0.14); }
.status-filters .status-item.dragover { position: relative; z-index: 5; transform: scale(1.04); border-color: var(--green); background: var(--green-soft); color: var(--green); box-shadow: 0 8px 20px rgba(22, 131, 95, 0.18); }
.drop-hint { min-width: 58px; padding: 2px 7px; border-radius: 999px; background: var(--green); color: #fff; font-size: 12px; text-align: center; }
.board { display: grid; gap: 12px; }
.requirement-card { display: grid; grid-template-columns: 1fr auto; gap: 16px; padding: 16px; border: 1px solid var(--line); border-radius: 8px; background: var(--surface); box-shadow: 0 1px 0 rgba(23, 32, 51, 0.03); text-align: left; cursor: grab; }
.requirement-card:active { cursor: grabbing; }
.requirement-card.active { border-color: var(--blue); box-shadow: var(--shadow); }
.requirement-card.dragging { opacity: 0.28; transform: scale(0.99); }
.card-title { margin: 0 0 8px; font-size: 16px; line-height: 1.35; }
.card-desc { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.55; }
.card-foot { display: flex; align-items: center; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
.badge { display: inline-flex; align-items: center; gap: 6px; min-height: 24px; padding: 3px 8px; border-radius: 999px; background: #eef2f7; color: var(--muted); font-size: 12px; font-weight: 800; }
.badge.blue { background: var(--blue-soft); color: var(--blue); }
.badge.green { background: var(--green-soft); color: var(--green); }
.badge.amber { background: var(--amber-soft); color: var(--amber); }
.badge.red { background: var(--red-soft); color: var(--red); }

.side-panel { min-width: 0; padding: 22px 18px; border-left: 1px solid var(--line); background: #fbfcfe; }
.panel { padding: 18px; border: 1px solid var(--line); border-radius: 8px; background: var(--surface); }
.panel + .panel { margin-top: 14px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.detail-desc { margin: 8px 0 14px; color: var(--muted); font-size: 13px; line-height: 1.6; white-space: pre-wrap; overflow-wrap: anywhere; }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; margin-top: 12px; }
.detail-grid div,
.link-info div { min-width: 0; display: grid; gap: 4px; padding: 9px 10px; border-radius: 8px; background: var(--surface-soft); }
.detail-grid strong,
.link-info strong { color: var(--text); font-size: 12px; }
.detail-grid span,
.link-info span { color: var(--muted); font-size: 13px; line-height: 1.55; white-space: pre-wrap; overflow-wrap: anywhere; }
.link-info { display: grid; gap: 8px; margin-top: 12px; color: var(--muted); font-size: 13px; }
.link-info div { padding: 10px; }
.link-info strong { color: var(--text); }
.flow { display: grid; grid-template-columns: repeat(9, minmax(0, 1fr)); gap: 4px; margin: 16px 0 2px; }
.flow-step { min-height: 34px; padding: 7px 5px; border: 1px solid var(--line); border-radius: 6px; background: var(--surface-soft); color: var(--muted); font-size: 12px; font-weight: 800; text-align: center; overflow-wrap: anywhere; }
.flow-step.done { border-color: #9fd8c3; background: var(--green-soft); color: var(--green); }
.flow-step.current { border-color: var(--blue); background: var(--blue-soft); color: var(--blue); }
.assignment-form { display: grid; gap: 8px; }
.assignment-form label { color: var(--muted); font-size: 13px; font-weight: 800; }
.assignment-tip { padding: 8px 10px; border-radius: 8px; background: var(--blue-soft); color: var(--blue); font-size: 12px; font-weight: 800; }
.task-list { display: grid; gap: 8px; }
.task { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 10px; padding: 10px; border: 1px solid var(--line); border-radius: 8px; background: var(--surface-soft); text-align: left; }
.task-side { display: grid; justify-items: end; gap: 6px; }
.task-status-select { min-height: 30px; padding: 0 28px 0 10px; border: 1px solid var(--line-strong); border-radius: 999px; background: var(--surface); color: var(--text); font-size: 12px; font-weight: 800; outline: 0; cursor: pointer; }
.task-status-select.scheduled { border-color: var(--line-strong); background: #eef2f7; color: var(--muted); }
.task-status-select.todo { border-color: #f3d19e; background: var(--amber-soft); color: var(--amber); }
.task-status-select.doing { border-color: #93c5fd; background: var(--blue-soft); color: var(--blue); }
.task-status-select.done { border-color: #9ed9c0; background: var(--green-soft); color: var(--green); }
.task-status-select:focus { border-color: var(--blue); box-shadow: 0 0 0 2px var(--blue-soft); }
.task-name { min-width: 0; font-size: 13px; font-weight: 800; }
.task-role { display: block; margin-top: 2px; color: var(--muted); font-size: 12px; }
.transition { display: grid; gap: 10px; }
.transition-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.blocker { min-height: 36px; padding: 9px 10px; border-radius: 8px; background: var(--green-soft); color: var(--green); font-size: 13px; font-weight: 800; }
.blocker.warning { background: var(--amber-soft); color: var(--amber); }
.empty { padding: 28px 18px; border: 1px dashed var(--line-strong); border-radius: 8px; background: var(--surface); color: var(--muted); text-align: center; font-size: 13px; }
.case-actions { display: flex; gap: 10px; margin-top: 12px; }
.form-note { min-height: 34px; margin-bottom: 12px; padding: 8px 10px; border-radius: 8px; background: var(--blue-soft); color: var(--blue); font-size: 13px; font-weight: 800; }
.form-row { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }

.drag-preview { position: fixed; top: -1000px; left: -1000px; max-width: 220px; padding: 8px 12px; border-radius: 8px; background: var(--blue); color: #fff; font-size: 13px; font-weight: 800; box-shadow: var(--shadow); pointer-events: none; }

@media (max-width: 1180px) {
  .requirement-prototype { grid-template-columns: minmax(420px, 1fr); }
  .side-panel { border-left: 0; border-top: 1px solid var(--line); }
}
@media (max-width: 760px) {
  .requirement-prototype { display: block; }
  .prototype-sidebar,
  .side-panel { border: 0; }
  .prototype-toolbar { display: grid; }
  .toolbar-actions,
  .search { width: 100%; }
  .flow,
  .detail-grid,
  .form-row { grid-template-columns: 1fr; }
  .requirement-card { grid-template-columns: 1fr; }
}
</style>
