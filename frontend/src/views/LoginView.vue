<template>
  <el-card class="login-card">
    <template #header>登录测试平台</template>
    <el-form label-width="80px" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '../api/auth';
import { menuItems } from '../router';

const router = useRouter();
const loading = ref(false);
const form = reactive({ username: 'tester', password: '123456' });

async function submit() {
  loading.value = true;
  try {
    const response = await login(form.username, form.password);
    ElMessage.success('登录成功');
    const firstMenu = response.user.roles?.includes('ADMIN')
      ? menuItems[0]?.path
      : menuItems.find((item) => response.user.permissions?.includes(item.permission))?.path;
    router.push(firstMenu || '/projects');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-card {
  max-width: 460px;
  margin: 80px auto;
}
</style>
