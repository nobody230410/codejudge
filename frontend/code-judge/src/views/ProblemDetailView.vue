<template>
  <div v-loading="loading" class="sheet" :class="{ 'sheet--admin': authStore.isAdmin }">
    <template v-if="detail">
      <div class="detail-nav">
        <div class="detail-nav-row">
          <el-button round class="btn-back-list" @click="goBackList">← 返回</el-button>
          <div v-if="!authStore.isAdmin && detail" class="mode-switch">
            <div class="mode-btns">
              <el-button round :type="!runMode ? 'primary' : 'default'" @click="setRunMode(false)">题目</el-button>
              <el-button round :type="runMode ? 'primary' : 'default'" @click="setRunMode(true)">运行</el-button>
            </div>
            <el-button round class="btn-open-run-page" @click="goStandaloneRun">在新页面打开</el-button>
          </div>
        </div>
      </div>
      <header class="sheet-head">
        <div class="head-text">
          <h2 class="title">{{ detail.title }}</h2>
          <p class="meta">
            <span>难度 {{ formatDifficulty(detail.difficulty) }}</span>
            <span v-if="authStore.isAdmin">时间上限 {{ formatLimitSec(detail.timeLimit) }} · 内存上限 {{ formatLimitMem(detail.memoryLimit) }}</span>
            <span v-if="!authStore.isAdmin && detail.submitTime">最近提交 {{ formatTime(detail.submitTime) }}</span>
          </p>
        </div>
        <div v-if="authStore.isAdmin" class="admin-bar" @click.stop>
          <el-button round class="btn-admin-head btn-edit" @click="openEdit">修改</el-button>
          <el-button round class="btn-admin-head btn-case" @click="openCases">修改测试用例</el-button>
          <el-button round class="btn-admin-head btn-del" @click="confirmDelete">删除</el-button>
        </div>
      </header>

      <div class="two-col" :class="{ 'two-col--admin': authStore.isAdmin }">
        <section class="panel desc-panel">
          <template v-if="authStore.isAdmin">
            <h3 class="panel-title">题目描述</h3>
            <div class="desc">{{ detail.description }}</div>
          </template>
          <template v-else-if="!runMode">
            <h3 class="panel-title">题目描述</h3>
            <div class="desc">{{ detail.description }}</div>
          </template>
          <template v-else>
            <h3 class="panel-title">标准输入</h3>
            <el-input
              v-model="runStdin"
              type="textarea"
              :rows="10"
              class="run-textarea"
              placeholder="传递给程序的 stdin（可选）"
            />
            <div class="run-actions">
              <el-button round type="primary" class="submit-btn" :loading="running" @click="executeRun">
                运行
              </el-button>
            </div>
            <h3 class="panel-title run-out-title">输出</h3>
            <pre class="run-output">{{ runOutput || '（尚无输出）' }}</pre>
            <p v-if="runMetaLine" class="run-meta">{{ runMetaLine }}</p>
          </template>
        </section>
        <section v-if="!authStore.isAdmin" class="panel code-panel">
          <h3 class="panel-title">代码</h3>
          <div class="toolbar">
            <el-select v-model="language" placeholder="语言" class="lang-select" teleported>
              <el-option label="C++" value="cpp" />
              <el-option label="Python" value="python" />
              <el-option label="Java" value="java" />
            </el-select>
            <el-select v-model="codeFontSize" placeholder="字号" class="font-size-select" teleported>
              <el-option v-for="n in CODE_FONT_SIZES" :key="n" :label="`${n}px`" :value="n" />
            </el-select>
            <el-button
              round
              type="primary"
              class="submit-btn"
              :loading="submitting"
              :disabled="!code.trim()"
              @click="submit"
            >
              提交
            </el-button>
          </div>
          <MonacoCodeEditor v-model="code" :language="language" :font-size="codeFontSize" class="code-editor-slot" />
        </section>
      </div>

      <section v-if="!authStore.isAdmin" class="panel result-panel">
        <h3 class="panel-title">判题结果</h3>
        <div v-if="detail.status" class="result-main">
          <p class="status-line">
            <span class="label">状态</span>
            <span class="value">{{ detail.status }}</span>
          </p>
          <p v-if="execSummary" class="exec-line">{{ execSummary }}</p>
          <p v-if="detail.judgeMessage" class="msg-text">{{ detail.judgeMessage }}</p>
          <p v-if="resultHint" class="hint-text">{{ resultHint }}</p>
        </div>
        <p v-else class="empty-result">尚未提交或暂无评测记录，提交后将在此显示通过或错误信息。</p>
      </section>
    </template>

    <el-dialog v-model="editVisible" title="修改题目" width="520px" destroy-on-close @closed="resetEdit">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
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
        <el-form-item label="时间上限（秒）">
          <el-input-number
            v-model="editForm.timeLimit"
            :min="0.001"
            :step="0.5"
            :precision="3"
            controls-position="right"
            placeholder="留空则判题使用默认 10s"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内存上限（KB）">
          <el-input-number
            v-model="editForm.memoryLimit"
            :min="1"
            :step="1024"
            :precision="0"
            controls-position="right"
            placeholder="留空则判题使用默认 262144 KB"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="editVisible = false">取消</el-button>
        <el-button round type="primary" :loading="editSaving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="caseVisible" title="测试用例" width="640px" destroy-on-close @open="onCaseDialogOpen">
      <div v-loading="caseLoading" class="case-body">
        <div v-for="tc in caseList" :key="tc.id ?? tc._local" class="case-row">
          <el-input v-model="tc.input" type="textarea" :rows="2" placeholder="输入" />
          <el-input v-model="tc.output" type="textarea" :rows="2" placeholder="输出" />
          <el-button v-if="tc.id" round size="small" class="btn-del" @click="removeCase(tc)">删除</el-button>
        </div>
        <el-button round class="btn-add" @click="addCaseRow">添加一组</el-button>
      </div>
      <template #footer>
        <el-button round @click="caseVisible = false">关闭</el-button>
        <el-button round type="primary" :loading="caseSaving" @click="saveCases">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchProblemDetail,
  updateProblem,
  deleteProblem,
} from '@/api/problem'
import { submitCode } from '@/api/submission'
import { runCode } from '@/api/run'
import { saveRunSnapshot } from '@/utils/runSnapshot'
import { fetchTestCasesByProblem, createTestCases, deleteTestCase, updateTestCase } from '@/api/testcase'
import { useAuthStore } from '@/stores/auth'
import MonacoCodeEditor from '@/components/MonacoCodeEditor.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const problemId = computed(() => Number(route.params.problemId))

