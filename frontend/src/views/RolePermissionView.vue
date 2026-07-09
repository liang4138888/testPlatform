<template>
  <section class="role-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <h2>权限管理</h2>
            <p>所有角色统一列表展示，点击角色详情后配置菜单、操作权限和数据范围。</p>
          </div>
          <div class="header-actions">
            <el-button v-if="canManageRole" @click="permissionManagerVisible = true">权限字典维护</el-button>
            <el-button v-if="canManageRole" type="primary" @click="openCreateRole">新增角色</el-button>
          </div>
        </div>
      </template>

      <el-table :data="roles" v-loading="loading" border>
        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            <div class="role-name">{{ row.roleName }}</div>
            <div class="muted-code">{{ row.roleCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="权限数" width="110">
          <template #default="{ row }">{{ row.permissions.length }}</template>
        </el-table-column>
        <el-table-column label="已授权权限" min-width="320">
          <template #default="{ row }">
            <div v-if="row.permissions.length" class="role-permission-summary">
              <el-tag v-for="code in row.permissions.slice(0, 4)" :key="code" type="info">{{ permissionName(code) }}</el-tag>
              <span v-if="row.permissions.length > 4" class="more-text">等 {{ row.permissions.length }} 项</span>
            </div>
            <span v-else class="empty-text">未配置权限</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoleDetail(row)">角色详情</el-button>
            <el-button v-if="canManageRole" link type="primary" @click="openEditRole(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!roles.length && !loading" description="暂无角色" />
    </el-card>

    <el-dialog v-model="roleDetailVisible" :title="selectedRole ? `${selectedRole.roleName} - 角色详情` : '角色详情'" width="760px">
      <template v-if="selectedRole">
        <div class="detail-head">
          <div>
            <div class="role-name">{{ selectedRole.roleName }}</div>
            <div class="muted-code">{{ selectedRole.roleCode }}</div>
          </div>
          <div class="detail-count">{{ checkedPermissionCount }} / {{ permissions.length }} 个权限已选择</div>
        </div>

        <el-tree
          v-if="permissionTree.length"
          ref="rolePermissionTreeRef"
          :key="selectedRole.id"
          class="permission-tree"
          :data="permissionTree"
          node-key="permissionCode"
          show-checkbox
          check-strictly
          default-expand-all
          :default-checked-keys="checkedPermissions"
          :props="permissionTreeProps"
          @check="syncCheckedPermissions"
        >
          <template #default="{ data }">
            <span class="permission-tree-node">
              <el-tag size="small" :type="permissionTypeTag(data.permissionType)">{{ permissionTypeText(data.permissionType) }}</el-tag>
              <span class="permission-name">{{ data.permissionName }}</span>
              <span class="muted-code">{{ data.permissionCode }}</span>
            </span>
          </template>
        </el-tree>
        <el-empty v-else description="暂无权限" />
      </template>
      <template #footer>
        <el-button @click="roleDetailVisible = false">关闭</el-button>
        <el-button v-if="canManageRole" type="primary" :disabled="!selectedRole" :loading="saving" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>

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

    <el-drawer v-model="permissionManagerVisible" class="permission-drawer" title="权限字典维护" size="1004px">
      <div class="permission-manager-head">
        <p>维护菜单、页面和按钮/操作权限节点。角色授权请在角色详情弹窗中配置。</p>
        <el-button v-if="canManageRole" type="primary" @click="openCreatePermission()">新增根节点</el-button>
      </div>
      <el-tree v-if="permissionTree.length" class="dictionary-tree" :data="permissionTree" node-key="id" default-expand-all :props="permissionTreeProps">
        <template #default="{ data }">
          <div class="dictionary-tree-node">
            <div class="permission-tree-node">
              <el-tag size="small" :type="permissionTypeTag(data.permissionType)">{{ permissionTypeText(data.permissionType) }}</el-tag>
              <span class="permission-name">{{ data.permissionName }}</span>
              <span class="muted-code">{{ data.permissionCode }}</span>
              <span class="muted-code">{{ data.pageName }} / {{ data.pageCode }}</span>
            </div>
            <div v-if="canManageRole" class="tree-node-actions">
              <el-button link type="primary" @click.stop="openCreatePermission(data)">新增子节点</el-button>
              <el-button link type="primary" @click.stop="openEditPermission(data)">编辑</el-button>
              <el-button link type="danger" @click.stop="removePermission(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
      <el-empty v-else description="暂无权限" />
    </el-drawer>

    <el-dialog v-model="permissionDialogVisible" :title="editingPermission ? '编辑权限' : '新增权限'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="节点类型" required>
          <el-select v-model="permissionForm.permissionType">
            <el-option label="菜单" value="MENU" />
            <el-option label="页面" value="PAGE" />
            <el-option label="按钮/操作" value="ACTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="父节点">
          <el-select v-model="permissionForm.parentId" clearable filterable placeholder="不选则作为根节点">
            <el-option v-for="option in parentPermissionOptions" :key="option.id" :label="option.label" :value="option.id" :disabled="option.disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="页面编码" required>
          <el-input v-model="permissionForm.pageCode" placeholder="如：PROJECT" @input="normalizePermissionPageCode" />
        </el-form-item>
        <el-form-item label="页面名称" required>
          <el-input v-model="permissionForm.pageName" placeholder="如：项目管理" />
        </el-form-item>
        <el-form-item label="权限编码" required>
          <el-input v-model="permissionForm.permissionCode" placeholder="如：MENU_REPORT 或 REPORT_VIEW" @input="normalizePermissionCode" />
        </el-form-item>
        <el-form-item label="权限名称" required>
          <el-input v-model="permissionForm.permissionName" placeholder="如：菜单-报表管理" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="permissionForm.sortOrder" :min="0" :step="10" />
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
import { computed, nextTick, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox, type TreeInstance } from 'element-plus';
import { createPermission, createRole, deletePermission, listPermissions, listRoles, updatePermission, updateRole, updateRolePermissions, type PermissionItem, type RoleItem } from '../api/roles';
import { showErrorMessage } from '../api/http';
import { hasPermission } from '../utils/permissions';

const roles = ref<RoleItem[]>([]);
const permissions = ref<PermissionItem[]>([]);
const selectedRole = ref<RoleItem>();
const checkedPermissions = ref<string[]>([]);
const loading = ref(false);
const saving = ref(false);
const rolePermissionTreeRef = ref<TreeInstance>();
const roleDetailVisible = ref(false);
const roleDialogVisible = ref(false);
const roleSaving = ref(false);
const editingRole = ref<RoleItem>();
const roleForm = ref({ roleCode: '', roleName: '' });
const permissionManagerVisible = ref(false);
const permissionDialogVisible = ref(false);
const permissionSaving = ref(false);
const editingPermission = ref<PermissionItem>();
const permissionForm = ref<PermissionFormState>(emptyPermissionForm());

const canManageRole = computed(() => hasPermission('ROLE_MANAGE'));
const permissionMap = computed(() => new Map(permissions.value.map((item) => [item.permissionCode, item.permissionName])));
const checkedPermissionCount = computed(() => checkedPermissions.value.length);

interface PermissionTreeNode extends PermissionItem {
  children?: PermissionTreeNode[];
}

interface ParentPermissionOption {
  id: number;
  label: string;
  disabled: boolean;
}

interface PermissionFormState {
  permissionCode: string;
  permissionName: string;
  pageCode: string;
  pageName: string;
  parentId?: number;
  permissionType: PermissionItem['permissionType'];
  sortOrder: number;
}

const permissionTreeProps = {
  label: 'permissionName',
  children: 'children'
};

const permissionTree = computed<PermissionTreeNode[]>(() => buildPermissionTree(permissions.value));
const parentPermissionOptions = computed<ParentPermissionOption[]>(() => {
  const disabledIds = editingPermission.value ? collectDescendantIds(editingPermission.value.id) : new Set<number>();
  if (editingPermission.value) {
    disabledIds.add(editingPermission.value.id);
  }
  return flattenPermissionOptions(permissionTree.value, disabledIds);
});

function emptyPermissionForm(parent?: PermissionItem): PermissionFormState {
  return {
    permissionCode: '',
    permissionName: '',
    pageCode: parent?.pageCode || '',
    pageName: parent?.pageName || '',
    parentId: parent?.id,
    permissionType: nextPermissionType(parent?.permissionType),
    sortOrder: 0
  };
}

function nextPermissionType(parentType?: PermissionItem['permissionType']): PermissionItem['permissionType'] {
  if (parentType === 'MENU') return 'PAGE';
  if (parentType === 'PAGE') return 'ACTION';
  return 'MENU';
}

function buildPermissionTree(items: PermissionItem[]): PermissionTreeNode[] {
  const nodeMap = new Map<number, PermissionTreeNode>();
  const roots: PermissionTreeNode[] = [];
  const sortedItems = [...items].sort(comparePermissions);
  sortedItems.forEach((item) => nodeMap.set(item.id, { ...item, children: [] }));
  sortedItems.forEach((item) => {
    const node = nodeMap.get(item.id);
    if (!node) return;
    const parent = item.parentId ? nodeMap.get(item.parentId) : undefined;
    if (parent) {
      parent.children?.push(node);
    } else {
      roots.push(node);
    }
  });
  return roots;
}

function comparePermissions(left: PermissionItem, right: PermissionItem) {
  const sortDiff = (left.sortOrder ?? 0) - (right.sortOrder ?? 0);
  return sortDiff !== 0 ? sortDiff : left.id - right.id;
}

function flattenPermissionOptions(nodes: PermissionTreeNode[], disabledIds: Set<number>, level = 0): ParentPermissionOption[] {
  return nodes.flatMap((node) => [
    {
      id: node.id,
      label: `${'　'.repeat(level)}${permissionTypeText(node.permissionType)} - ${node.permissionName}（${node.permissionCode}）`,
      disabled: disabledIds.has(node.id)
    },
    ...flattenPermissionOptions(node.children ?? [], disabledIds, level + 1)
  ]);
}

function collectDescendantIds(permissionId: number) {
  const childrenMap = new Map<number, PermissionItem[]>();
  permissions.value.forEach((permission) => {
    if (!permission.parentId) return;
    const children = childrenMap.get(permission.parentId) ?? [];
    children.push(permission);
    childrenMap.set(permission.parentId, children);
  });
  const result = new Set<number>();
  const stack = [...(childrenMap.get(permissionId) ?? [])];
  while (stack.length) {
    const permission = stack.pop();
    if (!permission || result.has(permission.id)) continue;
    result.add(permission.id);
    stack.push(...(childrenMap.get(permission.id) ?? []));
  }
  return result;
}

async function loadData() {
  loading.value = true;
  try {
    const [roleList, permissionList] = await Promise.all([listRoles(), listPermissions()]);
    roles.value = roleList;
    permissions.value = permissionList;
    if (selectedRole.value) {
      selectRole(roleList.find((role) => role.id === selectedRole.value?.id));
    }
  } finally {
    loading.value = false;
  }
}

function selectRole(role?: RoleItem) {
  selectedRole.value = role;
  checkedPermissions.value = role?.permissions ? [...role.permissions] : [];
  nextTick(() => {
    rolePermissionTreeRef.value?.setCheckedKeys(checkedPermissions.value);
  });
}

function openRoleDetail(role: RoleItem) {
  selectRole(role);
  roleDetailVisible.value = true;
}

function permissionName(code: string) {
  return permissionMap.value.get(code) || code;
}

function permissionTypeText(type: PermissionItem['permissionType']) {
  if (type === 'MENU') return '菜单';
  if (type === 'PAGE') return '页面';
  return '操作';
}

function permissionTypeTag(type: PermissionItem['permissionType']) {
  if (type === 'MENU') return 'success';
  if (type === 'PAGE') return 'primary';
  return 'warning';
}

function syncCheckedPermissions() {
  checkedPermissions.value = rolePermissionTreeRef.value?.getCheckedKeys(false).map(String) ?? [];
}

function normalizeRoleCode(value: string) {
  roleForm.value.roleCode = value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
}

function openCreateRole() {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  editingRole.value = undefined;
  roleForm.value = { roleCode: '', roleName: '' };
  roleDialogVisible.value = true;
}

function openEditRole(role: RoleItem) {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  editingRole.value = role;
  roleForm.value = { roleCode: role.roleCode, roleName: role.roleName };
  roleDialogVisible.value = true;
}

async function saveRoleItem() {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
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
  } catch (error) {
    showErrorMessage(error, editingRole.value ? '角色更新失败' : '角色新增失败');
  } finally {
    roleSaving.value = false;
  }
}

function normalizePermissionPageCode(value: string) {
  permissionForm.value.pageCode = value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
}

function normalizePermissionCode(value: string) {
  permissionForm.value.permissionCode = value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
}

function openCreatePermission(parent?: PermissionItem) {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  editingPermission.value = undefined;
  permissionForm.value = emptyPermissionForm(parent);
  permissionDialogVisible.value = true;
}

function openEditPermission(permission: PermissionItem) {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  editingPermission.value = permission;
  permissionForm.value = {
    pageCode: permission.pageCode,
    pageName: permission.pageName,
    permissionCode: permission.permissionCode,
    permissionName: permission.permissionName,
    parentId: permission.parentId,
    permissionType: permission.permissionType,
    sortOrder: permission.sortOrder ?? 0
  };
  permissionDialogVisible.value = true;
}

async function savePermissionItem() {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  const payload = {
    pageCode: permissionForm.value.pageCode.trim(),
    pageName: permissionForm.value.pageName.trim(),
    permissionCode: permissionForm.value.permissionCode.trim(),
    permissionName: permissionForm.value.permissionName.trim(),
    parentId: permissionForm.value.parentId,
    permissionType: permissionForm.value.permissionType,
    sortOrder: permissionForm.value.sortOrder
  };
  if (!payload.pageCode || !payload.pageName || !payload.permissionCode || !payload.permissionName || !payload.permissionType) {
    ElMessage.warning('请填写节点类型、页面信息、权限编码和权限名称');
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
  } catch (error) {
    showErrorMessage(error, editingPermission.value ? '权限更新失败' : '权限新增失败');
  } finally {
    permissionSaving.value = false;
  }
}

async function removePermission(permission: PermissionItem) {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除权限 ${permission.permissionName}？`, '删除权限', { type: 'warning' });
    await deletePermission(permission.id);
    checkedPermissions.value = checkedPermissions.value.filter((code) => code !== permission.permissionCode);
    ElMessage.success('权限已删除');
    await loadData();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    showErrorMessage(error, '权限删除失败');
  }
}

async function savePermissions() {
  if (!canManageRole.value) {
    ElMessage.warning('无角色管理权限');
    return;
  }
  if (!selectedRole.value) {
    return;
  }
  syncCheckedPermissions();
  const permissionsToSave = withAncestorPermissions(checkedPermissions.value);
  saving.value = true;
  try {
    const updated = await updateRolePermissions(selectedRole.value.id, permissionsToSave);
    const index = roles.value.findIndex((item) => item.id === updated.id);
    if (index >= 0) {
      roles.value[index] = updated;
    }
    selectRole(updated);
    roleDetailVisible.value = false;
    ElMessage.success('权限已保存，相关用户重新登录后生效');
  } catch (error) {
    showErrorMessage(error, '权限保存失败');
  } finally {
    saving.value = false;
  }
}

function withAncestorPermissions(permissionCodes: string[]) {
  const codeSet = new Set(permissionCodes);
  const permissionByCode = new Map(permissions.value.map((permission) => [permission.permissionCode, permission]));
  const permissionById = new Map(permissions.value.map((permission) => [permission.id, permission]));
  for (const code of permissionCodes) {
    let current = permissionByCode.get(code);
    const visited = new Set<number>();
    while (current?.parentId && !visited.has(current.parentId)) {
      visited.add(current.parentId);
      const parent = permissionById.get(current.parentId);
      if (!parent) {
        break;
      }
      codeSet.add(parent.permissionCode);
      current = parent;
    }
  }
  return Array.from(codeSet);
}

onMounted(loadData);
</script>

<style scoped>
.role-page { display: grid; gap: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.card-header h2 { margin: 0; font-size: 18px; }
.card-header p { margin: 6px 0 0; color: #6b7280; }
.header-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.role-name { font-weight: 700; color: #1f2937; }
.muted-code { margin-top: 3px; color: #909399; font-size: 12px; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
.role-permission-summary { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.more-text, .empty-text { color: #909399; font-size: 13px; }
.detail-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 16px; padding: 14px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 10px; }
.detail-count { color: #6b7280; font-size: 13px; }
.permission-drawer :deep(.el-drawer__body) { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.permission-tree { max-height: 56vh; overflow: auto; padding-right: 4px; }
.dictionary-tree { flex: 1; min-height: 0; overflow: auto; padding-right: 4px; }
.permission-tree-node { display: inline-flex; align-items: center; gap: 8px; min-height: 28px; }
.dictionary-tree-node { display: flex; align-items: center; justify-content: space-between; gap: 12px; width: 100%; min-height: 34px; }
.tree-node-actions { display: inline-flex; align-items: center; gap: 6px; }
.page-group-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 10px 12px; background: #f9fafb; border: 1px solid #eef2f7; border-radius: 8px; }
.page-group-head h4 { margin: 0; color: #1f2937; }
.permission-groups { display: grid; gap: 8px; padding: 0 4px 8px; }
.permission-group { display: grid; gap: 8px; padding-bottom: 12px; border-bottom: 1px solid #eef2f7; }
.permission-group h4 { margin: 0; color: #374151; }
.permission-group :deep(.el-checkbox) { display: flex; height: auto; margin-right: 0; padding: 6px 0; }
.permission-line { display: inline-flex; align-items: baseline; gap: 10px; }
.permission-name { color: #1f2937; }
.permission-manager-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.permission-manager-head p { margin: 0; color: #6b7280; }
@media (max-width: 960px) {
  .card-header { align-items: flex-start; flex-direction: column; }
  .detail-head { flex-direction: column; }
}
</style>
