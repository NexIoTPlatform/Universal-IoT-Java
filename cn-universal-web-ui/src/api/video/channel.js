/**
 * 视频平台通道相关API
 */
import request from '@/utils/request'

/**
 * 查询通道列表
 * @param {string} instanceKey - 平台实例Key
 * @param {string} deviceId - 设备ID(可选)
 * @param {object} params - 查询参数
 * @param {string} params.keyword - 关键词搜索(通道名称/ID)
 * @param {string} params.status - 通道状态筛选
 * @param {number} params.ptzType - PTZ类型筛选
 */
export function getChannelList(instanceKey, deviceId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/channels`,
    method: 'get',
    params: {
      deviceId,
      ...params
    }
  })
}

/**
 * 获取设备通道列表(从配置JSON中读取)
 * @deprecated 建议使用 getChannelList 从通道表查询
 */
export function getDeviceChannels(instanceKey, deviceId) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels`,
    method: 'get'
  })
}

/**
 * 获取通道预览流地址
 */
export function getPreviewUrl(instanceKey, deviceId, channelId, streamType = 'main') {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/preview`,
    method: 'get',
    params: { streamType }
  })
}

/**
 * 获取通道回放流地址
 */
export function getPlaybackUrl(instanceKey, deviceId, channelId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/playback`,
    method: 'get',
    params
  })
}

/**
 * PTZ云台控制
 */
export function controlPTZ(instanceKey, deviceId, channelId, command, speed = 50) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/ptz`,
    method: 'post',
    data: { command, speed }
  })
}

/**
 * 查询录像记录
 */
export function queryRecords(instanceKey, deviceId, channelId, startTime, endTime) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/records`,
    method: 'get',
    params: { startTime, endTime }
  })
}

/**
 * 查询录像记录(别名)
 */
export const getRecords = queryRecords

/**
 * 开始国标录像下载
 */
export function startGBRecordDownload(instanceKey, deviceId, channelId, data) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/gb-records/download/start`,
    method: 'post',
    data
  })
}

/**
 * 停止国标录像下载
 */
export function stopGBRecordDownload(instanceKey, deviceId, channelId, stream) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/gb-records/download/stop`,
    method: 'post',
    data: { stream }
  })
}

/**
 * 获取国标录像下载进度
 */
export function getGBRecordDownloadProgress(instanceKey, deviceId, channelId, stream) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/gb-records/download/progress`,
    method: 'get',
    params: { stream }
  })
}

/**
 * 查询云端录像日期列表
 */
export function queryCloudRecordDates(instanceKey, deviceId, channelId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/cloud-records/dates`,
    method: 'get',
    params
  })
}

/**
 * 查询云端录像列表
 */
export function queryCloudRecords(instanceKey, deviceId, channelId, params) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/cloud-records`,
    method: 'get',
    params
  })
}

/**
 * 加载云端录像文件
 */
export function loadCloudRecord(instanceKey, deviceId, channelId, data) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/cloud-records/load`,
    method: 'post',
    data
  })
}

/**
 * 云端录像定位
 */
export function seekCloudRecord(instanceKey, deviceId, channelId, data) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/cloud-records/seek`,
    method: 'post',
    data
  })
}

/**
 * 设置云端录像倍速
 */
export function setCloudRecordSpeed(instanceKey, deviceId, channelId, data) {
  return request({
    url: `/api/video/platforms/${instanceKey}/devices/${deviceId}/channels/${channelId}/cloud-records/speed`,
    method: 'post',
    data
  })
}

/**
 * 获取云端录像下载地址
 */
export function getCloudRecordPlayPath(instanceKey, recordId) {
  return request({
    url: `/api/video/platforms/${instanceKey}/cloud-records/play/path`,
    method: 'get',
    params: { recordId }
  })
}
