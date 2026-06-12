<template>
  <section class="workspace">
    <article class="panel">
      <div class="panel-title">
        <div class="case-title-block">
          <h2>用例树</h2>
          <span v-if="suite" class="muted case-subtitle">
            {{ suite.requirementNo }} {{ suite.requirementName }} · {{ suite.name }}
          </span>
        </div>
        <div class="actions-inline">
          <el-button @click="goBack">返回列表</el-button>
          <el-button type="primary" :loading="saving" @click="saveTree">保存用例树</el-button>
        </div>
      </div>
      <div class="mind-toolbar">
        <el-button size="small" @click="addRootNode">新增根节点</el-button>
        <el-button size="small" @click="expandAll">全部展开</el-button>
        <el-button size="small" @click="collapseAll">全部收起</el-button>
        <el-button size="small" @click="zoomOut">缩小</el-button>
        <span class="mind-zoom-text">{{ zoomPercent }}%</span>
        <el-button size="small" @click="zoomIn">放大</el-button>
        <el-button size="small" @click="resetZoom">重置</el-button>
        <span class="toolbar-divider"></span>
        <el-tag size="small" type="info">待开始 {{ statusSummary.PENDING }}</el-tag>
        <el-tag size="small" type="success">通过 {{ statusSummary.PASSED }}</el-tag>
        <el-tag size="small" type="danger">失败 {{ statusSummary.FAILED }}</el-tag>
      </div>
      <p class="muted tree-tip">点击节点可打开详情弹窗，点击展开/收起控制层级，点击添加可新增子节点。</p>
      <div v-loading="loading" class="mind-map">
        <div ref="mindCanvasRef" class="mind-canvas" :style="mindCanvasStyle">
          <svg class="mind-lines" :width="svgSize.width" :height="svgSize.height">
            <path v-for="path in mindPaths" :key="path" :d="path" />
          </svg>
          <div ref="mindRootRef" class="mind-root">
            {{ suite?.name ?? '用例集' }}
          </div>
          <div class="mind-branches" v-if="treeData.length">
          <div v-for="module in treeData" :key="module.key" class="mind-branch">
            <div class="mind-node-wrap">
              <button class="mind-node module" :data-node-key="module.key" :class="[{ active: selectedKey === module.key }, statusClass(module)]" @click="onNodeClick(module)">
                <span>{{ module.name }}</span>
                <select class="case-status-select" :class="statusClass(module)" :value="module.executionStatus" @click.stop @change="changeNodeStatus(module, $event)">
                  <option value="PENDING">待开始</option>
                  <option value="PASSED">通过</option>
                  <option value="FAILED">失败</option>
                </select>
              </button>
              <button
                v-if="module.children.length"
                class="mind-toggle"
                :title="isCollapsed(module.key) ? '展开' : '收起'"
                @click="toggleNode(module.key)"
              >
                {{ isCollapsed(module.key) ? '展开' : '收起' }}
              </button>
              <button v-else class="mind-add" title="添加子节点" @click="addChildTo(module)">+</button>
            </div>
            <div v-if="module.children.length && !isCollapsed(module.key)" class="mind-children">
              <div v-for="caseNode in module.children" :key="caseNode.key" class="mind-branch">
                <div class="mind-node-wrap">
                  <button class="mind-node case" :data-node-key="caseNode.key" :class="[{ active: selectedKey === caseNode.key }, statusClass(caseNode)]" @click="onNodeClick(caseNode)">
                    <span>{{ caseNode.name }}</span>
                    <select class="case-status-select" :class="statusClass(caseNode)" :value="caseNode.executionStatus" @click.stop @change="changeNodeStatus(caseNode, $event)">
                      <option value="PENDING">待开始</option>
                      <option value="PASSED">通过</option>
                      <option value="FAILED">失败</option>
                    </select>
                  </button>
                  <button
                    v-if="caseNode.children.length"
                    class="mind-toggle"
                    :title="isCollapsed(caseNode.key) ? '展开' : '收起'"
                    @click="toggleNode(caseNode.key)"
                  >
                    {{ isCollapsed(caseNode.key) ? '展开' : '收起' }}
                  </button>
                  <button v-else class="mind-add" title="添加子节点" @click="addChildTo(caseNode)">+</button>
                </div>
                <div v-if="caseNode.children.length && !isCollapsed(caseNode.key)" class="mind-children">
                  <div v-for="step in caseNode.children" :key="step.key" class="mind-branch">
                    <div class="mind-node-wrap">
                      <button class="mind-node step" :data-node-key="step.key" :class="[{ active: selectedKey === step.key }, statusClass(step)]" @click="onNodeClick(step)">
                        <span>{{ step.name }}</span>
                        <select class="case-status-select" :class="statusClass(step)" :value="step.executionStatus" @click.stop @change="changeNodeStatus(step, $event)">
                          <option value="PENDING">待开始</option>
                          <option value="PASSED">通过</option>
                          <option value="FAILED">失败</option>
                        </select>
                      </button>
                      <button
                        v-if="step.children.length"
                        class="mind-toggle"
                        :title="isCollapsed(step.key) ? '展开' : '收起'"
                        @click="toggleNode(step.key)"
                      >
                        {{ isCollapsed(step.key) ? '展开' : '收起' }}
                      </button>
                      <button v-else class="mind-add" title="添加子节点" @click="addChildTo(step)">+</button>
                    </div>
                    <div v-if="step.children.length && !isCollapsed(step.key)" class="mind-children">
                      <div v-for="expected in step.children" :key="expected.key" class="mind-branch">
                        <div class="mind-node-wrap">
                          <button class="mind-node expected" :data-node-key="expected.key" :class="[{ active: selectedKey === expected.key }, statusClass(expected)]" @click="onNodeClick(expected)">
                            <span>{{ expected.name }}</span>
                            <select class="case-status-select" :class="statusClass(expected)" :value="expected.executionStatus" @click.stop @change="changeNodeStatus(expected, $event)">
                              <option value="PENDING">待开始</option>
                              <option value="PASSED">通过</option>
                              <option value="FAILED">失败</option>
                            </select>
                          </button>
                          <button v-if="expected.children.length" class="mind-toggle" :title="isCollapsed(expected.key) ? '展开' : '收起'" @click="toggleNode(expected.key)">
                            {{ isCollapsed(expected.key) ? '展开' : '收起' }}
                          </button>
                          <button v-else class="mind-add" title="添加子节点" @click="addChildTo(expected)">+</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          </div>
          <el-empty v-else description="暂无用例节点" />
        </div>
      </div>
    </article>

    <el-dialog v-model="nodeDialogVisible" title="节点详情" width="520px">
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
          <el-form-item label="执行状态">
            <el-radio-group v-model="selectedNode.executionStatus">
              <el-radio-button label="PENDING">待开始</el-radio-button>
              <el-radio-button label="PASSED">通过</el-radio-button>
              <el-radio-button label="FAILED">失败</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="selectedNode.description" type="textarea" maxlength="1000" show-word-limit />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="addChildNode">新增子节点</el-button>
          <el-button @click="addRootNode">新增根节点</el-button>
          <el-button type="danger" @click="removeSelectedNode">删除节点</el-button>
          <el-button type="primary" :loading="saving" @click="completeNodeEdit">完成</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getCaseSuite, saveCaseNodes, exportCaseSuite, type CaseSuiteDetail } from '../api/caseSuites';
