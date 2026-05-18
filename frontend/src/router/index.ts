import { createRouter, createWebHistory } from 'vue-router';
import ProjectView from '../views/ProjectView.vue';
import RequirementView from '../views/RequirementView.vue';
import CaseSuiteListView from '../views/CaseSuiteListView.vue';
import CaseEditorView from '../views/CaseEditorView.vue';
import FileView from '../views/FileView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/projects'
    },
    {
      path: '/projects',
      component: ProjectView,
      meta: {
        title: '项目管理',
        description: '维护测试平台中的项目，需求和用例集都归属到项目。'
      }
    },
    {
      path: '/requirements',
      component: RequirementView,
      meta: {
        title: '需求管理',
        description: '按项目维护需求，并进入 XMind 用例上传和编辑流程。'
      }
    },
    {
      path: '/cases',
      component: CaseSuiteListView,
      meta: {
        title: '用例管理',
        description: '查看全部用例集，进入编辑或导出。'
      }
    },
    {
      path: '/cases/edit',
      component: CaseEditorView,
      meta: {
        title: '用例编辑',
        description: '编辑用例树结构，支持拖拽排序与层级调整。'
      }
    },
    {
      path: '/files',
      component: FileView,
      meta: {
        title: '文件导出',
        description: '下载原始 XMind 文件和平台导出的 XMind 文件。'
      }
    }
  ]
});

export default router;
