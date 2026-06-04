import request from '@/utils/request'

// 发起交换
export function createExchange(data) {
  return request({ url: '/exchange/create', method: 'post', data })
}

// 响应交换请求 (status: 1=接受, 2=拒绝)
export function respondExchange(id, data) {
  return request({ url: `/exchange/${id}/respond`, method: 'post', data })
}

// 获取交换详情
export function getExchange(id) {
  return request({ url: `/exchange/${id}`, method: 'get' })
}

// 获取用户的所有交换记录
export function getUserExchanges(userId) {
  return request({ url: `/exchange/user/${userId}`, method: 'get' })
}

// 获取用户的待处理交换请求
export function getPendingExchanges(userId) {
  return request({ url: `/exchange/user/${userId}/pending`, method: 'get' })
}

// 取消交换
export function cancelExchange(id) {
  return request({ url: `/exchange/${id}/cancel`, method: 'post' })
}