/** 左侧：题目描述 ↔ 运行（标准输入/输出）；query.run=1 为运行视图 */
const runMode = computed(() => route.query.run === '1')

const loading = ref(false)
const submitting = ref(false)
const detail = ref(null)
const code = ref('')
const language = ref('cpp')

/** 代码编辑器字号下拉选项（px） */
const CODE_FONT_SIZES = Array.from({ length: 11 }, (_, i) => 13 + i)
const codeFontSize = ref(18)

const running = ref(false)
const runStdin = ref('')
const runOutput = ref('')
const runStatus = ref('')
const runTime = ref(null)
const runMemory = ref(null)

const runMetaLine = computed(() => {
  if (!runStatus.value) return ''
  const parts = [runStatus.value]
  if (runTime.value != null) parts.push(`${runTime.value} s`)
  if (runMemory.value != null) parts.push(`${Math.round(Number(runMemory.value))} KB`)
  return parts.join(' · ')
})

const resultHint = computed(() => {
  const s = String(detail.value?.status ?? '').toUpperCase()
  if (!s) return ''
  if (s.includes('PASS') || s.includes('SUCCESS') || s === 'AC') return '本题评测通过。'
  if (s.includes('COMPILATION')) return '编译失败，请检查语法与语言是否匹配。'
  if (s.includes('RUNTIME')) return '运行时出错，请检查边界条件与非法操作。'
  if (s.includes('TIME_LIMIT')) return '超出时间限制，可尝试优化算法复杂度。'
  if (s.includes('MEMORY_LIMIT')) return '超出内存限制，请减少空间占用。'
  if (s.includes('ERROR') || s.includes('WRONG') || s.includes('TIME') || s.includes('MEMORY')) {
    return '请根据状态信息排查代码或算法逻辑。'
  }
  return ''
})

const editVisible = ref(false)
const editSaving = ref(false)
const editForm = reactive({
  title: '',
  description: '',
  difficulty: 'EASY',
  timeLimit: null,
  memoryLimit: null,
})

const caseVisible = ref(false)
const caseLoading = ref(false)
const caseSaving = ref(false)
const caseList = ref([])

/** @type {WebSocket | null} */
let judgeWs = null

const execSummary = computed(() => {
  const t = detail.value?._execTime
  const m = detail.value?._execMemory
  const parts = []
  if (t != null && t !== '') parts.push(`用时 ${Number(t)} s`)
  if (m != null && m !== '') parts.push(`内存 ${m} KB`)
  return parts.length ? parts.join(' · ') : ''
})

