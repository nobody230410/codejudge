<template>
  <div class="page">
    <h2 class="heading">题库</h2>
    <div v-loading="loading" class="list-wrap">
      <el-empty v-if="!loading && sortedRows.length === 0" description="暂无题目" />
      <template v-else>
        <article
          v-for="row in pagedRows"
          :key="row.problemId"
          class="problem-strip problem-strip--click"
          role="button"
          tabindex="0"
          @click="onRowClick(row)"
          @keydown.enter="onRowClick(row)"
        >
          <span class="strip-seq">{{ row.displaySeq }}</span>
          <div class="strip-title-diff">
            <span class="strip-title">{{ row.title }}</span>
            <span class="strip-diff">{{ formatDifficulty(row.difficulty) }}</span>
          </div>
        </article>
      </template>
    </div>

    <div class="pager-wrap">
      <el-pagination
        v-model:current-page="page"
        background
        layout="prev, pager, next, total"
        :page-size="pageSize"
        :total="sortedRows.length"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProblemList } from '@/api/problem'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const page = ref(1)
const pageSize = 10

function rowSortKey(row) {
  const t = row.createdAt ? Date.parse(row.createdAt) : NaN
  if (!Number.isNaN(t)) return t
  return row.problemId ?? 0
}

const sortedRows = computed(() =>
  [...rows.value].sort((a, b) => {
    const ka = rowSortKey(a)
    const kb = rowSortKey(b)
    if (ka !== kb) return ka - kb
    return (a.problemId ?? 0) - (b.problemId ?? 0)
  }),
)

const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize
  const slice = sortedRows.value.slice(start, start + pageSize)
  return slice.map((row, i) => ({
    ...row,
    displaySeq: start + i + 1,
  }))
})

function formatDifficulty(v) {
  if (!v) return '—'
  const u = String(v).toUpperCase()
  const map = { EASY: '简单', MIDDLE: '中等', HARD: '困难' }
  return map[u] || v
}

function onRowClick(row) {
  router.push({ name: 'problem-detail', params: { problemId: row.problemId } })
}

async function load() {
  loading.value = true
  try {
    const { data } = await fetchProblemList()
    rows.value = data?.problems ?? []
    page.value = 1
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载题目列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  width: 100%;
}

.heading {
  margin: 0 0 1.25rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2e2e34;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-height: 100px;
}

.problem-strip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.85rem 1rem;
  padding: 1rem 1.2rem;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 22px rgba(0, 0, 0, 0.06);
}

.problem-strip--click {
  cursor: pointer;
  transition:
    box-shadow 0.15s ease,
    transform 0.12s ease,
    border-color 0.15s ease;
}

.problem-strip--click:hover {
  border-color: rgba(0, 0, 0, 0.1);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
}

.problem-strip--click:active {
  transform: scale(0.995);
}

.strip-seq {
  font-variant-numeric: tabular-nums;
  font-size: 0.875rem;
  font-weight: 600;
  color: #6a6a72;
  min-width: 2rem;
  flex-shrink: 0;
}

.strip-title-diff {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  column-gap: 0.65rem;
  row-gap: 0.25rem;
  flex: 1;
  min-width: 0;
}

.strip-title {
  flex: 0 1 auto;
  min-width: 140px;
  max-width: 100%;
  font-size: 0.9375rem;
  font-weight: 500;
  color: #1c1c20;
  line-height: 1.4;
}

.strip-diff {
  flex: 0 0 auto;
  font-size: 0.875rem;
  color: #4a4a52;
  white-space: nowrap;
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
