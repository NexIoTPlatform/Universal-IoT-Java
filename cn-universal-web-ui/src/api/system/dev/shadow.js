// 查询设备影子信息
import request from '@/utils/request'

export function devListShadow(id) {
  return request({
    url: '/iot/device/shadow/' + id,
    method: 'get'
  })
}

export function getDeviceShadow(id) {
  return request({
    url: '/admin/v1/device/shadow/' + id,
    method: 'get'
  })
}
