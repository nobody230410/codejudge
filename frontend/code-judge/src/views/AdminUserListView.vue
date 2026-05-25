<template>
  <div class="page">
    <div class="toolbar-head">
      <h2 class="heading">用户管理</h2>
    </div>
    <el-table v-loading="loading" :data="rows" class="table" empty-text="暂无用户">
      <el-table-column prop="id" label="#" width="72" />
      <el-table-column prop="username" label="用户名" min-width="160" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button round size="small" class="btn-reset" @click="openReset(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="resetVisible" title="重置用户密码" width="440px" destroy-on-close @closed="resetPwdForm">
      <el-form label-position="top">
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" show-password autocomplete="new-password" round />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="resetForm.confirmPassword" type="password" show-password autocomplete="new-password" round />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="resetVisible = false">取消</el-button>
        <el-button round type="primary" class="btn-save" :loading="resetSaving" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserList, resetUserPassword } from '@/api/admin'

const loading = ref(false)
const rows = ref([])

const resetVisible = ref(false)
const resetSaving = ref(false)
const resetTargetId = ref(null)
const resetForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

async function load() {
  loading.value = true
  try {
    const { data } = await fetchUserList()
    rows.value = Array.isArray(data) ? data : []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function resetPwdForm() {
  resetTargetId.value = null
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
}

function openReset(row) {
  resetTargetId.value = row.id
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
  resetVisible.value = true
}

async function submitReset() {
  if (!resetTargetId.value) return
  if (!resetForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  resetSaving.value = true
  try {
    await resetUserPassword(resetTargetId.value, resetForm.newPassword)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '重置失败')
  } finally {
    resetSaving.value = false
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
  color: #f0f0f4;
}

.table {
  width: 100%;
  border-radius: 14px;
  overflow: hidden;
  --el-table-bg-color: rgba(34, 34, 40, 0.92);
  --el-table-tr-bg-color: rgba(34, 34, 40, 0.92);
  --el-table-border-color: rgba(255, 255, 255, 0.08);
  --el-table-header-bg-color: rgba(255, 255, 255, 0.06);
  --el-table-header-text-color: rgba(255, 255, 255, 0.75);
  --el-table-row-hover-bg-color: rgba(34, 34, 40, 0.92);
  --el-table-text-color: #e8e8ec;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.35);
}

.table :deep(.el-table__body tr.hover-row > td.el-table__cell),
.table :deep(.el-table__body tr:hover > td.el-table__cell) {
  background-color: rgba(34, 34, 40, 0.92) !important;
}

.table :deep(td),
.table :deep(th) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.table :deep(.cell) {
  color: #e8e8ec;
}

.btn-reset {
  border-radius: 999px !important;
  border-color: rgba(255, 255, 255, 0.22) !important;
  color: rgba(255, 255, 255, 0.92) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.btn-reset:hover {
  background: rgba(255, 255, 255, 0.14) !important;
  border-color: rgba(255, 255, 255, 0.35) !important;
  color: #ffffff !important;
}

.btn-save {
  border-radius: 999px !important;
  --el-button-bg-color: #e8e8ee;
  --el-button-border-color: #e8e8ee;
  --el-button-text-color: #1a1a1e;
  --el-button-hover-bg-color: #ffffff;
  --el-button-hover-border-color: #ffffff;
  --el-button-hover-text-color: #121216;
}
</style>