function formatDifficulty(v) {
  if (!v) return '—'
  const u = String(v).toUpperCase()
  const map = { EASY: '简单', MIDDLE: '中等', HARD: '困难' }
  if (map[u]) return map[u]
  const low = String(v).toLowerCase()
  const map2 = { easy: '简单', middle: '中等', hard: '困难' }
  return map2[low] || v
}

function formatTime(t) {
  if (!t) return ''
  try {
    return new Date(t).toLocaleString()
  } catch {
    return String(t)
  }
}

function formatLimitSec(v) {
  if (v == null || v === '') return '默认 10 s（Envoy options）'
  return `${v} s`
}

function formatLimitMem(v) {
  if (v == null || v === '') return '默认 262144 KB（Envoy options）'
  return `${v} KB`
}

function applyDetail(d) {
  detail.value = d
  if (d?.language) language.value = d.language
  code.value = d?.code ? d.code : ''
}

function closeJudgeWs() {
  if (judgeWs) {
    judgeWs.close()
    judgeWs = null
  }
}

function openJudgeWs(submissionId) {
  closeJudgeWs()
  const wsProto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${wsProto}//${window.location.host}/ws/submission/${submissionId}`
  judgeWs = new WebSocket(url)
  judgeWs.onmessage = (ev) => {
    try {
      const r = JSON.parse(ev.data)
      if (!detail.value) return
      detail.value = {
        ...detail.value,
        status: r.status != null ? String(r.status) : detail.value.status,
        judgeMessage: r.message != null ? String(r.message) : '',
        _execTime: r.totalTime,
        _execMemory: r.totalMemory,
      }
      void refreshSubmitMeta()
    } catch {
      /* ignore malformed payload */
    }
  }
  judgeWs.onerror = () => {}
  judgeWs.onclose = () => {
    judgeWs = null
  }
}

async function load() {
  if (!Number.isFinite(problemId.value) || problemId.value <= 0) {
    ElMessage.error('题目不存在')
    detail.value = null
    return
  }
  loading.value = true
  try {
    const { data } = await fetchProblemDetail(problemId.value)
    applyDetail(data)
  } catch (e) {
    const msg = e?.response?.data?.message
    ElMessage.error(typeof msg === 'string' ? msg : '加载题目失败')
    const pid = problemId.value
    if (!detail.value || detail.value.problemId !== pid) {
      detail.value = null
    }
  } finally {
    loading.value = false
  }
}

function goBackList() {
  router.push(authStore.isAdmin ? { name: 'admin-problems' } : { name: 'problems' })
}

function setRunMode(on) {
  const q = { ...route.query }
  if (on) q.run = '1'
  else delete q.run
  router.replace({ path: route.path, query: q })
}

function goStandaloneRun() {
  saveRunSnapshot({
    code: code.value,
    language: language.value,
    codeFontSize: codeFontSize.value,
  })
  router.push({ name: 'code-run' })
}

async function executeRun() {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }
  running.value = true
  runOutput.value = ''
  runStatus.value = ''
  runTime.value = null
  runMemory.value = null
  try {
    const { data } = await runCode({
      language: language.value,
      code: code.value,
      stdin: runStdin.value || undefined,
    })
    runOutput.value = data?.output ?? ''
    runStatus.value = data?.status ?? ''
    runTime.value = data?.time ?? null
    runMemory.value = data?.memory ?? null
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '运行失败')
  } finally {
    running.value = false
  }
}

/** 同步服务端「最近提交时间」，不覆盖编辑器代码 */
async function refreshSubmitMeta() {
  if (!problemId.value || authStore.isAdmin) return
  try {
    const { data } = await fetchProblemDetail(problemId.value)
    if (detail.value && data?.submitTime != null) {
      detail.value.submitTime = data.submitTime
    }
  } catch {
    /* ignore */
  }
}

async function submit() {
  submitting.value = true
  try {
    const { data } = await submitCode({
      code: code.value,
      language: language.value,
      problemId: problemId.value,
    })
    const submissionId = typeof data === 'number' ? data : Number(data)
    ElMessage.success('已提交')
    if (detail.value) {
      detail.value = {
        ...detail.value,
        status: 'waiting',
        judgeMessage: '正在评测，请稍候…',
        _execTime: undefined,
        _execMemory: undefined,
      }
    }
    if (Number.isFinite(submissionId)) {
      openJudgeWs(submissionId)
    }
    await refreshSubmitMeta()
  } catch (e) {
    const msg = e?.response?.data?.message
    ElMessage.error(typeof msg === 'string' ? msg : '提交失败')
  } finally {
    submitting.value = false
  }
}

