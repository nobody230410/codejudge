<template>
  <div class="page">
    <div class="toolbar-head">
      <h2 class="heading">账号设置</h2>
    </div>

    <div v-loading="loading" class="panels">
      <section class="panel">
        <h3 class="panel-title">基本信息</h3>
        <el-form label-position="top" class="form">
          <el-form-item label="用户名">
            <el-input :model-value="profile.username" disabled round />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="emailForm.email" type="email" placeholder="name@example.com" round />
          </el-form-item>
          <el-form-item>
            <el-button round type="primary" class="btn-save" :loading="emailSaving" @click="saveEmail">
              保存邮箱
            </el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="panel">
        <h3 class="panel-title">修改密码</h3>
        <el-form label-position="top" class="form">
          <el-form-item label="当前密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password autocomplete="current-password" round />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password autocomplete="new-password" round />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password autocomplete="new-password" round />
          </el-form-item>
          <el-form-item>
            <el-button round type="primary" class="btn-save" :loading="pwdSaving" @click="savePassword">
              更新密码
            </el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserProfile, updateUserEmail, changeUserPassword } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const emailSaving = ref(false)
const pwdSaving = ref(false)

const profile = reactive({
  id: 0,
  username: '',
  email: '',
})

const emailForm = reactive({ email: '' })
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function load() {
  loading.value = true
  try {
    const { data } = await fetchUserProfile()
    profile.id = data?.id ?? 0
    profile.username = data?.username ?? ''
    profile.email = data?.email ?? ''
    emailForm.email = profile.email || ''
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载资料失败')
  } finally {
    loading.value = false
  }
}

async function saveEmail() {
  emailSaving.value = true
  try {
    const { data } = await updateUserEmail(emailForm.email)
    profile.email = data?.email ?? emailForm.email
    authStore.patchUserInfo({ email: profile.email })
    ElMessage.success('邮箱已更新')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    emailSaving.value = false
  }
}

async function savePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  pwdSaving.value = true
  try {
    await changeUserPassword(pwdForm.oldPassword, pwdForm.newPassword)
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    ElMessage.success('密码已更新')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '修改密码失败')
  } finally {
    pwdSaving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  width: 100%;
}

.toolbar-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}

.heading {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2e2e34;
}

.panels {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 120px;
}

.panel {
  padding: 1.15rem 1.25rem;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 22px rgba(0, 0, 0, 0.06);
}

.panel-title {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: #1c1c20;
}

.form {
  max-width: 420px;
}

.btn-save {
  border-radius: 999px !important;
  --el-button-bg-color: #000000;
  --el-button-border-color: #000000;
  --el-button-hover-bg-color: #333333;
  --el-button-hover-border-color: #333333;
}
</style>
