<template>
  <section class="panel">
    <div class="panel-title">
      <h2>用例集列表</h2>
    </div>

    <div class="toolbar">
      <el-select v-model="selectedProjectId" clearable placeholder="全部项目" @change="onProjectChange">
        <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
      </el-select>
      <el-select
        v-model="selectedRequirementId"
        clearable
        placeholder="全部需求"
        :disabled="!selectedProjectId"
        @change="loadSuites"
      >
        <el-option
          v-for="requirement in requirements"
          :key="requirement.id"
          :label="`${requirement.requirementNo} ${requirement.name}`"
          :value="requirement.id"
        />
      </el-select>
      <el-button @click="loadSuites">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="suites" empty-text="暂无用例集，请先在需求管理页上传 XMind">
      <el-table-column prop="name" label="用例集" min-width="160" />
      <el-table-column prop="projectName" label="项目" width="140" />
      <el-table-column label="需求" min-width="180">
        <template #default="{ row }">{{ row.requirementNo }} {{ row.requirementName }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SAVED' ? 'success' : 'info'" size="small">
            {{ row.status === 'SAVED' ? '已保存' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="190" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goToEditor(row.id)">编辑用例</el-button>
          <el-button link type="danger" @click="removeSuite(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listProjects, type Project } from '../api/projects';
import { listRequirements, type Requirement } from '../api/requirements';
import { deleteCaseSuite, searchCaseSuites, type CaseSuiteListItem } from '../api/caseSuites';

const router = useRouter();
const projects = ref<Project[]>([]);
const requirements = ref<Requirement[]>([]);
const suites = ref<CaseSuiteListItem[]>([]);
const selectedProjectId = ref<number>();
const selectedRequirementId = ref<number>();
const loading = ref(false);

async function loadProjects() {
  try {
    projects.value = await listProjects();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '项目加载失败');
  }
}

async function onProjectChange() {
  selectedRequirementId.value = undefined;
  requirements.value = [];
  if (selectedProjectId.value) {
    try {
      requirements.value = await listRequirements(selectedProjectId.value);
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '需求加载失败');
    }
  }
  await loadSuites();
}

async function loadSuites() {
  loading.value = true;
  try {
    suites.value = await searchCaseSuites({
      projectId: selectedProjectId.value,
      requirementId: selectedRequirementId.value
    });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用例集加载失败');
  } finally {
    loading.value = false;
  }
}

function goToEditor(suiteId: number) {
  router.push({ path: '/cases/edit', query: { suiteId: String(suiteId) } });
}

async function removeSuite(suite: CaseSuiteListItem) {
  try {
    await ElMessageBox.confirm(`确定删除用例集「${suite.name}」吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await deleteCaseSuite(suite.id);
    ElMessage.success('用例集已删除');
    await loadSuites();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

onMounted(async () => {
  await loadProjects();
  await loadSuites();
});
</script>
