import request from '@/utils/request'

// 仪表盘总览
export function getDashboardStats() {
  return request({ url: '/admin/dashboard/stats', method: 'get' })
}

// 周统计
export function getWeeklyStats() {
  return request({ url: '/admin/dashboard/weekly', method: 'get' })
}

// 月统计
export function getMonthlyStats() {
  return request({ url: '/admin/dashboard/monthly', method: 'get' })
}

// 分类统计
export function getCategoryStats() {
  return request({ url: '/admin/dashboard/categories', method: 'get' })
}

// 热卖商品/TOP卖家
export function getTopSellers() {
  return request({ url: '/admin/dashboard/top-sellers', method: 'get' })
}

// 最近订单
export function getRecentOrders() {
  return request({ url: '/admin/dashboard/recent-orders', method: 'get' })
}

// 每日统计(旧接口)
export function getDailyStats() {
  return request({ url: '/admin/stats/daily', method: 'get' })
}
