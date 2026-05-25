<template>
  <div class="page">
    <div class="toolbar-head">
      <h2 class="heading">题库管理</h2>
      <el-button round type="primary" class="btn-add-problem" @click="goCreate">添加题目</el-button>
    </div>
    <div v-loading="loading" class="list-wrap">
      <el-empty v-if="!loading && sortedRows.length === 0" description="暂无题目" />
      <template v-else>
        <article v-for="row in pagedRows" :key="row.problemId" class="problem-strip">
          <div class="strip-main">
            <span class="strip-id">{{ row.problemId }}</span>
            <div class="strip-title-diff">
              <span class="strip-title">{{ row.title }}</span>
              <span class="strip-diff">{{ formatDifficulty(row.difficulty) }}</span>
            </div>
          </div>
          <div class="actions">
            <el-button round class="btn-strip btn-view" @click="goView(row)">查看</el-button>
            <el-button round class="btn-strip btn-edit" @click="openEdit(row)">修改</el-button>
            <el-button round class="btn-strip btn-case" @click="openCases(row)">修改测试用例</el-button>
            <el-button round class="btn-strip btn-del" @click="confirmDelete(row)">删除</el-button>
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

    <el-dialog v-model="editVisible" title="修改题目" width="520px" destroy-on-close @closed="resetEdit">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" round />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="editForm.difficulty" class="admin-dialog-select-dark" placeholder="难度" teleported style="width: 100%">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MIDDLE" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="editVisible = false">取消</el-button>
        <el-button round type="primary" class="btn-save" :loading="editSaving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="caseVisible" title="测试用例" width="640px" destroy-on-close @open="onCaseDialogOpen">
      <div v-loading="caseLoading" class="case-body">
        <div v-for="tc in caseList" :key="tc.id ?? tc._local" class="case-row">
          <el-input v-model="tc.input" type="textarea" :rows="2" placeholder="输入" class="case-in" />
          <el-input v-model="tc.output" type="textarea" :rows="2" placeholder="输出" class="case-out" />
          <el-button
            v-if="tc.id"
            round
            size="small"
            class="btn-del"
            @click="removeCase(tc)"
          >
            删除
          </el-button>
        </div>
        <el-button round class="btn-add" @click="addCaseRow">添加一组</el-button>
      </div>
      <template #footer>
        <el-button round @click="caseVisible = false">关闭</el-button>
        <el-button round type="primary" class="btn-save" :loading="caseSaving" @click="saveCases">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchProblemList, fetchProblemDetail, updateProblem, deleteProblem } from '@/api/problem'
import { fetchTestCasesByProblem, createTestCases, deleteTestCase, updateTestCase } from '@/api/testcase'

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
  return sortedRows.value.slice(start, start + pageSize)
})

const editVisible = ref(false)
const editSaving = ref(false)
const editForm = reactive({
  problemId: 0,
  title: '',
  description: '',
  difficulty: 'EASY',
})

const caseVisible = ref(false)
const caseLoading = ref(false)
const caseSaving = ref(false)
const caseProblemId = ref(0)
const caseList = ref([])

function goCreate() {
  router.push({ name: 'admin-problem-create' })
}

function formatDifficulty(v) {
  if (!v) return '—'
  const u = String(v).toUpperCase()
  const map = { EASY: '简单', MIDDLE: '中等', HARD: '困难' }
  return map[u] || v
}

function goView(row) {
  router.push({ name: 'problem-detail', params: { problemId: row.problemId } })
}

function openEdit(row) {
  editForm.problemId = row.problemId
  editForm.title = row.title
  editForm.description = ''
  editForm.difficulty = 'EASY'
  editVisible.value = true
  loadDescForEdit(row.problemId)
}

async function loadDescForEdit(problemId) {
  try {
    const { data } = await fetchProblemDetail(problemId)
    editForm.description = data?.description ?? ''
    const d = String(data?.difficulty ?? '').toUpperCase()
    if (d.includes('EASY') || d === 'EASY') editForm.difficulty = 'EASY'
    else if (d.includes('MIDDLE') || d === 'MIDDLE') editForm.difficulty = 'MIDDLE'
    else if (d.includes('HARD') || d === 'HARD') editForm.difficulty = 'HARD'
    else if (data?.difficulty === 'easy') editForm.difficulty = 'EASY'
    else if (data?.difficulty === 'middle') editForm.difficulty = 'MIDDLE'
    else if (data?.difficulty === 'hard') editForm.difficulty = 'HARD'
  } catch {
    ElMessage.error('加载题目详情失败')
  }
}

function resetEdit() {
  editForm.problemId = 0
  editForm.title = ''
  editForm.description = ''
  editForm.difficulty = 'EASY'
}

async function saveEdit() {
  editSaving.value = true
  try {
    await updateProblem(editForm.problemId, {
      title: editForm.title,
      description: editForm.description,
      difficulty: editForm.difficulty,
    })
    ElMessage.success('已保存')
    editVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    editSaving.value = false
  }
}

function openCases(row) {
  caseProblemId.value = row.problemId
  caseVisible.value = true
}

async function reloadCaseList() {
  caseLoading.value = true
  try {
    const { data } = await fetchTestCasesByProblem(caseProblemId.value)
    const list = data?.testCaseList ?? []
    caseList.value = list.map((tc) => ({
      id: tc.id,
      input: tc.input ?? '',
      output: tc.output ?? '',
      problemId: caseProblemId.value,
      _local: null,
    }))
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载测试用例失败')
    caseList.value = []
  } finally {
    caseLoading.value = false
  }
}

