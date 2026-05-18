<template>
  <section class="page-grid">
    <article class="panel">
      <div class="panel-title">
        <div class="filters">
          <el-select v-model="selectedProjectId" placeholder="选择项目" @change="loadRequirements">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </div>
        <el-button type="primary" :disabled="!selectedProjectId" @click="openCreateDialog">新建需求</el-button>
      </div>

      <el-table v-loading="loading" :data="requirements" empty-text="请选择项目或创建需求">
        <el-table-column prop="requirementNo" label="需求编号" width="150" />
        <el-table-column prop="name" label="需求名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" width="190" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="primary" @click="openUploadDialog(row)">上传 XMind</el-button>
            <el-button link type="primary" @click="openSuitePicker(row)">查看用例</el-button>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <article class="panel">
      <h2>页面说明</h2>
      <p class="muted">需求必须归属到项目。上传 XMind 后会生成用例集，可在用例管理页编辑树形结构。</p>
    </article>
  </section>

  <el-dialog v-model="dialogVisible" title="新建需求" width="520px">
    <el-form label-width="88px">
      <el-form-item label="需求编号" required>
        <el-input v-model="form.requirementNo" maxlength="64" />
      </el-form-item>
      <el-form-item label="需求名称" required>
        <el-input v-model="form.name" maxlength="150" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" maxlength="1000" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="uploadVisible" title="上传 XMind" width="520px">
    <el-form label-width="88px">
      <el-form-item label="需求">
        <el-input :model-value="uploadRequirementLabel" disabled />
      </el-form-item>
      <el-form-item label="用例集名称">
        <el-input v-model="uploadForm.name" maxlength="150" placeholder="默认使用文件名" />
      </el-form-item>
      <el-form-item label="文件" required>
        <input type="file" accept=".xmind" @change="onFileChange" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="uploadVisible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="submitUpload">上传并解析</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="suitePickerVisible" title="选择用例集" width="640px">
    <el-table v-loading="suiteLoading" :data="caseSuites" empty-text="该需求下暂无用例集，请先上传 XMind">
      <el-table-column prop="name" label="用例集" min-width="180" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="updatedAt" label="更新时间" width="190" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="goToEditor(row.id)">编辑用例</el-button>
          <el-button link type="primary" @click="goToFiles(row.id)">文件导出</el-button>
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
import { createRequirement, listRequirements, type Requirement } from '../api/requirements';
import { listCaseSuites, uploadCaseSuite, type CaseSuiteSummary } from '../api/caseSuites';

const router = useRouter();
const projects = ref<Project[]>([]);
const requirements = ref<Requirement[]>([]);
const caseSuites = ref<CaseSuiteSummary[]>([]);
const selectedProjectId = ref<number>();
const loading = ref(false);
const saving = ref(false);
const uploading = ref(false);
const suiteLoading = ref(false);
const dialogVisible = ref(false);
const uploadVisible = ref(false);
const suitePickerVisible = ref(false);
const uploadRequirement = ref<Requirement>();
const uploadFile = ref<File>();
const form = reactive({
  requirementNo: '',
  name: '',
  description: ''
});
const uploadForm = reactive({
  name: ''
});

const uploadRequirementLabel = computed(() => {
  if (!uploadRequirement.value) {
    return '';
  }
  return `${uploadRequirement.value.requirementNo} ${uploadRequirement.value.name}`;
});

async function loadProjects() {
  try {
    projects.value = await listProjects();
    selectedProjectId.value = projects.value[0]?.id;
    if (selectedProjectId.value) {
      await loadRequirements();
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '项目加载失败');
  }
}

async function loadRequirements() {
  if (!selectedProjectId.value) {
    requirements.value = [];
    return;
  }

  loading.value = true;
  try {
    requirements.value = await listRequirements(selectedProjectId.value);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '需求加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  form.requirementNo = '';
  form.name = '';
  form.description = '';
  dialogVisible.value = true;
}

function openUploadDialog(requirement: Requirement) {
  uploadRequirement.value = requirement;
  uploadForm.name = '';
  uploadFile.value = undefined;
  uploadVisible.value = true;
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  uploadFile.value = input.files?.[0];
}

async function submitUpload() {
  if (!uploadRequirement.value) {
    return;
  }
  if (!uploadFile.value) {
    ElMessage.warning('请选择 .xmind 文件');
    return;
  }

  uploading.value = true;
  try {
    const suite = await uploadCaseSuite(
      uploadRequirement.value.id,
      uploadFile.value,
      uploadForm.name.trim() || undefined
    );
    ElMessage.success('上传并解析成功');
    uploadVisible.value = false;
    router.push({ path: '/cases/edit', query: { suiteId: String(suite.id) } });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '上传失败');
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
    ElMessage.error(error instanceof Error ? error.message : '用例集加载失败');
  } finally {
    suiteLoading.value = false;
  }
}

function goToEditor(suiteId: number) {
  suitePickerVisible.value = false;
  router.push({ path: '/cases/edit', query: { suiteId: String(suiteId) } });
}

function goToFiles(suiteId: number) {
  suitePickerVisible.value = false;
  router.push({ path: '/files', query: { suiteId: String(suiteId) } });
}

async function submit() {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目');
    return;
  }

  if (!form.requirementNo.trim() || !form.name.trim()) {
    ElMessage.warning('请输入需求编号和需求名称');
    return;
  }

  saving.value = true;
  try {
    await createRequirement(selectedProjectId.value, {
      requirementNo: form.requirementNo.trim(),
      name: form.name.trim(),
      description: form.description.trim() || undefined
    });
    ElMessage.success('需求创建成功');
    dialogVisible.value = false;
    await loadRequirements();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '需求创建失败');
  } finally {
    saving.value = false;
  }
}

onMounted(loadProjects);
</script>
