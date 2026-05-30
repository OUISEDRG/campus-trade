import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.currentToken
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    if (userStore.activeUserId) {
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
      const userStore = useUserStore()
      if (userStore.activeUserId) {
        userStore.removeAccount(userStore.activeUserId)
      }
      window.location.href = '/'
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
    const errorMsg = error.response?.data?.message || 
                     error.response?.statusText || 
                     '网络开了个小差'
    ElMessage.error(errorMsg)
    return Promise.reject(error)
  }
)

export default request
