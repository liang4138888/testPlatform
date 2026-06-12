<template>
  <section class="role-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <h2>权限管理</h2>
            <p>配置角色可访问菜单、操作权限和数据范围。</p>
          </div>
          <el-button @click="openCreateRole">新增角色</el-button>
          <el-button @click="openCreatePermission">新增权限</el-button>
          <el-button type="primary" :disabled="!selectedRole" :loading="saving" @click="savePermissions">保存权限</el-button>
        </div>
      </template>

      <div class="role-layout">
        <el-table :data="roles" v-loading="loading" border highlight-current-row @current-change="selectRole">
          <el-table-column prop="roleName" label="角色" min-width="130" />
          <el-table-column prop="roleCode" label="编码" min-width="130" />
          <el-table-column label="权限数" width="90">
            <template #default="{ row }">{{ row.permissions.length }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEditRole(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="permission-panel">
          <h3>{{ selectedRole ? selectedRole.roleName : '请选择角色' }}</h3>
          <el-checkbox-group v-model="checkedPermissions" class="permission-groups">
            <div v-for="group in permissionGroups" :key="group.title" class="permission-group">
              <h4>{{ group.title }}</h4>
              <el-checkbox v-for="permission in group.items" :key="permission.permissionCode" :label="permission.permissionCode">
                <span class="permission-line">
                  <span>{{ permission.permissionName }}（{{ permission.permissionCode }}）</span>
                  <span class="permission-actions">
                    <el-button link type="primary" @click.stop="openEditPermission(permission)">编辑</el-button>
                    <el-button link type="danger" @click.stop="removePermission(permission)">删除</el-button>
                  </span>
                </span>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <el-empty v-if="!permissions.length" description="暂无权限" />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="roleDialogVisible" :title="editingRole ? '编辑角色' : '新增角色'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="角色编码" required>
          <el-input v-model="roleForm.roleCode" placeholder="如：REPORTER" @input="normalizeRoleCode" />
        </el-form-item>
        <el-form-item label="角色名称" required>
          <el-input v-model="roleForm.roleName" placeholder="如：报表管理员" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSaving" @click="saveRoleItem">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" :title="editingPermission ? '编辑权限' : '新增权限'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="权限编码" required>
          <el-input v-model="permissionForm.permissionCode" placeholder="如：MENU_REPORT 或 REPORT_VIEW" @input="normalizePermissionCode" />
        </el-form-item>
        <el-form-item label="权限名称" required>
          <el-input v-model="permissionForm.permissionName" placeholder="如：菜单-报表管理" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionSaving" @click="savePermissionItem">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createPermission, createRole, deletePermission, listPermissions, listRoles, updatePermission, updateRole, updateRolePermissions, type PermissionItem, type RoleItem } from '../api/roles';

const roles = ref<RoleItem[]>([]);
const permissions = ref<PermissionItem[]>([]);
const selectedRole = ref<RoleItem>();
const checkedPermissions = ref<string[]>([]);
const loading = ref(false);
const saving = ref(false);
const roleDialogVisible = ref(false);
const roleSaving = ref(false);
const editingRole = ref<RoleItem>();
const roleForm = ref({ roleCode: '', roleName: '' });
const permissionDialogVisible = ref(false);
const permissionSaving = ref(false);
const editingPermission = ref<PermissionItem>();
const permissionForm = ref({ permissionCode: '', permissionName: '' });

const permissionGroups = computed(() => {
  const groups = [
    { title: '菜单权限', items: permissions.value.filter((item) => item.permissionCode.startsWith('MENU_')) },
    { title: 'Bug 权限', items: permissions.value.filter((item) => item.permissionCode.startsWith('BUG_')) },
    { title: '管理权限', items: permissions.value.filter((item) => item.permissionCode.includes('MANAGE') || item.permissionCode.startsWith('USER_') || item.permissionCode.startsWith('ROLE_')) },
    { title: '数据权限', items: permissions.value.filter((item) => item.permissionCode.startsWith('DATA_')) },
    { title: '其他权限', items: permissions.value.filter((item) => !item.permissionCode.startsWith('MENU_') && !item.permissionCode.startsWith('BUG_') && !item.permissionCode.includes('MANAGE') && !item.permissionCode.startsWith('USER_') && !item.permissionCode.startsWith('ROLE_') && !item.permissionCode.startsWith('DATA_')) }
  ];
  return groups.filter((group) => group.items.length);
});

