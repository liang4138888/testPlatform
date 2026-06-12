<template>
  <section class="bug-page">
    <div class="metric-grid">
      <div class="metric">
        <span>待修复</span>
        <strong>{{ statusCount('待修复') }}</strong>
        <el-tag type="warning">待修复</el-tag>
      </div>
      <div class="metric">
        <span>已修复</span>
        <strong>{{ statusCount('已修复') }}</strong>
        <el-tag>已修复</el-tag>
      </div>
      <div class="metric">
        <span>修复完成</span>
        <strong>{{ statusCount('修复完成') }}</strong>
        <el-tag type="success">修复完成</el-tag>
      </div>
      <div class="metric">
        <span>严重问题</span>
        <strong>{{ bugs.filter((bug) => bug.severity === '严重').length }}</strong>
        <el-tag type="danger">严重</el-tag>
      </div>
    </div>

    <div class="content-grid">
      <el-card class="main-card">
        <template #header>
          <div class="card-header">
            <div>
              <h2>缺陷列表</h2>
              <p>按项目、需求、状态、指派人和关键词筛选 Bug。</p>
            </div>
            <div class="actions">
              <el-button @click="loadBugs">刷新</el-button>
              <el-button type="primary" @click="openCreate">新建 Bug</el-button>
            </div>
          </div>
        </template>

        <div class="toolbar">
          <el-select v-model="filters.projectId" clearable placeholder="项目：全部" @change="onFilterProjectChange">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="String(project.id)" />
          </el-select>
          <el-select v-model="filters.requirementId" clearable filterable placeholder="需求：全部">
            <el-option v-for="requirement in filterRequirements" :key="requirement.id" :label="requirementLabel(requirement)" :value="String(requirement.id)" />
          </el-select>
          <el-select v-model="filters.status" clearable placeholder="状态：全部">
            <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
          </el-select>
          <el-select v-model="filters.assigneeId" clearable placeholder="指派人：全部">
            <el-option v-for="user in users" :key="user.id" :label="user.displayName" :value="String(user.id)" />
          </el-select>
          <el-input v-model="filters.keyword" placeholder="搜索 Bug 编号 / 标题" clearable />
          <el-button type="primary" @click="loadBugs">查询</el-button>
        </div>

        <el-table :data="bugs" v-loading="loading" border class="bug-table">
          <el-table-column prop="bugNo" label="编号" width="110" />
          <el-table-column prop="title" label="标题" min-width="210" show-overflow-tooltip />
          <el-table-column label="需求" width="170" show-overflow-tooltip>
            <template #default="{ row }">{{ requirementName(row.requirementId) }}</template>
          </el-table-column>
          <el-table-column label="严重级别" width="105">
            <template #default="{ row }"><el-tag :type="severityType(row.severity)">{{ row.severity }}</el-tag></template>
          </el-table-column>
          <el-table-column label="优先级" width="90">
            <template #default="{ row }"><el-tag :type="priorityType(row.priority)">{{ row.priority }}</el-tag></template>
          </el-table-column>
          <el-table-column label="状态" width="150">
            <template #default="{ row }">
              <el-select :model-value="row.status" :class="['status-select', statusClass(row.status)]" placeholder="修改状态" :disabled="!canChangeStatus" @change="(status: string) => changeStatus(row, status)">
                <el-option v-if="canSubmitTestStatus" label="待修复" value="待修复">
                  <el-tag type="warning">待修复</el-tag>
                </el-option>
                <el-option v-if="canSubmitFixed" label="已修复" value="已修复">
                  <el-tag type="primary">已修复</el-tag>
                </el-option>
                <el-option v-if="canSubmitTestStatus" label="修复完成" value="修复完成">
                  <el-tag type="success">修复完成</el-tag>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="报告人" width="110">
            <template #default="{ row }">{{ userName(row.reporterId) }}</template>
          </el-table-column>
          <el-table-column label="指派人" width="110">
            <template #default="{ row }">{{ userName(row.assigneeId) }}</template>
          </el-table-column>
          <el-table-column prop="attachmentCount" label="图片" width="75" />
          <el-table-column prop="updatedAt" label="更新时间" width="180" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="side-card">
        <template #header><h2>协作动态</h2></template>
        <el-timeline>
          <el-timeline-item v-for="history in recentHistories" :key="history.key" :timestamp="history.time">
            <strong>{{ history.title }}</strong>
            <p>{{ history.desc }}</p>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!recentHistories.length" description="暂无协作动态" :image-size="80" />
      </el-card>
    </div>

    <el-dialog v-model="createVisible" title="新建 Bug" width="860px" class="bug-dialog">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="项目">
            <el-select v-model="createProjectId" clearable placeholder="选择项目后筛选需求" @change="createForm.requirementId = undefined">
              <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="需求" required>
            <el-select v-model="createForm.requirementId" filterable placeholder="搜索需求编号或名称">
              <el-option v-for="requirement in createRequirements" :key="requirement.id" :label="requirementLabel(requirement)" :value="requirement.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="用例集">
            <el-input disabled placeholder="后续选择需求下用例集（可选）" />
          </el-form-item>
          <el-form-item label="指派人" required>
            <el-select v-model="createForm.assigneeId" filterable placeholder="请选择指派人">
              <el-option v-for="user in users" :key="user.id" :label="user.displayName" :value="user.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="严重级别">
            <el-select v-model="createForm.severity">
              <el-option v-for="item in severities" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="createForm.priority">
              <el-option v-for="item in priorities" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题" required class="full">
            <el-input v-model="createForm.title" maxlength="200" />
          </el-form-item>
          <el-form-item label="描述" class="full">
            <el-input v-model="createForm.description" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="问题截图" class="full">
            <el-upload class="create-image-upload" drag multiple :auto-upload="false" :on-change="addCreateImage" :on-remove="removeCreateImage" accept="image/png,image/jpeg,image/gif,image/webp">
              <div class="upload-zone-content">
                <strong>点击或拖拽图片到这里上传</strong>
                <p>支持 png、jpg、jpeg、gif、webp，单张 10MB。</p>
              </div>
            </el-upload>
            <p class="hint">提交后由后端生成编号，例如 BUG-0001，初始状态为待修复，并通知指派人。</p>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" width="1120px" class="bug-detail-dialog">
      <template #header>
        <div v-if="detail" class="detail-title">
          <h2>{{ detail.bugNo }} {{ detail.title }}</h2>
          <p>{{ requirementName(detail.requirementId) }} · {{ projectName(detail.projectId) }}</p>
        </div>
      </template>

      <div v-if="detail" class="detail-shell">
        <div class="detail-main">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="base">
              <el-form label-position="top">
                <div class="form-grid">
                  <el-form-item label="当前状态">
                    <div class="readonly-field"><el-tag :type="statusType(detail.status)">{{ detail.status }}</el-tag></div>
                    <p class="hint">待修复和修复完成由测试提交，已修复可由开发或产品提交；列表可直接修改状态。</p>
                  </el-form-item>
                  <el-form-item label="指派人">
                    <el-select v-model="detail.assigneeId" clearable filterable>
                      <el-option v-for="user in users" :key="user.id" :label="user.displayName" :value="user.id" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="严重级别">
                    <el-select v-model="detail.severity"><el-option v-for="item in severities" :key="item" :label="item" :value="item" /></el-select>
                  </el-form-item>
                  <el-form-item label="优先级">
                    <el-select v-model="detail.priority"><el-option v-for="item in priorities" :key="item" :label="item" :value="item" /></el-select>
                  </el-form-item>
                  <el-form-item label="标题" class="full"><el-input v-model="detail.title" /></el-form-item>
                  <el-form-item label="描述" class="full"><el-input v-model="detail.description" type="textarea" :rows="4" /></el-form-item>
                </div>
              </el-form>
              <div class="actions detail-actions">
                <el-button type="primary" @click="saveDetail">保存修改</el-button>
                <el-button v-if="canSubmitFixed" type="primary" plain @click="setDetailStatus('已修复')">提交已修复</el-button>
                <el-button v-if="canSubmitTestStatus" @click="setDetailStatus('修复完成')">测试确认修复完成</el-button>
                <el-button v-if="canSubmitTestStatus" type="danger" plain @click="setDetailStatus('待修复')">重新打开为待修复</el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="图片附件" name="images">
              <div class="readonly-upload-note">
                查看 Bug 时仅展示已上传图片；如需补充截图，请在新建 Bug 时上传。
              </div>
              <div class="image-grid">
                <div v-for="item in detail.attachments" :key="item.id" class="image-card">
                  <img :src="filePreviewUrl(item.fileId)" alt="Bug 图片" />
                  <div class="image-name">图片附件 #{{ item.id }}</div>
                  <div class="image-meta"><span>{{ item.createdAt }}</span><a :href="filePreviewUrl(item.fileId)" target="_blank">预览</a></div>
                </div>
              </div>
              <el-empty v-if="!detail.attachments.length" description="暂无图片附件" :image-size="80" />
            </el-tab-pane>

            <el-tab-pane label="评论" name="comments">
              <div v-for="comment in detail.comments" :key="comment.id" class="comment">
                <div class="comment-head"><strong>{{ userName(comment.createdBy) }}</strong><span>{{ comment.createdAt }}</span></div>
                <p>{{ comment.content }}</p>
              </div>
              <el-empty v-if="!detail.comments.length" description="暂无评论" :image-size="80" />
              <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="输入评论，评论后会通知报告人和指派人" />
              <el-button type="primary" class="comment-button" @click="submitComment">发表评论</el-button>
            </el-tab-pane>
          </el-tabs>
        </div>

        <aside class="history-panel">
          <h3>操作历史</h3>
          <el-timeline>
            <el-timeline-item v-for="history in detail.histories" :key="history.id" :timestamp="history.createdAt">
              <strong>{{ history.actionType }}</strong>
              <p>{{ history.oldValue || '-' }} → {{ history.newValue || '-' }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="!detail.histories.length" description="暂无操作历史" :image-size="80" />
        </aside>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, type UploadFile, type UploadFiles } from 'element-plus';
