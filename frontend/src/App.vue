<template>
  <el-container class="app-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">测试平台</div>
      <el-menu :default-active="$route.path" router class="nav">
        <el-menu-item index="/projects">项目管理</el-menu-item>
        <el-menu-item index="/requirements">需求管理</el-menu-item>
        <el-menu-item index="/cases">用例管理</el-menu-item>
        <el-menu-item index="/files">文件导出</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div>
          <h1>{{ routeTitle }}</h1>
          <p>{{ routeDescription }}</p>
        </div>
      </el-header>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const routeTitle = computed(() => String(route.meta.title ?? '测试平台'));
const routeDescription = computed(() => String(route.meta.description ?? 'XMind 用例管理一期'));
</script>
