<template>
  <section class="user-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button v-if="canManageUser" type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="90">
          <template #default="{ row }">
            <span class="avatar">{{ avatarText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="displayName" label="姓名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="组织" min-width="180">
          <template #default="{ row }">{{ organizationText(row.organizationId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'DISABLED' ? 'info' : 'success'">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">{{ roleText(row) }}</template>
        </el-table-column>
        <el-table-column v-if="canManageUser" label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openResetPassword(row)">重置密码</el-button>
            <el-button link :type="row.status === 'DISABLED' ? 'success' : 'warning'" @click="toggleStatus(row)">
              {{ row.status === 'DISABLED' ? '启用' : '禁用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="userDialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="640px">
      <el-form label-width="96px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="Boolean(editingUser)" placeholder="只能输入英文或数字" @input="normalizeUsername" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.displayName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="头像">
          <el-input v-model="userForm.avatar" maxlength="100" placeholder="头像文本或地址" />
        </el-form-item>
        <el-form-item label="组织">
          <el-select v-model="userForm.organizationId" clearable filterable placeholder="请选择组织">
            <el-option v-for="organization in flatOrganizations" :key="organization.id" :label="organization.label" :value="organization.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.roleCode" placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingUser" label="初始密码">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitUser">保存用户</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="420px">
      <el-form label-width="88px">
        <el-form-item label="用户">
          <span>{{ passwordUser?.displayName }}（{{ passwordUser?.username }}）</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="submitPasswordReset">确认重置</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  createUser,
  listOrganizationTreeForUser,
  listRolesForUser,
  listUsers,
  resetUserPassword,
  updateUser,
  type AssignableUser,
  type OrganizationOption,
  type RoleOption,
  type UserStatus
} from '../api/users';
import { showErrorMessage } from '../api/http';
import { hasPermission } from '../utils/permissions';

interface UserFormState {
  username: string;
  password: string;
  displayName: string;
  email: string;
  avatar: string;
  organizationId?: number;
  roleCode: string;
  status: UserStatus;
}

interface FlatOrganizationOption {
  id: number;
  label: string;
  orgName: string;
}

const users = ref<AssignableUser[]>([]);
const roles = ref<RoleOption[]>([]);
const organizations = ref<OrganizationOption[]>([]);
const loading = ref(false);
const saving = ref(false);
const passwordSaving = ref(false);
const userDialogVisible = ref(false);
const passwordDialogVisible = ref(false);
const editingUser = ref<AssignableUser | null>(null);
const passwordUser = ref<AssignableUser | null>(null);
const userForm = reactive<UserFormState>({
  username: '',
  password: '123456',
  displayName: '',
  email: '',
  avatar: '',
  organizationId: undefined,
  roleCode: 'TESTER',
  status: 'ACTIVE'
});
const passwordForm = reactive({ password: '' });

const flatOrganizations = computed<FlatOrganizationOption[]>(() => flattenOrganizations(organizations.value));
const canManageUser = computed(() => hasPermission('USER_MANAGE'));
const organizationNameMap = computed(() => {
  const map = new Map<number, string>();
  flatOrganizations.value.forEach((organization) => map.set(organization.id, organization.orgName));
  return map;
});

function flattenOrganizations(nodes: OrganizationOption[], level = 0): FlatOrganizationOption[] {
  return nodes.flatMap((node) => {
    const prefix = level > 0 ? `${'　'.repeat(level)}└ ` : '';
    return [
      { id: node.id, label: `${prefix}${node.orgName}`, orgName: node.orgName },
      ...flattenOrganizations(node.children ?? [], level + 1)
    ];
  });
}

function defaultRoleCode() {
  return roles.value.find((role) => role.roleCode === 'TESTER')?.roleCode || roles.value[0]?.roleCode || 'TESTER';
}

function resetUserForm() {
  userForm.username = '';
  userForm.password = '123456';
  userForm.displayName = '';
  userForm.email = '';
  userForm.avatar = '';
  userForm.organizationId = undefined;
  userForm.roleCode = defaultRoleCode();
  userForm.status = 'ACTIVE';
}

function openCreate() {
  if (!canManageUser.value) {
    ElMessage.warning('无用户管理权限');
    return;
  }
  editingUser.value = null;
  resetUserForm();
  userDialogVisible.value = true;
}

function openEdit(user: AssignableUser) {
  if (!canManageUser.value) {
    ElMessage.warning('无用户管理权限');
    return;
  }
  editingUser.value = user;
  userForm.username = user.username;
  userForm.password = '';
  userForm.displayName = user.displayName;
  userForm.email = user.email || '';
  userForm.avatar = user.avatar || '';
  userForm.organizationId = user.organizationId;
  userForm.roleCode = user.roleCodes?.[0] || defaultRoleCode();
  userForm.status = user.status || 'ACTIVE';
  userDialogVisible.value = true;
}

function openResetPassword(user: AssignableUser) {
  if (!canManageUser.value) {
    ElMessage.warning('无用户管理权限');
    return;
  }
  passwordUser.value = user;
  passwordForm.password = '';
  passwordDialogVisible.value = true;
}

function avatarText(user: AssignableUser) {
  return user.avatar || user.displayName?.slice(0, 1) || user.username.slice(0, 1).toUpperCase();
}

function normalizeUsername(value: string) {
  userForm.username = value.replace(/[^A-Za-z0-9]/g, '');
}

function roleText(user: AssignableUser) {
  return user.roleNames?.join('、') || user.roleCodes?.join('、') || '-';
}

function statusText(status?: UserStatus) {
  return status === 'DISABLED' ? '禁用' : '启用';
}

function organizationText(organizationId?: number) {
  if (!organizationId) return '-';
  const organizationName = organizationNameMap.value.get(organizationId);
  return organizationName ? `${organizationName}（${organizationId}）` : String(organizationId);
}

function buildUserPayload() {
  return {
    displayName: userForm.displayName.trim(),
    email: userForm.email.trim() || undefined,
    avatar: userForm.avatar.trim() || undefined,
    organizationId: userForm.organizationId,
    roleCode: userForm.roleCode,
    status: userForm.status
  };
}

function showUserOperationError(error: unknown, fallbackMessage: string) {
  showErrorMessage(error, fallbackMessage);
}

function validateUserForm() {
  if (!editingUser.value && !userForm.username.trim()) {
    ElMessage.warning('请填写用户名');
    return false;
  }
  if (!editingUser.value && !/^[A-Za-z0-9]+$/.test(userForm.username.trim())) {
    ElMessage.warning('用户名只能包含英文或数字');
    return false;
  }
  if (!userForm.displayName.trim()) {
    ElMessage.warning('请填写姓名');
    return false;
  }
  if (!userForm.roleCode) {
    ElMessage.warning('请选择角色');
    return false;
  }
  if (!editingUser.value && userForm.password.trim().length < 6) {
    ElMessage.warning('初始密码至少 6 位');
    return false;
  }
  return true;
}

async function loadUsers() {
  loading.value = true;
  try {
    users.value = await listUsers();
  } finally {
    loading.value = false;
  }
}

async function loadRoles() {
  roles.value = await listRolesForUser();
  if (!roles.value.some((role) => role.roleCode === userForm.roleCode)) {
    userForm.roleCode = defaultRoleCode();
  }
}

async function loadOrganizations() {
  organizations.value = await listOrganizationTreeForUser();
}

async function loadData() {
  await Promise.all([loadUsers(), loadRoles(), loadOrganizations()]);
}

async function submitUser() {
  if (!canManageUser.value) {
    ElMessage.warning('无用户管理权限');
    return;
  }
  if (!validateUserForm()) return;
  saving.value = true;
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.id, buildUserPayload());
      ElMessage.success('用户已更新');
    } else {
      await createUser({
        ...buildUserPayload(),
        username: userForm.username.trim(),
        password: userForm.password.trim()
      });
      ElMessage.success('用户已新增');
    }
    userDialogVisible.value = false;
    await loadUsers();
  } catch (error) {
    showUserOperationError(error, editingUser.value ? '用户更新失败' : '用户新增失败');
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(user: AssignableUser) {
  if (!canManageUser.value) {
    ElMessage.warning('无用户管理权限');
    return;
  }
  const nextStatus: UserStatus = user.status === 'DISABLED' ? 'ACTIVE' : 'DISABLED';
  try {
    await updateUser(user.id, {
      displayName: user.displayName,
      email: user.email || undefined,
      avatar: user.avatar || undefined,
      organizationId: user.organizationId,
      roleCode: user.roleCodes?.[0] || defaultRoleCode(),
      status: nextStatus
    });
    ElMessage.success(nextStatus === 'ACTIVE' ? '用户已启用' : '用户已禁用');
    await loadUsers();
  } catch (error) {
    showUserOperationError(error, nextStatus === 'ACTIVE' ? '用户启用失败' : '用户禁用失败');
  }
}

async function submitPasswordReset() {
  if (!canManageUser.value) {
    ElMessage.warning('无用户管理权限');
    return;
  }
  if (!passwordUser.value) return;
  const password = passwordForm.password.trim();
  if (password.length < 6) {
    ElMessage.warning('密码长度不能小于 6 位');
    return;
  }
  passwordSaving.value = true;
  try {
    await resetUserPassword(passwordUser.value.id, { password });
    passwordDialogVisible.value = false;
    ElMessage.success('密码已重置');
  } catch (error) {
    showUserOperationError(error, '密码重置失败');
  } finally {
    passwordSaving.value = false;
  }
}

onMounted(loadData);
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.avatar { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; color: #fff; background: #409eff; border-radius: 50%; font-weight: 600; overflow: hidden; }
</style>
