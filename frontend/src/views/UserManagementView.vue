<template>
  <section class="user-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column label="头像" width="90">
          <template #default="{ row }"><span class="avatar">{{ avatarText(row) }}</span></template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="displayName" label="姓名" min-width="180" />
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">{{ roleText(row) }}</template>
        </el-table-column>
        <el-table-column label="说明" min-width="220">
          <template #default="{ row }">{{ roleHint(row) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVisible" title="新增用户" width="620px">
      <el-form label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" placeholder="只能输入英文或数字" @input="normalizeUsername" /></el-form-item>
        <el-form-item label="头像"><el-input v-model="form.avatar" maxlength="8" placeholder="如：张、测、A" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.displayName" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存用户</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createUser, listAssignableUsers, listRolesForUser, type AssignableUser, type RoleOption } from '../api/users';

const users = ref<AssignableUser[]>([]);
const roles = ref<RoleOption[]>([]);
const loading = ref(false);
const createVisible = ref(false);
const form = reactive({ username: '', password: '123456', displayName: '', email: '', avatar: '', roleCode: 'DEVELOPER', status: 'ACTIVE' });

function defaultRoleCode() {
  return roles.value.find((role) => role.roleCode === 'TESTER')?.roleCode || roles.value[0]?.roleCode || 'TESTER';
}

function openCreate() {
  form.username = '';
  form.password = '123456';
  form.displayName = '';
  form.email = '';
  form.avatar = '';
  form.roleCode = defaultRoleCode();
  form.status = 'ACTIVE';
  createVisible.value = true;
}

function avatarText(user: AssignableUser) {
  return user.avatar || user.displayName?.slice(0, 1) || user.username.slice(0, 1).toUpperCase();
}

function normalizeUsername(value: string) {
  form.username = value.replace(/[^A-Za-z0-9]/g, '');
}

function roleText(user: AssignableUser) {
  return user.roleNames?.join('、') || user.roleCodes?.join('、') || '-';
}

function roleHint(user: AssignableUser) {
  const roleCodes = user.roleCodes ?? [];
  if (roleCodes.includes('ADMIN')) return '管理员，拥有全部权限';
  if (roleCodes.includes('DEVELOPER')) return '开发人员，可提交已修复';
  if (roleCodes.includes('PRODUCT')) return '产品人员，可提交已修复';
  return '测试人员，可提交待修复和修复完成';
}

async function loadUsers() {
  loading.value = true;
  try {
    users.value = await listAssignableUsers();
  } finally {
    loading.value = false;
  }
}

async function loadRoles() {
  roles.value = await listRolesForUser();
  if (!roles.value.some((role) => role.roleCode === form.roleCode)) {
    form.roleCode = defaultRoleCode();
  }
}

async function loadData() {
  await Promise.all([loadUsers(), loadRoles()]);
}

async function submitCreate() {
  if (!form.username.trim() || !form.displayName.trim()) {
    ElMessage.warning('请填写用户名和姓名');
    return;
  }
  if (!/^[A-Za-z0-9]+$/.test(form.username.trim())) {
    ElMessage.warning('用户名只能包含英文或数字');
    return;
  }
  await createUser({ ...form, username: form.username.trim() });
  createVisible.value = false;
  ElMessage.success('用户已新增');
  await loadUsers();
}

onMounted(loadData);
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.avatar { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; color: #fff; background: #409eff; border-radius: 50%; font-weight: 600; }
</style>
