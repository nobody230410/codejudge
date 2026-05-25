import { http } from './http'

/**
 * @param {{ code: string; language: string; problemId: number }} payload
 */
export function submitCode(payload) {
  return http.post('/submission/', payload)
}
