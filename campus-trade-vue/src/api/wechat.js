import request from '@/utils/request'

// 获取微信扫码登录URL
export function getQRCodeUrl(redirectUri) {
  return request({ url: '/wechat/qrcode-url', method: 'get', params: { redirectUri } })
}

// 微信登录(code回调)
export function wechatLogin(data) {
  return request({ url: '/wechat/login', method: 'post', data })
}
