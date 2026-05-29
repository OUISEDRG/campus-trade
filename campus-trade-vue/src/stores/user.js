import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)

  const login = (userData) => {
    user.value = userData
  }

  const logout = () => {
    user.value = null
  }

  return {
    user,
    login,
    logout
  }
})