const RUN_WAIT_MS = 125_000

/**
 * 等待 /ws/run/{runId} 推送的运行结果。
 * @param {string} runId
 * @returns {Promise<{ output?: string; status?: string; time?: number; memory?: number; error?: string }>}
 */
export function waitForRunResult(runId) {
  return new Promise((resolve, reject) => {
    const wsProto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${wsProto}//${window.location.host}/ws/run/${encodeURIComponent(runId)}`
    const ws = new WebSocket(url)

    const timer = window.setTimeout(() => {
      ws.close()
      reject(new Error('代码运行超时，请稍后重试'))
    }, RUN_WAIT_MS)

    const finish = (fn) => {
      window.clearTimeout(timer)
      ws.close()
      fn()
    }

    ws.onmessage = (ev) => {
      try {
        const data = JSON.parse(ev.data)
        if (data?.error) {
          finish(() => reject(new Error(String(data.error))))
        } else {
          finish(() => resolve(data))
        }
      } catch {
        finish(() => reject(new Error('运行结果解析失败')))
      }
    }

    ws.onerror = () => {
      finish(() => reject(new Error('运行结果连接失败')))
    }
  })
}