import { cachedUser } from '../api/auth';
import { addBugComment, createBug, getBug, listBugs, updateBug, uploadBugImage, type BugDetail, type BugListItem } from '../api/bugs';
import { filePreviewUrl } from '../api/files';
import { listProjects, type Project } from '../api/projects';
import { listRequirements, type Requirement } from '../api/requirements';
import { listAssignableUsers, type AssignableUser } from '../api/users';

const statuses = ['待修复', '已修复', '修复完成'];
const severities = ['严重', '高', '中', '低'];
const priorities = ['高', '中', '低'];

const loading = ref(false);
const bugs = ref<BugListItem[]>([]);
const users = ref<AssignableUser[]>([]);
const projects = ref<Project[]>([]);
const requirements = ref<Requirement[]>([]);
const detail = ref<BugDetail>();
const detailVisible = ref(false);
const createVisible = ref(false);
const commentContent = ref('');
const activeTab = ref('base');
const createProjectId = ref<number>();
const createImages = ref<File[]>([]);
const filters = reactive({ projectId: '', requirementId: '', status: '', assigneeId: '', keyword: '' });
const createForm = reactive({ requirementId: undefined as number | undefined, title: '', description: '', severity: '中', priority: '中', assigneeId: undefined as number | undefined });

const currentPermissions = computed(() => cachedUser()?.permissions ?? []);
const canSubmitFixed = computed(() => currentPermissions.value.includes('BUG_STATUS_FIXED'));
const canSubmitTestStatus = computed(() => currentPermissions.value.includes('BUG_STATUS_TEST'));
const canChangeStatus = computed(() => canSubmitFixed.value || canSubmitTestStatus.value);

