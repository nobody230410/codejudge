import { http } from './http'

export function fetchUserProfile() {
  return http.get('/user/me')
}

export function updateUserEmail(email) {
  return http.put('/user/email', { email })
}

export function changeUserPassword(oldPassword, newPassword) {
  return http.put('/user/password', { oldPassword, newPassword })
}
