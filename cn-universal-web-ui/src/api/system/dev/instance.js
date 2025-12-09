import request from '@/utils/request'

export function getModels(params) {
  return request({
    url: '/admin/v1/device/models', method: 'get', params
  }).then(response => {
    return response.data
  })
}

// 查询设备列表
export function listInstance(query) {
  return request({
    url: '/admin/v1/device/list', method: 'get', params: query
  })
}

// 新：按第三方平台过滤的设备列表（用于萤石/乐橙）
export function listInstanceByPlatform(query) {
  return request({
    url: '/admin/v1/device/listByPlatform', method: 'get', params: query
  })
}

// 查询分组未绑定设备列表
export function listNotBindInstance(query, groupId) {
  return request({
    url: '/admin/v1/device/list/' + groupId, method: 'get', params: query
  })
}

// 查询未绑定设备列表
export function listUnInstance(query) {
  return request({
    url: '/admin/v1/device/unlist', method: 'get', params: query
  })
}

// 查询设备详细
export function getInstance(id) {
  return request({
    url: '/admin/v1/device/' + id, method: 'get'
  })
}

// 新增设备
export function addInstance(data) {
  return request({
    url: '/admin/v1/device', method: 'post', data: data
  })
}

// 修改设备
export function updateInstance(data) {
  return request({
    url: '/admin/v1/device', method: 'put', data: data
  })
}

// 绑定平台
export function bindInstance(data) {
  return request({
    url: '/admin/v1/device/app', method: 'put', data: data
  })
}

// 删除设备
export function delInstance(id) {
  return request({
    url: '/admin/v1/device/' + id, method: 'delete'
  })
}

// 设备功能下发（未加密）
export function functionDown(productKey, data) {
  return request({
    url: '/admin/v1/device/down/' + productKey, method: 'post', data: data
  })
}

// 设备上行调试（模拟）
export function upValue(productKey, data) {
  return request({
    url: '/admin/v1/device/up/' + productKey, method: 'post', data: data
  })
}

// 根据productKey查询设备型号
export function getmodel(productKey, data) {
  return request({
    url: '/admin/v1/device/model/' + productKey, method: 'get', data: data
  })
}

// 设备导入
export function importDevice(data, productKey, iotId) {
  return request({
    url: '/admin/v1/device/import', method: 'post', data: data, params: {
      productKey, iotId
    }
  })
}

// 根据网关设备查询子设备关系
export function getSubDeviceRelation(productKey, deviceId) {
  return request({
    url: '/admin/v1/device/gateway/sub/relation/' + productKey + '/' + deviceId, 
    method: 'get'
  })
}
