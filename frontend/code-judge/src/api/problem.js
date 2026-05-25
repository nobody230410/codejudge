import { http } from './http'

export function fetchProblemList() {
  return http.get('/problems/list')
}

export function fetchProblemDetail(problemId) {
  return http.get(`/problems/${problemId}`)
}

export function updateProblem(problemId, payload) {
  return http.put(`/problems/${problemId}`, payload)
}

export function deleteProblem(problemId) {
  return http.delete(`/problems/${problemId}`)
}

export function createProblem(payload) {
  return http.post('/problems/create', payload)
}

