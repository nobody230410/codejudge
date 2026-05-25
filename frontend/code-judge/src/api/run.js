import { http } from './http'
import { waitForRunResult } from '@/utils/runWebSocket'

/**
 * 提交运行并入队；先连接 WebSocket，再 POST，避免结果先于连接到达。
 * @param {{ language: string; code: string; stdin?: string }} payload
 */
export async function runCode(payload) {
  const runId = crypto.randomUUID()
  const resultPromise = waitForRunResult(runId)
  await http.post('/run', { ...payload, runId })
  return { data: await resultPromise }
}
