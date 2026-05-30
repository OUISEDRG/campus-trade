import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useAdminStore } from '../stores/admin'
import router from '../router'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const adminStore = useAdminStore()
    
    const path = config.url || ''
    const isAdminPath = path.startsWith('/admin') || window.location.pathname.startsWith('/admin')
    
    // 这个项目暂时不需要 token 认证，只需要标识用户身份
    
    if (!isAdminPath && userStore.activeUserId) {
      config.headers['X-User-Id'] = userStore.activeUserId
    }
    
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 40301 || (res.message && res.message.includes('被封禁'))) {
      ElMessage.error({
        message: '登录失败：您的账号已被系统封禁。',
        duration: 5000
      })
      const isAdminPath = window.location.pathname.startsWith('/admin')
      
      if (isAdminPath) {
        const adminStore = useAdminStore()
        adminStore.clear()
        router.push('/')
      } else {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/')
      }
      return Promise.reject(new Error(res.message))
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message))
    }
    return response
  },
  error => {
    console.error('请求错误:', error)
    const status = error.response ? error.response.status : null
    const isAdminPath = window.location.pathname.startsWith('/admin')
    
    if (status === 401 || status === 403) {
      if (isAdminPath) {
        const adminStore = useAdminStore()
        adminStore.clear()
        ElMessage.error('管理员会话已过期，请重新登录')
        router.push('/')
      } else {
        const userStore = useUserStore()
        userStore.logout()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/')
      }
      return Promise.reject(error)
    }
    
    const errorMsg = error.response?.data?.message || 
                     error.response?.statusText || 
                     '网络开了个小差'
    ElMessage.error(errorMsg)
    return Promise.reject(error)
  }
)

export default request