function openEdit() {
  if (!detail.value) return
  editForm.title = detail.value.title
  editForm.description = detail.value.description ?? ''
  const d = String(detail.value.difficulty ?? '').toUpperCase()
  if (d.includes('EASY')) editForm.difficulty = 'EASY'
  else if (d.includes('MIDDLE')) editForm.difficulty = 'MIDDLE'
  else if (d.includes('HARD')) editForm.difficulty = 'HARD'
  else editForm.difficulty = 'EASY'
  editForm.timeLimit = detail.value.timeLimit != null ? Number(detail.value.timeLimit) : null
  editForm.memoryLimit = detail.value.memoryLimit != null ? Number(detail.value.memoryLimit) : null
  editVisible.value = true
}

function resetEdit() {
  editForm.title = ''
  editForm.description = ''
  editForm.difficulty = 'EASY'
  editForm.timeLimit = null
  editForm.memoryLimit = null
}

async function saveEdit() {
  editSaving.value = true
  try {
    await updateProblem(problemId.value, {
      title: editForm.title,
      description: editForm.description,
      difficulty: editForm.difficulty,
      timeLimit: editForm.timeLimit ?? null,
      memoryLimit: editForm.memoryLimit ?? null,
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

function openCases() {
  caseVisible.value = true
}

async function reloadCaseList() {
  caseLoading.value = true
  try {
    const { data } = await fetchTestCasesByProblem(problemId.value)
    const list = data?.testCaseList ?? []
    caseList.value = list.map((tc) => ({
      id: tc.id,
      input: tc.input ?? '',
      output: tc.output ?? '',
      problemId: problemId.value,
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
    problemId: problemId.value,
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
  const pid = problemId.value
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

async function confirmDelete() {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(`确定删除题目「${detail.value.title}」？`, '确认删除', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteProblem(problemId.value)
    ElMessage.success('已删除')
    await router.replace({ name: 'admin-problems' })
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

watch(
  () => route.params.problemId,
  () => {
    closeJudgeWs()
    load()
  },
  { immediate: true },
)

onUnmounted(() => {
  closeJudgeWs()
})
</script>

<style scoped>
.sheet {
  width: 100%;
  font-family: 'DM Sans', system-ui, -apple-system, sans-serif;
}

.detail-nav {
  margin-bottom: 0.75rem;
}

.detail-nav-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem 1rem;
}

.mode-switch {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 1rem;
  margin-left: auto;
}

.mode-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.btn-open-run-page {
  border-radius: 999px !important;
  font-weight: 500;
}

.run-out-title {
  margin-top: 1rem;
}

.run-textarea :deep(.el-textarea__inner) {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 0.875rem;
  line-height: 1.5;
}

.run-actions {
  margin-top: 0.65rem;
}

.run-output {
  margin: 0;
  padding: 0.75rem 1rem;
  min-height: 120px;
  border-radius: 10px;
  background: rgba(26, 26, 30, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.08);
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 0.8125rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  color: #2a2a2e;
}

.run-meta {
  margin: 0.5rem 0 0;
  font-size: 0.8125rem;
  color: #5c5c66;
}

.btn-back-list {
  font-weight: 600;
  color: #3a3a42 !important;
  background: rgba(255, 255, 255, 0.65) !important;
  border: 1px solid #c8c8d0 !important;
}

.btn-back-list:hover {
  background: #f0f0f3 !important;
  border-color: #a8a8b4 !important;
}

.sheet-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
  padding: 1rem 1.25rem;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.55);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}

.head-text {
  flex: 1;
  min-width: 200px;
}

.title {
  margin: 0 0 0.35rem;
  font-size: 1.35rem;
  font-weight: 600;
  color: #1c1c20;
}

.meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.8125rem;
  color: #6a6a72;
}

.admin-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.admin-bar :deep(.btn-admin-head.el-button) {
  padding: 0.48rem 1.1rem;
  font-size: 0.9375rem;
  min-height: 40px;
}

.btn-edit {
  background: #e5f4ec !important;
  border-color: #c5e5d5 !important;
  color: #2d5a40 !important;
}

.btn-case {
  background: #faf4e4 !important;
  border-color: #ebe0c8 !important;
  color: #6b5a2a !important;
}

.btn-del {
  background: #fcecec !important;
  border-color: #f0d4d4 !important;
  color: #8a3a3a !important;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 1rem;
  align-items: stretch;
}

.two-col--admin {
  grid-template-columns: 1fr;
}

@media (max-width: 900px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}

.panel {
  border-radius: 14px;
  padding: 1rem 1.15rem;
  background: rgba(255, 255, 255, 0.55);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}

.panel-title {
  margin: 0 0 0.75rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #3a3a42;
}

.desc {
  white-space: pre-wrap;
  font-size: 0.9375rem;
  line-height: 1.65;
  color: #2a2a2e;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.65rem;
  margin-bottom: 0.65rem;
}

.lang-select {
  width: 140px;
}

.font-size-select {
  width: 104px;
}

.submit-btn {
  border-radius: 999px !important;
  --el-button-bg-color: #3a3a42;
  --el-button-border-color: #3a3a42;
  --el-button-hover-bg-color: #52525c;
  --el-button-hover-border-color: #52525c;
}

.code-editor-slot {
  width: 100%;
}

.result-panel {
  margin-top: 1rem;
}

.result-main {
  font-size: 0.9375rem;
}

.status-line {
  margin: 0 0 0.5rem;
  display: flex;
  gap: 0.5rem;
  align-items: baseline;
}

.status-line .label {
  color: #6a6a72;
  font-size: 0.8125rem;
}

.status-line .value {
  font-weight: 600;
  color: #1c1c20;
}

.exec-line {
  margin: 0 0 0.35rem;
  font-size: 0.8125rem;
  color: #5c5c66;
}

.msg-text {
  margin: 0.35rem 0 0;
  font-size: 0.8125rem;
  color: #4a4a52;
  line-height: 1.55;
  white-space: pre-wrap;
}

.hint-text {
  margin: 0.35rem 0 0;
  font-size: 0.8125rem;
  color: #5c5c66;
  line-height: 1.5;
}

.empty-result {
  margin: 0;
  font-size: 0.875rem;
  color: #888892;
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
}

/* —— 管理员视角：与管理员登录页一致的深色界面 —— */
.sheet--admin .btn-back-list {
  color: rgba(255, 255, 255, 0.92) !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.sheet--admin .btn-back-list:hover {
  background: rgba(255, 255, 255, 0.16) !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.sheet--admin .sheet-head {
  background: rgba(34, 34, 40, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.35);
}

.sheet--admin .title {
  color: #f0f0f4;
}

.sheet--admin .meta {
  color: rgba(255, 255, 255, 0.55);
}

.sheet--admin .panel {
  background: rgba(34, 34, 40, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.3);
}

.sheet--admin .panel-title {
  color: rgba(255, 255, 255, 0.88);
}

.sheet--admin .desc {
  color: rgba(255, 255, 255, 0.82);
}

.sheet--admin .btn-edit {
  background: rgba(94, 214, 159, 0.18) !important;
  border-color: rgba(94, 214, 159, 0.35) !important;
  color: #b8f0d2 !important;
}

.sheet--admin .btn-edit:hover,
.sheet--admin .btn-edit:focus {
  background: rgba(120, 230, 175, 0.5) !important;
  border-color: rgba(150, 240, 195, 0.82) !important;
  color: #0f3d26 !important;
}

.sheet--admin .btn-case {
  background: rgba(245, 214, 122, 0.15) !important;
  border-color: rgba(245, 214, 122, 0.35) !important;
  color: #f5e6a8 !important;
}

.sheet--admin .btn-case:hover,
.sheet--admin .btn-case:focus {
  background: rgba(250, 230, 150, 0.52) !important;
  border-color: rgba(252, 238, 175, 0.88) !important;
  color: #4a3d12 !important;
}

.sheet--admin .btn-del {
  background: rgba(248, 113, 113, 0.14) !important;
  border-color: rgba(248, 113, 113, 0.35) !important;
  color: #ffb4b4 !important;
}

.sheet--admin .btn-del:hover,
.sheet--admin .btn-del:focus {
  background: rgba(255, 175, 175, 0.48) !important;
  border-color: rgba(255, 200, 200, 0.82) !important;
  color: #6b1c1c !important;
}

.sheet--admin .admin-dialog-select-dark :deep(.el-select__wrapper) {
  background-color: rgba(28, 28, 34, 0.96) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.18) inset !important;
}

.sheet--admin .admin-dialog-select-dark :deep(.el-select__placeholder),
.sheet--admin .admin-dialog-select-dark :deep(.el-select__selected-item),
.sheet--admin .admin-dialog-select-dark :deep(.el-select__caret) {
  color: rgba(255, 255, 255, 0.9) !important;
}

</style>
