import request from '@/utils/request'

// 获取实时消息列表
export function getRealtimeMessages(params) {
  return request({
    url: '/api/messages/realtime',
    method: 'get',
    params
  })
}

// 获取系统告警消息
export function getAlertMessages(params) {
  return request({
    url: '/api/messages/alerts',
    method: 'get',
    params
  })
}

// 获取设备状态变更消息
export function getDeviceStatusMessages(params) {
  return request({
    url: '/api/messages/device-status',
    method: 'get',
    params
  })
}

// 标记消息为已读
export function markMessageRead(messageId) {
  return request({
    url: `/api/messages/${messageId}/read`,
    method: 'put'
  })
}

// 批量标记消息为已读
export function markMessagesRead(messageIds) {
  return request({
    url: '/api/messages/batch-read',
    method: 'put',
    data: {messageIds}
  })
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/api/messages/${messageId}`,
    method: 'delete'
  })
}

// 获取消息统计信息
export function getMessageStats() {
  return request({
    url: '/api/messages/stats',
    method: 'get'
  })
}

// 获取今日新增设备数量
export function getTodayNewDevices() {
  return request({
    url: '/api/devices/today-new-count',
    method: 'get'
  })
}

// 模拟API响应数据（用于开发测试）
export function getMockMessages() {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          {
            id: Date.now() + 1,
            type: 'device',
            title: '设备告警',
            content: '温度传感器TH-002温度过高',
            priority: 'high',
            timestamp: new Date(),
            deviceId: 'TH-002',
            location: '机房B-02'
          },
          {
            id: Date.now() + 2,
            type: 'system',
            title: '系统更新',
            content: '系统将在2小时后进行维护更新',
            priority: 'normal',
            timestamp: new Date()
          },
          {
            id: Date.now() + 3,
            type: 'warning',
            title: '网络延迟',
            content: '检测到网络延迟异常',
            priority: 'normal',
            timestamp: new Date()
          }
        ]
      })
    }, 1000)
  })
}

// 模拟获取今日新增设备数量
export function getMockTodayNewDevices() {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: {
          count: Math.floor(Math.random() * 50) + 10,
          increase: Math.floor(Math.random() * 10) + 1
        }
      })
    }, 500)
  })
} 