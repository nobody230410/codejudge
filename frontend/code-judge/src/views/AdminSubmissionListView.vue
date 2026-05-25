<template>
  <div class="page">
    <div class="toolbar-head">
      <h2 class="heading">提交管理</h2>
    </div>

    <div class="filter-row">
      <el-input-number
        v-model="filters.userId"
        class="filter-id"
        :min="1"
        :precision="0"
        placeholder="用户 ID"
        controls-position="right"
      />
      <el-input v-model="filters.username" class="filter-name" placeholder="用户名（模糊）" clearable />
      <el-select v-model="filters.problemId" class="filter-problem" placeholder="题目" clearable filterable teleported>
        <el-option v-for="p in problemOptions" :key="p.problemId" :label="`${p.problemId} · ${p.title}`" :value="p.problemId" />
      </el-select>
      <el-button round type="primary" class="btn-search" :loading="loading" @click="search">查询</el-button>
    </div>

    <div v-if="!hasQueried" class="pre-query-hint">
      <p>请设置用户 ID、用户名或题目等条件后，点击「查询」查看提交记录。</p>
    </div>

    <template v-else>
      <div v-loading="loading" class="list-wrap">
        <el-empty v-if="!loading && rows.length === 0" description="当前条件下暂无提交记录" />
        <template v-else>
          <article v-for="row in rows" :key="row.id" class="sub-strip">
            <div class="strip-main">
              <span class="strip-id">#{{ row.id }}</span>
              <div class="strip-mid">
                <span class="strip-user">{{ row.username || '—' }} <span class="uid">(uid {{ row.userId }})</span></span>
                <span class="strip-problem">{{ row.problemTitle || '—' }} <span class="pid">#{{ row.problemId }}</span></span>
              </div>
              <div class="strip-meta">
                <span class="tag">{{ row.language || '—' }}</span>
                <span class="tag tag-status">{{ row.status || '—' }}</span>
                <span class="time">{{ formatTime(row.submitTime) }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button round class="btn-strip btn-view" @click="goDetail(row.id)">查看</el-button>
            </div>
          </article>
        </template>
      </div>

      <div v-if="total > 0" class="pager-wrap">
        <el-pagination
          v-model:current-page="page"
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="total"
          @current-change="load"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminSubmissions } from '@/api/admin'
import { fetchProblemList } from '@/api/problem'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const problemOptions = ref([])
/** 仅在为 true 时展示列表（未点击过查询则不展示） */
const hasQueried = ref(false)

const filters = reactive({
  username: '',
  userId: undefined,
  problemId: undefined,
})

function formatTime(t) {
  if (!t) return '—'
  try {
    return new Date(t).toLocaleString()
  } catch {
    return String(t)
  }
}

function buildQueryParams() {
  const q = {
    page: page.value,
    pageSize,
  }
  const u = filters.username?.trim()
  if (u) q.username = u
  if (filters.userId != null && filters.userId > 0) q.userId = filters.userId
  if (filters.problemId != null && filters.problemId > 0) q.problemId = filters.problemId
  return q
}

async function load() {
  if (!hasQueried.value) return
  loading.value = true
  try {
    const { data } = await fetchAdminSubmissions(buildQueryParams())
    rows.value = Array.isArray(data?.records) ? data.records : []
    total.value = typeof data?.total === 'number' ? data.total : 0
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search() {
  hasQueried.value = true
  page.value = 1
  load()
}

function goDetail(id) {
  router.push({ name: 'admin-submission-detail', params: { submissionId: String(id) } })
}

async function loadProblems() {
  try {
    const { data } = await fetchProblemList()
    problemOptions.value = data?.problems ?? []
  } catch {
    problemOptions.value = []
  }
}

onMounted(loadProblems)
</script>

<style scoped>
.page {
  width: 100%;
}

.toolbar-head {
  margin-bottom: 1rem;
}

.heading {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f0f0f4;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.65rem;
  margin-bottom: 1.25rem;
}

.filter-id {
  flex: 1;
  min-width: 0;
}

.filter-name {
  flex: 1;
  min-width: 0;
}

.filter-problem {
  flex: 2;
  min-width: 0;
}

.filter-row .btn-search {
  flex-shrink: 0;
}

.filter-id :deep(.el-input-number) {
  width: 100%;
}

.filter-id :deep(.el-input__wrapper) {
  border-radius: 999px;
}

.filter-name :deep(.el-input__wrapper) {
  border-radius: 999px;
}

.filter-problem :deep(.el-select__wrapper) {
  border-radius: 999px;
}

.btn-search {
  border-radius: 999px !important;
  --el-button-bg-color: #e8e8ee;
  --el-button-border-color: #e8e8ee;
  --el-button-text-color: #1a1a1e;
}

.pre-query-hint {
  padding: 2rem 1.25rem;
  border-radius: 14px;
  background: rgba(34, 34, 40, 0.65);
  border: 1px dashed rgba(255, 255, 255, 0.12);
  text-align: center;
}

.pre-query-hint p {
  margin: 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.6;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-height: 120px;
}

.list-wrap :deep(.el-empty__description p) {
  color: rgba(255, 255, 255, 0.55);
}

.sub-strip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem 1.25rem;
  padding: 1rem 1.2rem;
  border-radius: 14px;
  background: rgba(34, 34, 40, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 22px rgba(0, 0, 0, 0.28);
}

.strip-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.85rem 1rem;
  flex: 1;
  min-width: min(100%, 240px);
}

.strip-id {
  font-variant-numeric: tabular-nums;
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.strip-mid {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 120px;
  flex: 1;
}

.strip-user {
  font-size: 0.9rem;
  font-weight: 500;
  color: #e8e8ec;
}

.uid,
.pid {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.45);
  font-weight: 400;
}

.strip-problem {
  font-size: 0.8125rem;
  color: rgba(255, 255, 255, 0.78);
}

.strip-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
}

.tag {
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.tag-status {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
}

.actions {
  flex-shrink: 0;
}

.btn-strip {
  border-radius: 999px !important;
}

.btn-view {
  border-color: rgba(255, 255, 255, 0.22) !important;
  color: rgba(255, 255, 255, 0.92) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.25rem;
}

.pager-wrap :deep(.el-pagination.is-background .el-pager li.is-active) {
  border-radius: 999px;
}

.pager-wrap :deep(.el-pagination.is-background .btn-prev),
.pager-wrap :deep(.el-pagination.is-background .btn-next),
.pager-wrap :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 999px;
}
</style>
