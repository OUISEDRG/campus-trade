import request from '@/utils/request'

// 下架商品
export function offShelvesGoods(id) {
  return request({
    url: `/goods/${id}/off-shelves`,
    method: 'put'
  })
}

// 上架商品
export function onShelvesGoods(id) {
  return request({
    url: `/goods/${id}/on-shelves`,
    method: 'put'
  })
}

// 获取商品列表
export function getGoodsList(params) {
  return request({
    url: '/goods/list',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getGoodsDetail(id) {
  return request({
    url: `/goods/${id}`,
    method: 'get'
  })
}

// 发布商品
export function publishGoods(data) {
  return request({
    url: '/goods/add',
    method: 'post',
    data
  })
}

// 更新商品
export function updateGoods(data) {
  return request({
    url: '/goods/update',
    method: 'post',
    data
  })
}
