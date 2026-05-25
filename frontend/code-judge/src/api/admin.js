import { http } from './http'

export function fetchUserList() {
  return http.get('/admin/users')
}

export function resetUserPassword(userId, newPassword) {
  return http.put(`/admin/users/${userId}/password`, { newPassword })
}

/**
 * @param {{ username?: string; userId?: number; problemId?: number; page?: number; pageSize?: number }} params
 */
export function fetchAdminSubmissions(params) {
  return http.get('/admin/submissions', { params })
}

export function fetchAdminSubmissionDetail(submissionId) {
  return http.get(`/admin/submissions/${submissionId}`)
}
