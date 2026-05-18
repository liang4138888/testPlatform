<template>
  <section v-if="!suiteId" class="panel">
    <el-empty description="请从需求管理页选择用例集查看文件" />
  </section>

  <section v-else class="panel">
    <div class="panel-title">
      <div>
        <h2>文件列表</h2>
        <p v-if="suite" class="muted">{{ suite.requirementNo }} {{ suite.requirementName }} · {{ suite.name }}</p>
      </div>
      <div class="actions-inline">
        <el-button @click="goToEditor">编辑用例</el-button>
        <el-button type="primary" :loading="exporting" @click="exportXmind">生成导出文件</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="files" empty-text="暂无可下载文件">
      <el-table-column label="文件类型" width="160">
        <template #default="{ row }">
          {{ row.fileKind === 'ORIGINAL' ? '原始文件' : '平台导出文件' }}
        </template>
      </el-table-column>
      <el-table-column prop="originalName" label="文件名" min-width="240" />
      <el-table-column prop="createdAt" label="生成时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="downloadFile(row.id)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { exportCaseSuite, getCaseSuite, type CaseSuiteDetail } from '../api/caseSuites';
import { fileDownloadUrl, listCaseSuiteFiles, type FileObject } from '../api/files';

const route = useRoute();
const router = useRouter();
const suiteId = computed(() => {
  const value = Number(route.query.suiteId);
  return Number.isFinite(value) && value > 0 ? value : undefined;
});

const loading = ref(false);
const exporting = ref(false);
const suite = ref<CaseSuiteDetail>();
const files = ref<FileObject[]>([]);

async function loadData() {
  if (!suiteId.value) {
    return;
  }
  loading.value = true;
  try {
    suite.value = await getCaseSuite(suiteId.value);
    files.value = await listCaseSuiteFiles(suiteId.value);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '文件列表加载失败');
  } finally {
    loading.value = false;
  }
}

async function exportXmind() {
  if (!suiteId.value) {
    return;
  }
  exporting.value = true;
  try {
    const result = await exportCaseSuite(suiteId.value);
    ElMessage.success(`导出成功：${result.fileName}`);
    await loadData();
    window.open(fileDownloadUrl(result.exportedFileId), '_blank');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导出失败');
  } finally {
    exporting.value = false;
  }
}

function downloadFile(fileId: number) {
  window.open(fileDownloadUrl(fileId), '_blank');
}

function goToEditor() {
  if (!suiteId.value) {
    return;
  }
  router.push({ path: '/cases/edit', query: { suiteId: String(suiteId.value) } });
}

watch(
  () => route.query.suiteId,
  () => {
    if (suiteId.value) {
      loadData();
    } else {
      suite.value = undefined;
      files.value = [];
    }
  }
);

onMounted(() => {
  if (!suiteId.value) {
    router.replace('/requirements');
  } else {
    loadData();
  }
});
</script>
