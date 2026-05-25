<template>
  <div class="admin-login-page">
    <header class="hero">
      <h1 class="brand">CODE JUDGE</h1>
      <p class="tagline">在线评测系统</p>
    </header>

    <div class="card">
      <h2 class="card-title">管理员登录</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="form"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" autocomplete="username" clearable size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
            clearable
            size="large"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-form-item class="actions">
          <el-button type="primary" class="btn-primary" :loading="loading" size="large" @click="submit">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="divider" aria-hidden="true" />

      <div class="secondary-actions">
        <el-button class="btn-secondary" size="large" @click="goUserLogin">返回普通用户登录</el-button>
      </div>

      <p class="hint">此为管理员专用入口，请使用后台分配的账号。</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function extractErrorMessage(err) {
  const msg = err?.response?.data?.message
  return typeof msg === 'string' ? msg : null
}

function goUserLogin() {
  router.push({ name: 'login', query: route.query.redirect ? { redirect: route.query.redirect } : {} })
}

async function submit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  loading.value = true
  try {
    const { data } = await login({
      username: form.username.trim(),
      password: form.password,
      userType: 'ADMIN',
    })
    if (!data?.token) {
      ElMessage.error(data?.message || '登录失败')
      return
    }
    if (data?.userInfo?.userType !== 'ADMIN') {
      ElMessage.error('该账号不是管理员')
      return
    }
    authStore.setSession(data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect.startsWith('/')) {
      await router.replace(redirect)
    } else {
      await router.replace({ name: 'admin-problems' })
    }
  } catch (e) {
    ElMessage.error(extractErrorMessage(e) || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem 3rem;
  box-sizing: border-box;
  font-family: 'DM Sans', system-ui, -apple-system, sans-serif;
  background: linear-gradient(165deg, #141418 0%, #1c1c22 38%, #121216 100%);
  color: #e6e6ea;
}

.hero {
  text-align: center;
  margin-bottom: 2rem;
}

.brand {
  margin: 0;
  font-family: 'Orbitron', ui-sans-serif, system-ui, sans-serif;
  font-size: clamp(1.75rem, 5vw, 2.25rem);
  font-weight: 700;
  letter-spacing: 0.28em;
  color: #ffffff;
  text-shadow: 0 0 48px rgba(255, 255, 255, 0.12);
}

.tagline {
  margin: 0.65rem 0 0;
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: 0.35em;
  color: rgba(255, 255, 255, 0.52);
  text-transform: uppercase;
}

.card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  max-width: 400px;
  padding: 2rem 2rem 1.75rem;
  background: rgba(34, 34, 40, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow:
    0 8px 40px rgba(0, 0, 0, 0.45),
    0 1px 0 rgba(255, 255, 255, 0.06) inset;
}

.card-title {
  margin: 0 0 1.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #f0f0f4;
}

.form {
  width: 100%;
}

.form :deep(.el-form-item) {
  width: 100%;
}

.form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.72);
  font-weight: 500;
  font-size: 0.8125rem;
}

.form :deep(.el-form-item__content) {
  width: 100%;
  max-width: 100%;
  margin-left: 0 !important;
  justify-content: flex-start;
}

.form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(18, 18, 22, 0.85);
}

.form :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.22);
}

.form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(255, 255, 255, 0.38);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.form :deep(.el-input__inner) {
  color: #f2f2f6;
}

.form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.35);
}

.actions {
  margin-bottom: 0;
}

.actions :deep(.el-form-item__content) {
  display: flex;
  justify-content: stretch;
}

.actions :deep(.el-form-item__content .el-button) {
  flex: 1;
  width: 100%;
}

.btn-primary {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 0.08em;
  --el-button-bg-color: #e8e8ee;
  --el-button-border-color: #e8e8ee;
  --el-button-text-color: #1a1a1e;
  --el-button-hover-bg-color: #ffffff;
  --el-button-hover-border-color: #ffffff;
  --el-button-hover-text-color: #121216;
  --el-button-active-bg-color: #d8d8de;
  --el-button-active-border-color: #d8d8de;
}

.divider {
  height: 1px;
  margin: 1.625rem 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.18) 20%,
    rgba(255, 255, 255, 0.18) 80%,
    transparent
  );
}

.secondary-actions {
  width: 100%;
}

.btn-secondary {
  width: 100%;
  max-width: 100%;
  margin-left: 0;
  margin-right: 0;
  box-sizing: border-box;
  border-radius: 10px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.24);
  color: #ffffff;
}

.hint {
  margin: 1.25rem 0 0;
  font-size: 0.6875rem;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.42);
  text-align: center;
}
</style>
