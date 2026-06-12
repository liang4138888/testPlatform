import type { CaseNode, CaseNodePayload, ExecutionStatus, NodeType } from '../api/caseSuites';

export interface EditableNode {
  key: string;
  label: string;
  nodeType: NodeType;
  name: string;
  description?: string;
  sortOrder: number;
  executionStatus: ExecutionStatus;
  children: EditableNode[];
}

let nodeSeq = 1;

export function resetNodeSeq() {
  nodeSeq = 1;
}

export function nextNodeKey() {
  return `node-${nodeSeq++}`;
}

export function toEditable(node: CaseNode, index: number): EditableNode {
  return {
    key: `db-${node.id}`,
    label: node.name,
    nodeType: node.nodeType,
    name: node.name,
    description: node.description,
    sortOrder: node.sortOrder ?? index,
    executionStatus: node.executionStatus ?? 'PENDING',
    children: (node.children ?? []).map((child, childIndex) => toEditable(child, childIndex))
  };
}

export function toPayload(nodes: EditableNode[]): CaseNodePayload[] {
  return nodes.map((node, index) => ({
    nodeType: node.nodeType,
    name: node.name.trim(),
    description: node.description?.trim() || undefined,
    sortOrder: node.sortOrder ?? index,
    executionStatus: node.executionStatus,
    children: node.children.length ? toPayload(node.children) : []
  }));
}

export function findNode(nodes: EditableNode[], key?: string): EditableNode | undefined {
  if (!key) {
    return undefined;
  }
  for (const node of nodes) {
    if (node.key === key) {
      return node;
    }
    const child = findNode(node.children, key);
    if (child) {
      return child;
    }
  }
  return undefined;
}

export function findParent(nodes: EditableNode[], key: string, parent?: EditableNode): EditableNode | undefined {
  for (const node of nodes) {
    if (node.key === key) {
      return parent;
    }
    const found = findParent(node.children, key, node);
    if (found !== undefined) {
      return found;
    }
  }
  return undefined;
}

export function reindexSortOrders(nodes: EditableNode[]) {
  nodes.forEach((node, index) => {
    node.sortOrder = index;
    if (node.children.length) {
      reindexSortOrders(node.children);
    }
  });
}

export function isDescendant(nodes: EditableNode[], ancestorKey: string, targetKey: string): boolean {
  const ancestor = findNode(nodes, ancestorKey);
  if (!ancestor) {
    return false;
  }
  return containsKey(ancestor.children, targetKey);
}

function containsKey(nodes: EditableNode[], key: string): boolean {
  for (const node of nodes) {
    if (node.key === key) {
      return true;
    }
    if (containsKey(node.children, key)) {
      return true;
    }
  }
  return false;
}
