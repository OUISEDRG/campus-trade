import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    // 如果后续后端加了 JWT，可以在这里附带 Token
    // if (user.token) { config.headers.Authorization = `Bearer ${user.token}` }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 业务状态码 40301 代表账号被封禁
    if (res.code === 40301 || (res.message && res.message.includes('被封禁'))) {
      ElMessage.error({
        message: '登录失败：您的账号已被系统封禁。',
        duration: 5000
      })
      localStorage.removeItem('user')
      window.location.href = '/'
      return Promise.reject(new Error(res.message))
    }
    // 如果业务状态码不是 200，显示错误信息
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