import {
  findNode,
  findParent,
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
const nodeDialogVisible = ref(false);
const collapsedKeys = ref<Set<string>>(new Set());
const zoom = ref(1);
const mindCanvasRef = ref<HTMLElement>();
const mindRootRef = ref<HTMLElement>();
const mindPaths = ref<string[]>([]);
const svgSize = ref({ width: 0, height: 0 });

const zoomPercent = computed(() => Math.round(zoom.value * 100));
const mindCanvasStyle = computed(() => ({
  transform: `scale(${zoom.value})`
}));
const selectedNode = computed(() => findNode(treeData.value, selectedKey.value));
const statusSummary = computed(() => {
  const summary = { PENDING: 0, PASSED: 0, FAILED: 0 };
  collectLeafStatuses(treeData.value, summary);
  return summary;
});

function onNodeClick(data: EditableNode) {
  selectedKey.value = data.key;
  nodeDialogVisible.value = true;
}

async function changeNodeStatus(node: EditableNode, event: Event) {
  node.executionStatus = (event.target as HTMLSelectElement).value as EditableNode['executionStatus'];
  await saveTree(false);
}

function scheduleRenderLines() {
  nextTick(() => renderLines());
}

function renderLines() {
  const canvas = mindCanvasRef.value;
  const root = mindRootRef.value;
  if (!canvas || !root) {
    mindPaths.value = [];
    return;
  }

  svgSize.value = {
    width: canvas.scrollWidth,
    height: canvas.scrollHeight
  };

  const paths: string[] = [];
  for (const module of treeData.value) {
    addPath(paths, root, module.key);
    collectNodePaths(module, paths);
  }
  mindPaths.value = paths;
}

function collectNodePaths(parent: EditableNode, paths: string[]) {
  if (isCollapsed(parent.key)) {
    return;
  }
  for (const child of parent.children) {
    addPath(paths, getNodeElement(parent.key), child.key);
    collectNodePaths(child, paths);
  }
}

function addPath(paths: string[], parent: HTMLElement | null | undefined, childKey: string) {
  const child = getNodeElement(childKey);
  const canvas = mindCanvasRef.value;
  if (!parent || !child || !canvas) {
    return;
  }

  const canvasRect = canvas.getBoundingClientRect();
  const parentRect = parent.getBoundingClientRect();
  const childRect = child.getBoundingClientRect();
  const scale = zoom.value;
  const startX = (parentRect.right - canvasRect.left) / scale;
  const startY = (parentRect.top + parentRect.height / 2 - canvasRect.top) / scale;
  const endX = (childRect.left - canvasRect.left) / scale;
  const endY = (childRect.top + childRect.height / 2 - canvasRect.top) / scale;
  const midX = startX + Math.max(32, (endX - startX) / 2);
  paths.push(`M ${startX} ${startY} C ${midX} ${startY}, ${midX} ${endY}, ${endX} ${endY}`);
}

function getNodeElement(key: string) {
  return mindCanvasRef.value?.querySelector<HTMLElement>(`[data-node-key="${key}"]`);
}

function syncSelectedLabel() {
  const node = selectedNode.value;
  if (node) {
    node.label = node.name;
    scheduleRenderLines();
  }
}

async function addChildTo(parent: EditableNode) {
  const child: EditableNode = {
    key: nextNodeKey(),
    label: '新节点',
    nodeType: nextChildType(parent.nodeType),
    name: '新节点',
    description: '',
    sortOrder: parent.children.length,
    executionStatus: 'PENDING',
    children: []
  };
  parent.children.push(child);
  collapsedKeys.value.delete(parent.key);
  selectedKey.value = child.key;
  nodeDialogVisible.value = true;
  scheduleRenderLines();
  await saveTree(false);
}

async function addChildNode() {
  const parent = selectedNode.value;
  if (!parent) {
    ElMessage.warning('请先选择父节点');
    return;
  }
  await addChildTo(parent);
}

async function addRootNode() {
  const node: EditableNode = {
    key: nextNodeKey(),
    label: '新模块',
    nodeType: 'module',
    name: '新模块',
    description: '',
    sortOrder: treeData.value.length,
    executionStatus: 'PENDING',
    children: []
  };
  treeData.value.push(node);
  selectedKey.value = node.key;
  nodeDialogVisible.value = true;
  scheduleRenderLines();
  await saveTree(false);
}

async function removeSelectedNode() {
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
    collapsedKeys.value.delete(key);
    selectedKey.value = undefined;
    nodeDialogVisible.value = false;
    scheduleRenderLines();
    await saveTree(false);
  }
}

