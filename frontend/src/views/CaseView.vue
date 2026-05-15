<template>
  <section class="workspace">
    <article class="panel">
      <h2>用例树</h2>
      <el-tree :data="caseTree" node-key="id" default-expand-all :expand-on-click-node="false" />
    </article>

    <article class="panel">
      <h2>节点详情</h2>
      <el-form label-width="88px">
        <el-form-item label="节点名称">
          <el-input model-value="密码登录成功" />
        </el-form-item>
        <el-form-item label="节点类型">
          <el-select model-value="case">
            <el-option label="模块" value="module" />
            <el-option label="用例" value="case" />
            <el-option label="步骤" value="step" />
            <el-option label="预期" value="expected" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input type="textarea" model-value="下一步接入 XMind 上传解析和用例树保存接口。" />
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button>新增子节点</el-button>
        <el-button>删除节点</el-button>
        <el-button type="primary">保存修改</el-button>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
const caseTree = [
  {
    id: 'module-login',
    label: '登录模块',
    children: [
      {
        id: 'case-login-success',
        label: '密码登录成功',
        children: [
          { id: 'step-input-account', label: '输入有效账号和密码' },
          { id: 'expected-login-home', label: '进入系统首页' }
        ]
      }
    ]
  }
];
</script>
