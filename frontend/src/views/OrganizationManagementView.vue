<template>
  <div class="organization-page">
    <el-card>
      <template #header>
        <div class="card-head">
          <div>
            <strong>组织架构</strong>
            <p>维护公司、部门、负责人和启用状态。</p>
          </div>
          <el-button v-if="canManageOrganization" type="primary" @click="openCreate">新增组织</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="organizations" row-key="id" default-expand-all>
        <el-table-column prop="orgName" label="组织名称" min-width="220" />
        <el-table-column prop="orgCode" label="组织编码" width="160" />
        <el-table-column prop="leaderName" label="负责人" width="140">
          <template #default="{ row }">{{ row.leaderName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManageOrganization" label="操作" width="190">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCreateChild(row)">创建子组织</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑组织' : '新增组织'" width="560px">
      <el-form label-width="100px">
        <el-form-item label="组织编码">
          <el-input v-model="form.orgCode" placeholder="英文、数字或下划线" />
        </el-form-item>
        <el-form-item label="组织名称">
          <el-input v-model="form.orgName" />
        </el-form-item>
        <el-form-item label="上级组织">
          <el-tree-select
            v-model="form.parentId"
            :data="organizationOptions"
            clearable
            check-strictly
            :render-after-expand="false"
            node-key="id"
            placeholder="不选则为根组织"
          />
        </el-form-item>
        <el-form-item v-if="editingId" label="负责人">
          <el-select v-model="form.leaderUserId" clearable filterable placeholder="请选择负责人">
            <el-option v-for="user in users" :key="user.id" :label="user.displayName" :value="user.id">
              <span>{{ user.displayName }}</span>
              <span class="option-code">{{ user.username }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveOrganization">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createOrganization, listOrganizationTree, updateOrganization, type OrganizationNode, type OrganizationPayload } from '../api/organizations';
import { showErrorMessage } from '../api/http';
import { listAssignableUsers, type AssignableUser } from '../api/users';
import { hasPermission } from '../utils/permissions';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const organizations = ref<OrganizationNode[]>([]);
const users = ref<AssignableUser[]>([]);
const form = reactive<OrganizationPayload>({ orgCode: '', orgName: '', parentId: undefined, leaderUserId: undefined, sortOrder: 0, status: 'ACTIVE' });
const canManageOrganization = computed(() => hasPermission('ORGANIZATION_MANAGE'));

interface TreeSelectOption {
  id: number;
  value: number;
  label: string;
  disabled: boolean;
  children?: TreeSelectOption[];
}

const organizationOptions = computed<TreeSelectOption[]>(() => toTreeOptions(organizations.value));

onMounted(async () => {
  await Promise.all([loadOrganizations(), loadUsers()]);
});

async function loadOrganizations() {
  loading.value = true;
  try {
    organizations.value = await listOrganizationTree();
  } finally {
    loading.value = false;
  }
}

async function loadUsers() {
  users.value = await listAssignableUsers();
}

function openCreate() {
  if (!canManageOrganization.value) {
    ElMessage.warning('无组织管理权限');
    return;
  }
  openCreateWithParent(undefined);
}

function openCreateChild(row: OrganizationNode) {
  if (!canManageOrganization.value) {
    ElMessage.warning('无组织管理权限');
    return;
  }
  openCreateWithParent(row.id);
}

function openCreateWithParent(parentId: number | undefined) {
  if (!canManageOrganization.value) {
    ElMessage.warning('无组织管理权限');
    return;
  }
  editingId.value = undefined;
  Object.assign(form, { orgCode: '', orgName: '', parentId, leaderUserId: undefined, sortOrder: 0, status: 'ACTIVE' });
  dialogVisible.value = true;
}

function openEdit(row: OrganizationNode) {
  if (!canManageOrganization.value) {
    ElMessage.warning('无组织管理权限');
    return;
  }
  editingId.value = row.id;
  Object.assign(form, {
    orgCode: row.orgCode,
    orgName: row.orgName,
    parentId: row.parentId,
    leaderUserId: row.leaderUserId,
    sortOrder: row.sortOrder,
    status: row.status
  });
  dialogVisible.value = true;
}

async function saveOrganization() {
  if (!canManageOrganization.value) {
    ElMessage.warning('无组织管理权限');
    return;
  }
  saving.value = true;
  try {
    const payload = { ...form, leaderUserId: editingId.value ? form.leaderUserId : undefined };
    if (editingId.value) {
      await updateOrganization(editingId.value, payload);
    } else {
      await createOrganization(payload);
    }
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    await loadOrganizations();
  } catch (error) {
    showErrorMessage(error, '保存失败');
  } finally {
    saving.value = false;
  }
}

function statusLabel(status: string) {
  return status === 'ACTIVE' ? '启用' : '禁用';
}

function toTreeOptions(nodes: OrganizationNode[]): TreeSelectOption[] {
  return nodes.map((node) => ({
    id: node.id,
    value: node.id,
    label: node.orgName,
    disabled: editingId.value === node.id,
    children: node.children?.length ? toTreeOptions(node.children) : undefined
  }));
}
</script>

<style scoped>
.organization-page { display: grid; gap: 16px; }
.card-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.card-head p { margin: 4px 0 0; color: #606266; font-size: 13px; }
.option-code { float: right; color: #909399; font-size: 12px; }
</style>
