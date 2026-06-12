<template>
  <el-container class="app-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">测试平台</div>
      <el-menu :default-active="activeMenu" router class="nav">
        <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div>
          <h1>{{ routeTitle }}</h1>
          <p>{{ routeDescription }}</p>
        </div>
        <div class="header-actions" v-if="route.path !== '/login'">
          <el-popover placement="bottom-end" width="360" trigger="click" @show="loadNotifications">
            <template #reference>
              <el-badge :value="noticeCount" :hidden="noticeCount === 0">
                <el-button>通知</el-button>
              </el-badge>
            </template>
            <div class="notice-panel">
              <div class="notice-head">
                <strong>站内通知</strong>
                <el-button link type="primary" @click="readAll">全部已读</el-button>
              </div>
              <div v-for="item in notices" :key="item.id" class="notice-item" @click="openNotice(item)">
                <span :class="['notice-dot', { read: item.readStatus === 'READ' }]" />
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.content }}</p>
                </div>
              </div>
              <el-empty v-if="!notices.length" description="暂无通知" :image-size="64" />
            </div>
          </el-popover>
          <span class="user-avatar">{{ userAvatar }}</span>
          <el-button @click="doLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { cachedUser, logout } from './api/auth';
import { getToken } from './api/http';
import { listNotifications, markAllNotificationsRead, markNotificationRead, unreadCount, type NotificationItem } from './api/notifications';
import { menuItems } from './router';

const route = useRoute();
const router = useRouter();
const notices = ref<NotificationItem[]>([]);
const noticeCount = ref(0);
const noticeLoading = ref(false);
const visibleMenus = computed(() => {
  route.path;
  const user = cachedUser();
  if (user?.roles?.includes('ADMIN')) {
    return menuItems;
  }
  const permissions = user?.permissions ?? [];
  return menuItems.filter((item) => permissions.includes(item.permission));
});

const userAvatar = computed(() => {
  const user = cachedUser();
  return user?.avatar || user?.displayName?.slice(0, 1) || user?.username?.slice(0, 1).toUpperCase() || '未';
});

const routeTitle = computed(() => String(route.meta.title ?? '测试平台'));
const routeDescription = computed(() => String(route.meta.description ?? 'XMind 用例管理一期'));
const activeMenu = computed(() => {
  if (route.path.startsWith('/cases')) {
    return '/cases';
  }
  if (route.path.startsWith('/bugs')) {
    return '/bugs';
  }
  if (route.path.startsWith('/users')) {
    return '/users';
  }
  if (route.path.startsWith('/roles')) {
    return '/roles';
  }
  return route.path;
});

async function loadNoticeCount() {
  if (route.path === '/login' || !getToken()) {
    noticeCount.value = 0;
    return;
  }
  noticeCount.value = await unreadCount();
}

async function loadNotifications() {
  if (noticeLoading.value || route.path === '/login' || !getToken()) {
    return;
  }
  noticeLoading.value = true;
  try {
    notices.value = await listNotifications();
    noticeCount.value = notices.value.filter((item) => item.readStatus === 'UNREAD').length;
  } finally {
    noticeLoading.value = false;
  }
}

async function openNotice(item: NotificationItem) {
  await markNotificationRead(item.id);
  await loadNotifications();
  await loadNoticeCount();
  if (item.bizType === 'BUG') {
    router.push('/bugs');
  }
}

async function readAll() {
  await markAllNotificationsRead();
  await loadNotifications();
  await loadNoticeCount();
}

onMounted(loadNoticeCount);

watch(
  () => route.path,
  () => {
    loadNoticeCount();
  }
);

async function doLogout() {
  await logout();
  router.push('/login');
}
</script>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.user-avatar { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; color: #fff; background: #409eff; border-radius: 50%; font-weight: 600; }
.notice-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.notice-item { display: grid; grid-template-columns: auto minmax(0, 1fr); gap: 8px; padding: 10px 0; border-bottom: 1px solid #ebeef5; cursor: pointer; }
.notice-item p { margin: 4px 0 0; color: #606266; }
.notice-dot { width: 8px; height: 8px; margin-top: 7px; background: #f56c6c; border-radius: 50%; }
.notice-dot.read { background: #c0c4cc; }
</style>
