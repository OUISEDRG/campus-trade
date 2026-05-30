import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const adminInfo = ref(null)

  const isAdminLoggedIn = computed(() => {
    return adminInfo.value !== null && 
           adminInfo.value.id !== null && 
           adminInfo.value.id !== undefined
  })

  const setAdminLoginInfo = (data) => {
    adminInfo.value = data
    persistToLocalStorage()
  }

  const clear = () => {
    adminInfo.value = null
    localStorage.removeItem('campus-trade-admin-storage')
  }

  const persistToLocalStorage = () => {
    try {
      const payload = { adminInfo: adminInfo.value }
      localStorage.setItem('campus-trade-admin-storage', JSON.stringify(payload))
    } catch (e) {
      console.error('[adminStore] 持久化管理员会话失败:', e)
    }
  }

  const loadFromLocalStorage = () => {
    try {
      const saved = localStorage.getItem('campus-trade-admin-storage')
      if (saved) {
        const data = JSON.parse(saved)
        adminInfo.value = data.adminInfo || null
      }
    } catch (e) {
      console.error('[adminStore] 加载管理员会话失败:', e)
    }
  }

  loadFromLocalStorage()

  return {
    adminInfo,
    isAdminLoggedIn,
    setAdminLoginInfo,
    clear,
    loadFromLocalStorage
  }
})
