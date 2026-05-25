import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const TOKEN_KEY = 'cj_token'
const USER_KEY = 'cj_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  const userInfo = ref(readUser())

  const isAuthenticated = computed(() => Boolean(token.value))

  const isAdmin = computed(() => userInfo.value?.userType === 'ADMIN')

  function readUser() {
    try {
      const raw = localStorage.getItem(USER_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }

  function setSession(loginResponse) {
    token.value = loginResponse.token
    userInfo.value = loginResponse.userInfo ?? null
    localStorage.setItem(TOKEN_KEY, loginResponse.token)
    if (loginResponse.userInfo) {
      localStorage.setItem(USER_KEY, JSON.stringify(loginResponse.userInfo))
    }
  }

  function clearSession() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function patchUserInfo(partial) {
    if (!userInfo.value) return
    userInfo.value = { ...userInfo.value, ...partial }
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    isAdmin,
    setSession,
    clearSession,
    patchUserInfo,
  }
})
