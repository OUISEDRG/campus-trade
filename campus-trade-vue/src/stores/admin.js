import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const adminToken = ref('')
  const adminInfo = ref(null)

  const isAdminLoggedIn = computed(() => {
    return adminToken.value !== ''
  })

  const setAdminLoginInfo = (data) => {
    adminToken.value = data.token || ''
    adminInfo.value = data
    persistToLocalStorage()
  }

  const clear = () => {
    adminToken.value = ''
    adminInfo.value = null
    localStorage.removeItem('campus-trade-admin-storage')
  }

  const persistToLocalStorage = () => {
    try {
      localStorage.setItem('campus-trade-admin-storage', JSON.stringify({
        adminToken: adminToken.value,
        adminInfo: adminInfo.value
      }))
    } catch (e) {
      console.error('持久化管理员会话失败:', e)
    }
  }

  const loadFromLocalStorage = () => {
    try {
      const saved = localStorage.getItem('campus-trade-admin-storage')
      if (saved) {
        const data = JSON.parse(saved)
        adminToken.value = data.adminToken || ''
        adminInfo.value = data.adminInfo || null
      }
    } catch (e) {
      console.error('加载管理员会话失败:', e)
    }
  }

  loadFromLocalStorage()

  return {
    adminToken,
    adminInfo,
    isAdminLoggedIn,
    setAdminLoginInfo,
    clear,
    loadFromLocalStorage
  }
})
