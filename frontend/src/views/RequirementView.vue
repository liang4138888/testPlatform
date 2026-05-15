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
        <el-table-column label="操作" width="220">
          <template #default>
            <el-button link type="primary">上传 XMind</el-button>
            <el-button link type="primary">查看用例</el-button>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <article class="panel">
      <h2>页面说明</h2>
      <p class="muted">需求必须归属到项目。后续上传 XMind 时会选择某个需求，用例集也挂在该需求下。</p>
      <el-alert
        title="当前已接入项目和需求接口；上传 XMind 和用例树编辑会在下一步实现。"
        type="info"
        :closable="false"
        show-icon
      />
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { listProjects, type Project } from '../api/projects';
import { createRequirement, listRequirements, type Requirement } from '../api/requirements';

const projects = ref<Project[]>([]);
const requirements = ref<Requirement[]>([]);
const selectedProjectId = ref<number>();
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const form = reactive({
  requirementNo: '',
  name: '',
  description: ''
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
