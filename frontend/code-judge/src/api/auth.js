import { http } from './http'

/**
 * @param {{ username: string; password: string; userType: 'USER' | 'ADMIN' }} payload
 */
export function login(payload) {
  return http.post('/auth/login', payload)
}

export function logout() {
  return http.post('/auth/logout')
}

/**
 * @param {{ username: string; password: string }} payload
 */
export function register(payload) {
  return http.post('/auth/register', payload)
}
