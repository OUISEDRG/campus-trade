import request from '@/utils/request'

// 创建砍价
export function createBargain(data) {
  return request({ url: '/bargain/create', method: 'post', data })
}

// 参与砍价
export function participateBargain(data) {
  return request({ url: '/bargain/participate', method: 'post', data })
}

// 获取砍价详情
export function getBargain(id) {
  return request({ url: `/bargain/${id}`, method: 'get' })
}

// 获取商品的所有砍价
export function getBargainsByGoods(goodsId) {
  return request({ url: `/bargain/goods/${goodsId}`, method: 'get' })
}

// 获取砍价记录
export function getBargainRecords(id) {
  return request({ url: `/bargain/${id}/records`, method: 'get' })
}

// 取消砍价
export function cancelBargain(id) {
  return request({ url: `/bargain/${id}/cancel`, method: 'post' })
}
