import request from '@/utils/request'

// 获取设备日志
export function listDeviceMetaV2(query) {
  return request({
    url: '/admin/v2/device/log/meta/list',
    method: 'get',
    params: query
  })
}

// 获取设备事件统计信息
export function getEventTotalV2(productKey, iotId) {
  return request({
    url: '/admin/v2/device/log/event/total/' + productKey + '/' + iotId,
    method: 'get'
  })
}

// 获取设备日志
export function listDeviceLog(query) {
  return request({
    url: '/admin/v2/device/log/list',
    method: 'get',
    params: query
  })
}

// 获取设备事件统计信息
export function getEventTotal(productKey, iotId) {
  return request({
    url: '/admin/v2/device/log/event/total/' + productKey + '/' + iotId,
    method: 'get'
  })
}
