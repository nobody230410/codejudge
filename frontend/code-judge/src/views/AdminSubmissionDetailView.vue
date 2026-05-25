<template>
  <div v-loading="loading" class="page">
    <div class="toolbar-head">
      <el-button round class="btn-back" @click="goBack">← 返回列表</el-button>
      <h2 class="heading">提交详情 #{{ submissionId }}</h2>
    </div>

    <template v-if="detail">
      <section class="panel">
        <h3 class="panel-title">基本信息</h3>
        <dl class="kv">
          <dt>用户</dt>
          <dd>{{ detail.username }}（ID {{ detail.userId }}）</dd>
          <dt>题目</dt>
          <dd>{{ detail.problemTitle }}（ID {{ detail.problemId }}）</dd>
          <dt>语言</dt>
          <dd>{{ detail.language || '—' }}</dd>
          <dt>状态</dt>
          <dd class="status">{{ detail.status || '—' }}</dd>
          <dt>提交时间</dt>
          <dd>{{ formatTime(detail.submitTime) }}</dd>
          <dt>用时</dt>
          <dd>{{ detail.execTime != null ? `${detail.execTime} s` : '—' }}</dd>
          <dt>内存</dt>
          <dd>{{ detail.execMemory != null ? `${detail.execMemory} KB` : '—' }}</dd>
        </dl>
      </section>

      <section class="panel">
        <h3 class="panel-title">评测返回</h3>
        <pre class="msg-block">{{ detail.message || '（无）' }}</pre>
      </section>

      <section class="panel code-panel">
        <h3 class="panel-title">代码</h3>
        <MonacoCodeEditor
          class="code-editor-slot"
          :model-value="detail.code ?? ''"
          :language="editorLang"
          :font-size="16"
          editor-theme="vs-dark"
          read-only
        />
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminSubmissionDetail } from '@/api/admin'
import MonacoCodeEditor from '@/components/MonacoCodeEditor.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)

const submissionId = computed(() => Number(route.params.submissionId))

const editorLang = computed(() => {
  const l = String(detail.value?.language ?? 'cpp').toLowerCase()
  if (l === 'python' || l === 'java' || l === 'cpp') return l
  return 'cpp'
})

function formatTime(t) {
  if (!t) return '—'
  try {
    return new Date(t).toLocaleString()
  } catch {
    return String(t)
  }
}

function goBack() {
  router.push({ name: 'admin-submissions' })
}

async function load() {
  if (!Number.isFinite(submissionId.value) || submissionId.value <= 0) {
    ElMessage.error('无效的提交 ID')
    detail.value = null
    return
  }
  loading.value = true
  try {
    const { data } = await fetchAdminSubmissionDetail(submissionId.value)
    detail.value = data
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
    detail.value = null
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
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.btn-back {
  border-radius: 999px !important;
  border-color: rgba(255, 255, 255, 0.22) !important;
  color: rgba(255, 255, 255, 0.92) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.heading {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f0f0f4;
}

.panel {
  margin-bottom: 1rem;
  padding: 1rem 1.15rem;
  border-radius: 14px;
  background: rgba(34, 34, 40, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 22px rgba(0, 0, 0, 0.28);
}

.panel-title {
  margin: 0 0 0.75rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
}

.kv {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 0.5rem 1rem;
  margin: 0;
  font-size: 0.875rem;
}

.kv dt {
  margin: 0;
  color: rgba(255, 255, 255, 0.5);
}

.kv dd {
  margin: 0;
  color: #e8e8ec;
}

.status {
  font-weight: 600;
  color: #c8e6ff;
}

.msg-block {
  margin: 0;
  padding: 0.75rem 1rem;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
  font-size: 0.8125rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.code-panel.code-panel {
  padding-bottom: 1.25rem;
}

.code-editor-slot {
  width: 100%;
}
</style>
