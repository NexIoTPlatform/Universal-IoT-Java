import request from '@/utils/request'

// 查询网络组件列表
export function listNetwork(query) {
  return request({
    url: '/admin/network/list',
    method: 'get',
    params: query
  })
}

// 查询网络组件详细
export function getNetwork(id) {
  return request({
    url: '/admin/network/' + id,
    method: 'get'
  })
}

// 新增网络组件
export function addNetwork(data) {
  return request({
    url: '/admin/network',
    method: 'post',
    data: data
  })
}

// 修改网络组件
export function updateNetwork(data) {
  return request({
    url: '/admin/network',
    method: 'put',
    data: data
  })
}

// 删除网络组件
export function delNetwork(id) {
  return request({
    url: '/admin/network/' + id,
    method: 'delete'
  })
}

// 批量删除网络组件
export function delNetworkBatch(ids) {
  return request({
    url: '/admin/network/batch/' + ids,
    method: 'delete'
  })
}

// 启动网络组件
export function startNetwork(id) {
  return request({
    url: '/admin/network/start/' + id,
    method: 'post'
  })
}

// 停止网络组件
export function stopNetwork(id) {
  return request({
    url: '/admin/network/stop/' + id,
    method: 'post'
  })
}

// 重启网络组件
export function restartNetwork(id) {
  return request({
    url: '/admin/network/restart/' + id,
    method: 'post'
  })
}

// 获取网络类型列表
export function getNetworkTypes() {
  return request({
    url: '/admin/network/types',
    method: 'get'
  })
}

// 验证网络组件配置
export function validateNetwork(data) {
  return request({
    url: '/admin/network/validate',
    method: 'post',
    data: data
  })
} 