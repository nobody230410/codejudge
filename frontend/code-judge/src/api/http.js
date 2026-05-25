import axios from 'axios'

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('cj_token')
  if (token) {
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
  return config
})
