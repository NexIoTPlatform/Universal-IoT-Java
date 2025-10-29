import request from '@/utils/request'

/**
 * 查询摄像头列表
 * @param {*} query
 * @returns
 */
export function getCameraList(query) {
  return request({
    url: '/admin/wvpDevice/page',
    method: 'get',
    params: query
  })
}

/**
 * 添加摄像头
 * @param {*} data
 * @returns
 */
export function addDevice(data) {
  return request({
    url: '/admin/wvpDevice',
    method: 'post',
    data
  })
}

/**
 * 删除摄像头
 * @param {*} ids
 * @returns
 */
export function removeDevice(ids) {
  return request({
    url: `/admin/wvpDevice/${ids}`,
    method: 'delete'
  })
}

/**
 * 修改摄像头
 * @param {*} data
 * @returns
 */
export function modifyDevice(data) {
  return request({
    url: `/admin/wvpDevice`,
    method: 'put',
    data
  })
}

/**
 * 查询设备详情
 * @param {*} id
 * @returns
 */
export function deviceDetail(id) {
  return request({
    url: `/admin/wvpDevice/${id}`,
    method: 'get'
  })
}

/**
 * 查询流媒体节点
 */
export function getMediaIds() {
  return request({
    url: `/admin/wvpDevice/media`,
    method: 'get'
  })
}

/**
 * 获取设备通道列表
 * @param {*} deviceId
 * @returns
 */
export function getChannelList(deviceId) {
  return request({
    url: `/admin/wvpDevice/channel/${deviceId}`,
    method: 'get',
    params: {
      deviceId
    }
  })
}

/**
 * 通道修改
 * @param {*} data
 * @returns
 */
export function modifyChannel(data) {
  return request({
    url: `/admin/wvpDevice/channel`,
    method: 'put',
    data
  })
}

/**
 * 摄像头取流
 * @param {*} deviceId
 * @param {*} channelId
 * @returns
 */
export function getPlayUrl(deviceId, channelId) {
  return request({
    url: `/admin/wvpDevice/play/${deviceId}/${channelId}`,
    method: 'get'
  })
}

/**
 * 停止取流
 * @param {*} deviceId
 * @param {*} channelId
 * @returns
 */
export function stopPlayLive(deviceId, channelId) {
  return request({
    url: `/admin/wvpDevice/stop/${deviceId}/${channelId}`,
    method: 'get'
  })
}

/**
 * 批量导入
 * @param {*} productKey
 * @returns
 */
export function handleBatchImport(productKey, data) {
  return request({
    url: `/admin/wvpDevice/import`,
    method: 'post',
    params: {
      productKey
    },
    data
  })
}

/**
 * 摄像头获取本地录像列表
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} startTime
 * @param {*} endTime
 * @returns
 */
export function getDeviceVideoTapeList(deviceId, channelId, startTime,
                                       endTime) {
  return request({
    url: `/admin/wvpDevice/local/record/list/${deviceId}/${channelId}`,
    method: 'get',
    params: {
      startTime,
      endTime
    }
  })
}

/**
 * 获取本地录像段
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} startTime
 * @param {*} endTime
 * @returns
 */
export function getDeviceVideoTapeCtx(deviceId, channelId, startTime, endTime) {
  return request({
    url: `/admin/wvpDevice/local/record/play/${deviceId}/${channelId}`,
    method: 'get',
    params: {
      startTime,
      endTime
    }
  })
}

/**
 * 倍速播放本地录像
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} stream
 * @param {*} speed
 * @returns
 */
export function getScaleDeviceVideoTapeCtx(deviceId, channelId, stream, speed) {
  return request({
    url: `/admin/wvpDevice/local/record/speed/${deviceId}/${channelId}/${stream}/${speed}`,
    method: 'get'
  })
}

/**
 * 停止播放本地录像
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} stream
 * @returns
 */
export function stopDeviceVideoTape(deviceId, channelId, stream) {
  return request({
    url: `/admin/wvpDevice/local/record/stop/${deviceId}/${channelId}/${stream}`,
    method: 'get'
  })
}

/**
 * 暂停播放本地录像
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} stream
 * @returns
 */
export function pauseDeviceVideoTape(deviceId, channelId, stream) {
  return request({
    url: `/admin/wvpDevice/local/record/pause/${deviceId}/${channelId}/${stream}`,
    method: 'get'
  })
}

/**
 * 恢复播放本地录像
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} stream
 * @returns
 */
export function resumeDeviceVideoTape(deviceId, channelId, stream) {
  return request({
    url: `/admin/wvpDevice/local/record/resume/${deviceId}/${channelId}/${stream}`,
    method: 'get'
  })
}

/**
 * 开始下载本地录像
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} startTime
 * @param {*} endTime
 * @param {*} downloadSpeed 倍速
 * @returns
 */
export function downloadDeviceVideoTape(deviceId, channelId, startTime, endTime,
                                        downloadSpeed = 128) {
  return request({
    url: `/admin/wvpDevice/local/record/download/start/${deviceId}/${channelId}`,
    method: 'get',
    params: {
      startTime,
      endTime,
      downloadSpeed
    }
  })
}

/**
 * 获取设备录像下载进度
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} stream
 * @returns
 */
export function getDownloadDeviceProgress(deviceId, channelId, stream) {
  return request({
    url: `/admin/wvpDevice/local/record/download/progress/${deviceId}/${channelId}/${stream}`,
    method: 'get'
  })
}

/**
 * 摄像头获取云端录像列表
 * @param {*} deviceId
 * @param {*} channelId
 * @returns
 */
export function getCloudVideoTapeList(deviceId, channelId, startTime, endTime) {
  return request({
    url: `/admin/wvpDevice/cloud/record/list/${deviceId}/${channelId}`,
    method: 'get',
    params: {
      startTime,
      endTime
    }
  })
}

/**
 * 获取云端录像片段
 * @param {*} deviceId
 * @param {*} channelId
 * @param {*} recordId
 * @returns
 */
export function getCloudVideoTapeCtx(deviceId, channelId, recordId) {
  return request({
    url: `/admin/wvpDevice/cloud/record/play/${deviceId}/${channelId}`,
    method: 'get',
    params: {
      recordId
    }
  })
}