const filterRequirements = computed(() => {
  if (!filters.projectId) return requirements.value;
  return requirements.value.filter((requirement) => requirement.projectId === Number(filters.projectId));
});

const createRequirements = computed(() => {
  if (!createProjectId.value) return requirements.value;
  return requirements.value.filter((requirement) => requirement.projectId === createProjectId.value);
});

const recentHistories = computed(() => bugs.value.slice(0, 5).map((bug) => ({
  key: `${bug.id}-${bug.updatedAt}`,
  title: `${bug.bugNo} ${bug.status}`,
  desc: `${bug.title} · 指派给 ${userName(bug.assigneeId)}`,
  time: bug.updatedAt
})));

async function loadBugs() {
  loading.value = true;
  try {
    bugs.value = await listBugs(filters);
  } finally {
    loading.value = false;
  }
}

async function loadBaseData() {
  const [projectList, userList] = await Promise.all([listProjects(), listAssignableUsers()]);
  projects.value = projectList;
  users.value = userList;
  const results = await Promise.all(projectList.map((project) => listRequirements(project.id)));
  requirements.value = results.flat();
}

function openCreate() {
  resetCreateForm();
  createVisible.value = true;
}

function resetCreateForm() {
  createProjectId.value = undefined;
  createForm.requirementId = undefined;
  createForm.title = '';
  createForm.description = '';
  createForm.severity = '中';
  createForm.priority = '中';
  createForm.assigneeId = undefined;
  createImages.value = [];
}

