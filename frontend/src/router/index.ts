import { createRouter, createWebHistory } from 'vue-router';
import ProjectView from '../views/ProjectView.vue';
import RequirementView from '../views/RequirementView.vue';
import CaseSuiteListView from '../views/CaseSuiteListView.vue';
import CaseEditorView from '../views/CaseEditorView.vue';
import LoginView from '../views/LoginView.vue';
import BugView from '../views/BugView.vue';
import UserManagementView from '../views/UserManagementView.vue';
import RolePermissionView from '../views/RolePermissionView.vue';
import { cachedUser, currentUser } from '../api/auth';
import { getToken } from '../api/http';

const routePermissions: Record<string, string> = {
  '/projects': 'MENU_PROJECT',
  '/requirements': 'MENU_REQUIREMENT',
  '/cases': 'MENU_CASE',
  '/cases/edit': 'MENU_CASE',
  '/bugs': 'MENU_BUG',
  '/users': 'MENU_USER',
  '/roles': 'MENU_ROLE'
};

export const menuItems = [
  { path: '/projects', label: '项目管理', permission: 'MENU_PROJECT' },
  { path: '/requirements', label: '需求管理', permission: 'MENU_REQUIREMENT' },
  { path: '/cases', label: '用例管理', permission: 'MENU_CASE' },
  { path: '/bugs', label: '缺陷管理', permission: 'MENU_BUG' },
  { path: '/users', label: '用户管理', permission: 'MENU_USER' },
  { path: '/roles', label: '权限管理', permission: 'MENU_ROLE' }
];

function hasPermission(permission?: string) {
  if (!permission) {
    return true;
  }
  const user = cachedUser();
  if (user?.roles?.includes('ADMIN')) {
    return true;
  }
  return user?.permissions?.includes(permission) ?? false;
}

function firstAllowedPath() {
  return menuItems.find((item) => hasPermission(item.permission))?.path ?? '/login';
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/projects'
    },
    {
      path: '/login',
      component: LoginView,
      meta: {
        title: '登录',
        description: '登录后进入测试平台。',
        public: true
      }
    },
    {
      path: '/projects',
      component: ProjectView,
      meta: {
        title: '项目管理',
        description: '维护测试平台中的项目，需求和用例集都归属到项目。',
        permission: 'MENU_PROJECT'
      }
    },
    {
      path: '/requirements',
      component: RequirementView,
      meta: {
        title: '需求管理',
        description: '按项目维护需求，并进入 XMind 用例上传和编辑流程。',
        permission: 'MENU_REQUIREMENT'
      }
    },
    {
      path: '/cases',
      component: CaseSuiteListView,
      meta: {
        title: '用例管理',
        description: '查看全部用例集，进入编辑或导出。',
        permission: 'MENU_CASE'
      }
    },
    {
      path: '/cases/edit',
      component: CaseEditorView,
      meta: {
        title: '用例编辑',
        description: '编辑用例树结构，支持拖拽排序与层级调整。',
        permission: 'MENU_CASE'
      }
    },
    {
      path: '/bugs',
      component: BugView,
      meta: {
        title: '缺陷管理',
        description: '维护 Bug、图片、评论、历史和状态流转。',
        permission: 'MENU_BUG'
      }
    },
    {
      path: '/users',
      component: UserManagementView,
      meta: {
        title: '用户管理',
        description: '维护用户、角色和可指派人员。',
        permission: 'MENU_USER'
      }
    },
    {
      path: '/roles',
      component: RolePermissionView,
      meta: {
        title: '权限管理',
        description: '配置角色可访问菜单、操作权限和数据范围。',
        permission: 'MENU_ROLE'
      }
    }
  ]
});

router.beforeEach(async (to) => {
  if (to.meta.public) {
    return true;
  }
  if (!getToken()) {
    return '/login';
  }
  if (!cachedUser()) {
    await currentUser();
  }
  const permission = String(to.meta.permission ?? routePermissions[to.path] ?? '');
  if (permission && !hasPermission(permission)) {
    return firstAllowedPath();
  }
  return true;
});

export default router;