function isCollapsed(key: string) {
  return collapsedKeys.value.has(key);
}

function toggleNode(key: string) {
  const next = new Set(collapsedKeys.value);
  if (next.has(key)) {
    next.delete(key);
  } else {
    next.add(key);
  }
  collapsedKeys.value = next;
  scheduleRenderLines();
}

function expandAll() {
  collapsedKeys.value = new Set();
  scheduleRenderLines();
}

function zoomIn() {
  zoom.value = Math.min(1.5, Number((zoom.value + 0.1).toFixed(1)));
  scheduleRenderLines();
}

function zoomOut() {
  zoom.value = Math.max(0.5, Number((zoom.value - 0.1).toFixed(1)));
  scheduleRenderLines();
}

function resetZoom() {
  zoom.value = 1;
  scheduleRenderLines();
}

function collapseAll() {
  const keys = new Set<string>();
  collectBranchKeys(treeData.value, keys);
  collapsedKeys.value = keys;
  scheduleRenderLines();
}

function collectBranchKeys(nodes: EditableNode[], keys: Set<string>) {
  for (const node of nodes) {
    if (node.children.length) {
      keys.add(node.key);
      collectBranchKeys(node.children, keys);
    }
  }
}

function nextChildType(type: EditableNode['nodeType']) {
  if (type === 'module') {
    return 'case';
  }
  if (type === 'case') {
    return 'step';
  }
  return 'expected';
}

function collectLeafStatuses(nodes: EditableNode[], summary: Record<'PENDING' | 'PASSED' | 'FAILED', number>) {
  for (const node of nodes) {
    summary[node.executionStatus] += 1;
    if (node.children.length) {
      collectLeafStatuses(node.children, summary);
    }
  }
}

function statusClass(node: EditableNode) {
  return `status-${node.executionStatus.toLowerCase()}`;
}

async function completeNodeEdit() {
  nodeDialogVisible.value = false;
  await saveTree(false);
}

async function loadSuite() {
  if (!suiteId.value) {
    return;
  }
  loading.value = true;
  try {
    suite.value = await getCaseSuite(suiteId.value);
    resetNodeSeq();
    collapsedKeys.value = new Set();
    treeData.value = (suite.value.nodes ?? []).map((node, index) => toEditable(node, index));
    selectedKey.value = treeData.value[0]?.key;
    scheduleRenderLines();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用例集加载失败');
  } finally {
    loading.value = false;
  }
}

async function saveTree(showMessage = true) {
  if (!suiteId.value || saving.value) {
    return;
  }
  if (!treeData.value.length) {
    if (showMessage) {
      ElMessage.warning('用例树不能为空');
    }
    return;
  }

  saving.value = true;
  try {
    suite.value = await saveCaseNodes(suiteId.value, toPayload(treeData.value));
    await exportCaseSuite(suiteId.value);
    if (showMessage) {
      suite.value = await getCaseSuite(suiteId.value);
      resetNodeSeq();
      treeData.value = (suite.value.nodes ?? []).map((node, index) => toEditable(node, index));
    }
    scheduleRenderLines();
    if (showMessage) {
      ElMessage.success('用例树已保存并生成 XMind 文件');
    }
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
