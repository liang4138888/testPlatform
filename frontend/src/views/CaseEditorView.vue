<template>
  <section class="workspace">
    <article class="panel">
      <div class="panel-title">
        <div>
          <h2>用例树</h2>
          <p v-if="suite" class="muted">
            {{ suite.requirementNo }} {{ suite.requirementName }} · {{ suite.name }}
          </p>
        </div>
        <div class="actions-inline">
          <el-button @click="goBack">返回列表</el-button>
          <el-button type="primary" :loading="saving" @click="saveTree">保存用例树</el-button>
        </div>
      </div>
      <p class="muted tree-tip">支持拖拽节点调整同级顺序或移动到其他父节点下，调整后请保存。</p>
      <el-tree
        v-loading="loading"
        :data="treeData"
        node-key="key"
        default-expand-all
        highlight-current
        draggable
        :expand-on-click-node="false"
        :allow-drop="allowDrop"
        @node-click="onNodeClick"
        @node-drop="onNodeDrop"
      />
    </article>

    <article class="panel">
      <h2>节点详情</h2>
      <template v-if="selectedNode">
        <el-form label-width="88px">
          <el-form-item label="节点名称" required>
            <el-input v-model="selectedNode.name" maxlength="255" @input="syncSelectedLabel" />
          </el-form-item>
          <el-form-item label="节点类型" required>
            <el-select v-model="selectedNode.nodeType">
              <el-option label="模块" value="module" />
              <el-option label="用例" value="case" />
              <el-option label="步骤" value="step" />
              <el-option label="预期" value="expected" />
            </el-select>
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="selectedNode.description" type="textarea" maxlength="1000" show-word-limit />
          </el-form-item>
        </el-form>
        <div class="actions">
          <el-button @click="addChildNode">新增子节点</el-button>
          <el-button @click="addRootNode">新增根节点</el-button>
          <el-button type="danger" @click="removeSelectedNode">删除节点</el-button>
        </div>
      </template>
      <el-empty v-else description="请选择左侧树节点" />
    </article>
  </section>
</template>

<script setup lang="ts">
import type { AllowDropType } from 'element-plus/es/components/tree/src/tree.type';
import type Node from 'element-plus/es/components/tree/src/model/node';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getCaseSuite, saveCaseNodes, type CaseSuiteDetail } from '../api/caseSuites';
import {
  findNode,
  findParent,
  isDescendant,
  nextNodeKey,
  reindexSortOrders,
  resetNodeSeq,
  toEditable,
  toPayload,
  type EditableNode
} from '../utils/caseTree';

const route = useRoute();
const router = useRouter();
const suiteId = computed(() => {
  const value = Number(route.query.suiteId);
  return Number.isFinite(value) && value > 0 ? value : undefined;
});

const loading = ref(false);
const saving = ref(false);
const suite = ref<CaseSuiteDetail>();
const treeData = ref<EditableNode[]>([]);
const selectedKey = ref<string>();

const selectedNode = computed(() => findNode(treeData.value, selectedKey.value));

function onNodeClick(data: EditableNode) {
  selectedKey.value = data.key;
}

function syncSelectedLabel() {
  const node = selectedNode.value;
  if (node) {
    node.label = node.name;
  }
}

function addChildNode() {
  const parent = selectedNode.value;
  if (!parent) {
    ElMessage.warning('请先选择父节点');
    return;
  }
  const child: EditableNode = {
    key: nextNodeKey(),
    label: '新节点',
    nodeType: 'step',
    name: '新节点',
    description: '',
    sortOrder: parent.children.length,
    children: []
  };
  parent.children.push(child);
  selectedKey.value = child.key;
}

function addRootNode() {
  const node: EditableNode = {
    key: nextNodeKey(),
    label: '新模块',
    nodeType: 'module',
    name: '新模块',
    description: '',
    sortOrder: treeData.value.length,
    children: []
  };
  treeData.value.push(node);
  selectedKey.value = node.key;
}

function removeSelectedNode() {
  const key = selectedKey.value;
  if (!key) {
    return;
  }
  const parent = findParent(treeData.value, key);
  const siblings = parent ? parent.children : treeData.value;
  const index = siblings.findIndex((item) => item.key === key);
  if (index >= 0) {
    siblings.splice(index, 1);
    reindexSortOrders(treeData.value);
    selectedKey.value = undefined;
  }
}

function allowDrop(draggingNode: Node, dropNode: Node, type: AllowDropType) {
  const draggingKey = (draggingNode.data as EditableNode).key;
  const dropKey = (dropNode.data as EditableNode).key;
  if (draggingKey === dropKey) {
    return false;
  }
  if (type === 'inner' && isDescendant(treeData.value, draggingKey, dropKey)) {
    return false;
  }
  if (type !== 'inner' && isDescendant(treeData.value, draggingKey, dropKey)) {
    return false;
  }
  return true;
}

function onNodeDrop() {
  reindexSortOrders(treeData.value);
}

async function loadSuite() {
  if (!suiteId.value) {
    return;
  }
  loading.value = true;
  try {
    suite.value = await getCaseSuite(suiteId.value);
    resetNodeSeq();
    treeData.value = (suite.value.nodes ?? []).map((node, index) => toEditable(node, index));
    selectedKey.value = treeData.value[0]?.key;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用例集加载失败');
  } finally {
    loading.value = false;
  }
}

async function saveTree() {
  if (!suiteId.value) {
    return;
  }
  if (!treeData.value.length) {
    ElMessage.warning('用例树不能为空');
    return;
  }

  saving.value = true;
  try {
    suite.value = await saveCaseNodes(suiteId.value, toPayload(treeData.value));
    resetNodeSeq();
    treeData.value = (suite.value.nodes ?? []).map((node, index) => toEditable(node, index));
    ElMessage.success('用例树已保存');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push('/cases');
}

watch(
  () => route.query.suiteId,
  () => {
    if (suiteId.value) {
      loadSuite();
    }
  }
);

onMounted(() => {
  if (!suiteId.value) {
    router.replace('/cases');
  } else {
    loadSuite();
  }
});
</script>
