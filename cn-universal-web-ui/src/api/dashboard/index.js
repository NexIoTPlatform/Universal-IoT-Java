import request from '@/utils/request'

// 获取仪表盘概览数据
export function getDashboardOverview() {
  return request({
    url: '/dashboard/overview',
    method: 'get'
  })
}

// 获取设备消息分类图表数据
export function getDashboardMessageClassification(timeRange = 'today') {
  return request({
    url: '/api/dashboard/message-classification',
    method: 'get',
    params: {timeRange}
  })
}

// 获取系统性能监控数据
export function getDashboardSystemMonitor(refreshInterval = 5) {
  return request({
    url: '/api/dashboard/system-monitor',
    method: 'get',
    params: {refreshInterval}
  })
}

// 获取实时数据（兼容旧接口）
export function getDashboardRealtime() {
  return request({
    url: '/dashboard/realtime',
    method: 'get'
  })
}

// 获取告警数据（兼容旧接口）
export function getDashboardAlerts(params) {
  return request({
    url: '/dashboard/alerts',
    method: 'get',
    params
  })
}

// 获取用户活动数据（兼容旧接口）
export function getDashboardUserActivity(params) {
  return request({
    url: '/dashboard/user-activity',
    method: 'get',
    params
  })
}

// 获取图表数据（兼容旧接口）
export function getDashboardChartData(params) {
  return request({
    url: '/dashboard/chart-data',
    method: 'get',
    params
  })
}

// 获取设备统计（兼容旧接口）
export function getDashboardDeviceStats(params) {
  return request({
    url: '/dashboard/device-stats',
    method: 'get',
    params
  })
}

// 获取消息统计（兼容旧接口）
export function getDashboardMessageStats(params) {
  return request({
    url: '/dashboard/message-stats',
    method: 'get',
    params
  })
}

// 获取渠道统计（兼容旧接口）
export function getDashboardChannelStats(params) {
  return request({
    url: '/dashboard/channel-stats',
    method: 'get',
    params
  })
}

// 获取小时统计（兼容旧接口）
export function getDashboardHourlyStats(params) {
  return request({
    url: '/dashboard/hourly-stats',
    method: 'get',
    params
  })
}

// 获取应用统计（兼容旧接口）
export function getDashboardAppStats(params) {
  return request({
    url: '/dashboard/app-stats',
    method: 'get',
    params
  })
}

// 获取产品统计（兼容旧接口）
export function getDashboardProductStats(params) {
  return request({
    url: '/dashboard/product-stats',
    method: 'get',
    params
  })
}

// 获取规则统计（兼容旧接口）
export function getDashboardRuleStats(params) {
  return request({
    url: '/dashboard/rule-stats',
    method: 'get',
    params
  })
}
