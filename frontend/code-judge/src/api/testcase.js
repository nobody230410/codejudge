import { http } from './http'

export function fetchTestCasesByProblem(problemId) {
  return http.get(`/testcase/problem/${problemId}`)
}

export function createTestCases(payload) {
  return http.post('/testcase/create', payload)
}

/**
 * @param {{ problemId: number; input?: string; output?: string }} payload
 */
export function updateTestCase(id, payload) {
  return http.put(`/testcase/${id}`, payload)
}

export function deleteTestCase(id) {
  return http.delete(`/testcase/${id}`)
}
