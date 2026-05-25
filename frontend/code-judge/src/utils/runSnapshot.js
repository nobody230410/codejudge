const KEY = 'cj_run_snapshot'

/**
 * 题目页跳转到独立「代码运行」页时保存编辑器状态。
 * @param {{ code?: string; language?: string; codeFontSize?: number }} snapshot
 */
export function saveRunSnapshot(snapshot) {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(snapshot))
  } catch {
    /* ignore quota / private mode */
  }
}

/** @returns {{ code?: string; language?: string; codeFontSize?: number } | null} */
export function loadRunSnapshot() {
  try {
    const raw = sessionStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

