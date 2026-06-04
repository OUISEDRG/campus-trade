import request from '@/utils/request'

// 获取所有消息模板
export function getTemplates() {
  return request({ url: '/message/templates', method: 'get' })
}

// 获取单个模板
export function getTemplate(code) {
  return request({ url: `/message/template/${code}`, method: 'get' })
}

// 添加模板
export function addTemplate(data) {
  return request({ url: '/message/template', method: 'post', data })
}

// 更新模板
export function updateTemplate(data) {
  return request({ url: '/message/template', method: 'put', data })
}

// 删除模板
export function deleteTemplate(id) {
  return request({ url: `/message/template/${id}`, method: 'delete' })
}

// 发送模板消息
export function sendByTemplate(data) {
  return request({ url: '/message/send', method: 'post', data })
}

// 发送直接消息
export function sendDirect(data) {
  return request({ url: '/message/send-direct', method: 'post', data })
}

// 获取用户所有消息
export function getUserMessages(userId) {
  return request({ url: `/message/user/${userId}`, method: 'get' })
}

// 获取用户未读消息
export function getUnreadMessages(userId) {
  return request({ url: `/message/user/${userId}/unread`, method: 'get' })
}

// 标记单条已读
export function markAsRead(id) {
  return request({ url: `/message/${id}/read`, method: 'post' })
}

// 标记全部已读
export function markAllAsRead(userId) {
  return request({ url: `/message/user/${userId}/read-all`, method: 'post' })
}
