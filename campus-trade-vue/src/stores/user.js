import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('multiUser', () => {
  const userMap = ref({})
  const activeUserId = ref(null)

  const currentUser = computed(() => {
    return userMap.value[activeUserId.value]?.userInfo || null
  })

  const allUsers = computed(() => {
    return Object.keys(userMap.value).map(id => ({
      id: Number(id),
      ...userMap.value[id].userInfo
    }))
  })

  const loggedInUserCount = computed(() => {
    return Object.keys(userMap.value).length
  })

  const isLoggedIn = computed(() => {
    return activeUserId.value !== null && 
           userMap.value[activeUserId.value]?.userInfo !== null && 
           userMap.value[activeUserId.value]?.userInfo !== undefined
  })

  const addUserSession = (userId, token, userInfo) => {
    userMap.value[userId] = { userInfo }
    activeUserId.value = userId
    persistToLocalStorage()
  }

  const switchAccount = (userId) => {
    if (userMap.value[userId]) {
      activeUserId.value = userId
      persistToLocalStorage()
    }
  }

  const removeAccount = (userId) => {
    delete userMap.value[userId]
    const remainingIds = Object.keys(userMap.value)
    if (remainingIds.length > 0) {
      activeUserId.value = Number(remainingIds[0])
    } else {
      activeUserId.value = null
    }
    persistToLocalStorage()
  }

  const logout = () => {
    if (activeUserId.value !== null) {
      removeAccount(activeUserId.value)
    }
  }

  const persistToLocalStorage = () => {
    try {
      localStorage.setItem('campus-trade-multi-sessions', JSON.stringify({
        userMap: userMap.value,
        activeUserId: activeUserId.value
      }))
    } catch (e) {
      console.error('持久化用户会话失败:', e)
    }
  }

  const loadFromLocalStorage = () => {
    try {
      const saved = localStorage.getItem('campus-trade-multi-sessions')
      if (saved) {
        const data = JSON.parse(saved)
        userMap.value = data.userMap || {}
        activeUserId.value = data.activeUserId || null
      }
    } catch (e) {
      console.error('加载用户会话失败:', e)
    }
  }

  loadFromLocalStorage()

  return {
    userMap,
    activeUserId,
    currentUser,
    allUsers,
    loggedInUserCount,
    isLoggedIn,
    addUserSession,
    switchAccount,
    removeAccount,
    logout,
    loadFromLocalStorage
  }
})
