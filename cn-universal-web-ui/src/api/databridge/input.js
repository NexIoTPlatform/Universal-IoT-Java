import request from '@/utils/request'

// 启动数据输入任务
export function startInputTask(configId) {
  return request({
    url: `/databridge/input/start/${configId}`,
    method: 'post'
  })
}

// 停止数据输入任务
export function stopInputTask(configId) {
  return request({
    url: `/databridge/input/stop/${configId}`,
    method: 'post'
  })
}

// 检查任务运行状态
export function getTaskStatus(configId) {
  return request({
    url: `/databridge/input/status/${configId}`,
    method: 'get'
  })
}

// 获取输入配置列表
export function getInputConfigs(params) {
  return request({
    url: '/databridge/input/configs',
    method: 'get',
    params
  })
}

// 获取输入日志列表
export function getInputLogs(params) {
  return request({
    url: '/databridge/input/logs',
    method: 'get',
    params
  })
}

// 获取最近的输入日志
export function getRecentLogs(configId, limit = 10) {
  return request({
    url: `/databridge/input/logs/recent/${configId}`,
    method: 'get',
    params: {limit}
  })
}

// 获取成功率统计
export function getSuccessRate(configId, startTime, endTime) {
  return request({
    url: `/databridge/input/stats/success-rate/${configId}`,
    method: 'get',
    params: {startTime, endTime}
  })
}

// 批量启动输入任务
export function batchStartInputTasks(configIds) {
  return request({
    url: '/databridge/input/batch/start',
    method: 'post',
    data: configIds
  })
}

// 批量停止输入任务
export function batchStopInputTasks(configIds) {
  return request({
    url: '/databridge/input/batch/stop',
    method: 'post',
    data: configIds
  })
}

// 获取输入任务概览统计
export function getInputOverview() {
  return request({
    url: '/databridge/input/overview',
    method: 'get'
  })
}
