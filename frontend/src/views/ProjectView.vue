<template>
  <section class="page-grid">
    <article class="panel">
      <div class="panel-title">
        <h2>项目列表</h2>
        <el-button type="primary" @click="openCreateDialog">新建项目</el-button>
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
        title="一期暂不做权限，所有项目对进入平台的用户可见。"
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
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createProject, listProjects, type Project } from '../api/projects';

const projects = ref<Project[]>([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const form = reactive({
  name: '',
  description: ''
});

async function loadProjects() {
  loading.value = true;
  try {
    projects.value = await listProjects();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '项目加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
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
    ElMessage.error(error instanceof Error ? error.message : '项目创建失败');
  } finally {
    saving.value = false;
  }
}

onMounted(loadProjects);
</script>
