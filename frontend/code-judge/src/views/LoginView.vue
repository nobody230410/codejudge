<template>
  <div class="login-page">
    <header class="hero">
      <h1 class="brand">CODE JUDGE</h1>
      <p class="tagline">在线评测系统</p>
    </header>

    <div class="card">
      <h2 class="card-title">{{ mode === 'login' ? '登录' : '注册' }}</h2>

      <el-form
        v-if="mode === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="form"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="loginForm.username" autocomplete="username" clearable size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            autocomplete="current-password"
            clearable
            size="large"
            @keyup.enter="submitLogin"
          />
        </el-form-item>
        <el-form-item class="actions">
          <el-button type="primary" class="btn-primary" :loading="loading" size="large" @click="submitLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        class="form"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="registerForm.username" autocomplete="username" clearable size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            autocomplete="new-password"
            clearable
            size="large"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            show-password
            autocomplete="new-password"
            clearable
            size="large"
            @keyup.enter="submitRegister"
          />
        </el-form-item>
        <el-form-item class="actions">
          <el-button type="primary" class="btn-primary" :loading="registerLoading" size="large" @click="submitRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="divider" aria-hidden="true" />

      <div class="secondary-actions">
        <el-button
          v-if="mode === 'login'"
          class="btn-secondary"
          size="large"
          @click="goAdminLogin"
        >
          管理员登录
        </el-button>
        <el-button class="btn-secondary" size="large" @click="toggleMode">
          {{ mode === 'login' ? '创建新账号' : '返回登录' }}
        </el-button>
      </div>

      <p class="hint">注册将创建普通用户账号；管理员请从「管理员登录」入口进入。</p>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const mode = ref('login')
const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const adminLoginTo = computed(() => {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.startsWith('/')
    ? { name: 'login-admin', query: { redirect } }
    : { name: 'login-admin' }
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, val, cb) => {
        if (val !== registerForm.password) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
}

function extractErrorMessage(err) {
  const msg = err?.response?.data?.message
  return typeof msg === 'string' ? msg : null
}

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
}

function goAdminLogin() {
  router.push(adminLoginTo.value)
}

async function submitLogin() {
  await loginFormRef.value?.validate().catch(() => Promise.reject())
  loading.value = true
  try {
    const { data } = await login({
      username: loginForm.username.trim(),
      password: loginForm.password,
      userType: 'USER',
    })
    if (!data?.token) {
      ElMessage.error(data?.message || '登录失败')
      return
    }
    authStore.setSession(data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    const isAdmin = data?.userInfo?.userType === 'ADMIN'
    if (typeof redirect === 'string' && redirect.startsWith('/')) {
      await router.replace(redirect)
    } else if (isAdmin) {
      await router.replace({ name: 'admin-problems' })
    } else {
      await router.replace({ name: 'problems' })
    }
  } catch (e) {
    ElMessage.error(extractErrorMessage(e) || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  await registerFormRef.value?.validate().catch(() => Promise.reject())
  registerLoading.value = true
  try {
    await register({
      username: registerForm.username.trim(),
      password: registerForm.password,
    })
    ElMessage.success('注册成功，请登录')
    loginForm.username = registerForm.username.trim()
    loginForm.password = ''
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    mode.value = 'login'
  } catch (e) {
    ElMessage.error(extractErrorMessage(e) || '注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem 3rem;
  box-sizing: border-box;
  font-family: 'DM Sans', system-ui, -apple-system, sans-serif;
  background: linear-gradient(180deg, #dcdcdf 0%, #e8e8ec 45%, #dedee2 100%);
  color: #2a2a2e;
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
  color: #1c1c20;
  text-shadow: 0 0 40px rgba(255, 255, 255, 0.35);
}

.tagline {
  margin: 0.65rem 0 0;
  font-size: 0.875rem;
  font-weight: 500;
  letter-spacing: 0.35em;
  color: #6a6a72;
  text-transform: uppercase;
}

.card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  max-width: 400px;
  padding: 2rem 2rem 1.75rem;
  background: #f0f0f3;
  border-radius: 16px;
  border: none;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.06),
    0 1px 0 rgba(255, 255, 255, 0.65) inset;
}

.card-title {
  margin: 0 0 1.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #3a3a42;
}

.form {
  width: 100%;
}

.form :deep(.el-form-item) {
  width: 100%;
}

.form :deep(.el-form-item__label) {
  color: #5c5c66;
  font-weight: 500;
  font-size: 0.8125rem;
}

.form :deep(.el-form-item__content) {
  width: 100%;
  max-width: 100%;
  margin-left: 0 !important;
  justify-content: flex-start;
}

.form :deep(.el-input__wrapper),
.form :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid #c8c8d0;
  background: #fafafc;
}

.form :deep(.el-input__wrapper:hover),
.form :deep(.el-select .el-input__wrapper:hover) {
  border-color: #a8a8b4;
}

.form :deep(.el-input__wrapper.is-focus),
.form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #6e6e78;
  box-shadow: 0 0 0 1px rgba(46, 46, 52, 0.12);
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
  --el-button-bg-color: #3a3a42;
  --el-button-border-color: #3a3a42;
  --el-button-hover-bg-color: #52525c;
  --el-button-hover-border-color: #52525c;
  --el-button-active-bg-color: #2e2e34;
  --el-button-active-border-color: #2e2e34;
}

.divider {
  height: 1px;
  margin: 1.625rem 0;
  background: linear-gradient(90deg, transparent, #c0c0c8 20%, #c0c0c8 80%, transparent);
}

.secondary-actions {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
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
  color: #3a3a42;
  background: #e4e4e8;
  border: 1px solid #c8c8d0;
}

.btn-secondary:hover {
  background: #dcdce2;
  border-color: #b8b8c2;
  color: #2a2a2e;
}

.hint {
  margin: 1.25rem 0 0;
  font-size: 0.6875rem;
  line-height: 1.5;
  color: #888892;
  text-align: center;
}
</style>