async function openDetail(id: number) {
  detail.value = await getBug(id);
  activeTab.value = 'base';
  detailVisible.value = true;
}

function onFilterProjectChange() {
  filters.requirementId = '';
}

function currentCreateImages(uploadFiles: UploadFiles) {
  return uploadFiles.reduce<File[]>((files, item) => {
    if (item.raw) {
      files.push(item.raw);
    }
    return files;
  }, []);
}

function addCreateImage(_uploadFile: UploadFile, uploadFiles: UploadFiles) {
  createImages.value = currentCreateImages(uploadFiles);
}

function removeCreateImage(_uploadFile: UploadFile, uploadFiles: UploadFiles) {
  createImages.value = currentCreateImages(uploadFiles);
}

async function submitCreate() {
  const requirementId = createForm.requirementId;
  if (!requirementId || requirementId <= 0) {
    ElMessage.warning('请选择需求');
    return;
  }
  if (!createForm.title.trim()) {
    ElMessage.warning('请填写 Bug 标题');
    return;
  }
  if (!createForm.assigneeId) {
    ElMessage.warning('请选择指派人');
    return;
  }
  const created = await createBug(requirementId, createForm);
  for (const file of createImages.value) {
    await uploadBugImage(created.id, file);
  }
  createVisible.value = false;
  ElMessage.success('创建成功');
  resetCreateForm();
  await loadBugs();
}

function requirementLabel(requirement: Requirement) {
  return `${requirement.requirementNo} ${requirement.name}`;
}

function requirementName(requirementId: number) {
  const requirement = requirements.value.find((item) => item.id === requirementId);
  return requirement ? `${requirement.requirementNo} ${requirement.name}` : `需求 #${requirementId}`;
}

function projectName(projectId: number) {
  return projects.value.find((project) => project.id === projectId)?.name ?? `项目 #${projectId}`;
}

function userName(userId?: number) {
  return users.value.find((user) => user.id === userId)?.displayName ?? '-';
}

function statusCount(status: string) {
  return bugs.value.filter((bug) => bug.status === status).length;
}

function statusType(status: string) {
  if (status === '修复完成') return 'success';
  if (status === '已修复') return 'primary';
  return 'warning';
}

function statusClass(status: string) {
  if (status === '修复完成') return 'status-done';
  if (status === '已修复') return 'status-fixed';
  return 'status-open';
}

function severityType(severity: string) {
  if (severity === '严重') return 'danger';
  if (severity === '高') return 'warning';
  if (severity === '低') return 'info';
  return 'primary';
}

function priorityType(priority: string) {
  if (priority === '高') return 'danger';
  if (priority === '低') return 'info';
  return 'warning';
}

async function saveDetail() {
  if (!detail.value) return;
  try {
    detail.value = await updateBug(detail.value.id, {
      title: detail.value.title,
      description: detail.value.description,
      severity: detail.value.severity,
      priority: detail.value.priority,
      assigneeId: detail.value.assigneeId,
      caseSuiteId: detail.value.caseSuiteId
    });
    ElMessage.success('保存成功');
    await loadBugs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  }
}

