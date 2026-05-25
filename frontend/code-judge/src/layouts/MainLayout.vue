<template>
  <div class="layout" :class="{ 'layout--admin': authStore.isAdmin }">
    <header class="top">
      <router-link class="brand" :to="{ name: brandRoute }">CODE JUDGE</router-link>
      <nav class="nav">
        <template v-if="authStore.isAdmin">
          <router-link class="nav-link" :to="{ name: 'admin-problems' }" active-class="nav-link--active">
            题库管理
          </router-link>
          <router-link class="nav-link" :to="{ name: 'admin-users' }" active-class="nav-link--active">
            用户管理
          </router-link>
          <router-link class="nav-link" :to="{ name: 'admin-submissions' }" active-class="nav-link--active">
            提交管理
          </router-link>
        </template>
        <template v-else>
          <router-link class="nav-link" :to="{ name: 'problems' }" active-class="nav-link--active">
            题库
          </router-link>
          <router-link class="nav-link" :to="{ name: 'code-run' }" active-class="nav-link--active">
            代码运行
          </router-link>
          <router-link class="nav-link" :to="{ name: 'user-account' }" active-class="nav-link--active">
            账号设置
          </router-link>
        </template>
      </nav>
      <div class="right">
        <span v-if="authStore.userInfo" class="who">
          {{ authStore.userInfo.username }}
          <span class="role-tag">{{ authStore.isAdmin ? '管理员' : '用户' }}</span>
        </span>
        <el-button round class="btn-outline" size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onUnmounted, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const brandRoute = computed(() => (authStore.isAdmin ? 'admin-problems' : 'problems'))

watchEffect(() => {
  document.documentElement.classList.toggle('admin-app', authStore.isAdmin)
})

onUnmounted(() => {
  document.documentElement.classList.remove('admin-app')
})

async function handleLogout() {
  const goAdminLogin = authStore.isAdmin
  try {
    await logout()
  } catch {
    /* ignore */
  }
  authStore.clearSession()
  ElMessage.success('已退出')
  await router.replace({ name: goAdminLogin ? 'login-admin' : 'login' })
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'DM Sans', system-ui, -apple-system, sans-serif;
  background: linear-gradient(180deg, #dcdcdf 0%, #e8e8ec 42%, #e2e2e6 100%);
  color: #2a2a2e;
}

.top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: rgba(252, 252, 254, 0.88);
  box-shadow: 0 6px 28px rgba(0, 0, 0, 0.07);
  border-bottom: none;
}

.brand {
  font-family: 'Orbitron', ui-sans-serif, system-ui, sans-serif;
  font-weight: 700;
  letter-spacing: 0.28em;
  font-size: clamp(0.85rem, 2vw, 1rem);
  color: #1c1c20;
  text-decoration: none;
  text-shadow: 0 0 24px rgba(255, 255, 255, 0.35);
}

.nav {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  align-items: center;
}

.nav-link {
  font-size: 0.875rem;
  font-weight: 500;
  color: #5c5c66;
  text-decoration: none;
  padding: 0.45rem 0.9rem;
  border-radius: 999px;
  background: transparent;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}

.nav-link:hover {
  color: #2e2e34;
  background: rgba(0, 0, 0, 0.05);
}

.nav-link--active {
  color: #1c1c20;
  background: rgba(0, 0, 0, 0.08);
  font-weight: 600;
}

.right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.who {
  font-size: 0.8125rem;
  color: #4a4a52;
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.role-tag {
  font-size: 0.6875rem;
  padding: 0.15rem 0.45rem;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.06);
  color: #6a6a72;
}

.btn-outline {
  border-radius: 999px !important;
  border-color: #a8a8b4 !important;
  color: #3a3a42 !important;
  background: rgba(255, 255, 255, 0.85) !important;
}

.btn-outline:hover {
  background: #3a3a42 !important;
  color: #fafafc !important;
  border-color: #3a3a42 !important;
}

.main {
  flex: 1;
  padding: 1.5rem;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.layout--admin {
  background: linear-gradient(165deg, #141418 0%, #1c1c22 38%, #121216 100%);
  color: #e6e6ea;
}

.layout--admin .top {
  background: rgba(34, 34, 40, 0.92);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.45);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.layout--admin .brand {
  color: #ffffff;
  text-shadow: 0 0 48px rgba(255, 255, 255, 0.12);
}

.layout--admin .nav-link {
  color: rgba(255, 255, 255, 0.65);
}

.layout--admin .nav-link:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.layout--admin .nav-link--active {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.14);
  font-weight: 600;
}

.layout--admin .who {
  color: rgba(255, 255, 255, 0.78);
}

.layout--admin .role-tag {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.88);
}

.layout--admin .btn-outline {
  border-color: rgba(255, 255, 255, 0.26) !important;
  color: rgba(255, 255, 255, 0.92) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.layout--admin .btn-outline:hover {
  background: rgba(255, 255, 255, 0.16) !important;
  color: #ffffff !important;
  border-color: rgba(255, 255, 255, 0.38) !important;
}
</style>
