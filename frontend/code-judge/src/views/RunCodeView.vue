<template>
  <div class="sheet">
    <div class="detail-nav">
      <div class="detail-nav-row">
        <el-button round class="btn-back-list" @click="goBack">← 返回题库</el-button>
      </div>
    </div>

    <header class="sheet-head">
      <div class="head-text">
        <h2 class="title">代码运行</h2>
        <p class="meta">在线运行当前编辑器中的代码；时间与内存限制由判题服务默认策略决定。</p>
      </div>
    </header>

    <div class="two-col">
      <section class="panel io-panel">
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
      </section>

      <section class="panel code-panel">
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
        </div>
        <MonacoCodeEditor v-model="code" :language="language" :font-size="codeFontSize" class="code-editor-slot" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { runCode } from '@/api/run'
import { loadRunSnapshot } from '@/utils/runSnapshot'
import MonacoCodeEditor from '@/components/MonacoCodeEditor.vue'

const router = useRouter()

const CODE_FONT_SIZES = Array.from({ length: 11 }, (_, i) => 13 + i)
const code = ref('')
const language = ref('cpp')
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

function goBack() {
  router.push({ name: 'problems' })
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

onMounted(() => {
  const snap = loadRunSnapshot()
  if (snap) {
    if (typeof snap.code === 'string') code.value = snap.code
    if (snap.language) language.value = snap.language
    if (snap.codeFontSize != null) codeFontSize.value = snap.codeFontSize
  }
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
  font-size: 0.8125rem;
  color: #6a6a72;
  line-height: 1.5;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 1rem;
  align-items: stretch;
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
</style>