async function setDetailStatus(status: string) {
  if (!detail.value) return;
  try {
    detail.value = await updateBug(detail.value.id, { status });
    ElMessage.success('状态已更新');
    await loadBugs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function changeStatus(row: BugListItem, status: string) {
  try {
    await updateBug(row.id, { status });
    ElMessage.success('状态已更新');
    await loadBugs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
    await loadBugs();
  }
}

async function submitComment() {
  if (!detail.value || !commentContent.value.trim()) return;
  await addBugComment(detail.value.id, commentContent.value.trim());
  commentContent.value = '';
  detail.value = await getBug(detail.value.id);
  ElMessage.success('评论成功');
}

onMounted(async () => {
  await loadBaseData();
  await loadBugs();
});
</script>

<style scoped>
.bug-page { display: grid; gap: 16px; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.metric { padding: 16px; background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04); }
.metric span { color: #6b7280; }
.metric strong { display: block; margin: 6px 0; color: #1f2937; font-size: 26px; }
.content-grid { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.card-header h2, .side-card h2 { margin: 0; font-size: 18px; }
.card-header p { margin: 6px 0 0; color: #6b7280; }
.actions { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.toolbar { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr 1.2fr auto; gap: 10px; margin-bottom: 16px; }
.bug-table :deep(.el-table__header th) { background: #f9fafb; color: #4b5563; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.full { grid-column: 1 / -1; }
.hint { margin: 8px 0 0; color: #6b7280; font-size: 13px; }
.create-image-upload { width: 100%; }
.create-image-upload :deep(.el-upload) { width: 100%; }
.create-image-upload :deep(.el-upload-dragger) { display: grid; min-height: 150px; place-items: center; padding: 18px; background: #eff6ff; border: 2px dashed #93c5fd; border-radius: 12px; }
.create-image-upload :deep(.el-upload-list) { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-top: 12px; }
.create-image-upload :deep(.el-upload-list__item) { margin: 0; padding: 10px 12px; border: 1px solid #e5e7eb; border-radius: 10px; background: #f8fafc; }
.upload-zone-content { display: grid; min-height: 120px; place-items: center; color: #2563eb; text-align: center; }
.upload-zone-content strong { display: block; margin-bottom: 8px; font-size: 16px; }
.upload-zone-content p { margin: 0; color: #64748b; }
.readonly-upload-note { padding: 14px 16px; margin-bottom: 14px; color: #64748b; background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 10px; }
.detail-title h2 { margin: 0; font-size: 20px; }
.detail-title p { margin: 6px 0 0; color: #6b7280; }
.detail-shell { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 16px; }
.detail-main { min-width: 0; }
.readonly-field { display: flex; min-height: 40px; align-items: center; padding: 8px 10px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; }
.detail-actions { margin-top: 16px; }
.image-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-top: 14px; }
.image-card { overflow: hidden; background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 10px; }
.image-card img { display: block; width: 100%; height: 116px; object-fit: cover; background: #eef2ff; }
.image-name { padding: 8px 10px; color: #4b5563; font-size: 13px; }
.image-meta { display: flex; justify-content: space-between; gap: 8px; padding: 0 10px 9px; color: #94a3b8; font-size: 12px; }
.comment { padding: 12px; margin-bottom: 10px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 10px; }
.comment-head { display: flex; justify-content: space-between; gap: 10px; margin-bottom: 8px; color: #6b7280; }
.comment p { margin: 0; }
.comment-button { margin-top: 10px; }
.history-panel { padding: 0 4px; }
.history-panel h3 { margin: 0 0 16px; }
.side-card :deep(.el-timeline), .history-panel :deep(.el-timeline) { padding-left: 4px; }
.side-card p, .history-panel p { margin: 4px 0 0; color: #6b7280; }
.status-select { width: 100%; }
.status-select :deep(.el-select__wrapper) { font-weight: 600; }
.status-open :deep(.el-select__wrapper) { background: #fdf6ec; box-shadow: 0 0 0 1px #f3d19e inset; }
.status-open :deep(.el-select__selected-item) { color: #b88230; }
.status-fixed :deep(.el-select__wrapper) { background: #ecf5ff; box-shadow: 0 0 0 1px #a0cfff inset; }
.status-fixed :deep(.el-select__selected-item) { color: #337ecc; }
.status-done :deep(.el-select__wrapper) { background: #f0f9eb; box-shadow: 0 0 0 1px #b3e19d inset; }
.status-done :deep(.el-select__selected-item) { color: #529b2e; }
@media (max-width: 1180px) {
  .metric-grid, .content-grid, .toolbar, .detail-shell, .form-grid, .image-grid { grid-template-columns: 1fr; }
}
</style>
