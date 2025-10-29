import request from '@/utils/request'

/**
 * 网关轮询配置 API
 */

// 保存轮询配置
export function savePollingConfig(data) {
  return request({
    url: '/admin/v1/gateway/polling/save',
    method: 'post',
    data: data
  })
}

// 获取轮询配置
export function getPollingConfig(productKey, deviceId) {
  return request({
    url: `/admin/v1/gateway/polling/get/${productKey}/${deviceId}`,
    method: 'get'
  })
}

// 删除轮询配置
export function deletePollingConfig(productKey, deviceId) {
  return request({
    url: `/admin/v1/gateway/polling/delete/${productKey}/${deviceId}`,
    method: 'delete'
  })
}

// 测试轮询
export function testPolling(productKey, deviceId) {
  return request({
    url: `/admin/v1/gateway/polling/test/${productKey}/${deviceId}`,
    method: 'post'
  })
}
