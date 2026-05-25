<template>
  <div ref="containerRef" class="monaco-wrap" :class="{ 'monaco-wrap--dark': isDarkTheme }" />
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, shallowRef, watch } from 'vue'
import * as monaco from 'monaco-editor'

/** 与 CSS 一致：避免父级页面字体（如 DM Sans）渗入导致光标与字符错位 */
const CODE_FONT_STACK =
  "'JetBrains Mono', Consolas, 'Lucida Console', 'Courier New', monospace"

const props = defineProps({
  modelValue: { type: String, default: '' },
  /** 与判题一致：cpp | python | java */
  language: { type: String, default: 'cpp' },
  /** 编辑器字号（像素），建议由父组件限定在 13–23 */
  fontSize: { type: Number, default: 18 },
  /** 只读展示（如管理员查看提交） */
  readOnly: { type: Boolean, default: false },
  /** Monaco 主题：vs（浅色）| vs-dark */
  editorTheme: { type: String, default: 'vs' },
})

const emit = defineEmits(['update:modelValue'])

const isDarkTheme = computed(() => props.editorTheme === 'vs-dark')

function resolveMonacoTheme() {
  return isDarkTheme.value ? 'vs-dark' : 'vs'
}

const containerRef = shallowRef(null)
const editorRef = shallowRef(null)

function clampFontSize(n) {
  const x = Number(n)
  if (!Number.isFinite(x)) return 18
  return Math.min(23, Math.max(13, Math.round(x)))
}

function mapMonacoLanguage(lang) {
  switch (lang) {
    case 'python':
      return 'python'
    case 'java':
      return 'java'
    case 'cpp':
    default:
      return 'cpp'
  }
}

onMounted(() => {
  if (!containerRef.value) return

  const editor = monaco.editor.create(containerRef.value, {
    value: props.modelValue ?? '',
    language: mapMonacoLanguage(props.language),
    theme: resolveMonacoTheme(),
    readOnly: props.readOnly,
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: clampFontSize(props.fontSize),
    fontFamily: CODE_FONT_STACK,
    fontLigatures: false,
    tabSize: 4,
    insertSpaces: true,
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    smoothScrolling: true,
    padding: { top: 10, bottom: 10 },
    renderLineHighlight: 'line',
    scrollbar: {
      verticalScrollbarSize: 10,
      horizontalScrollbarSize: 10,
    },
  })

  editorRef.value = editor

  editor.onDidChangeModelContent(() => {
    if (props.readOnly) return
    emit('update:modelValue', editor.getValue())
  })

  void document.fonts.ready.then(() => {
    nextTick(() => {
      editor.layout()
    })
  })
})

onBeforeUnmount(() => {
  editorRef.value?.dispose()
  editorRef.value = null
})

watch(
  () => props.modelValue,
  (v) => {
    const ed = editorRef.value
    if (!ed) return
    const next = v ?? ''
    if (next !== ed.getValue()) {
      ed.setValue(next)
    }
  },
)

watch(
  () => props.language,
  (lang) => {
    const ed = editorRef.value
    if (!ed) return
    const model = ed.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, mapMonacoLanguage(lang))
    }
  },
)

watch(
  () => props.fontSize,
  (size) => {
    const ed = editorRef.value
    if (!ed) return
    ed.updateOptions({ fontSize: clampFontSize(size) })
    nextTick(() => ed.layout())
  },
)

watch(
  () => props.readOnly,
  (ro) => {
    const ed = editorRef.value
    if (!ed) return
    ed.updateOptions({ readOnly: Boolean(ro) })
  },
)

watch(
  () => props.editorTheme,
  () => {
    monaco.editor.setTheme(resolveMonacoTheme())
    nextTick(() => editorRef.value?.layout())
  },
)
</script>

<style scoped>
.monaco-wrap {
  min-height: 420px;
  height: 420px;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.12);
  box-sizing: border-box;
  /* 隔离页面比例字体，避免继承后 Monaco 测量与绘制不一致 */
  font-family: 'JetBrains Mono', Consolas, 'Lucida Console', 'Courier New', monospace;
}

.monaco-wrap :deep(.monaco-editor),
.monaco-wrap :deep(.monaco-editor .view-lines),
.monaco-wrap :deep(.monaco-editor textarea.inputarea),
.monaco-wrap :deep(.monaco-editor .monaco-mouse-cursor-text) {
  font-family: 'JetBrains Mono', Consolas, 'Lucida Console', 'Courier New', monospace !important;
  font-variant-ligatures: none;
}

.monaco-wrap--dark {
  border-color: rgba(255, 255, 255, 0.14);
}
</style>
