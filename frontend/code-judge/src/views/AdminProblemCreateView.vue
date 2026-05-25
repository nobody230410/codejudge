<template>
  <div class="create-page">
    <div class="card">
      <p class="eyebrow">ADMIN</p>
      <h1 class="card-title">添加题目</h1>
      <p class="sub">填写题目信息后创建，可随时返回修改。</p>

      <el-form ref="formRef" class="form" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" size="large" placeholder="题目标题" clearable />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="10" placeholder="题目描述（支持多行）" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="选择难度" teleported size="large" style="width: 100%">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MIDDLE" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间上限（秒）" prop="timeLimit">
          <el-input-number
            v-model="form.timeLimit"
            :min="0.001"
            :step="0.5"
            :precision="3"
            controls-position="right"
            placeholder="可选，留空则判题默认 10s"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内存上限（KB）" prop="memoryLimit">
          <el-input-number
            v-model="form.memoryLimit"
            :min="1"
            :step="1024"
            :precision="0"
            controls-position="right"
            placeholder="可选，留空则判题默认 262144 KB"
            style="width: 100%"
          />
        </el-form-item>
        <div class="actions">
          <el-button round class="btn-secondary" size="large" @click="goBack">取消</el-button>
          <el-button round type="primary" class="btn-primary" size="large" :loading="saving" @click="submit">
            创建题目
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createProblem } from '@/api/problem'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const saving = ref(false)

const form = reactive({
  title: '',
  description: '',
  difficulty: 'EASY',
  timeLimit: null,
  memoryLimit: null,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

function goBack() {
  router.push({ name: 'admin-problems' })
}

async function submit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  const creatorId = authStore.userInfo?.id
  if (creatorId == null || Number.isNaN(Number(creatorId))) {
    ElMessage.error('无法获取管理员信息，请重新登录')
    return
  }
  saving.value = true
  try {
    await createProblem({
      title: form.title.trim(),
      description: form.description,
      difficulty: form.difficulty,
      creator: Number(creatorId),
      timeLimit: form.timeLimit ?? null,
      memoryLimit: form.memoryLimit ?? null,
    })
    ElMessage.success('题目已创建')
    await router.replace({ name: 'admin-problems' })
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '创建失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.create-page {
  width: 100%;
  box-sizing: border-box;
  font-family: 'DM Sans', system-ui, -apple-system, sans-serif;
  color: #e6e6ea;
}

.card {
  width: 100%;
  box-sizing: border-box;
  padding: 2rem 2rem 1.75rem;
  background: rgba(34, 34, 40, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow:
    0 8px 40px rgba(0, 0, 0, 0.45),
    0 1px 0 rgba(255, 255, 255, 0.06) inset;
}

.eyebrow {
  margin: 0 0 0.35rem;
  font-family: 'Orbitron', ui-sans-serif, system-ui, sans-serif;
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.35em;
  color: rgba(255, 255, 255, 0.45);
}

.card-title {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f0f0f4;
}

.sub {
  margin: 0 0 1.5rem;
  font-size: 0.8125rem;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.55);
}

.form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.72);
  font-weight: 500;
  font-size: 0.8125rem;
}

.form :deep(.el-input__wrapper),
.form :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(18, 18, 22, 0.85);
}

.form :deep(.el-input__wrapper:hover),
.form :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.22);
}

.form :deep(.el-input__wrapper.is-focus),
.form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: rgba(255, 255, 255, 0.38);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.form :deep(.el-input__inner) {
  color: #f2f2f6;
}

.form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.35);
}

.form :deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(18, 18, 22, 0.85);
  color: #f2f2f6;
  font-size: 0.9375rem;
  line-height: 1.55;
}

.form :deep(.el-textarea__inner:hover) {
  border-color: rgba(255, 255, 255, 0.22);
}

.form :deep(.el-textarea__inner:focus) {
  border-color: rgba(255, 255, 255, 0.38);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.btn-secondary {
  flex: 1;
  min-width: 120px;
  border-radius: 10px !important;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88) !important;
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.16) !important;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: rgba(255, 255, 255, 0.24) !important;
  color: #ffffff !important;
}

.btn-primary {
  flex: 1;
  min-width: 120px;
  border-radius: 10px !important;
  font-weight: 600;
  letter-spacing: 0.06em;
  --el-button-bg-color: #e8e8ee;
  --el-button-border-color: #e8e8ee;
  --el-button-text-color: #1a1a1e;
  --el-button-hover-bg-color: #ffffff;
  --el-button-hover-border-color: #ffffff;
  --el-button-hover-text-color: #121216;
}
</style>
