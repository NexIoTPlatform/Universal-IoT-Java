import request from '@/utils/request'

// 查询视频平台列表
export function listPlatform(query) {
  return request({
    url: '/api/video/platforms',
    method: 'get',
    params: query
  })
}

// 查询视频平台详细
export function getPlatform(instanceKey) {
  return request({
    url: `/api/video/platforms/${instanceKey}`,
    method: 'get'
  })
}

// 新增/编辑视频平台实例
export function savePlatform(data) {
  return request({
    url: '/api/video/platforms',
    method: 'post',
    data: data
  })
}

// 删除视频平台实例
export function delPlatform(id, params) {
  return request({
    url: `/api/video/platforms/${id}`,
    method: 'delete',
    params: params
  })
}

// 测试平台连接
export function testPlatform(data) {
  return request({
    url: '/api/video/platforms/test',
    method: 'post',
    data: data
  })
}

// 查询平台组织树
export function getPlatformOrgs(instanceKey, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/orgs`,
    method: 'get',
    params: params
  })
}

// 查询平台设备列表
export function getPlatformDevices(instanceKey, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices`,
    method: 'get',
    params: params
  })
}

// 同步平台设备
export function syncPlatform(instanceKey) {
  return request({
    url: `/api/video/platforms/${instanceKey}/sync`,
    method: 'post'
  })
}

// 订阅平台事件
export function subscribePlatform(instanceKey) {
  return request({
    url: `/api/video/platforms/${instanceKey}/subscribe`,
    method: 'post'
  })
}

// 取消订阅平台事件
export function unsubscribePlatform(instanceKey) {
  return request({
    url: `/api/video/platforms/${instanceKey}/unsubscribe`,
    method: 'post'
  })
}

// 查询平台通道列表
export function getPlatformChannels(instanceKey, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/channels`,
    method: 'get',
    params: params
  })
}

// 获取预览流地址
export function getPreviewUrl(instanceKey, deviceId, channelId, streamType = 'main') {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/preview`,
    method: 'get',
    params: { streamType }
  })
}

// 获取回放流地址
export function getPlaybackUrl(instanceKey, deviceId, channelId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/playback`,
    method: 'get',
    params: params
  })
}

// 查询录像记录
export function getRecords(instanceKey, deviceId, channelId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/records`,
    method: 'get',
    params: params
  })
}

// 获取通道流地址（通用）
export function getStreamUrl(instanceKey, deviceId, channelId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/stream`,
    method: 'get',
    params: params
  })
}

// 导入设备
export function importDevices(instanceKey, data) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/import`,
    method: 'post',
    data: data
  })
}

// PTZ控制
export function ptzControl(instanceKey, deviceId, channelId, data) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/ptz`,
    method: 'post',
    data: data
  })
}
