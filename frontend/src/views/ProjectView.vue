<template>
  <section class="page-grid">
    <article class="panel">
      <div class="panel-title">
        <h2>项目列表</h2>
        <el-button v-if="canCreateProject" type="primary" @click="openCreateDialog">新建项目</el-button>
      </div>

      <el-table v-loading="loading" :data="projects" empty-text="暂无项目">
        <el-table-column prop="name" label="项目名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" width="190" />
      </el-table>
    </article>

    <article class="panel">
      <h2>页面说明</h2>
      <p class="muted">项目是需求和用例集的上层归属。创建项目后，可到需求管理页面为该项目创建需求。</p>
      <el-alert
        title="项目数据按角色数据权限展示，操作按钮按按钮权限展示。"
        type="info"
        :closable="false"
        show-icon
      />
    </article>
  </section>

  <el-dialog v-model="dialogVisible" title="新建项目" width="480px">
    <el-form label-width="88px">
      <el-form-item label="项目名称" required>
        <el-input v-model="form.name" maxlength="100" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createProject, listProjects, type Project } from '../api/projects';
import { showErrorMessage } from '../api/http';
import { hasPermission } from '../utils/permissions';

const projects = ref<Project[]>([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const form = reactive({
  name: '',
  description: ''
});
const canCreateProject = computed(() => hasPermission('PROJECT_CREATE'));

async function loadProjects() {
  loading.value = true;
  try {
    projects.value = await listProjects();
  } catch (error) {
    showErrorMessage(error, '项目加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  if (!canCreateProject.value) {
    ElMessage.warning('没有新建项目权限');
    return;
  }
  form.name = '';
  form.description = '';
  dialogVisible.value = true;
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入项目名称');
    return;
  }

  saving.value = true;
  try {
    await createProject({
      name: form.name.trim(),
      description: form.description.trim() || undefined
    });
    ElMessage.success('项目创建成功');
    dialogVisible.value = false;
    await loadProjects();
  } catch (error) {
    showErrorMessage(error, '项目创建失败');
  } finally {
    saving.value = false;
  }
}

onMounted(loadProjects);
</script>
