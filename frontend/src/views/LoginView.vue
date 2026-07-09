<template>
  <div class="auth-page">
    <el-card class="login-card">
      <template #header>
        <div class="card-head">
          <strong>测试平台</strong>
          <span>登录后进入测试管理工作台</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form label-width="80px" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="submitLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-width="90px" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="英文或数字" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.displayName" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="registerForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="所属组织">
              <el-select v-model="registerForm.organizationId" placeholder="请选择所属组织" filterable>
                <el-option v-for="item in organizations" :key="item.id" :label="item.orgName" :value="item.id">
                  <span>{{ item.orgName }}</span>
                  <span class="option-code">{{ item.orgCode }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="submitRegister">注册并登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login, register, registerOptions, type LoginResponse, type RegisterOrganizationOption } from '../api/auth';
import { showErrorMessage } from '../api/http';
import { menuItems } from '../router';

const router = useRouter();
const activeTab = ref('login');
const loading = ref(false);
const organizations = ref<RegisterOrganizationOption[]>([]);
const loginForm = reactive({ username: 'tester', password: '123456' });
const registerForm = reactive({ username: '', displayName: '', password: '', confirmPassword: '', organizationId: undefined as number | undefined });

onMounted(loadRegisterOptions);

async function loadRegisterOptions() {
  try {
    const response = await registerOptions();
    organizations.value = response.organizations;
    if (!registerForm.organizationId && response.organizations.length) {
      registerForm.organizationId = response.organizations[0].id;
    }
  } catch (error) {
    showErrorMessage(error, '注册选项加载失败');
  }
}

function firstMenuPath(response: LoginResponse) {
  return response.user.roles?.includes('ADMIN')
    ? menuItems[0]?.path
    : menuItems.find((item) => response.user.permissions?.includes(item.permission))?.path;
}

async function submitLogin() {
  loading.value = true;
  try {
    const response = await login(loginForm.username, loginForm.password);
    ElMessage.success('登录成功');
    router.push(firstMenuPath(response) || '/login');
  } catch (error) {
    showErrorMessage(error, '登录失败');
  } finally {
    loading.value = false;
  }
}

async function submitRegister() {
  loading.value = true;
  try {
    const response = await register(registerForm);
    ElMessage.success('注册成功');
    router.push(firstMenuPath(response) || '/login');
  } catch (error) {
    showErrorMessage(error, '注册失败');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: 32px; background: linear-gradient(135deg, #eef2ff, #f8fafc); }
.login-card { width: min(520px, 100%); }
.card-head { display: grid; gap: 4px; }
.card-head strong { font-size: 22px; }
.card-head span { color: #606266; font-size: 13px; }
.option-code { float: right; color: #909399; font-size: 12px; }
</style>