async function loadData() {
  loading.value = true;
  try {
    const [roleList, permissionList] = await Promise.all([listRoles(), listPermissions()]);
    roles.value = roleList;
    permissions.value = permissionList;
    if (roleList.length) {
      selectRole(roleList[0]);
    }
  } finally {
    loading.value = false;
  }
}

function selectRole(role?: RoleItem) {
  selectedRole.value = role;
  checkedPermissions.value = role?.permissions ? [...role.permissions] : [];
}

function normalizeRoleCode(value: string) {
  roleForm.value.roleCode = value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
}

function openCreateRole() {
  editingRole.value = undefined;
  roleForm.value = { roleCode: '', roleName: '' };
  roleDialogVisible.value = true;
}

function openEditRole(role: RoleItem) {
  editingRole.value = role;
  roleForm.value = { roleCode: role.roleCode, roleName: role.roleName };
  roleDialogVisible.value = true;
}

async function saveRoleItem() {
  const payload = {
    roleCode: roleForm.value.roleCode.trim(),
    roleName: roleForm.value.roleName.trim()
  };
  if (!payload.roleCode || !payload.roleName) {
    ElMessage.warning('请填写角色编码和角色名称');
    return;
  }
  roleSaving.value = true;
  try {
    let saved: RoleItem;
    if (editingRole.value) {
      saved = await updateRole(editingRole.value.id, payload);
      ElMessage.success('角色已更新');
    } else {
      saved = await createRole(payload);
      ElMessage.success('角色已新增');
    }
    roleDialogVisible.value = false;
    await loadData();
    const latest = roles.value.find((item) => item.id === saved.id);
    selectRole(latest);
  } finally {
    roleSaving.value = false;
  }
}

function normalizePermissionCode(value: string) {
  permissionForm.value.permissionCode = value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
}

function openCreatePermission() {
  editingPermission.value = undefined;
  permissionForm.value = { permissionCode: '', permissionName: '' };
  permissionDialogVisible.value = true;
}

function openEditPermission(permission: PermissionItem) {
  editingPermission.value = permission;
  permissionForm.value = { permissionCode: permission.permissionCode, permissionName: permission.permissionName };
  permissionDialogVisible.value = true;
}

async function savePermissionItem() {
  const payload = {
    permissionCode: permissionForm.value.permissionCode.trim(),
    permissionName: permissionForm.value.permissionName.trim()
  };
  if (!payload.permissionCode || !payload.permissionName) {
    ElMessage.warning('请填写权限编码和权限名称');
    return;
  }
  permissionSaving.value = true;
  try {
    if (editingPermission.value) {
      await updatePermission(editingPermission.value.id, payload);
      ElMessage.success('权限已更新');
    } else {
      await createPermission(payload);
      ElMessage.success('权限已新增');
    }
    permissionDialogVisible.value = false;
    await loadData();
  } finally {
    permissionSaving.value = false;
  }
}

async function removePermission(permission: PermissionItem) {
  await ElMessageBox.confirm(`确认删除权限 ${permission.permissionName}？`, '删除权限', { type: 'warning' });
  await deletePermission(permission.id);
  checkedPermissions.value = checkedPermissions.value.filter((code) => code !== permission.permissionCode);
  ElMessage.success('权限已删除');
  await loadData();
}

async function savePermissions() {
  if (!selectedRole.value) {
    return;
  }
  saving.value = true;
  try {
    const updated = await updateRolePermissions(selectedRole.value.id, checkedPermissions.value);
    const index = roles.value.findIndex((item) => item.id === updated.id);
    if (index >= 0) {
      roles.value[index] = updated;
    }
    selectRole(updated);
    ElMessage.success('权限已保存，相关用户重新登录后生效');
  } finally {
    saving.value = false;
  }
}

onMounted(loadData);
</script>

<style scoped>
.role-page { display: grid; gap: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.card-header h2 { margin: 0; font-size: 18px; }
.card-header p { margin: 6px 0 0; color: #6b7280; }
.role-layout { display: grid; grid-template-columns: 380px minmax(0, 1fr); gap: 16px; }
.permission-panel { min-height: 360px; padding: 16px; border: 1px solid #e5e7eb; border-radius: 10px; }
.permission-panel h3 { margin: 0 0 12px; }
.permission-groups { display: grid; gap: 14px; }
.permission-group { display: grid; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid #eef2f7; }
.permission-group h4 { margin: 0; color: #374151; }
.permission-group :deep(.el-checkbox) { display: flex; height: auto; margin-right: 0; }
.permission-line { display: inline-flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px; }
.permission-actions { display: inline-flex; gap: 4px; }
@media (max-width: 960px) {
  .role-layout { grid-template-columns: 1fr; }
}
</style>
