<template>
  <section class="panel">
    <div class="panel-title">
      <h2>文件列表</h2>
      <el-button type="primary">生成导出文件</el-button>
    </div>
    <el-table :data="files" empty-text="暂无文件">
      <el-table-column prop="type" label="文件类型" width="160" />
      <el-table-column prop="name" label="文件名" min-width="240" />
      <el-table-column prop="createdAt" label="生成时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default>
          <el-button link type="primary">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
const files = [
  {
    type: '原始文件',
    name: 'REQ-001-login.xmind',
    createdAt: '2026-05-15 18:00:00'
  }
];
</script>