async function onCaseDialogOpen() {
  await reloadCaseList()
}

function addCaseRow() {
  caseList.value.push({
    id: null,
    input: '',
    output: '',
    problemId: caseProblemId.value,
    _local: `n-${Date.now()}`,
  })
}

async function removeCase(tc) {
  if (!tc.id) {
    caseList.value = caseList.value.filter((x) => x !== tc)
    return
  }
  try {
    await deleteTestCase(tc.id)
    ElMessage.success('已删除')
    caseList.value = caseList.value.filter((x) => x.id !== tc.id)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

async function saveCases() {
  const pid = caseProblemId.value
  const existing = caseList.value.filter((x) => x.id)
  const newRows = caseList.value.filter((x) => !x.id && (x.input.trim() || x.output.trim()))
  if (existing.length === 0 && newRows.length === 0) {
    ElMessage.info('没有可保存的内容（请填写输入或输出，或修改已有用例）')
    return
  }
  caseSaving.value = true
  try {
    for (const x of existing) {
      await updateTestCase(x.id, {
        problemId: pid,
        input: x.input ?? '',
        output: x.output ?? '',
      })
    }
    if (newRows.length > 0) {
      await createTestCases({
        testCaseList: newRows.map((x) => ({
          problemId: pid,
          input: x.input,
          output: x.output,
        })),
      })
    }
    ElMessage.success('已保存')
    await reloadCaseList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    caseSaving.value = false
  }
}

async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除题目「${row.title}」？关联提交与测试用例将一并删除。`, '确认删除', {
      type: 'warning',
      roundButton: true,
    })
  } catch {
    return
  }
  try {
    await deleteProblem(row.problemId)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await fetchProblemList()
    rows.value = data?.problems ?? []
    page.value = 1
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
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

.toolbar-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}

.btn-add-problem {
  border-radius: 999px !important;
  --el-button-bg-color: #e8e8ee;
  --el-button-border-color: #e8e8ee;
  --el-button-text-color: #1a1a1e;
  --el-button-hover-bg-color: #ffffff;
  --el-button-hover-border-color: #ffffff;
  --el-button-hover-text-color: #121216;
}

.heading {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f0f0f4;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-height: 100px;
}

.list-wrap :deep(.el-empty__description p) {
  color: rgba(255, 255, 255, 0.55);
}

.problem-strip {
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
  align-items: baseline;
  gap: 0.85rem 1rem;
  flex: 1;
  min-width: min(100%, 200px);
}

.strip-id {
  font-variant-numeric: tabular-nums;
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.5);
  min-width: 2.25rem;
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
  min-width: 120px;
  max-width: 100%;
  font-size: 0.9375rem;
  font-weight: 500;
  color: #e8e8ec;
  line-height: 1.4;
}

.strip-diff {
  flex: 0 0 auto;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.72);
  white-space: nowrap;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
}

.actions :deep(.btn-strip.el-button) {
  padding: 0.48rem 1.1rem;
  font-size: 0.9375rem;
  min-height: 40px;
}

.btn-view {
  background: rgba(138, 180, 248, 0.18) !important;
  border-color: rgba(138, 180, 248, 0.35) !important;
  color: #c8dafb !important;
}

.btn-view:hover,
.btn-view:focus {
  background: rgba(172, 200, 255, 0.52) !important;
  border-color: rgba(200, 218, 255, 0.85) !important;
  color: #1a2842 !important;
}

.btn-edit {
  background: rgba(94, 214, 159, 0.18) !important;
  border-color: rgba(94, 214, 159, 0.35) !important;
  color: #b8f0d2 !important;
}

.btn-edit:hover,
.btn-edit:focus {
  background: rgba(120, 230, 175, 0.5) !important;
  border-color: rgba(150, 240, 195, 0.82) !important;
  color: #0f3d26 !important;
}

.btn-case {
  background: rgba(245, 214, 122, 0.15) !important;
  border-color: rgba(245, 214, 122, 0.35) !important;
  color: #f5e6a8 !important;
}

.btn-case:hover,
.btn-case:focus {
  background: rgba(250, 230, 150, 0.52) !important;
  border-color: rgba(252, 238, 175, 0.88) !important;
  color: #4a3d12 !important;
}

.btn-del {
  background: rgba(248, 113, 113, 0.14) !important;
  border-color: rgba(248, 113, 113, 0.35) !important;
  color: #ffb4b4 !important;
}

.btn-del:hover,
.btn-del:focus {
  background: rgba(255, 175, 175, 0.48) !important;
  border-color: rgba(255, 200, 200, 0.82) !important;
  color: #6b1c1c !important;
}

.btn-save {
  border-radius: 999px !important;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.25rem;
}

.pager-wrap :deep(.el-pagination.is-background .el-pager li),
.pager-wrap :deep(.el-pagination.is-background .btn-prev),
.pager-wrap :deep(.el-pagination.is-background .btn-next) {
  border-radius: 999px;
}

.case-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.case-row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 0.5rem;
  align-items: start;
}

.btn-add {
  align-self: flex-start;
  margin-top: 0.25rem;
}

.admin-dialog-select-dark :deep(.el-select__wrapper) {
  background-color: rgba(28, 28, 34, 0.96) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.18) inset !important;
}

.admin-dialog-select-dark :deep(.el-select__placeholder),
.admin-dialog-select-dark :deep(.el-select__selected-item),
.admin-dialog-select-dark :deep(.el-select__caret) {
  color: rgba(255, 255, 255, 0.9) !important;
}
</style>
